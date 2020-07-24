/*
 * Copyright (c) 2011-2012 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package de.fzj.unicore.xuudb.server.db;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.h2.Driver;

import de.fzj.unicore.xuudb.AbstractConfiguration;
import de.fzj.unicore.xuudb.Log;

import eu.unicore.util.configuration.ConfigurationException;
import eu.unicore.util.configuration.DocumentationReferenceMeta;
import eu.unicore.util.configuration.DocumentationReferencePrefix;
import eu.unicore.util.configuration.PropertiesHelper;
import eu.unicore.util.configuration.PropertyMD;
import eu.unicore.util.db.DBPropertiesHelper;

/**
 * Configuration of database used.
 * 
 * @author K. Benedyczak
 */
public class DatabaseProperties extends PropertiesHelper
{
	private static final Logger log = Log.getLogger(Log.XUUDB_DB, DatabaseProperties.class);
	
	private enum DbDialect {h2, mysql};
	
	@DocumentationReferencePrefix
	public static final String PROP_PREFIX = AbstractConfiguration.PROP_PREFIX+DBPropertiesHelper.PREFIX;
	
	@DocumentationReferenceMeta
	public final static Map<String, PropertyMD> DEFAULTS = new HashMap<String, PropertyMD>();
	static 
	{
		DEFAULTS.putAll(DBPropertiesHelper.getMetadata(Driver.class, "jdbc:h2:data/xuudb2", DbDialect.h2, ""));
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
