/*
 * Copyright (c) 2012 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package de.fzj.unicore.xuudb.server;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;

import de.fzJuelich.unicore.xuudb.FindMappingRequestDocument;
import de.fzJuelich.unicore.xuudb.FindMappingRequestType;
import de.fzJuelich.unicore.xuudb.FindMappingResponseDocument;
import de.fzJuelich.unicore.xuudb.FindReverseMappingRequestDocument;
import de.fzJuelich.unicore.xuudb.FindReverseMappingResponseDocument;
import de.fzJuelich.unicore.xuudb.FreezeMappingRequestDocument;
import de.fzJuelich.unicore.xuudb.FreezeRemoveMappingRequestType;
import de.fzJuelich.unicore.xuudb.ListMappingRequestDocument;
import de.fzJuelich.unicore.xuudb.ListMappingRequestType;
import de.fzJuelich.unicore.xuudb.ListMappingResponseDocument;
import de.fzJuelich.unicore.xuudb.ListPoolsResponseDocument;
import de.fzJuelich.unicore.xuudb.ListPoolsResponseType;
import de.fzJuelich.unicore.xuudb.MappingDataType;
import de.fzJuelich.unicore.xuudb.MappingListDataType;
import de.fzJuelich.unicore.xuudb.PoolInfoType;
import de.fzJuelich.unicore.xuudb.RemoveMappingRequestDocument;
import de.fzJuelich.unicore.xuudb.RemovePoolRequestDocument;
import de.fzj.unicore.xuudb.Log;
import de.fzj.unicore.xuudb.interfaces.IDAPAdmin;
import de.fzj.unicore.xuudb.server.db.IPoolStorage;
import de.fzj.unicore.xuudb.server.db.MappingBean;
import de.fzj.unicore.xuudb.server.db.PoolInfoBean;
import de.fzj.unicore.xuudb.server.dynamic.DAPConfiguration;
import de.fzj.unicore.xuudb.server.dynamic.MappingStatus;
import de.fzj.unicore.xuudb.server.dynamic.Pool;

/**
 * Dynamic attributes administration endpoint
 * @author K. Benedyczak
 */
public class DAPAdminImpl implements IDAPAdmin
{
	private static final Logger log = Log.getLogger(Log.XUUDB_SERVER, DAPPublicImpl.class);
	private IPoolStorage backend;
	private DAPConfiguration configuration;
	
	public DAPAdminImpl(IPoolStorage backend, DAPConfiguration configuration) 
			throws IOException, XmlException, ParseException
	{
		this.backend = backend;
		this.configuration = configuration;
	}

	@Override
	public ListMappingResponseDocument listMappings(ListMappingRequestDocument xml)
	{
		ListMappingRequestType request = xml.getListMappingRequest();
		
		String poolName = request.getPoolId();
		String mappingType = request.getMappingType();
		log.debug("listMappings invoked for pool: " + poolName + " type: " + mappingType);
		
		MappingStatus status;
		try
		{
			status = MappingStatus.valueOf(mappingType);
		} catch (Exception e)
		{
			throw new IllegalArgumentException("Mapping status '" + mappingType + "' is invalid, " +
					"can have only those values: " + Arrays.toString(MappingStatus.values()));
		}
		Integer poolId = getPoolId(poolName);

		List<MappingBean> mappings = backend.listMappings(poolId, status);
		ListMappingResponseDocument retDoc = ListMappingResponseDocument.Factory.newInstance();
		convertToListResponse(mappings, retDoc.addNewListMappingResponse());
		return retDoc;
	}

	@Override
	public FindMappingResponseDocument findMapping(FindMappingRequestDocument xml)
	{
		FindMappingRequestType req = xml.getFindMappingRequest();
		log.debug("findMapping invoked for type: " + req.getType() + " value: " + req.getValue());
		List<MappingBean> mappings = backend.listMappingsByValue(req.getType(), req.getValue());
		FindMappingResponseDocument retDoc = FindMappingResponseDocument.Factory.newInstance();
		convertToListResponse(mappings, retDoc.addNewFindMappingResponse());
		return retDoc;
	}

	@Override
	public FindReverseMappingResponseDocument findReverseMapping(
			FindReverseMappingRequestDocument xml)
	{
		FindMappingRequestType req = xml.getFindReverseMappingRequest();
		log.debug("findReverseMapping invoked for type: " + req.getType() + 
				" value: " + req.getValue());
		List<MappingBean> mappings = backend.listMappingsByKey(req.getType(), req.getValue());
		FindReverseMappingResponseDocument retDoc = FindReverseMappingResponseDocument.Factory.newInstance();
		convertToListResponse(mappings, retDoc.addNewFindReverseMappingResponse());
		return retDoc;
	}
	
