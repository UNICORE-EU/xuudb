package de.fzj.unicore.xuudb.client.wsapi.impl;

import java.util.Calendar;
import java.util.Date;

import org.apache.logging.log4j.Logger;

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
import de.fzJuelich.unicore.xuudb.MappingDataType;
import de.fzJuelich.unicore.xuudb.PoolInfoType;
import de.fzJuelich.unicore.xuudb.RemoveMappingRequestDocument;
import de.fzJuelich.unicore.xuudb.RemovePoolRequestDocument;
import de.fzJuelich.unicore.xuudb.RemovePoolRequestType;
import de.fzj.unicore.xuudb.client.wsapi.IDAPAdminExtInterface;
import de.fzj.unicore.xuudb.interfaces.IDAPAdmin;
import de.fzj.unicore.xuudb.Log;

public class IDAPAdminExtImpl implements IDAPAdminExtInterface {
	private static final Logger logger = Log.getLogger(
			Log.XUUDB_CLIENT, IDAPAdminExtImpl.class);
	private IDAPAdmin proxy;

	public IDAPAdminExtImpl(IDAPAdmin dapAdmin) {
		proxy = dapAdmin;
	}

	@Override
	public MappingDataType[] findReverse(String type, String value)
			throws Exception {
		FindReverseMappingRequestDocument outXml = FindReverseMappingRequestDocument.Factory
				.newInstance();
		FindMappingRequestType findReverseMappingRequest = FindMappingRequestType.Factory
				.newInstance();
		findReverseMappingRequest.setType(type);
		findReverseMappingRequest.setValue(value);

		outXml.setFindReverseMappingRequest(findReverseMappingRequest);

		logger.debug("Invoking service");
		FindReverseMappingResponseDocument inXML = null;
		try {
			inXML = proxy.findReverseMapping(outXml);
		} catch (Exception xfe) {
			String msg = "Error invoking service";
			logger.error(msg, xfe);
			throw new Exception(msg, xfe);
		}

		MappingDataType[] resp = inXML.getFindReverseMappingResponse()
				.getMappingArray();
		return resp;

	}

	@Override
	public MappingDataType[] find(String type, String value) throws Exception {
		FindMappingRequestDocument outXml = FindMappingRequestDocument.Factory
				.newInstance();

		FindMappingRequestType findMappingRequest = FindMappingRequestType.Factory
				.newInstance();
		findMappingRequest.setType(type);
		findMappingRequest.setValue(value);

		outXml.setFindMappingRequest(findMappingRequest);

		logger.debug("Invoking service");
		FindMappingResponseDocument inXML = null;
		try {
			inXML = proxy.findMapping(outXml);
		} catch (Exception xfe) {
			String msg = "Error invoking service";
			logger.error(msg, xfe);
			throw new Exception(msg, xfe);
		}

		MappingDataType[] resp = inXML.getFindMappingResponse()
				.getMappingArray();
		return resp;

	}

	@Override
	public void freeze(String id, String pool) throws Exception {
		FreezeMappingRequestDocument outXml = FreezeMappingRequestDocument.Factory
				.newInstance();

		FreezeRemoveMappingRequestType freezeMappingRequest = FreezeRemoveMappingRequestType.Factory
				.newInstance();
		freezeMappingRequest.setId(id);
		freezeMappingRequest.setPoolId(pool);
		outXml.setFreezeMappingRequest(freezeMappingRequest);

		logger.debug("Invoking service");

		try {
			proxy.freezeMapping(outXml);
		} catch (Exception xfe) {
			String msg = "Error invoking service";
			logger.error(msg, xfe);
			throw new Exception(msg, xfe);
		}

	}

