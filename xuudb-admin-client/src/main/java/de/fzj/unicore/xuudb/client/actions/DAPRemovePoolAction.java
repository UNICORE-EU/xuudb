package de.fzj.unicore.xuudb.client.actions;

import org.apache.log4j.Logger;

import de.fzj.unicore.xuudb.Log;

public class DAPRemovePoolAction extends AbstractAction {
	private static final Logger logger = Log.getLogger(
			Log.XUUDB_CLIENT, DAPRemovePoolAction.class);

	public DAPRemovePoolAction(ConnectionManager cm) {
		super(cm, "removePool", "Remove specified pool\n" + " Syntax: \n"
				+ "        removePool <pool-id> \n" + " Example:\n"
				+ "        removePool pool5", 1, 1, "rmp");
		setCategory(AbstractAction.Category.dynamic.toString());
	}

	@Override
	public boolean invoke(String[] args, boolean isBatch) throws Exception {
		logger.debug("Command: removePool ");
		for (int i = 0; i < args.length; i++) {
			logger.debug("Parameter " + i + ": " + args[i]);
		}

		cm.dapAdmin.removePool(args[0]);

		return true;

	}
}
