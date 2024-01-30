package eu.unicore.xuudb.client;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.Logger;

import eu.unicore.security.canl.CredentialProperties;
import eu.unicore.security.canl.TruststoreProperties;
import eu.unicore.util.configuration.FilePropertiesHelper;
import eu.unicore.util.configuration.PropertyMD;
import eu.unicore.util.httpclient.ClientProperties;
import eu.unicore.xuudb.CommonConfiguration;
import eu.unicore.xuudb.Log;

/**
 * Client configuration
 *
 * @author K. Benedyczak
 */
public class ClientConfiguration extends CommonConfiguration {

	private static final Logger logger = Log.getLogger(Log.CONFIGURATION, ClientConfiguration.class);

	public static final String PROP_BATCH = "batch";
	
	public final static Map<String, PropertyMD> DEFAULTS = new HashMap<>();
	static 
	{
		DEFAULTS.put(PROP_ADDRESS, new PropertyMD(DEFAULT_ADDRESS));
		DEFAULTS.put(PROP_BATCH, new PropertyMD("false"));
		DEFAULTS.put(TruststoreProperties.DEFAULT_PREFIX, new PropertyMD().setCanHaveSubkeys().setHidden().
				setDescription("Properties with this prefix are used to configure the server's credential. See separate documentation for details."));
		DEFAULTS.put(CredentialProperties.DEFAULT_PREFIX, new PropertyMD().setCanHaveSubkeys().setHidden().
				setDescription("Properties with this prefix are used to configure trust and certificate validation settings. See separate documentation for details."));
		DEFAULTS.put(ClientProperties.DEFAULT_PREFIX, new PropertyMD().setCanHaveSubkeys().setHidden().
				setDescription("Properties with this prefix are used to configure advanced HTTP client settings. See separate documentation for details."));
	}

	public ClientConfiguration(File config) throws IOException {
		super(PROP_PREFIX, FilePropertiesHelper.load(config), DEFAULTS, logger);
	}
	
	public ClientConfiguration(Properties p) {
		super(PROP_PREFIX, p, DEFAULTS, logger);
	}

	public boolean isBatch() {
		return getBooleanValue(PROP_BATCH);
	}
}







