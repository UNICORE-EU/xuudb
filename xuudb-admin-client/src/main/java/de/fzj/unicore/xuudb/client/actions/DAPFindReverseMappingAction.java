package de.fzj.unicore.xuudb.client.actions;

import de.fzJuelich.unicore.xuudb.MappingDataType;

public class DAPFindReverseMappingAction extends AbstractAction {

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
		logArguments(args);
		MappingDataType[] resp = cm.dapAdmin.findReverse(args[0], args[1]);
		printMapping(resp);
		return true;
	}

	

}
