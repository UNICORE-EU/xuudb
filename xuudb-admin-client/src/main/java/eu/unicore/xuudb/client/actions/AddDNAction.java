package eu.unicore.xuudb.client.actions;

import eu.emi.security.authn.x509.impl.X500NameUtils;
import eu.unicore.xuudb.client.wsapi.XUUDBResponse;

public class AddDNAction extends AbstractAction {

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
		logArguments(args);
		String projects = null;
		if (args.length == 5) {
			projects = args[4];
		}
		System.out.print("Validation of DN : " + args[1]);
		String tdn = X500NameUtils.getPortableRFC2253Form(args[1]);
		System.out.println("           OK\n");
		XUUDBResponse resp = cm.admin.adddn(args[0], tdn, args[2], args[3],
				projects);
		System.out.println("Done. Received: " + resp.getStatus());
		return true;
	}
}