	@Override
	public void freezeMapping(FreezeMappingRequestDocument xml)
	{
		FreezeRemoveMappingRequestType req = xml.getFreezeMappingRequest();
		Calendar older = req.getDate();
		String id = req.getId();
		String poolName = req.getPoolId();
		log.debug("freezeMapping invoked for id: " + id + " pool: " + poolName + " older: " + older);

		if (older != null && id != null)
			throw new IllegalArgumentException("Ambiguous arguments: either time or mapping " +
					"id can be specified");
		Pool pool = getPool(poolName);
		if (pool == null)
			throw new IllegalArgumentException("Pool must be specified");

		if (older != null)
		{
			backend.freezeInactive(pool, new Date(older.getTimeInMillis()));
		} else
		{
			backend.freezeSpecified(pool, id);
		}
	}

	@Override
	public void removeFrozenMapping(RemoveMappingRequestDocument xml)
	{
		FreezeRemoveMappingRequestType req = xml.getRemoveMappingRequest();
		Calendar older = req.getDate();
		String id = req.getId();
		String poolName = req.getPoolId();
		log.debug("removeFrozenMapping invoked for id: " + id + " pool: " + poolName + " older: " + older);

		if (older != null && id != null)
			throw new IllegalArgumentException("Ambiguous arguments: either time or mapping " +
					"id can be specified");
		Pool pool = getPool(poolName);
		if (pool == null)
			throw new IllegalArgumentException("Pool must be specified");

		if (older != null)
		{
			backend.deleteOld(pool, new Date(older.getTimeInMillis()));
		} else
		{
			backend.deleteSpecified(pool, id);
		}
	}

	@Override
	public void removePool(RemovePoolRequestDocument xml)
	{
		String poolName = xml.getRemovePoolRequest().getPoolId();
		log.debug("removePool invoked for pool: " + poolName);
		if (getPool(poolName) != null)
			throw new IllegalArgumentException("The pool is not disabled in configuraiton." +
					" It must be removed from configuration first.");
		backend.removePool(poolName);
	}

	@Override
	public ListPoolsResponseDocument listPools()
	{
		ListPoolsResponseDocument respDoc = ListPoolsResponseDocument.Factory.newInstance();
		ListPoolsResponseType resp = respDoc.addNewListPoolsResponse();
		log.debug("listPools invoked");

		List<PoolInfoBean> pools = backend.listPools();
		for (PoolInfoBean bean: pools)
		{
			PoolInfoType xml = resp.addNewPool();
			xml.setActiveMappings(bean.getAliveMappings());
			xml.setFreeSlots(bean.getFreeSlots());
			xml.setFrozenMappings(bean.getFrozenMappings());
			xml.setPoolId(bean.getPoolName());
			xml.setPoolKeyType(bean.getPoolKeyType());
			xml.setPoolType(bean.getPoolType());
		}
		return respDoc;
	}

	private Pool getPool(String poolName)
	{
		if (poolName != null)
		{
			Pool pool = configuration.getPools().get(poolName);
			if (pool == null)
				throw new IllegalArgumentException("Pool " + poolName + " is not known");
			return pool;
		}
		return null;
	}

	private Integer getPoolId(String poolName)
	{
		Pool p = getPool(poolName);
		if (p != null)
			return p.getDbKey();
		return null;
	}
	
	private void convertToListResponse(List<MappingBean> mappings, MappingListDataType ret)
	{
		for (MappingBean mapping: mappings)
		{
			MappingDataType xmlMapping = ret.addNewMapping();
			xmlMapping.setId(mapping.getId()+"");
			xmlMapping.setKey(mapping.getMappingKey());
			xmlMapping.setValue(mapping.getEntry());
			Calendar c = Calendar.getInstance();
			c.setTime(mapping.getLastAccess());
			xmlMapping.setLastAccess(c);
			if (mapping.getFreezeTime() != null)
			{
				Calendar c2 = Calendar.getInstance();
				c2.setTime(mapping.getFreezeTime());
				xmlMapping.setFreezeTime(c2);
			}
			xmlMapping.setPoolName(mapping.getPoolName());
			xmlMapping.setKeyType(mapping.getMappingKeyType());
		}
	}
	
}
