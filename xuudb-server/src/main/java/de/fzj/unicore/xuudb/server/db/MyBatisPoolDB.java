/*
 * Copyright (c) 2012 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package de.fzj.unicore.xuudb.server.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import de.fzj.unicore.xuudb.Log;
import de.fzj.unicore.xuudb.server.dynamic.MappingStatus;
import de.fzj.unicore.xuudb.server.dynamic.MappingType;
import de.fzj.unicore.xuudb.server.dynamic.Pool;
import de.fzj.unicore.xuudb.server.dynamic.ProcessInvoker;
import eu.unicore.util.configuration.ConfigurationException;

public class MyBatisPoolDB implements IPoolStorage 
{
	public static final Logger log = Log.getLogger(Log.XUUDB_DB, MyBatisPoolDB.class);
	private MyBatisSessionFactory factory;
	private static Pattern PATTERN = Pattern.compile("\\[\\d+\\-\\d+\\]");
	private static final String ST_CREATE_TABLE_POOLS = "create-pools-table";
	private static final String ST_CREATE_TABLE_MAPPINGS = "create-mappings-table";

	
	public MyBatisPoolDB(MyBatisSessionFactory factory) 
	{
		this.factory = factory;
		SqlSession session = factory.openSession(false);
		try
		{
			session.update(ST_CREATE_TABLE_POOLS);
			session.update(ST_CREATE_TABLE_MAPPINGS);
		} finally 
		{
			factory.closeSession(session);
		}
	}

	@Override
	public void initializePools(Iterable<Pool> pools) throws ConfigurationException
	{
		log.debug("Initializing pools in the database");
		SqlSession session = factory.openBatchSession(true);
		try
		{
			UudbPoolMapper mapper = session.getMapper(UudbPoolMapper.class);
			for (Pool pool: pools) 
			{
				MappingBean bean = mapper.checkPoolExistence(pool.getId());
				if (bean == null)
				{
					log.debug("Adding pool " + pool.getId() + " to the database");
					MappingBean input = new MappingBean();
					input.setPoolName(pool.getId());
					input.setMappingKeyType(pool.getKeyTypeCode());
					input.setMappingValueType(pool.getType().toString());
					mapper.createPool(input);
					session.flushStatements();
					int newKey = input.getId();
					log.debug("New pool DB id is " + newKey);
					pool.setDbKey(newKey);
					populatePool(pool, mapper);
				} else
				{
					log.debug("Pool " + pool.getId() + " already exists in the database");
					if (!pool.getKeyTypeCode().equals(bean.getMappingKeyType()))
						throw new ConfigurationException("Inconsistency between database " +
								"and configuration detected. Pool " + pool.getId() + 
								" key type in DB is " + bean.getMappingKeyType() + 
								" and in configuration is: " + pool.getKeyTypeCode());
					if (!pool.getType().toString().equals(bean.getMappingValueType()))
						throw new ConfigurationException("Inconsistency between database " +
								"and configuration detected. Pool " + pool.getId() + 
								" value type in DB is " + bean.getMappingValueType() + 
								" and in configuration is: " + pool.getType());
					pool.setDbKey(bean.getId());
				}
			}
			session.commit();
		} finally 
		{
			factory.closeSession(session);
		}
	}
	

	@Override
	public String getOrCreateMapping(Pool pool, String key, boolean dryRun) 
	{
		log.debug("Trying to find mapping of " + key + " in pool " + pool.getId());
		SqlSession session = factory.openSession(true);
		try
		{
			UudbPoolMapper mapper = session.getMapper(UudbPoolMapper.class);
			MappingBean mapping = mapper.getAliveMapping(pool.getDbKey(), key);
			if (mapping != null) 
			{
				log.debug("Mapping of " + key + " in pool " + pool.getId() + " was found: " + 
						mapping.getEntry());
				if (!dryRun)
				{
					mapper.updateAccessTime(mapping.getId());
					session.commit();
				}
				return mapping.getEntry();
			}
			log.debug("Mapping of " + key + " in pool " + pool.getId() + " wasn't found, creating a new one.");
			MappingBean freeMappingKey = mapper.getFreeMapping(pool.getDbKey());
			if (freeMappingKey == null)
			{
				log.debug("Pool " + pool.getId() + " is empty, can't create a new mapping");
				return null;
			}
			log.debug("New mapping of " + key + " in pool " + pool.getId() + 
					" was established: " + freeMappingKey.getEntry());
			if (!dryRun)
			{
				if (!runCreateHandler(freeMappingKey.getEntry(), key, pool))
				{
					log.debug("Handler returned that this mapping should not be created: "
							 + key + " -> " + freeMappingKey.getEntry());
					return null;
				}
				mapper.addMapping(key, freeMappingKey.getId());
				session.commit();
			}
			return freeMappingKey.getEntry();
		} finally 
		{
			factory.closeSession(session);
		}
	}
	
	

	@Override
	public void freezeInactive(Pool pool, Date inactiveFrom)
	{
		log.debug("Freezing mappings inactive since " + inactiveFrom + " in pool " + pool.getId());
		
		String handler = pool.getConfiguration().getHandlerAboutToFreeze();
		ProcessInvoker invoker = new ProcessInvoker(pool.getConfiguration().getHandlerInvocationTimeLimit());
		
		SqlSession session = factory.openSession(true);
		try
		{
			UudbPoolMapper mapper = session.getMapper(UudbPoolMapper.class);
			List<MappingBean> inactive = mapper.getInactiveMappings(pool.getDbKey(), inactiveFrom);
			
			for (MappingBean bean: inactive)
			{
				if (!runFreezingHandler(handler, invoker, bean, pool))
				{
					log.debug("Handler returned that this mapping should not be frozen: "
							 + bean.getId() + " " + bean.getEntry());
					continue;
				}
				log.debug("Freezing a mapping: " + bean.getId() + " " + bean.getEntry());
				mapper.freezeMapping(bean.getId());
			}
			session.commit();
		} finally 
		{
			factory.closeSession(session);
		}
	}

	@Override
	public void freezeSpecified(Pool pool, String value)
	{
		log.debug("Freezing mapping of " + value + " in pool " + pool.getId());
		
		String handler = pool.getConfiguration().getHandlerAboutToFreeze();
		ProcessInvoker invoker = new ProcessInvoker(pool.getConfiguration().getHandlerInvocationTimeLimit());
		
		SqlSession session = factory.openSession(true);
		try
		{
			UudbPoolMapper mapper = session.getMapper(UudbPoolMapper.class);
			MappingBean bean = mapper.getAliveMapping(pool.getDbKey(), value);
			if (bean == null)
				throw new IllegalArgumentException("Mapping of " + value + 
						" does not exist in the pool " + pool.getId() + " or is not alive");

			if (!runFreezingHandler(handler, invoker, bean, pool))
				log.warn("Handler returned that this mapping should not be frozen: "
						 + bean.getId() + " " + bean.getEntry() + 
						 ". Ignoring as freezeing was forced.");
			log.debug("Freezing a mapping: " + bean.getId() + " " + bean.getEntry());
			mapper.freezeMapping(bean.getId());
			session.commit();
		} finally 
		{
			factory.closeSession(session);
		}
	}

	@Override
	public void deleteOld(Pool pool, Date inactiveFrom)
	{
		log.debug("Deleting mappings frozen since " + inactiveFrom + " in pool " + pool.getId());
		
		String handler = pool.getConfiguration().getHandlerAboutToDelete();
		ProcessInvoker invoker = new ProcessInvoker(pool.getConfiguration().getHandlerInvocationTimeLimit());
		
		SqlSession session = factory.openSession(true);
		try
		{
			UudbPoolMapper mapper = session.getMapper(UudbPoolMapper.class);
			List<MappingBean> old = mapper.getOldMappings(pool.getDbKey(), inactiveFrom);
			
			for (MappingBean bean: old)
			{
				if (!runRemovalHandler(handler, invoker, bean, pool))
				{
					log.debug("Handler returned that this mapping should not be deleted: "
							 + bean.getId() + " " + bean.getEntry());
					continue;
				}
				log.debug("Removing a mapping: " + bean.getId() + " " + bean.getEntry());
				mapper.removeMapping(bean.getId());
			}
			session.commit();
		} finally 
		{
			factory.closeSession(session);
		}
	}	
	
	@Override
	public void deleteSpecified(Pool pool, String value)
	{
		log.debug("Deleting mapping of " + value + " in pool " + pool.getId());
		
		String handler = pool.getConfiguration().getHandlerAboutToDelete();
		ProcessInvoker invoker = new ProcessInvoker(pool.getConfiguration().getHandlerInvocationTimeLimit());
		
		SqlSession session = factory.openSession(true);
		try
		{
			UudbPoolMapper mapper = session.getMapper(UudbPoolMapper.class);
			List<MappingBean> beans = mapper.getFrozenMappings(pool.getDbKey(), value);
			if (beans == null || beans.size() == 0)
				throw new IllegalArgumentException("Mapping of " + value + 
						" does not exist in the pool " + pool.getId());
			for (MappingBean bean: beans) 
			{
				if (!runRemovalHandler(handler, invoker, bean, pool))
					log.warn("Handler returned that this mapping should not be deleted: "
						 + bean.getId() + " " + bean.getEntry() + 
						 ". Ignoring as removal was externally forced.");
				log.debug("Removing a mapping: " + bean.getId() + " " + bean.getEntry());
				mapper.removeMapping(bean.getId());
			}
			session.commit();
		} finally 
		{
			factory.closeSession(session);
		}
	}
	
	@Override
	public void checkEmptiness(Pool pool, int warningThreshold)
	{
		SqlSession session = factory.openSession(false);
		long occupied;
		try
		{
			UudbPoolMapper mapper = session.getMapper(UudbPoolMapper.class);
			occupied = mapper.countOccupiedMappings(pool.getDbKey());
		} finally 
		{
			factory.closeSession(session);
		}
		
		ProcessInvoker invoker = new ProcessInvoker(pool.getConfiguration().getHandlerInvocationTimeLimit());
		long remainingFree = pool.getRealEntries() - occupied; 
		if (remainingFree < 0)
			throw new IllegalStateException("Pools database is corrupted, please report this. " +
					"Pool id is " + pool.getDbKey() + " pool " + pool.getId() + " has " + 
					occupied + " occupied entries, while it is reported that " + 
					pool.getRealEntries() + " entries are in total.");
		if (remainingFree == 0 && pool.getConfiguration().getHandlerPoolEmpty() != null)
		{
			String h = pool.getConfiguration().getHandlerPoolEmpty();
			String []cmdLine = new String[] {h, pool.getId(), pool.getType().toString()};
			try
			{
				invoker.invoke(cmdLine);
			} catch (Exception e)
			{
				Log.logException("Can't invoke application notifying about empty pool, " +
						"cmd line was " + Arrays.toString(cmdLine) + "; " + e.toString(), e);
			}
		} 
		
		if (remainingFree > 0 && remainingFree < warningThreshold && pool.getConfiguration().getHandlerPoolGettingEmpty() != null)
		{
			String h = pool.getConfiguration().getHandlerPoolGettingEmpty();
			String []cmdLine = new String[] {h, pool.getId(), pool.getType().toString(), remainingFree+""};
			try
			{
				invoker.invoke(cmdLine);
			} catch (Exception e)
			{
				Log.logException("Can't invoke application notifying about pool getting empty, " +
						"cmd line was " + Arrays.toString(cmdLine) + "; " + e.toString(), e);
			}
			
		}
	}
	
	@Override
	public List<MappingBean> listMappings(Integer poolId, MappingStatus statusEnum)
	{
		SqlSession session = factory.openSession(false);
		try
		{
			UudbPoolMapper mapper = session.getMapper(UudbPoolMapper.class);
			String status = null;
			if (statusEnum != MappingStatus.any)
				status = statusEnum.name();
			return mapper.getMappings(poolId, status);
		} finally 
		{
			factory.closeSession(session);
		}
	}

	@Override
	public List<MappingBean> listMappingsByValue(String valueType, String value)
	{
		SqlSession session = factory.openSession(false);
		try
		{
			UudbPoolMapper mapper = session.getMapper(UudbPoolMapper.class);
			return mapper.findMappingsByValue(value, valueType);
		} finally 
		{
			factory.closeSession(session);
		}
	}

	@Override
	public List<MappingBean> listMappingsByKey(String keyType, String keyValue)
	{
		SqlSession session = factory.openSession(false);
		try
		{
			UudbPoolMapper mapper = session.getMapper(UudbPoolMapper.class);
			return mapper.findMappingsByKey(keyType, keyValue);
		} finally 
		{
			factory.closeSession(session);
		}
	}


	@Override
	public void removePool(String poolName)
	{
		SqlSession session = factory.openSession(true);
		try
		{
			UudbPoolMapper mapper = session.getMapper(UudbPoolMapper.class);
			
			MappingBean poolInfo = mapper.checkPoolExistence(poolName);
			if (poolInfo == null)
				throw new IllegalArgumentException("Pool " + poolName + " does not exist in DB.");
			int poolKey = poolInfo.getId();
			
			List<MappingBean> poolMappings = mapper.getMappings(poolKey, "any");
			if (poolMappings.size() > 0)
				throw new IllegalArgumentException("Pool " + poolName + " is not empty - it has " + 
						poolMappings.size() + " mappings assigned (both alive and frozen). " +
						"You have to remove them first.");
			try
			{
				mapper.removePool(poolName);
			} catch (Exception e)
			{
				throw new IllegalArgumentException("Can not remove the pool " + poolName + 
						", are  you sure that the pool is empty and disabled in configuration? " + 
						e.toString());
			}
			session.commit();
		} finally 
		{
			factory.closeSession(session);
		}
	}


	@Override
	public List<PoolInfoBean> listPools()
	{
		SqlSession session = factory.openSession(false);
		try
		{
			UudbPoolMapper mapper = session.getMapper(UudbPoolMapper.class);
			return mapper.listPools();
		} finally 
		{
			factory.closeSession(session);
		}
	}

	private boolean runCreateHandler(String identifier, String key, Pool pool)
	{
		String handler = null;
		if (pool.getType().equals(MappingType.uid))
			handler = pool.getConfiguration().getHandlerCreateSystemUid();
		else if (pool.getType().equals(MappingType.gid))
			handler = pool.getConfiguration().getHandlerCreateSystemGid();
		if (handler != null)
		{
			ProcessInvoker invoker = new ProcessInvoker(pool.getConfiguration().getHandlerInvocationTimeLimit());
			String[] args = new String[] {pool.getId(), identifier, key};
			return invoker.invokeWithChecking(handler, args);
		}
		return true;
	}
	
	private boolean runFreezingHandler(String handler, ProcessInvoker invoker, MappingBean bean, Pool pool)
	{
		if (handler != null)
		{
			long inactiveSec = (System.currentTimeMillis() - bean.getLastAccess().getTime())/1000;
			
			String[] args = new String[] {pool.getId(), pool.getType().toString(), 
					bean.getEntry(), inactiveSec+""};
			return invoker.invokeWithChecking(handler, args);
		}
		return true;
	}
	
	private boolean runRemovalHandler(String handler, ProcessInvoker invoker, MappingBean bean, Pool pool)
	{
		if (handler != null)
		{
			long oldSec = (System.currentTimeMillis() - bean.getFreezeTime().getTime())/1000;
			
			String[] args = new String[] {pool.getId(), pool.getType().toString(), 
					bean.getEntry(), oldSec+""};
			return invoker.invokeWithChecking(handler, args);
		}
		return true;
	}
	
	private void populatePool(Pool pool, UudbPoolMapper mapper) throws ConfigurationException
	{
		List<String> rawEntries = pool.getRawEntries();
		int allEntries = 0;
		for (String entry: rawEntries)
		{
			List<String> realEntries = extrapolateEntry(entry);
			allEntries += realEntries.size();
			for (String realE: realEntries)
				mapper.populatePool(pool.getDbKey(), realE);
		}
		pool.setRealEntries(allEntries);
	}
	
	public static List<String> extrapolateEntry(String template) throws ConfigurationException
	{
		List<String> realEntries = new ArrayList<String>();
		Matcher matcher = PATTERN.matcher(template);
		if (!matcher.find())
		{
			realEntries.add(template);
		} else
		{
			String expression = matcher.group();
			expression = expression.substring(1, expression.length()-1);
			String[] rangeS = expression.split("-");
			int min, max;
			min = Integer.parseInt(rangeS[0]);
			max = Integer.parseInt(rangeS[1]);
			if (min < 0 || max < 0)
				throw new ConfigurationException("Pool entry " + template + " is invalid: range " +
						"boundaries must be smaller then " + Integer.MAX_VALUE);
			if (min > max)
				throw new ConfigurationException("Pool entry " + template + " is invalid: it uses range " +
						"with low boundary larger then top boundary (" + min + " and " + max + ")");

			String prefix = template.substring(0, matcher.start());
			String suffix = template.substring(matcher.end());

			for (int i=min; i<=max; i++)
				realEntries.add(prefix + i + suffix);
		}
		return realEntries;
	}

}



