package eu.unicore.xuudb.server;

import java.io.File;

import eu.unicore.xuudb.server.dynamic.ProcessInvoker;
import junit.framework.TestCase;

public class TestProcessInvoker extends TestCase {
	
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
