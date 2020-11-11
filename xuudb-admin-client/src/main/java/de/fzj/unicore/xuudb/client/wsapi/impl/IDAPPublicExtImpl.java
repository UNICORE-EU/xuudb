package de.fzj.unicore.xuudb.client.wsapi.impl;

import org.apache.logging.log4j.Logger;

import de.fzJuelich.unicore.xuudb.GetAttributesRequestDocument;
import de.fzJuelich.unicore.xuudb.GetAttributesRequestType;
import de.fzJuelich.unicore.xuudb.GetAttributesResponseDocument;
import de.fzJuelich.unicore.xuudb.GetAttributesResponseType;
import de.fzJuelich.unicore.xuudb.SimplifiedAttributeType;
import de.fzJuelich.unicore.xuudb.SimulateGetAttributesRequestDocument;
import de.fzJuelich.unicore.xuudb.SimulateGetAttributesResponseDocument;
import de.fzj.unicore.xuudb.client.wsapi.IDAPPublicExtInterface;
import de.fzj.unicore.xuudb.interfaces.IDynamicAttributesPublic;
import de.fzj.unicore.xuudb.Log;

public class IDAPPublicExtImpl implements IDAPPublicExtInterface {
	private static final Logger logger = Log.getLogger(
			Log.XUUDB_CLIENT, IDAPPublicExtImpl.class);
	IDynamicAttributesPublic proxy;

	public IDAPPublicExtImpl(IDynamicAttributesPublic pub) {
		proxy = pub;
	}

	private GetAttributesRequestType prepareReq(String userDN,
			String issuerDN, String role, String vo, String xlogin, String gid,
			String[] supplementaryGids,
			SimplifiedAttributeType[] extraAttributes) {
		GetAttributesRequestType req = GetAttributesRequestType.Factory.newInstance();

		req.setUserDN(userDN);
		req.setRole(role);
		if (issuerDN != null)
			req.setIssuerDN(issuerDN);
		if (vo != null)
			req.setVo(vo);
		if (xlogin != null)
			req.setXlogin(xlogin);
		if (gid != null)
			req.setGid(gid);
		if (supplementaryGids != null && supplementaryGids.length > 0)
			req.setSupplementaryGidsArray(supplementaryGids);
		if (extraAttributes != null && extraAttributes.length > 0)
			req.setExtraAttributesArray(extraAttributes);

		return req;
	}
	
	@Override
	public GetAttributesResponseType simulateGetAttributes(String userDN,
			String issuerDN, String role, String vo, String xlogin, String gid,
			String[] supplementaryGids,
			SimplifiedAttributeType[] extraAttributes) throws Exception {

		SimulateGetAttributesRequestDocument outXml = SimulateGetAttributesRequestDocument.Factory
				.newInstance();
		GetAttributesRequestType req = prepareReq(userDN, issuerDN, role, vo, xlogin, 
				gid, supplementaryGids, extraAttributes);
		outXml.setSimulateGetAttributesRequest(req);

		logger.debug("Invoking service");
		SimulateGetAttributesResponseDocument inXML = null;
		try {
			inXML = proxy.simulateGetAttributes(outXml);
		} catch (Exception xfe) {
			String msg = "Error invoking service";
			logger.error(msg, xfe);
			throw new Exception(msg, xfe);
		}
		return inXML.getSimulateGetAttributesResponse();
	}

	@Override
	public GetAttributesResponseType getAttributes(String userDN,
			String issuerDN, String role, String vo, String xlogin, String gid,
			String[] supplementaryGids,
			SimplifiedAttributeType[] extraAttributes) throws Exception {

		GetAttributesRequestDocument outXml = GetAttributesRequestDocument.Factory
				.newInstance();
		GetAttributesRequestType req = prepareReq(userDN, issuerDN, role, vo, xlogin, 
				gid, supplementaryGids, extraAttributes);

		outXml.setGetAttributesRequest(req);

		logger.debug("Invoking service");
		GetAttributesResponseDocument inXML = null;
		try {
			inXML = proxy.getAttributes(outXml);
		} catch (Exception xfe) {
			String msg = "Error invoking service";
			logger.error(msg, xfe);
			throw new Exception(msg, xfe);
		}
		return inXML.getGetAttributesResponse();
	}
}
