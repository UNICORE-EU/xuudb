package de.fzj.unicore.xuudb.client.actions;

import org.apache.logging.log4j.Logger;

import de.fzJuelich.unicore.xuudb.MappingDataType;
import de.fzj.unicore.xuudb.Log;

public class DAPFindReverseMappingAction extends AbstractAction {
	private static final Logger logger = Log.getLogger(
			Log.XUUDB_CLIENT, DAPFindReverseMappingAction.class);

	public DAPFindReverseMappingAction(ConnectionManager cm) {
		super(cm, "findReverseMapping", "Find what incarnation attribute value is mapped to a specified mapping key.\n"
				+ " Syntax: \n"
				+ "        findReverseMapping <keyType> <keyValue> \n"
				+ " Example:\n" + "        findReverseMapping dn \"cn=Alice,C=US\"", 2, 2,
				"fr");
		setCategory(AbstractAction.Category.dynamic.toString());
	}

	@Override
	public boolean invoke(String[] args, boolean isBatch) throws Exception {

		logger.debug("Command: findReverseMapping ");
		for (int i = 0; i < args.length; i++) {
			logger.debug("Parameter " + i + ": " + args[i]);
		}

		MappingDataType[] resp = cm.dapAdmin.findReverse(args[0], args[1]);

		printMapping(resp);
		

		return true;
	}

	

}
