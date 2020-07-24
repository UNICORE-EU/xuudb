/*
 * Copyright (c) 2012 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package de.fzj.unicore.xuudb.server.dynamic;

import de.fzj.unicore.xuudb.server.dynamic.xbeans.Configuration;
import eu.unicore.util.configuration.ConfigurationException;


/**
 * Maintains information about pool configuration.
 * @author K. Benedyczak
 */
public class PoolConfiguration
{
	private int automaticFreezeAfter = -1;
	private int automaticDeleteAfter = -1;
	private int emptyWarningAbsolute = -1;
	private int emptyWarningPercent = -1;
	
	private int handlerInvocationTimeLimit = -1;
	private String handlerAboutToFreeze = null;
	private String handlerAboutToDelete = null;
	private String handlerCreateSystemUid = null;
	private String handlerCreateSystemGid = null;
	private String handlerPoolGettingEmpty = null;
	private String handlerPoolEmpty = null;

	public PoolConfiguration(Configuration perPool, Configuration general) throws ConfigurationException
	{
		if (perPool.isSetAutomaticDeleteAfter())
			automaticDeleteAfter = perPool.getAutomaticDeleteAfter();
		else if (general.isSetAutomaticDeleteAfter())
			automaticDeleteAfter = general.getAutomaticDeleteAfter();

		if (perPool.isSetAutomaticFreezeAfter())
			automaticFreezeAfter = perPool.getAutomaticFreezeAfter();
		else if (general.isSetAutomaticFreezeAfter())
			automaticFreezeAfter = general.getAutomaticFreezeAfter();

		if (perPool.isSetEmptyWarningAbsolute())
			emptyWarningAbsolute = perPool.getEmptyWarningAbsolute();
		else if (general.isSetAutomaticFreezeAfter())
			emptyWarningAbsolute = general.getEmptyWarningAbsolute();

		if (perPool.isSetEmptyWarningPercent())
			emptyWarningPercent = perPool.getEmptyWarningPercent();
		else if (general.isSetAutomaticFreezeAfter())
			emptyWarningPercent = general.getEmptyWarningPercent();

		if (perPool.isSetHandlerInvocationTimeLimit())
			handlerInvocationTimeLimit = perPool.getHandlerInvocationTimeLimit();
		else if (general.isSetAutomaticFreezeAfter())
			handlerInvocationTimeLimit = general.getHandlerInvocationTimeLimit();

		if (handlerInvocationTimeLimit <= 0)
			handlerInvocationTimeLimit = ProcessInvoker.MAX_TIMEOUT;
		
		
		if (perPool.isSetHandlerAboutToDelete())
			handlerAboutToDelete = perPool.getHandlerAboutToDelete();
		else if (general.isSetHandlerAboutToDelete())
			handlerAboutToDelete = general.getHandlerAboutToDelete();
		if (handlerAboutToDelete != null && handlerAboutToDelete.trim().equals(""))
			handlerAboutToDelete = null;

		if (perPool.isSetHandlerAboutToFreeze())
			handlerAboutToFreeze = perPool.getHandlerAboutToFreeze();
		else if (general.isSetHandlerAboutToDelete())
			handlerAboutToFreeze = general.getHandlerAboutToFreeze();
		if (handlerAboutToFreeze != null && handlerAboutToFreeze.trim().equals(""))
			handlerAboutToFreeze = null;

		if (perPool.isSetHandlerCreateSystemGid())
			handlerCreateSystemGid = perPool.getHandlerCreateSystemGid();
		else if (general.isSetHandlerCreateSystemGid())
			handlerCreateSystemGid = general.getHandlerCreateSystemGid();
		if (handlerCreateSystemGid != null && handlerCreateSystemGid.trim().equals(""))
			handlerCreateSystemGid = null;

		if (perPool.isSetHandlerCreateSystemUid())
			handlerCreateSystemUid = perPool.getHandlerCreateSystemUid();
		else if (general.isSetHandlerCreateSystemUid())
			handlerCreateSystemUid = general.getHandlerCreateSystemUid();
		if (handlerCreateSystemUid != null && handlerCreateSystemUid.trim().equals(""))
			handlerCreateSystemUid = null;

		if (perPool.isSetHandlerPoolEmpty())
			handlerPoolEmpty = perPool.getHandlerPoolEmpty();
		else if (general.isSetHandlerPoolEmpty())
			handlerPoolEmpty = general.getHandlerPoolEmpty();
		if (handlerPoolEmpty != null && handlerPoolEmpty.trim().equals(""))
			handlerPoolEmpty = null;

		if (perPool.isSetHandlerPoolGettingEmpty())
			handlerPoolGettingEmpty = perPool.getHandlerPoolGettingEmpty();
		else if (general.isSetHandlerAboutToDelete())
			handlerPoolGettingEmpty = general.getHandlerPoolGettingEmpty();
		if (handlerPoolGettingEmpty != null && handlerPoolGettingEmpty.trim().equals(""))
			handlerPoolGettingEmpty = null;

		if (emptyWarningPercent > 99)
			throw new ConfigurationException("Percentage warning threshold must not be greater then 99");
		if ((emptyWarningAbsolute > 0 || emptyWarningPercent > 0) && handlerPoolGettingEmpty == null)
			throw new ConfigurationException("When warning about the pool getting empty is going to be " +
					"issued, the notification program must be defined.");
	}
	
	
	
	
	/**
	 * @return the automaticFreezeAfter
	 */
	public int getAutomaticFreezeAfter()
	{
		return automaticFreezeAfter;
	}
	/**
	 * @return the automaticDeleteAfter
	 */
	public int getAutomaticDeleteAfter()
	{
		return automaticDeleteAfter;
	}
	/**
	 * @return the emptyWarningAbsolute
	 */
	public int getEmptyWarningAbsolute()
	{
		return emptyWarningAbsolute;
	}
	/**
	 * @return the emptyWarningPercent
	 */
	public int getEmptyWarningPercent()
	{
		return emptyWarningPercent;
	}
	/**
	 * @return the handlerInvocationTimeLimit
	 */
	public int getHandlerInvocationTimeLimit()
	{
		return handlerInvocationTimeLimit;
	}
	/**
	 * @return the handlerAboutToFreeze
	 */
	public String getHandlerAboutToFreeze()
	{
		return handlerAboutToFreeze;
	}
	/**
	 * @return the handlerAboutToDelete
	 */
	public String getHandlerAboutToDelete()
	{
		return handlerAboutToDelete;
	}
	/**
	 * @return the handlerCreateSystemUid
	 */
	public String getHandlerCreateSystemUid()
	{
		return handlerCreateSystemUid;
	}
	/**
	 * @return the handlerCreateSystemGid
	 */
	public String getHandlerCreateSystemGid()
	{
		return handlerCreateSystemGid;
	}
	/**
	 * @return the handlerPoolGettingEmpty
	 */
	public String getHandlerPoolGettingEmpty()
	{
		return handlerPoolGettingEmpty;
	}
	/**
	 * @return the handlerPoolEmpty
	 */
	public String getHandlerPoolEmpty()
	{
		return handlerPoolEmpty;
	}
}
