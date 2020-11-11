package de.fzj.unicore.xuudb.client.actions;

import org.apache.logging.log4j.Logger;

import de.fzJuelich.unicore.xuudb.LoginDataType;
import de.fzj.unicore.xuudb.client.wsapi.XUUDBResponse;
import eu.emi.security.authn.x509.impl.X500NameUtils;
import de.fzj.unicore.xuudb.Log;

public class CheckDNAction extends AbstractAction {
	private static final Logger logger = Log.getLogger(
			Log.XUUDB_CLIENT, CheckDNAction.class);

	public CheckDNAction(ConnectionManager cm) {
		super(cm, "check-dn",
				"Print what the XUUDB contains for a certain DN.\n"
						+ " Syntax:\n" 
						+ "        check-dn  <gcid> <dn> \n"
						+ " Example:\n"
						+ "        check-dn test 'CN=John Doe, O=Test Inc'",
				2, 2, "chdn");
	}

	@Override
	public boolean invoke(String[] args, boolean isBatch) throws Exception {
		logger.debug("Command: check-dn");
		for (int i = 0; i < args.length; i++) {
			logger.debug("Parameter " + i + ": " + args[i]);
		}

		String dn = null;
		System.out.print("Validation of DN : " + args[1]);
		try {
			dn = X500NameUtils.getReadableForm(args[1]);
			System.out.println("           OK \n");
		} catch (IllegalArgumentException e) {
			String msg = ("DN format error");
			logger.error(msg, e);
			throw new IllegalArgumentException(msg);
		}

		XUUDBResponse resp = cm.query.checkDN(args[0], dn);

		LoginDataType data = resp.getData()[0];
		System.out.println("Done. Received:\n");
		System.out.printf("%-15s|%-65s|%-10s|%-10s|%-20s|\n", "     Gcid",
				"                           Token", "   Role", "  Xlogin",
				"      Projects");
		System.out
				.println("-----------------------------------------------------------------------------------------------------------------------------");

		System.out.printf("%15s|%65s|%10s|%10s|%20s|\n", data.getGcID(), data
				.getToken(), data.getRole(), data.getXlogin(), data
				.getProjects());

		return true;
	}
}
