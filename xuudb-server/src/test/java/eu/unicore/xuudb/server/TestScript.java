package eu.unicore.xuudb.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import eu.unicore.xuudb.server.dynamic.EvaluationContext;
import eu.unicore.xuudb.server.dynamic.MappingType;
import eu.unicore.xuudb.server.dynamic.ScriptMapping;

public class TestScript {

	@Test
	public void testOK() throws Exception
	{

		ScriptMapping mapping = new ScriptMapping("java -cp target/test-classes " +
				JavaEcho.class.getName()+" ${dryRun} ${userDN} ${issuerDN} ${role} ${vo}", 
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
	}
	
	@Test
	public void testTimeout() throws Exception
	{
		ScriptMapping mapping = new ScriptMapping("java -cp target/test-classes " +
				JavaWait.class.getName(), 
				MappingType.uid, 1000);
		EvaluationContext evalCtx = new EvaluationContext("ala", "fooI", "fooR", "fooVo", 
				null, null, null, null);
		mapping.applyAttributes(evalCtx, false, false);
		assertNull(evalCtx.getXlogin());
	}

	public void testError() throws Exception
	{
		ScriptMapping mapping = new ScriptMapping("java -cp target/test-classes " +
				JavaErrorReporter.class.getName(), 
				MappingType.uid, 2000);
		EvaluationContext evalCtx = new EvaluationContext("ala", "fooI", "fooR", "fooVo", 
				null, null, null, null);
		mapping.applyAttributes(evalCtx, false, false);
		assertNull(evalCtx.getXlogin());
	}
}
