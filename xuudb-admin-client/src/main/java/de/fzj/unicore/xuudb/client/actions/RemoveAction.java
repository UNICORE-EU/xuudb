package de.fzj.unicore.xuudb.client.actions;

import java.io.FileNotFoundException;

import de.fzJuelich.unicore.xuudb.LoginDataType;
import de.fzj.unicore.xuudb.client.wsapi.XUUDBResponse;

public class RemoveAction extends AbstractAction {

	public RemoveAction(ConnectionManager cm) {
		super(
				cm,
				"remove",
				"Remove entry from database.\n"
						+ " Syntax:\n"
						+ "        remove ALL|gcID=x|pemfile=file|dn='DN'|role=x|xlogin=x|project=x\n"
						+ " Example:\n" + "        remove xlogin=jdoe", 1, 6);
	}

	@Override
	public boolean invoke(String[] args, boolean isBatch) throws Exception {
		logArguments(args);

		LoginDataType data;

		if (args[0].equalsIgnoreCase("ALL")) {
			logger.debug("Clearing complete database.");
			String msg = "All entries will be removed!";
			if (!isBatch) {
				if (!confirm(msg)) {
					logger.debug("Operation stopped by user");
					System.out.println("Database was not changed.");
					return true;
				}
			}
			data = LoginDataType.Factory.newInstance();

		} else {
			try {
				logger.debug("Building Ulogin for db query");
				data = this.parseArgsToUlogin(args);
			} catch (FileNotFoundException e) {
				String msg = "Can't build query (check pem file path?";
				logger.error(msg, e);
				throw new Exception(msg, e);
			} catch (Exception e) {
				String msg = "Can't build query (check parameters?)";
				logger.error(msg, e);
				throw new Exception(msg, e);
			}
		}

		XUUDBResponse resp = cm.admin.remove(data);

		System.out.println("Done. Received: " + resp.getStatus());

		return true;
	}
}
