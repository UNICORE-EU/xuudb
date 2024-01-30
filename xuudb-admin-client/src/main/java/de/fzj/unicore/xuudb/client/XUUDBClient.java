package de.fzj.unicore.xuudb.client;

import eu.unicore.util.Log;

public class XUUDBClient {

	public static void main(String[] args) {
		try {
			CLCExecutor clc = new CLCExecutor();
			if (!clc.processCommandLineArgs(args)) {
				System.exit(1);
			}
		
			clc.registerHelpActions();
			clc.readConfig();

			if (clc.getParsedLine() != null) {
				if (clc.getParsedLine()[0] != null)
					if (!clc.getActions().containsKey(clc.getParsedLine()[0])) {
						clc.init();

					}
			}

			clc.registerAllActions();
			if (clc.getParsedLine() != null)
				clc.parseLine(clc.getParsedLine());

		} catch (Exception e) {
			System.out.println(Log.createFaultMessage("Error: ", e));
			System.exit(1);
		}

		System.exit(0);
	}
}
