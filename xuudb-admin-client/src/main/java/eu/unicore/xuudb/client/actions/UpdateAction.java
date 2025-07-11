package eu.unicore.xuudb.client.actions;

import java.io.FileNotFoundException;
import java.io.IOException;

import eu.unicore.xuudb.xbeans.LoginDataType;
import eu.emi.security.authn.x509.impl.X500NameUtils;
import eu.unicore.xuudb.X509Utils;
import eu.unicore.xuudb.client.wsapi.XUUDBResponse;

public class UpdateAction extends AbstractAction {
	
	public UpdateAction(ConnectionManager cm) {
		super(
				cm,
				"update",
				"Update entry in database.\n"
						+ " Syntax:\n"
						+ "        update <gcID> <pemfile or DN> gcID=x|pemfile=file|dn=\"dn\"|role=x|xlogin=x|project=x\n"
						+ " Example:\n"
						+ "        update DEMO-SITE certs/demouser.pem xlogin=jb007",
				3, 8);
	}

	@Override
	public boolean invoke(String[] args, boolean isBatch) throws Exception {
		logArguments(args);

		String[] lastArgs = new String[args.length - 2];
		for (int i = 2; i < args.length; ++i) {
			lastArgs[i - 2] = args[i];
		}
		String token = null;
		try {
			logger.debug("Trying to read token as a DN...");
			token = X500NameUtils.getPortableRFC2253Form(args[1]);
		} catch (Exception e) {
			logger.debug("Token is not a DN.");
			try {
				logger.debug("Trying to read token from file (x509)...");
				token = X509Utils.getStringFromPEMFile(args[1]);
			} catch (IOException e1) {
				String msg = "Can't read pem from file";
				logger.error(msg, e1);
				throw new Exception(msg, e1);
			}
		}

		LoginDataType data = null;
		try {
			logger.debug("Building Ulogin for db update");
			data = parseArgsToUlogin(lastArgs);
		} catch (FileNotFoundException e) {
			String msg = "Can't build query (check pem file path?)";
			logger.error(msg, e);
			throw new FileNotFoundException(msg);
		} catch (Exception e) {
			String msg = "Can't build query (check parameters?)";
			logger.error(msg, e);
			throw new Exception(msg, e);
		}

		XUUDBResponse resp = cm.admin.update(args[0], token, data);

		System.out.println("Done. Received: " + resp.getStatus());

		return true;
	}

}
