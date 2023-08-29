package de.fzj.unicore.xuudb.client.actions;

import de.fzJuelich.unicore.xuudb.LoginDataType;
import de.fzj.unicore.xuudb.client.wsapi.XUUDBResponse;
import eu.emi.security.authn.x509.impl.X500NameUtils;

public class CheckDNAction extends AbstractAction {

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
		logArguments(args);
		System.out.print("Validation of DN : " + args[1]);
		String dn = X500NameUtils.getReadableForm(args[1]);
		System.out.println("           OK \n");
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
