package eu.unicore.xuudb.server.db;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.Logger;
import org.h2.Driver;

import eu.unicore.util.configuration.ConfigurationException;
import eu.unicore.util.configuration.DocumentationReferenceMeta;
import eu.unicore.util.configuration.DocumentationReferencePrefix;
import eu.unicore.util.configuration.PropertiesHelper;
import eu.unicore.util.configuration.PropertyMD;
import eu.unicore.util.db.DBPropertiesHelper;
import eu.unicore.xuudb.CommonConfiguration;
import eu.unicore.xuudb.Log;

/**
 * Configuration of database used.
 * 
 * @author K. Benedyczak
 */
public class DatabaseProperties extends PropertiesHelper
{
	private static final Logger log = Log.getLogger(Log.XUUDB_DB, DatabaseProperties.class);
	
	private enum DbDialect {h2, mysql, pgsql};
	
	public static String CHARSET = "charset";

	@DocumentationReferencePrefix
	public static final String PROP_PREFIX = CommonConfiguration.PROP_PREFIX+DBPropertiesHelper.PREFIX;
	
	@DocumentationReferenceMeta
	public final static Map<String, PropertyMD> DEFAULTS = new HashMap<>();
	static 
	{
		DEFAULTS.putAll(DBPropertiesHelper.getMetadata(Driver.class, "jdbc:h2:data/xuudb2", DbDialect.h2, ""));
		DEFAULTS.put(CHARSET, new PropertyMD("utf8").setCategory(DBPropertiesHelper.dbCategory).
				setDescription("(MySQL) Charset to use for XUUDB tables."));
	}

	public DatabaseProperties(Properties properties) throws ConfigurationException
	{
		super(PROP_PREFIX, properties, DEFAULTS, log);
	}
	
	public Properties getProperties()
	{
		return properties;
	}
}
