/*
 * Copyright (c) 2012 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package de.fzj.unicore.xuudb.server.db;

/**
 * Contains information about a particular pool, including the number of its mappings
 * @author K. Benedyczak
 */
public class PoolInfoBean
{
	private int aliveMappings;
	private int frozenMappings;
	private int freeSlots;
	private String poolName;
	private String poolType;
	private String poolKeyType;
	
	/**
	 * @return the aliveMappings
	 */
	public int getAliveMappings()
	{
		return aliveMappings;
	}
	/**
	 * @param aliveMappings the aliveMappings to set
	 */
	public void setAliveMappings(int aliveMappings)
	{
		this.aliveMappings = aliveMappings;
	}
	/**
	 * @return the frozenMappings
	 */
	public int getFrozenMappings()
	{
		return frozenMappings;
	}
	/**
	 * @param frozenMappings the frozenMappings to set
	 */
	public void setFrozenMappings(int frozenMappings)
	{
		this.frozenMappings = frozenMappings;
	}
	/**
	 * @return the freeSlots
	 */
	public int getFreeSlots()
	{
		return freeSlots;
	}
	/**
	 * @param freeSlots the freeSlots to set
	 */
	public void setFreeSlots(int freeSlots)
	{
		this.freeSlots = freeSlots;
	}
	/**
	 * @return the poolName
	 */
	public String getPoolName()
	{
		return poolName;
	}
	/**
	 * @param poolName the poolName to set
	 */
	public void setPoolName(String poolName)
	{
		this.poolName = poolName;
	}
	/**
	 * @return the poolType
	 */
	public String getPoolType()
	{
		return poolType;
	}
	/**
	 * @param poolType the poolType to set
	 */
	public void setPoolType(String poolType)
	{
		this.poolType = poolType;
	}
	/**
	 * @return the poolKeyType
	 */
	public String getPoolKeyType()
	{
		return poolKeyType;
	}
	/**
	 * @param poolKeyType the poolKeyType to set
	 */
	public void setPoolKeyType(String poolKeyType)
	{
		this.poolKeyType = poolKeyType;
	}
}
