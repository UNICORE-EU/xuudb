package eu.unicore.xuudb.client.actions;

import org.apache.logging.log4j.Logger;

import eu.unicore.xuudb.xbeans.MappingDataType;
import eu.unicore.xuudb.Log;

public class DAPFindMappingAction extends AbstractAction {
	private static final Logger logger = Log.getLogger(
			Log.XUUDB_CLIENT, DAPFindMappingAction.class);

	public DAPFindMappingAction(ConnectionManager cm) {
		super(cm, "findMapping",
				"Find an alive mapping which provides a specified incarnation attribute.\n"
						+ " Syntax: \n"
						+ "        findMapping <uid|gid|supplementaryGid> <value> \n"
						+ " Example:\n" + "        findMapping uid user234", 2,
				2, "fm");
		setCategory(AbstractAction.Category.dynamic.toString());
	}

	@Override
	public boolean invoke(String[] args, boolean isBatch) throws Exception {
		logger.debug("Command: findMapping ");
		for (int i = 0; i < args.length; i++) {
			logger.debug("Parameter " + i + ": " + args[i]);
		}

		MappingDataType[] resp = cm.dapAdmin.find(args[0], args[1]);
		printMapping(resp);
		return true;
	}

}
