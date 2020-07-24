/*
 * Copyright (c) 2012 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package de.fzj.unicore.xuudb.server.dynamic;

import java.util.Collection;
import java.util.Date;

import de.fzj.unicore.xuudb.server.db.IPoolStorage;

/**
 * Performs periodic maintenance of pools.
 * 
 * @author K. Benedyczak
 */
public class PoolsWatchdog implements Runnable
{
	private IPoolStorage storage;
	private Collection<Pool> pools;
	
	public PoolsWatchdog(IPoolStorage storage, Collection<Pool> pools)
	{
		this.storage = storage;
		this.pools = pools;
	}

	@Override
	public void run()
	{
		for (Pool pool: pools)
		{
			checkEmptiness(pool);
			freezeInactive(pool);
			deleteOutdated(pool);
		}
	}
	
	private void checkEmptiness(Pool pool)
	{
		PoolConfiguration cfg = pool.getConfiguration();
		if (cfg.getHandlerPoolEmpty() == null && cfg.getHandlerPoolGettingEmpty() == null)
			return;
		
		int emptyWarningAbsolute = cfg.getEmptyWarningAbsolute();
		int emptyWarningPercent = cfg.getEmptyWarningPercent();
		
		if (emptyWarningAbsolute <= 0 && emptyWarningPercent <= 0)
			return;
		int warningAmount = -1;
		if (emptyWarningPercent > 0)
		{
			int allEntries = pool.getRealEntries();
			warningAmount = (int)((allEntries*emptyWarningPercent)/100.0);
		}
		if (emptyWarningAbsolute > 0 && emptyWarningAbsolute > warningAmount)
			warningAmount = emptyWarningAbsolute;
		
		storage.checkEmptiness(pool, warningAmount);
	}
	
	private void freezeInactive(Pool pool)
	{
		if (pool.getConfiguration().getAutomaticFreezeAfter() <= 0)
			return;
		int freezeTime = pool.getConfiguration().getAutomaticFreezeAfter();
		Date freezeSince = new Date(System.currentTimeMillis() - freezeTime*1000);
		storage.freezeInactive(pool, freezeSince);
	}
	
	private void deleteOutdated(Pool pool)
	{
		if (pool.getConfiguration().getAutomaticDeleteAfter() <= 0)
			return;
		int deleteTime = pool.getConfiguration().getAutomaticDeleteAfter();
		Date deleteSince = new Date(System.currentTimeMillis() - deleteTime*1000);
		storage.deleteOld(pool, deleteSince);
	}
}
