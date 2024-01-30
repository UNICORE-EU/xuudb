package eu.unicore.xuudb.server;

public class JavaErrorReporter
{
	/**
	 * To be used as standalone program simulating script returning error which should stop the job processiing.
	 * @param args
	 */
	public static void main(String[] args)
	{
		System.err.print("Test ERROR");
		System.err.flush();
		System.exit(10);
	}

}
