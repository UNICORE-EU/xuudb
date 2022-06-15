package de.fzj.unicore.xuudb.client.actions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DAPFreezeMappingAction extends AbstractAction {

	public DAPFreezeMappingAction(ConnectionManager cm) {
		super(
				cm,
				"freezeMappings",
				"Freeze mappings older than x, for specified pool or "
						+ "freeze a specified live mappings by key\n"
						+ " Syntax:\n"
						+ "        freezeMappings <yyyy-MM-dd[-HH:mm:ss]> <pool-id> \n"
						+ "        freezeMappings <key> <pool-id>\n"
						+ " Example:\n"
						+ "        freezeMapping 2011-12-12 pool4", 2, 2);

		setCategory(AbstractAction.Category.dynamic.toString());
	}

	@Override
	public boolean invoke(String[] args, boolean isBatch) throws Exception {
		logArguments(args);

		Date from = tryParseDate(args[0]);
		
		if (from != null) {
			System.out.println("Freezing older then " + from);
			cm.dapAdmin.freeze(from, args[1]);
		} else {
			System.out.println("Freezing by key: " + args[0]);
			cm.dapAdmin.freeze(args[0], args[1]);
		}
		System.out.println("Done.");
		return true;
	}
	
	public static Date tryParseDate(String arg) {
		Date from = null;
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
		try {
			from = format1.parse(arg);
		} catch (ParseException e) {
			SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd");
			try {
				from = format2.parse(arg);
			} catch (ParseException ee) {
			}
		}
		return from;
	}

}
