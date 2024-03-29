package eu.unicore.xuudb.client.actions;

import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

import eu.emi.security.authn.x509.impl.CertificateUtils;
import eu.emi.security.authn.x509.impl.FormatMode;
import eu.unicore.xuudb.X509Utils;
import eu.unicore.xuudb.client.wsapi.XUUDBResponse;

public class AddAction extends AbstractAction {

	public AddAction(ConnectionManager cm) {
		super(
				cm,
				"add",
				"Add entry to database.\n"
						+ " Syntax: \n"
						+ "        add <gcID>  <pemfile>  <xlogin> <role> [project1[,project2[,...]]]\n"
						+ " Example:\n"
						+ "         add DEMO-SITE /path/to/usercert.pem userlogin user",
				4, 5);
	}

	@Override
	public boolean invoke(String[] args, boolean isBatch) throws Exception {
		logArguments(args);
		System.out.print("Reading cert from file: " + args[1]);
		X509Certificate x509 = X509Utils.loadCertificate(args[1]);
		String certPem = X509Utils.getPEMStringFromX509(x509);
		System.out.println("           OK \n");
		// check validity of cert
		System.out.print("Checking validity of : " + args[1]);
		try {
			System.out.println(CertificateUtils.format(x509, FormatMode.FULL));
			x509.checkValidity();
			System.out.println(" OK \n");
		} catch (CertificateExpiredException cee) {
			String msg = "Certificate is expired: " + x509.getNotAfter();
			if (isBatch) {
				logger.warn(msg);
				System.out.println("Warning: " + msg);
			} else if (!confirm(msg)) {
				logger.error(msg, cee);
				throw new Exception(msg, cee);
			}
		} catch (CertificateNotYetValidException cnyve) {
			String msg = "Certificate is invalid, it will be valid from: "
					+ x509.getNotBefore();
			if (isBatch) {
				logger.warn(msg);
				System.out.println("Warning: " + msg);
			} else if (!confirm(msg)) {
				logger.error(msg, cnyve);
				throw new Exception("Error: " + msg, cnyve);
			}
		}

		String projects = null;

		if (args.length == 5) {
			projects = args[4];
		}

		if (!isBatch) {
			// ask admin to add cert
			String msg = "Do you really want to add this certificate?";
			if (!confirm(msg)) {
				msg = "Aborted by user";
				logger.debug(msg);
				System.out.println(msg);
				return true;
			}
		}

		XUUDBResponse resp = cm.admin.add(args[0], certPem, args[2], args[3],
				projects);

		System.out.println("Done. Received: " + resp.getStatus());

		return true;

	}
}
