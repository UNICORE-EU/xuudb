/*
 * Copyright (c) 2012 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package de.fzj.unicore.xuudb.server;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executors;

import org.junit.Test;

import de.fzj.unicore.xuudb.server.db.IStorage;
import de.fzj.unicore.xuudb.server.db.StorageFactory;
import de.fzj.unicore.xuudb.server.dynamic.DAPConfiguration;
import de.fzj.unicore.xuudb.server.dynamic.Rule;

import static org.junit.Assert.*;

public class TestDAPConfig {
	
	
	@Test
	public void test() throws Exception
	{
		ShutdownHook hook = new ShutdownHook();
		ServerConfiguration config = new ServerConfiguration(new File("src/test/resources/xuudb_server.conf"));
		IStorage storage = StorageFactory.getDatabase(config, hook);
		DAPConfiguration cfg = new DAPConfiguration(new File("src/test/resources/dap-configuration.xml"), 
				storage.getPoolStorage(), -1, Executors.newSingleThreadScheduledExecutor());
		
		List<Rule> rules = cfg.getRules();
		assertEquals(4, rules.size());
	}
}
