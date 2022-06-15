package de.fzj.unicore.xuudb.client.actions;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.cert.X509Certificate;

import de.fzJuelich.unicore.xuudb.LoginDataType;
import de.fzj.unicore.xuudb.X509Utils;
import de.fzj.unicore.xuudb.client.wsapi.XUUDBResponse;
import eu.emi.security.authn.x509.impl.X500NameUtils;

public class ListAction extends AbstractAction {

	public ListAction(ConnectionManager cm) {
		super(
				cm,
				"list",
				"Print content of database. \n"
						+ " Syntax: \n"
						+ "       list gcID=x|pemfile=file|dn='DN'|role=x|xlogin=x|project=x\n"
						+ " Example: \n" + "       list gcID='test' ", 0, 6);

	}

	@Override
	public boolean invoke(String[] args, boolean isBatch) throws Exception {
		logArguments(args);

		LoginDataType data = null;
		try {

			data = this.parseArgsToUlogin(args);
		} catch (FileNotFoundException e) {
			String msg="Cannot build query (check pem file path?)";
			logger.error(msg,e);
			throw new Exception(msg, e);
		}

		XUUDBResponse resp = cm.admin.list(data);

		String info = resp.getInfo();
		if (info != null) {
			System.out.println("XUUDB info: " + info);
		}

		LoginDataType[] rdata = resp.getData();

		if (resp.getData() == null)
			System.out.println("The database query result is empty.");
		else {
			System.out.println("The database query result contains   "
					+ rdata.length + "   entries:");
			System.out.printf("%-4s|%-20s|%-15s|%-10s|%-20s|%-30s|\n", "  Nr",
					"       GcID", "     Xlogin", "   Role", "     Projects", "             DN");
			System.out
					.println("--------------------------------------------------------------------------------------------------------");
			for (int i = 0; i < rdata.length; i++) {
				String dn = "Error resolving DN :/";
				try {
					dn = X500NameUtils.getReadableForm(rdata[i].getToken()); // user-friendly
					// form,
					// not
					// the
					// one
					// really
					// stored
					// in
					// XUUDB
				} catch (Exception pe) {
					try {
						X509Certificate cert = X509Utils
								.getX509FromPEMString(rdata[i].getToken());
						dn = X500NameUtils.getReadableForm(cert
								.getSubjectX500Principal());
					} catch (IOException e) {
						dn = "ERROR: can't parse the certificate: "
								+ e.getMessage();
					}
				}
				System.out.printf("%4d|%20s|%15s|%10s|%20s|%30s|\n", i + 1,
						rdata[i].getGcID(), rdata[i].getXlogin(), rdata[i]
								.getRole(), rdata[i].getProjects(), dn);
			}
		}

		return true;

	}
}
