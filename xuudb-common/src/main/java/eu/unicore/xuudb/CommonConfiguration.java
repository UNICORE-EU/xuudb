package eu.unicore.xuudb;

import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.Logger;

import eu.unicore.util.configuration.ConfigurationException;
import eu.unicore.util.configuration.PropertiesHelper;
import eu.unicore.util.configuration.PropertyMD;

/**
 * Defines common things for client's and server's configuration classes.
 * 
 * @author K. Benedyczak
 */
public abstract class CommonConfiguration extends PropertiesHelper {

	public static final String PROP_PREFIX = "xuudb.";
	
	public static final String PROP_ADDRESS = "address";

	public static final String DEFAULT_ADDRESS = "http://localhost:34463";

	public CommonConfiguration(String prefix, Properties properties,
			Map<String, PropertyMD> defaults, Logger log)
			throws ConfigurationException
	{
		super(prefix, properties, defaults, log);
	}

	public Properties getProperties() {
		return properties;
	}
}







