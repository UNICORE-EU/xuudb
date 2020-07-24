package de.fzj.unicore.xuudb.client.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import de.fzJuelich.unicore.xuudb.MappingDataType;
import de.fzj.unicore.xuudb.Log;

public class DAPListMappingAction extends AbstractAction {

	private static final Logger logger = Log.getLogger(
			Log.XUUDB_CLIENT, DAPListMappingAction.class);

	public DAPListMappingAction(ConnectionManager cm) {
		super(cm, "listMappings", "List mapping.\n" + " Syntax: \n"
				+ "        listMappings <any|live|frozen>  <pool-id>  \n"
				+ " Example:\n" + "        listMappings frozen pool1", 1, 2,
				"lm");
		setCategory(AbstractAction.Category.dynamic.toString());
	}

	@Override
	public boolean invoke(String[] args, boolean isBatch) throws Exception {
		List<String> types = new ArrayList<String>(Arrays.asList("any", "live",
				"frozen"));

		logger.debug("Command: listMappings ");
		for (int i = 0; i < args.length; i++) {
			logger.debug("Parameter " + i + ": " + args[i]);
		}

		if (!types.contains(args[0].toLowerCase()))
			throw new IllegalArgumentException("Mapping type is incorrect");
		
		MappingDataType[] resp=null;
		if(args.length>1)
			 resp= cm.dapAdmin.list(args[0], args[1]);
		else
			 resp = cm.dapAdmin.list(args[0], null);
		
		
		printMapping(resp);

		return true;
	}
}
