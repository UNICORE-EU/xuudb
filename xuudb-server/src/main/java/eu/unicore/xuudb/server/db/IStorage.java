package eu.unicore.xuudb.server.db;

public interface IStorage {
	public IClassicStorage getClassicStorage();
	public IRESTClassicStorage getRESTClassicStorage();
	public IPoolStorage getPoolStorage();
	public void shutdown();
}
