package eu.unicore.xuudb.server;

public interface IShutdownable {

	public void shutdown() throws Exception;
	
	public String getNameOfService();
	
}
