package eu.unicore.xuudb.server.dynamic;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.mvel2.MVEL;

import eu.unicore.xuudb.xbeans.SimplifiedAttributeType;
import eu.unicore.xuudb.Log;


/**
 * Performs the full evaluation of given rules. Immutable.
 * @author K. Benedyczak
 */
public class EvaluationEngine {

	private static final Logger log = Log.getLogger(Log.XUUDB_SERVER, EvaluationEngine.class);

	/**
	 * Performs the evaluation of context wrt rules. Context may be modified
	 * @param rules
	 * @param ctx
	 * @param dryRun
	 */
	public void evaluate(List<Rule> rules, EvaluationContext ctx, boolean dryRun) {
		for (int i = 0; i < rules.size(); i++) {
			Rule r = rules.get(i);
			Map<String,Object> spelCtx = createContextVariables(ctx);
			if (checkCondition(r.getCondition(), spelCtx, i)) {
				log.debug("Rule {} condition hit.", i);
				List<Mapping> mappings = r.getActions();
				for (Mapping map: mappings) {
					if (skip(map.getType(), ctx, r.isOverwrite()))
						continue;
					map.applyAttributes(ctx, r.isOverwrite(), dryRun);
				}
			} else
				log.debug("Rule {} condition was not hit.", i);
		}
	}
	
	protected boolean skip(MappingType type, EvaluationContext context, boolean overwrite)
	{
		if (overwrite)
			return false;
		if (type == MappingType.gid && context.isGidSet()) {
			log.debug("Skipping gid mapping as gid was already set");
			return true;
		}
		if (type == MappingType.uid && context.isXloginSet()) {
			log.debug("Skipping uid mapping as uid was already set");
			return true;
		}
		return false;
	}

	
	public static Map<String,Object> createContextVariables(EvaluationContext ctx) {
		Map<String,Object> root = new HashMap<>();
		Map<String, Set<String>> attributes = new HashMap<>();
		SimplifiedAttributeType[] attributesXml = ctx.getExtraAttributes();
		for (SimplifiedAttributeType xmlA: attributesXml) {
			Set<String> values = new HashSet<>();
			Collections.addAll(values, xmlA.getValueArray());
			attributes.put(xmlA.getName(), values);
		}
		root.put("attributes", attributes);
		root.put("userDN", ctx.getUserDN());
		root.put("gid", ctx.getGid());
		root.put("gidSet", ctx.isGidSet());
		Set<String> allGids = new HashSet<>();
		allGids.add(ctx.getGid());
		allGids.addAll(ctx.getSupplementaryGids());
		root.put("supplementaryGids", allGids);
		root.put("issuerDN", ctx.getIssuerDN());
		root.put("role", ctx.getRole());
		root.put("vo", ctx.getVo());
		root.put("xlogin", ctx.getXlogin());
		root.put("xloginSet", ctx.isXloginSet());
		root.put("dryRun", ctx.isDryRun());
		
		return root;
	}
	
	private boolean checkCondition(String condition, Map<String,Object> ctx, int i)
	{
		Object condResult;
		try
		{
			condResult = MVEL.eval(condition, ctx);
		} catch (Exception e)
		{
			log.error("Skipping the rule number " + i + " as evaluation of its condition finished" +
					" with an error: " + e.toString(), e);
			return false;
		}
		if (!(condResult instanceof Boolean) || condResult == null)
		{
			log.error("Skipping the rule number " + i + " as evaluation of its condition finished with " +
				"a non boolean result. Result type is: " + 
				condResult == null ? "null" : condResult.getClass().getName());
			return false;
		}
		boolean result = (Boolean) condResult;
		log.debug("Rule number {} condition returned {}", i, result);
		return result;
	}
}
