package eu.unicore.xuudb.server.dynamic;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.mvel2.templates.TemplateRuntime;

import eu.unicore.xuudb.Log;
import eu.unicore.xuudb.server.dynamic.ProcessInvoker.TimeLimitedThread;

/**
 * Sets the values obtained from a script.
 * 
 * @author K. Benedyczak
 */
public class ScriptMapping extends Mapping
{
	public static final Logger log = Log.getLogger(Log.XUUDB_SERVER, ScriptMapping.class);
	public static final String ID = "script";
	private final int timeout;
	
	public ScriptMapping(String configuration, MappingType maps, int timeout)
	{
		super(configuration, maps);
		this.timeout = timeout;
	}
	
	@Override
	public void applyAttributes(EvaluationContext context, boolean overwrite, boolean dryRun)
	{
		Map<String, Object>vars = EvaluationEngine.createContextVariables(context);
		String script = evaluateTemplate(getConfiguration(), vars);
		if (script == null)
			return;
		String[] cmdLineTokens = SimplifiedCmdLineLexer.tokenizeString(script);
		log.debug("Will run the following command line (comma is used to separate arguments): {}",
					()->Arrays.toString(cmdLineTokens));
		ProcessInvoker invoker = new ProcessInvoker(timeout);
		TimeLimitedThread tlt;
		try
		{
			tlt = invoker.invokeAndWait(cmdLineTokens);
		} catch (Exception e)
		{
			Log.logException("Execution of command line: " + Arrays.toString(cmdLineTokens) + 
					" finished with an error: " + e.toString(), e, log);
			return;
		}
		int exitCode = tlt.getP().exitValue(); 
		if (exitCode != 0)
		{
			log.warn("Mapping program '" + script + 
					"' finished with non-zero exit code " + exitCode
					+ ", the stdErr was: " + tlt.getStderr());
			return;
		}

		String returned = tlt.getStdout();
		log.debug("Got mapping for " + getType() + ": '" + returned + "'");
		if (returned != null)
			returned = returned.trim();
		if (returned == null || returned.length() == 0)
		{
			log.warn("Returned mapping is invalid (empty): '" + returned + "'");
			return;
		}
		String[] gids=null;
		if (getType() != MappingType.supplementaryGids)
		{
			if (returned.length() > 256 || !returned.matches("^[\\w]+$"))
			{
				log.warn("Returned mapping is invalid (invalid chars or too long): '" + returned + "'");
				return;
			}
		} else
		{
			gids = returned.split("[ ]+");
			for (String gid: gids)
			{
				if (gid.length() > 256 || !gid.matches("^[\\w]+$") || gid.length() == 0)
				{
					log.warn("Returned mapping of supplementary gid is invalid " +
							"(empty, invalid chars or too long): '" + gid + "'");
					return;
				}
			}
		}
		
		
		switch (getType()) {
		case uid:
			log.debug("Setting xlogin to: {}", returned);
			context.setXlogin(returned);
			break;
		case gid:
			log.debug("Setting gid to: {}", returned);
			context.setGid(returned);
			break;
		case supplementaryGids:
			if (overwrite) {
				context.setSupplementaryGids(Arrays.asList(gids));
				log.debug("Setting supplementary groups to: {}", context.getSupplementaryGids());
			} else {
				Collections.addAll(context.getSupplementaryGids(), gids);
				if(log.isDebugEnabled())
					log.debug("Adding the following supplementary groups: {}", Arrays.toString(gids));
			}
			break;
		}
	}
	
	public static String evaluateTemplate(String expr, Map<String,Object>vars) 
	{
		String mExpr = expr.replace("${", "@{");
		try
		{
			return (String)TemplateRuntime.eval(mExpr, vars);
		} catch(Exception e)
		{
			log.warn("Error parsing the expression '{}': {}", expr, Log.getDetailMessage(e));
			return null;
		}
	}

}
