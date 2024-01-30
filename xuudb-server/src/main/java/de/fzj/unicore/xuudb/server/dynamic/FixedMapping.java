package de.fzj.unicore.xuudb.server.dynamic;

import java.util.Arrays;
import java.util.Collections;

import org.apache.logging.log4j.Logger;

import de.fzj.unicore.xuudb.Log;

import eu.unicore.util.configuration.ConfigurationException;

/**
 * Sets the fixed, configured values.
 * 
 * @author K. Benedyczak
 */
public class FixedMapping extends Mapping
{
	private static final Logger log = Log.getLogger(Log.XUUDB_SERVER, FixedMapping.class);
	public static final String ID = "fixed";
	private final String[] values;

	public FixedMapping(String configuration, MappingType maps)
	{
		super(configuration, maps);
		String val = configuration.trim();
		if (val.length() == 0)
			throw new ConfigurationException("Mapping of type fixed can not be configured with empty value");
		if (getType().equals(MappingType.supplementaryGids))
			values = configuration.trim().split("[ ]+");
		else
			values = new String[] {val};
	}
	
	@Override
	public void applyAttributes(EvaluationContext context, boolean overwrite, boolean dryRun)
	{
		switch (getType()) {
		case uid:
			log.debug("Setting xlogin to: {}", values[0]);
			context.setXlogin(values[0]);
			break;
		case gid:
			log.debug("Setting gid to: {}", values[0]);
			context.setGid(values[0]);
			break;
		case supplementaryGids:
			if (overwrite) {
				context.setSupplementaryGids(Arrays.asList(values));
				log.debug("Setting supplementary groups to: {}", context.getSupplementaryGids());
			} else {
				Collections.addAll(context.getSupplementaryGids(), values);
				log.debug("Adding the following supplementary groups: {}", Arrays.toString(values));
			}
			break;
		}
	}
}
