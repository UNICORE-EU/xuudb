package eu.unicore.xuudb.server;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

import eu.unicore.xuudb.server.dynamic.ProcessInvoker;

public class TestProcessInvoker {
	
	@Test
	public void testInvoke(){
		
		
		File f1=new File("target/testinv");
		assertFalse(f1.exists());
		
		ProcessInvoker invoker=new ProcessInvoker();
		assertTrue(invoker.invokeWithChecking("touch", new String[]{"target/testinv"}));
		
		File f2=new File("target/testinv");
		assertTrue(f2.exists());
		f2.delete();
		
		assertFalse(invoker.invokeWithChecking("emptyCMD", new String[]{"emp"}));

		
		
		
		
		
		
		
		
		
	}

}
