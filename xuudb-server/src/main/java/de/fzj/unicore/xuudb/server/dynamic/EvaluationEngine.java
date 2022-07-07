/*
 * Copyright (c) 2012 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package de.fzj.unicore.xuudb.server.dynamic;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import de.fzJuelich.unicore.xuudb.SimplifiedAttributeType;
import de.fzj.unicore.xuudb.Log;


/**
 * Performs the full evaluation of given rules. Immutable.
 * @author K. Benedyczak
 */
public class EvaluationEngine {
	
	private static final Logger log = Log.getLogger(Log.XUUDB_SERVER, EvaluationEngine.class);

	/**
	 * Performs the evaluation of context wrt rules. Context may be modified. 
	 * @param ctx
	 */
	public void evaluate(List<Rule> rules, EvaluationContext ctx, boolean dryRun) {
		for (int i = 0; i < rules.size(); i++) {
			Rule r = rules.get(i);
			StandardEvaluationContext spelCtx = createSpelContext(ctx);
			if (checkCondition(r.getCondition(), spelCtx, i)) {
				log.debug("Rule{} condition hit.", i);
				List<Mapping> mappings = r.getActions();
				for (Mapping map: mappings) {
					if (skip(map.getType(), ctx, r.isOverwrite()))
						continue;
					map.applyAttributes(ctx, r.isOverwrite(), dryRun);
				}
			} else
				log.debug("Rule {}{} condition was not hit.", i);
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

	
	private StandardEvaluationContext createSpelContext(EvaluationContext ctx) {
		SpelContextBean root = new SpelContextBean();
		Map<String, Set<String>> attributes = new HashMap<String, Set<String>>();
		SimplifiedAttributeType[] attributesXml = ctx.getExtraAttributes();
		for (SimplifiedAttributeType xmlA: attributesXml) {
			Set<String> values = new HashSet<String>();
			Collections.addAll(values, xmlA.getValueArray());
			attributes.put(xmlA.getName(), values);
		}
		root.setAttributes(attributes);
		root.setDn(ctx.getUserDN());
		root.setGid(ctx.getGid());
		Set<String> allGids = new HashSet<String>();
		allGids.add(ctx.getGid());
		allGids.addAll(ctx.getSupplementaryGids());
		root.setGids(allGids);
		root.setIssuer(ctx.getIssuerDN());
		root.setRole(ctx.getRole());
		root.setVo(ctx.getVo());
		root.setXlogin(ctx.getXlogin());
		return new StandardEvaluationContext(root);
	}
	
	private boolean checkCondition(Expression condition, StandardEvaluationContext ctx, int i)
	{
		Object condResult;
		try
		{
			condResult = condition.getValue(ctx);
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