	@Override
	public void freeze(Date date, String pool) throws Exception {
		FreezeMappingRequestDocument outXml = FreezeMappingRequestDocument.Factory
				.newInstance();

		FreezeRemoveMappingRequestType freezeMappingRequest = FreezeRemoveMappingRequestType.Factory
				.newInstance();
		Calendar cl = Calendar.getInstance();
		cl.setTime(date);
		freezeMappingRequest.setDate(cl);
		freezeMappingRequest.setPoolId(pool);
		outXml.setFreezeMappingRequest(freezeMappingRequest);

		logger.debug("Invoking service");

		try {
			proxy.freezeMapping(outXml);
		} catch (Exception xfe) {
			String msg = "Error invoking service";
			logger.error(msg, xfe);
			throw new Exception(msg, xfe);
		}

	}

	@Override
	public MappingDataType[] list(String type, String pool) throws Exception {

		ListMappingRequestDocument outXml = ListMappingRequestDocument.Factory
				.newInstance();
		ListMappingRequestType req = ListMappingRequestType.Factory
				.newInstance();
		req.setMappingType(type);
		if (pool != null)
			req.setPoolId(pool);
		// req.setIdArray(ids);

		outXml.setListMappingRequest(req);

		logger.debug("Invoking service");
		ListMappingResponseDocument inXML = null;
		try {
			inXML = proxy.listMappings(outXml);
		} catch (Exception xfe) {
			String msg = "Error invoking service";
			logger.error(msg, xfe);
			throw new Exception(msg, xfe);
		}

		MappingDataType[] resp = inXML.getListMappingResponse()
				.getMappingArray();

		return resp;
	}

	@Override
	public void remove(String id, String pool) throws Exception {
		RemoveMappingRequestDocument outXml = RemoveMappingRequestDocument.Factory
				.newInstance();
		FreezeRemoveMappingRequestType removeMappingRequest = FreezeRemoveMappingRequestType.Factory
				.newInstance();
		removeMappingRequest.setId(id);
		removeMappingRequest.setPoolId(pool);

		outXml.setRemoveMappingRequest(removeMappingRequest);

		logger.debug("Invoking service");

		try {
			proxy.removeFrozenMapping(outXml);
		} catch (Exception xfe) {
			String msg = "Error invoking service";
			logger.error(msg, xfe);
			throw new Exception(msg, xfe);
		}

	}

	@Override
	public void remove(Date date, String pool) throws Exception {

		RemoveMappingRequestDocument outXml = RemoveMappingRequestDocument.Factory
				.newInstance();
		FreezeRemoveMappingRequestType removeMappingRequest = FreezeRemoveMappingRequestType.Factory
				.newInstance();
		Calendar cl = Calendar.getInstance();
		cl.setTime(date);
		removeMappingRequest.setDate(cl);
		removeMappingRequest.setPoolId(pool);

		outXml.setRemoveMappingRequest(removeMappingRequest);

		logger.debug("Invoking service");

		try {
			proxy.removeFrozenMapping(outXml);
		} catch (Exception xfe) {
			String msg = "Error invoking service";
			logger.error(msg, xfe);
			throw new Exception(msg, xfe);
		}

	}

	@Override
	public PoolInfoType[] listPools() throws Exception {

		logger.debug("Invoking service");

		ListPoolsResponseDocument inXML = null;
		try {
			inXML = proxy.listPools();
		} catch (Exception xfe) {
			String msg = "Error invoking service";
			logger.error(msg, xfe);
			throw new Exception(msg, xfe);
		}

		PoolInfoType[] resp = inXML.getListPoolsResponse().getPoolArray();
		return resp;
	}

	@Override
	public void removePool(String poolid) throws Exception {
		RemovePoolRequestDocument outXml = RemovePoolRequestDocument.Factory
				.newInstance();
		RemovePoolRequestType removePoolRequest = RemovePoolRequestType.Factory
				.newInstance();
		removePoolRequest.setPoolId(poolid);
		outXml.setRemovePoolRequest(removePoolRequest);

		logger.debug("Invoking service");

		try {
			proxy.removePool(outXml);
		} catch (Exception xfe) {
			String msg = "Error invoking service";
			logger.error(msg, xfe);
			throw new Exception(msg, xfe);
		}

	}
}
