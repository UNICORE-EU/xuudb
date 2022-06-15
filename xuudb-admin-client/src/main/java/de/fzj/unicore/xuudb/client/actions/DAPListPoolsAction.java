package de.fzj.unicore.xuudb.client.actions;

import de.fzJuelich.unicore.xuudb.PoolInfoType;

public class DAPListPoolsAction extends AbstractAction {
	
	public DAPListPoolsAction(ConnectionManager cm) {
		super(cm, "listPools", "List pools.\n" + " Syntax: \n"
				+ "        listPools   \n" + " Example:\n"
				+ "        listPools", 0, 0, "lp");
		setCategory(AbstractAction.Category.dynamic.toString());
	}

	@Override
	public boolean invoke(String[] args, boolean isBatch) throws Exception {
		logArguments(args);
		PoolInfoType[] resp = cm.dapAdmin.listPools();

		if (resp == null)
			System.out.println("The database query result is empty.");
		else {
			System.out.println("The database query result contains   "
					+ resp.length + "   entries:");
			System.out.printf("%-25s|%-15s|%-15s|%-15s|%-15s|%-15s|\n", "         Pool id", "   Pool type", " Pool key type","Active mappings","Frozen mappings","  Free slots");
			System.out
					.println("---------------------------------------------------------------------------------------------------------");
			for (int i = 0; i < resp.length; i++) {

				System.out.printf("%25s|%15s|%15s|%15s|%15s|%15s|\n", resp[i].getPoolId(),
						resp[i].getPoolType(), resp[i].getPoolKeyType(),
						resp[i].getActiveMappings(), resp[i]
								.getFrozenMappings(), resp[i].getFreeSlots());
			}
		}

		return true;

	}

}
