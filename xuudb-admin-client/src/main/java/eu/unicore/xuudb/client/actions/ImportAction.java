package eu.unicore.xuudb.client.actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import eu.unicore.xuudb.xbeans.LoginDataType;
import eu.emi.security.authn.x509.impl.X500NameUtils;
import eu.unicore.util.Log;
import eu.unicore.xuudb.client.wsapi.XUUDBResponse;

public class ImportAction extends AbstractAction {

	public ImportAction(ConnectionManager cm) {
		super(cm, "import", "Import entries from csv file to database.\n"
				+ " Syntax:\n" + "        import <csv-file> [clearDB]\n"
				+ " Example:\n" + "        import uudb.csv", 1, 2);
	}

	@Override
	public boolean invoke(String[] args, boolean isBatch) throws Exception {
		logArguments(args);

		boolean clear = false;

		if (args.length > 1) {
			clear = "clearDB".equalsIgnoreCase(args[1]);
		}
		File incsv = new File(args[0]);
		if (!incsv.exists()) {
			throw new IOException( "File does not exist: " + incsv.getAbsolutePath());
		}
		List<LoginDataType> data = new ArrayList<>();
		try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(incsv))))
		{
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
						login.setToken(X500NameUtils.getPortableRFC2253Form(token));
						data.add(login);
					}
					catch(Exception ex){
						// caused by improperly formed DN
						String msg = Log.createFaultMessage("  >> Action: Ignoring line " 
								+ linec+". Error parsing '"+token+"'",ex);
						logger.warn(msg);
					}
				} else {
					logger.warn("  >> Action: Ignoring line {}. Count of token is not 6", line);
				}
			}
		}
		LoginDataType [] dd = new LoginDataType[data.size()];
		data.toArray(dd);

		XUUDBResponse resp = cm.admin.importCsv(dd, clear);

		System.out.println("Done. Received: " + resp.getStatus());

		return true;
	}

}
