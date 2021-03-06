/*
 * Copyright (c) 2012 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package de.fzj.unicore.xuudb.server;

import de.fzj.unicore.xuudb.server.dynamic.EvaluationContext;
import de.fzj.unicore.xuudb.server.dynamic.MappingType;
import de.fzj.unicore.xuudb.server.dynamic.ScriptMapping;
import junit.framework.TestCase;

public class TestScript extends TestCase 
{
	public void testOK() throws Exception
	{
		try
		{
			ScriptMapping mapping = new ScriptMapping("java -cp target/test-classes " +
					"de.fzj.unicore.xuudb.server.JavaEcho ${dryRun} ${userDN} ${issuerDN} ${role} ${vo}", 
					MappingType.supplementaryGids, 4000);
			EvaluationContext evalCtx = new EvaluationContext("ala", "fooI", "fooR", "fooVo", 
					null, null, null, null);
			evalCtx.setDryRun(true);
			mapping.applyAttributes(evalCtx, false, true);
			assertEquals(5, evalCtx.getSupplementaryGids().size());
			assertEquals("true", evalCtx.getSupplementaryGids().get(0));
			assertEquals("ala", evalCtx.getSupplementaryGids().get(1));
			assertEquals("fooI", evalCtx.getSupplementaryGids().get(2));
			assertEquals("fooR", evalCtx.getSupplementaryGids().get(3));
			assertEquals("fooVo", evalCtx.getSupplementaryGids().get(4));
		} catch (Exception e)
		{
			e.printStackTrace();
			fail(e.toString());
		}
	}
	
	public void testTimeout() throws Exception
	{
		try
		{
			ScriptMapping mapping = new ScriptMapping("java -cp target/test-classes " +
					"de.fzj.unicore.xuudb.server.JavaWait", 
					MappingType.uid, 1000);
			EvaluationContext evalCtx = new EvaluationContext("ala", "fooI", "fooR", "fooVo", 
					null, null, null, null);
			mapping.applyAttributes(evalCtx, false, false);
			assertNull(evalCtx.getXlogin());
		} catch (Exception e)
		{
			e.printStackTrace();
			fail(e.toString());
		}
	}

	public void testError() throws Exception
	{
		try
		{
			ScriptMapping mapping = new ScriptMapping("java -cp target/test-classes " +
					"de.fzj.unicore.xuudb.server.JavaErrorReporter", 
					MappingType.uid, 2000);
			EvaluationContext evalCtx = new EvaluationContext("ala", "fooI", "fooR", "fooVo", 
					null, null, null, null);
			mapping.applyAttributes(evalCtx, false, false);
			assertNull(evalCtx.getXlogin());
		} catch (Exception e)
		{
			e.printStackTrace();
			fail(e.toString());
		}
	}
}
