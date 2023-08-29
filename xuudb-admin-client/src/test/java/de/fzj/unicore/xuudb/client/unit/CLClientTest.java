package de.fzj.unicore.xuudb.client.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.fzj.unicore.xuudb.client.CLCExecutor;

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

}
