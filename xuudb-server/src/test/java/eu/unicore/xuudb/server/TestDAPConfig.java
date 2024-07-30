package eu.unicore.xuudb.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;

import eu.unicore.xuudb.server.db.IStorage;
import eu.unicore.xuudb.server.db.StorageFactory;
import eu.unicore.xuudb.server.dynamic.DAPConfiguration;
import eu.unicore.xuudb.server.dynamic.Rule;

public class TestDAPConfig {
	
	
	@Test
	public void test() throws Exception
	{
		ShutdownHook hook = new ShutdownHook();
		ServerConfiguration config = new ServerConfiguration(new File("src/test/resources/xuudb_server.conf"));
		IStorage storage = StorageFactory.getDatabase(config, hook);
		DAPConfiguration cfg = new DAPConfiguration(new File("src/test/resources/dap-configuration.xml"), 
				storage.getPoolStorage(), -1);
		List<Rule> rules = cfg.getRules();
		assertEquals(4, rules.size());
	}
}
