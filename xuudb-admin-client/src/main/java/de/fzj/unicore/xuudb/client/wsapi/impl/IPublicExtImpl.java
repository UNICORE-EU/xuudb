package de.fzj.unicore.xuudb.client.wsapi.impl;

import org.apache.log4j.Logger;

import de.fzJuelich.unicore.xuudb.CheckCertificateDocument;
import de.fzJuelich.unicore.xuudb.CheckCertificateResponseDocument;
import de.fzJuelich.unicore.xuudb.CheckDNDataType;
import de.fzJuelich.unicore.xuudb.CheckDNDocument;
import de.fzJuelich.unicore.xuudb.CheckDNResponseDocument;
import de.fzJuelich.unicore.xuudb.CheckDataType;
import de.fzJuelich.unicore.xuudb.LoginDataType;
import de.fzj.unicore.xuudb.client.wsapi.IPublicExtInterface;
import de.fzj.unicore.xuudb.client.wsapi.XUUDBResponse;
import de.fzj.unicore.xuudb.interfaces.IPublic;
import de.fzj.unicore.xuudb.Log;

public class IPublicExtImpl implements IPublicExtInterface {
	private static final Logger logger = Log.getLogger(
			Log.XUUDB_CLIENT, IPublicExtImpl.class);
	private IPublic proxy;

	public IPublicExtImpl(IPublic p) {
		proxy = p;
	}

	public XUUDBResponse checkDN(String gcId, String dn) throws Exception {

		CheckDNDocument outXML = CheckDNDocument.Factory.newInstance();
		CheckDNDataType checkDNDataTypeXml = CheckDNDataType.Factory
				.newInstance();
		checkDNDataTypeXml.setGcID(gcId);
		checkDNDataTypeXml.setDistinguishedName(dn);
		outXML.setCheckDN(checkDNDataTypeXml);
		// calling ws
		logger.debug("Invoking service");

		CheckDNResponseDocument inXML = null;
		try {
			inXML = proxy.checkDN(outXML);
		} catch (Exception xfe) {
			String msg = "Error invoking service";
			logger.error(msg, xfe);
			throw new Exception(msg, xfe);
		}

		LoginDataType data = LoginDataType.Factory.newInstance();

		data.setGcID(inXML.getCheckDNResponse().getGcID());
		data.setXlogin(inXML.getCheckDNResponse().getXlogin());
		data.setRole(inXML.getCheckDNResponse().getRole());
		data.setProjects(inXML.getCheckDNResponse().getProjects());
		data.setToken(inXML.getCheckDNResponse().getToken());

		LoginDataType[] ldata = new LoginDataType[1];
		ldata[0] = data;
		XUUDBResponse resp = new XUUDBResponse("OK", null, ldata);
		return resp;

	}

	public XUUDBResponse checkCert(String gcId, String certPem)
			throws Exception {

		CheckCertificateDocument outXML = CheckCertificateDocument.Factory
				.newInstance();
		CheckDataType checkDataTypeXml = CheckDataType.Factory.newInstance();
		checkDataTypeXml.setGcID(gcId);
		checkDataTypeXml.setCertInPEM(certPem);
		outXML.setCheckCertificate(checkDataTypeXml);
	
		logger.debug("Invoking service");
		CheckCertificateResponseDocument inXML = null;
		try {
			inXML = proxy.checkCertificate(outXML);
		} catch (Exception xfe) {
			String msg = "Error invoking service";
			logger.error(msg, xfe);
			throw new Exception(msg, xfe);
		}

		LoginDataType data = LoginDataType.Factory.newInstance();

		data.setGcID(inXML.getCheckCertificateResponse().getGcID());
		data.setXlogin(inXML.getCheckCertificateResponse().getXlogin());
		data.setRole(inXML.getCheckCertificateResponse().getRole());
		data.setProjects(inXML.getCheckCertificateResponse().getProjects());
		data.setToken(inXML.getCheckCertificateResponse().getToken());

		LoginDataType[] ldata = new LoginDataType[1];
		ldata[0] = data;
		XUUDBResponse resp = new XUUDBResponse("OK", null, ldata);
		return resp;

	}
}
