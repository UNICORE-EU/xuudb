/*
 * Copyright (c) 2011-2012 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package de.fzj.unicore.xuudb.server.dynamic;

import java.util.List;


/**
 * Represents a pool of identifiers (gids or uids).
 * 
 * @author K. Benedyczak
 */
public class Pool
{
	protected int dbKey;
	protected String id;
	protected MappingType type;
	protected PoolKeyType keyType;
	protected String genericKey;
	protected boolean precreated;
	protected List<String> rawEntries;
	protected int realEntries;
	protected PoolConfiguration configuration;

	public Pool(String id, MappingType type, String key, boolean precreated, List<String> rawEntries, 
			PoolConfiguration configuration)
	{
		this.id = id;
		this.type = type;
		this.genericKey = key;
		try
		{
			keyType = PoolKeyType.valueOf(PoolKeyType.class, key);
		} catch (IllegalArgumentException e) 
		{
			keyType = PoolKeyType.generic;
		}
		this.precreated = precreated;
		this.rawEntries = rawEntries;
		this.configuration = configuration;
	}

	/**
	 * @return the id
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * @return the type
	 */
	public MappingType getType()
	{
		return type;
	}

	/**
	 * @return the key
	 */
	public PoolKeyType getKeyType()
	{
		return keyType;
	}

	/**
	 * @return the key type encoded as a string
	 */
	public String getKeyTypeCode()
	{
		if (getKeyType() == PoolKeyType.generic)
			return "generic/"+getGenericKey();
		return getKeyType().toString();
	}

	/**
	 * @return the precreated
	 */
	public boolean isPrecreated()
	{
		return precreated;
	}

	/**
	 * @return the configuration
	 */
	public PoolConfiguration getConfiguration()
	{
		return configuration;
	}

	/**
	 * @return the rawEntries
	 */
	public List<String> getRawEntries()
	{
		return rawEntries;
	}
	
	public String getGenericKey()
	{
		return genericKey;
	}
	
	public int getDbKey() {
		return dbKey;
	}

	public void setDbKey(int dbKey) {
		this.dbKey = dbKey;
	}

	public int getRealEntries() {
		return realEntries;
	}

	public void setRealEntries(int realEntries) {
		this.realEntries = realEntries;
	}
	
	
}
