package eu.unicore.xuudb.server;

import java.io.File;

import org.apache.logging.log4j.Logger;

import eu.unicore.xuudb.Log;

public class XUUDBServer {
	
	private static final Logger logger=Log.getLogger(Log.XUUDB_SERVER, XUUDBServer.class);

	public static void main(String[] args) throws Exception {
		checkLogSystem();
		if(args.length==0) {
			XUUDBServer.printUsage();
		}
		else {
			new XUUDBServer().run(args);
		}
	}
	
	private void run(String[] args) throws Exception {
		String configFile = new File("conf", "xuudb_server.conf").getPath();
		if(args[0].equalsIgnoreCase("--start")) {
			if(args.length>1){
				configFile=args[1];
			}
		}
		else {
			configFile=args[0];
		}
		logger.info("Starting XUUDB server with configuration from <{}>", configFile);
		try {
			new HttpsServer(configFile).start();
			logger.info("XUUDB server startup completed");
			System.out.println("XUUDB server startup completed");
		}catch(Exception ex) {
			logger.error("XUUDB server NOT started.", ex);
			ex.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * warn if no log4j2 config is set
	 */
	private static void checkLogSystem() {
		if(System.getProperty("log4j.configurationFile")==null) {
			System.err.println("***");
			System.err.println("*** NO log4j configuration set - will use defaults.");
			System.err.println("*** please configure log4j with -Dlog4j.configurationFile=file:/path/to/config");
			System.err.println("***");
		}
	}

	public static void printUsage() {
		System.out.println(
				"\nUsage: start_cmd  <command>  [parameters]\n" +
				"	--start  <configfile>   starts XUUDB server");
	}
}
