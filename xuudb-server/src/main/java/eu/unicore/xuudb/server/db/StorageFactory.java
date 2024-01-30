package eu.unicore.xuudb.server.db;

import eu.unicore.xuudb.server.ServerConfiguration;
import eu.unicore.xuudb.server.ShutdownHook;

/**
 * factory for obtaining an implementation of IStorage
 * 
 * @author schuller
 */
public class StorageFactory {
	public static IStorage getDatabase(ServerConfiguration config, ShutdownHook hook) throws Exception {
		DatabaseProperties dbConfig = new DatabaseProperties(config.getProperties());
		return new MyBatisDatabase(dbConfig, hook);
	}
}
