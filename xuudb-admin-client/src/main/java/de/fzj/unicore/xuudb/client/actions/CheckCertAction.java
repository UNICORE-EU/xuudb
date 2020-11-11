package de.fzj.unicore.xuudb.client.actions;

import java.io.IOException;

import org.apache.logging.log4j.Logger;

import de.fzJuelich.unicore.xuudb.LoginDataType;
import de.fzj.unicore.xuudb.X509Utils;
import de.fzj.unicore.xuudb.client.wsapi.XUUDBResponse;
import de.fzj.unicore.xuudb.Log;

public class CheckCertAction extends AbstractAction {
	private static final Logger logger = Log.getLogger(
			Log.XUUDB_CLIENT, CheckCertAction.class);

	public CheckCertAction(ConnectionManager cm) {
		super(cm, "check-cert",
				"Print what the XUUDB contains for a certain certificate.\n"
						+ " Syntax:\n"
						+ "        check-cert  <gcid> <pemfile> \n"
						+ " Example:\n"
						+ "        check-cert test certs/demouser.pem", 2, 2,
				"chc");
	}

	@Override
	public boolean invoke(String[] args, boolean isBatch) throws Exception {
		logger.debug("Command: check-cert");
		for (int i = 0; i < args.length; i++) {
			logger.debug("Parameter " + i + ": " + args[i]);
		}

		String cert = null;
		try {

			System.out.print("Reading cert from file: " + args[1]);
			cert = X509Utils.getStringFromPEMFile(args[1]);
			System.out.println("           OK \n");
		} catch (IOException e) {
			String msg = "Can't read certificate <" + args[1] + ">";
			logger.error(msg, e);
			throw new IOException(msg, e);
		}

		XUUDBResponse resp = cm.query.checkCert(args[0], cert);

		LoginDataType data = resp.getData()[0];

		System.out.println("Done. Received:\n");

		System.out
				.printf("%-15s|%-64s|%-10s|%-10s|%-20s|\n", "     GcID",
						"                            Token", "   Role", "  Xlogin",
						"      Projects");
		System.out
				.println("----------------------------------------------------------------------------------------------------------------------------");
		if (data.getToken().length() < 65)
			System.out.printf("%15s|%64s|%10s|%10s|%20s|\n", data.getGcID(),
					data.getToken(), data.getRole(), data.getXlogin(), data
							.getProjects());

		else {
			System.out.printf("%15s|%64s|%10s|%10s|%20s|\n", data.getGcID(),
					data.getToken().substring(0, 64), data.getRole(), data
							.getXlogin(), data.getProjects());

			for (int i = 64; i < data.getToken().length(); i=i+64) {
				int end = i + 64;
				if (end > data.getToken().length())
					end = data.getToken().length();
				System.out.printf("%15s|%-64s|%10s|%10s|%20s|\n", "", data
						.getToken().substring(i, end), "", "", "");
			}

		}
		return true;

	}
}
