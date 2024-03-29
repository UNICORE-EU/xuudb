package eu.unicore.xuudb.client.actions;

import java.util.Date;

public class DAPRemoveMappingAction extends AbstractAction {

	public DAPRemoveMappingAction(ConnectionManager cm) {
		super(
				cm,
				"removeMappings",
				"Remove mappings older than x,for specified pool or "
						+ "remove a specified frozen mappings by key \n"
						+ " Syntax: \n"
						+ "        removeMappings <yyyy-MM-dd[-HH:mm:ss]> <pool-id> \n"
						+ "        removeMappings <key> <pool-id> \n"
						+ " Example:\n"
						+ "        removeMappings 2011-12-12 pool5", 2, 2);
		setCategory(AbstractAction.Category.dynamic.toString());
	}

	@Override
	public boolean invoke(String[] args, boolean isBatch) throws Exception {
		logArguments(args);

		Date from = DAPFreezeMappingAction.tryParseDate(args[0]);

		if (from != null)
			cm.dapAdmin.remove(from, args[1]);
		else
			cm.dapAdmin.remove(args[0], args[1]);

		System.out.println("Done.");
		return true;

	}
}
