/*********************************************************************************
 * Copyright (c) 2006 Forschungszentrum Juelich GmbH 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * (1) Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the disclaimer at the end. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 * 
 * (2) Neither the name of Forschungszentrum Juelich GmbH nor the names of its 
 * contributors may be used to endorse or promote products derived from this 
 * software without specific prior written permission.
 * 
 * DISCLAIMER
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *********************************************************************************/
 

package de.fzj.unicore.xuudb.server;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.logging.log4j.Logger;

import de.fzj.unicore.xuudb.Log;

public class XUUDBServer {
	
	private static final Logger logger=Log.getLogger(Log.XUUDB_SERVER, XUUDBServer.class);
	
	private ScheduledExecutorService executorService;
	
	public static void main(String[] args) throws Exception {
		checkLogSystem();
		if(args.length==0)
			XUUDBServer.printUsage();
		else
			new XUUDBServer().run(args);
	}
	
	private void run(String[] args) throws Exception {
		executorService = Executors.newScheduledThreadPool(5);
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
		new HttpsServer(configFile, executorService).start();
		logger.info("XUUDB server startup completed");
		System.out.println("XUUDB server startup completed");
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
				"\n" +
				"Usage: start_cmd  <command>  [parameters]\n" +
				"	--start  <configfile>   starts XUUDB server");
	}
}
