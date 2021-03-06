package de.fzj.unicore.xuudb.client.actions;

import org.apache.logging.log4j.Logger;

import de.fzj.unicore.xuudb.client.wsapi.XUUDBResponse;
import eu.emi.security.authn.x509.impl.X500NameUtils;
import de.fzj.unicore.xuudb.Log;

public class AddDNAction extends AbstractAction {

	private static final Logger logger = Log.getLogger(
			Log.XUUDB_CLIENT, AddDNAction.class);

	public AddDNAction(ConnectionManager cm) {
		super(
				cm,
				"adddn",
				"Add entry to database\n"
						+ " Syntax:\n"
						+ "        adddn <gcID>  <DN>  <xlogin> <role> [project1[,project2[,...]]]\n"
						+ " Example: \n"
						+ "         adddn DEMO-SITE 'CN=John Doe, O=Test Inc' userlogin user",
				4, 5

		);

	}

	@Override
	public boolean invoke(String[] args, boolean isBatch) throws Exception {

		logger.debug("Command: adddn ");
		for (int i = 0; i < args.length; i++) {
			logger.debug("Parameter " + i + ": " + args[i]);
		}

		String projects = null;

		if (args.length == 5) {
			projects = args[4];
		}
		String tdn = null;

		// logger.debug("Validation of DN : " + args[1]);
		System.out.print("Validation of DN : " + args[1]);
		try {
			tdn = X500NameUtils.getPortableRFC2253Form(args[1]);
			System.out.println("           OK \n");
		} catch (Exception e) {
			String msg = "Cannot add. DN format error?";
			logger.error(msg, e);
			throw new Exception(msg, e);
		}

		XUUDBResponse resp = cm.admin.adddn(args[0], tdn, args[2], args[3],
				projects);

		System.out.println("Done. Received: " + resp.getStatus());

		return true;

	}
}
