package de.fzj.unicore.xuudb.client.actions;

public class DAPRemovePoolAction extends AbstractAction {
	
	public DAPRemovePoolAction(ConnectionManager cm) {
		super(cm, "removePool", "Remove specified pool\n" + " Syntax: \n"
				+ "        removePool <pool-id> \n" + " Example:\n"
				+ "        removePool pool5", 1, 1, "rmp");
		setCategory(AbstractAction.Category.dynamic.toString());
	}

	@Override
	public boolean invoke(String[] args, boolean isBatch) throws Exception {
		logArguments(args);

		cm.dapAdmin.removePool(args[0]);

		return true;

	}
}
