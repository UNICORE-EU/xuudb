package de.fzj.unicore.xuudb.client.actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import de.fzJuelich.unicore.xuudb.LoginDataType;
import de.fzj.unicore.xuudb.client.wsapi.XUUDBResponse;
import eu.emi.security.authn.x509.impl.X500NameUtils;
import de.fzj.unicore.xuudb.Log;

public class ImportAction extends AbstractAction {
	private static final Logger logger = Log.getLogger(
			Log.XUUDB_CLIENT, ImportAction.class);

	public ImportAction(ConnectionManager cm) {
		super(cm, "import", "Import entries from csv file to database.\n"
				+ " Syntax:\n" + "        import <csv-file> [clearDB]\n"
				+ " Example:\n" + "        import uudb.csv", 1, 2);
	}

	@Override
	public boolean invoke(String[] args, boolean isBatch) throws Exception {
		logger.debug("Command: import ");
		for (int i = 0; i < args.length; i++) {
			logger.debug("Parameter " + i + ": " + args[i]);
		}

		boolean clear = false;

		if (args.length > 1) {
			clear = "clearDB".equalsIgnoreCase(args[1]);
		}
		File incsv = new File(args[0]);
		if (!incsv.exists()) {
			String msg = "File does not exist: " + incsv.getAbsolutePath();
			logger.error(msg);
			throw new IOException(msg, null);
		}
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					incsv)));
		} catch (FileNotFoundException e) {
			logger.error(e);
			throw new FileNotFoundException("Error opening: "
					+ incsv.getAbsolutePath());
		}

		ArrayList<LoginDataType> data = new ArrayList<LoginDataType>();
		br.readLine(); // read header line
		String line = null;
		int linec = 0;
		while ((line = br.readLine()) != null) {
			linec++; // first line is 1
			LoginDataType login = LoginDataType.Factory.newInstance();
			String[] tokens = line.split(";");
			if (tokens.length == 6) {
				login.setGcID(tokens[1]);
				login.setXlogin(tokens[2]);
				login.setRole(tokens[3]);
				login.setProjects(tokens[4]);
				String token = tokens[5];
				try{
					login.setToken(X500NameUtils
							.getPortableRFC2253Form(token));
					data.add(login);
				}
				catch(Exception ex){
					// caused by improperly formed DN
					String msg = eu.unicore.util.Log.createFaultMessage("  >> Action: Ignoring line " 
							+ linec+". Error parsing '"+token+"'",ex);
					logger.warn(msg);
				}
			} else {
				logger.warn("  >> Action: Ignoring line " + linec
						+ ".  Count of token is not 6");
			}
		}

		LoginDataType [] dd = new LoginDataType[data.size()];
		data.toArray(dd);

		XUUDBResponse resp = cm.admin.importCsv(dd, clear);

		System.out.println("Done. Received: " + resp.getStatus());

		return true;
	}

}
