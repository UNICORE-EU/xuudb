/*
 * Copyright (c) 2011-2012 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package de.fzj.unicore.xuudb.server.dynamic;

import java.util.Arrays;
import java.util.Collections;

import org.apache.logging.log4j.Logger;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import de.fzj.unicore.xuudb.Log;
import de.fzj.unicore.xuudb.server.dynamic.ProcessInvoker.TimeLimitedThread;

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
		org.springframework.expression.EvaluationContext spCtx = new StandardEvaluationContext(context);
		Expression expression = parseSpEL(new SpelExpressionParser(),
				getConfiguration(), new TemplateParserContext("${", "}"));
		if (expression == null)
			return;
		Object o;
		try
		{
			o = expression.getValue(spCtx);
		} catch (EvaluationException e)
		{
			log.warn("Script SpEL expression '" + expression.getExpressionString()
					+ "' evaluation failed: " + e.getMessage());
			return;
		}
		if (o == null)
		{
			log.warn("Script SpEL expression '" + expression.getExpressionString()
					+ "' was evaluated to null");
			return;
		}
		String cmdLine = o.toString();
		String[] cmdLineTokens = SimplifiedCmdLineLexer.tokenizeString(cmdLine);
		if (log.isDebugEnabled())
		{
			log.debug("Will run the following command line (comma is used to separate arguments): {}",
					Arrays.toString(cmdLineTokens));
		}

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
			log.warn("Mapping program '" + expression.getExpressionString() + 
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
	
	private Expression parseSpEL(ExpressionParser spelParser, String expr, ParserContext parserCtx) 
	{
		try
		{
			return parserCtx == null ? spelParser.parseExpression(expr) : 
				spelParser.parseExpression(expr, parserCtx);
		} catch(org.springframework.expression.ParseException e)
		{
			log.warn("Error parsing the SpEl expression: " + e.toDetailedString());
			return null;
		} catch(Exception ee)
		{
			log.warn("Other problem parsing SpEL expression '" + expr + "': " + ee.toString());
			return null;
		}
	}
	
}
