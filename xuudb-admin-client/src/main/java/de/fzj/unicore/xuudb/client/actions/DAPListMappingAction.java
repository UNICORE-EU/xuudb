package de.fzj.unicore.xuudb.client.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.fzJuelich.unicore.xuudb.MappingDataType;

public class DAPListMappingAction extends AbstractAction {

	public DAPListMappingAction(ConnectionManager cm) {
		super(cm, "listMappings", "List mapping.\n" + " Syntax: \n"
				+ "        listMappings <any|live|frozen>  <pool-id>  \n"
				+ " Example:\n" + "        listMappings frozen pool1", 1, 2,
				"lm");
		setCategory(AbstractAction.Category.dynamic.toString());
	}

	@Override
	public boolean invoke(String[] args, boolean isBatch) throws Exception {
		logArguments(args);

		List<String> types = new ArrayList<>(Arrays.asList("any", "live",
				"frozen"));
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
