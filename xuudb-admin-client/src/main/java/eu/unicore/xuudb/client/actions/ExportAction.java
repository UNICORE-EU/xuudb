package eu.unicore.xuudb.client.actions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import de.fzJuelich.unicore.xuudb.LoginDataType;
import eu.unicore.xuudb.client.wsapi.XUUDBResponse;

public class ExportAction extends AbstractAction {
	
	public ExportAction(ConnectionManager cm) {
		super(cm, "export", "Export database content to csv file.\n"
				+ " Syntax:\n" + "" + "        export csv-file [force]\n"
				+ " Example:\n" + "        export uudb.csv", 1, 2);
	}

	@Override
	public boolean invoke(String[] args, boolean isBatch) throws Exception {
		logArguments(args);

		String fname = args[0];
		if (!fname.endsWith(".csv"))
			fname += ".csv";
		boolean force = args.length > 1 && "force".equalsIgnoreCase(args[1]);

		File exp = new File(fname);
		PrintStream writer = null;
		try {
			FileOutputStream fop;
			if (exp.createNewFile() == false && !force) {
				String msg = "File exists: " + exp.getAbsolutePath();
				System.out.println(msg);
				System.out.println("Use the 'force' parameter to override.");

				return true;
			}
			fop = new FileOutputStream(exp);
			writer = new PrintStream(fop, false);

		} catch (IOException e) {
			String msg = "Cannot write to file: " + exp.getAbsolutePath();
			logger.error(msg, e);
			throw new IOException(msg, e);
		}

		XUUDBResponse resp = cm.admin.exportCsv();

		LoginDataType[] data = resp.getData();
		if (data == null)
			System.out.println("The database query result is empty.");
		else {
			System.out.println("The database query result contains "
					+ data.length + " entries:");
			writer.println("Nr.;GcID;Xlogin;Role;Projects;CertInPEM");
			for (int i = 0; i < data.length; i++) {
				writer.println((i + 1) + ";" + data[i].getGcID() + ";"
						+ data[i].getXlogin() + ";" + data[i].getRole() + ";"
						+ data[i].getProjects() + ";" + data[i].getToken());

			}
			writer.close();
			System.out.println("Data written to: " + exp.getAbsolutePath());
		}

		return true;
	}
}
