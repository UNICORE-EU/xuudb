package de.fzj.unicore.xuudb.client.unit;

import java.security.Permission;

import org.junit.Test;

import de.fzj.unicore.xuudb.client.CLCExecutor;
import de.fzj.unicore.xuudb.client.XUUDBClient;

import static org.junit.Assert.*;

public class CLClientTest {

	@Test
	public void testClcExecutor() throws Exception {
		CLCExecutor e = new CLCExecutor();

		e = new CLCExecutor();
		String[] args2 = { "--config", "test.conf", "list", "gcID=x" };
		e.processCommandLineArgs(args2);

		String[] l = e.getParsedLine();
		assertEquals(l.length, 2);
		assertEquals("list", l[0]);
		assertEquals("gcID=x", l[1]);

		CLCExecutor e2 = new CLCExecutor("src/test/resources/client.conf");
		e2.readConfig();
		e2.registerHelpActions();
		e2.registerAllActions();

		String[] args3 = { "--config", "test.conf", "help" };
		e2.processCommandLineArgs(args3);
		assertTrue(e2.parseLine(e2.getParsedLine()));
		
		String[] args33 = { "--config", "test.conf", "help", "list" };
		e2.processCommandLineArgs(args33);
		assertTrue(e2.parseLine(e2.getParsedLine()));
		
		CLCExecutor e3 = new CLCExecutor("src/test/resources/client.conf");
		e3.readConfig();
		e3.registerHelpActions();
		e3.registerAllActions();

		String[] args4 = { "helpAll" };
		e3.processCommandLineArgs(args4);
		assertTrue(e3.parseLine(e3.getParsedLine()));
		
		CLCExecutor e5 = new CLCExecutor();
		String[] args6 = new String[0];
		assertFalse(e5.processCommandLineArgs(args6));
	}

	protected static class ExitException extends SecurityException {
		private static final long serialVersionUID = 1L;
		public final int status;

		public ExitException(int status) {
			this.status = status;
		}
	}

	private static class NoExitSecurityManager extends SecurityManager {
		public void checkPermission(Permission perm) {
			// allow anything.
		}

		public void checkPermission(Permission perm, Object context) {
			// allow anything.
		}

		@Override
		public void checkExit(int status) {
			super.checkExit(status);
			throw new ExitException(status);

		}
	}


	@Test
	@SuppressWarnings("static-access")
	public void testClc() throws Exception {

		SecurityManager old = System.getSecurityManager();
		System.setSecurityManager(new NoExitSecurityManager());
		XUUDBClient cl = new XUUDBClient();
		String[] args = { "--config", "src/test/resources/client.conf", "help" };
		
		try{
			cl.main(args);
		}catch (ExitException e) {
			if(e.status!=0)
				fail();
		}
		System.setSecurityManager(old);

	}
}
