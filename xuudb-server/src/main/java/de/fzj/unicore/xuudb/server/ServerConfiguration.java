package de.fzj.unicore.xuudb.server;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.Logger;

import de.fzj.unicore.xuudb.CommonConfiguration;
import de.fzj.unicore.xuudb.Log;
import eu.unicore.security.canl.CredentialProperties;
import eu.unicore.security.canl.TruststoreProperties;
import eu.unicore.util.configuration.DocumentationReferenceMeta;
import eu.unicore.util.configuration.FilePropertiesHelper;
import eu.unicore.util.configuration.PropertyMD;
import eu.unicore.util.db.DBPropertiesHelper;
import eu.unicore.util.jetty.HttpServerProperties;

/**
 * Server configuration
 *
 * @author K. Benedyczak
 */
public class ServerConfiguration extends CommonConfiguration {

	private static final Logger logger = Log.getLogger(Log.CONFIGURATION, ServerConfiguration.class);

	public enum XuudbModes {dn};
	
	public static final String PROP_XUUDBTYPE = "type";
	public static final String PROP_ACL_FILE = "aclFile";
	public static final String PROP_PROTECT_ALL = "protectAll";
	public static final String PROP_DAP_FILE = "dynamicAttributesConfig";
	
	@DocumentationReferenceMeta
	public final static Map<String, PropertyMD> DEFAULTS = new HashMap<>();
	static 
	{
		DEFAULTS.put(PROP_ADDRESS, new PropertyMD(DEFAULT_ADDRESS).
				setDescription("HTTPS or HTTP URL where the server should listen."));
		DEFAULTS.put(PROP_XUUDBTYPE, new PropertyMD().setDeprecated().
				setDescription("DEPRECATED - no longer required."));
		DEFAULTS.put(PROP_DAP_FILE, new PropertyMD("conf/dynamicAttributesCfg.xml").setPath().
				setDescription("File with configuration of the dynamic part of the XUUDB."));
		DEFAULTS.put(PROP_ACL_FILE, new PropertyMD().setPath().setMandatory().
				setDescription("File with DNs of servers/clients authorised to access protected XUUDB services."));
		DEFAULTS.put(PROP_PROTECT_ALL, new PropertyMD("false").
				setDescription("If true then access to both query and modify operations are protected by ACL. If false then only modification operations are protected."));
		DEFAULTS.put(DBPropertiesHelper.PREFIX, new PropertyMD().setCanHaveSubkeys().
				setDescription("Properties with this prefix are used to configure database backend, used by XUUDB. See separate documentation for details."));
		DEFAULTS.put(HttpServerProperties.DEFAULT_PREFIX, new PropertyMD().setCanHaveSubkeys().setHidden().
				setDescription("Properties with this prefix are used to configure advanced Jetty HTTP server settings. See separate documentation for details."));
		DEFAULTS.put(TruststoreProperties.DEFAULT_PREFIX, new PropertyMD().setCanHaveSubkeys().setHidden().
				setDescription("Properties with this prefix are used to configure the server's credential. See separate documentation for details."));
		DEFAULTS.put(CredentialProperties.DEFAULT_PREFIX, new PropertyMD().setCanHaveSubkeys().setHidden().
				setDescription("Properties with this prefix are used to configure trust and certificate validation settings. See separate documentation for details."));
	}

	public ServerConfiguration(File config) throws IOException {
		super(PROP_PREFIX, FilePropertiesHelper.load(config), DEFAULTS, logger);
	}
	
	public ServerConfiguration(Properties p) {
		super(PROP_PREFIX, p, DEFAULTS, logger);
	}

}







