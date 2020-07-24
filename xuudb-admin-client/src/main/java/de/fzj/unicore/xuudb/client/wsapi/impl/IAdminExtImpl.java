package de.fzj.unicore.xuudb.client.wsapi.impl;

import org.apache.log4j.Logger;

import de.fzJuelich.unicore.xuudb.AddCertificateDocument;
import de.fzJuelich.unicore.xuudb.AddCertificateResponseDocument;
import de.fzJuelich.unicore.xuudb.DatabaseType;
import de.fzJuelich.unicore.xuudb.ImportDatabaseDocument;
import de.fzJuelich.unicore.xuudb.ImportDatabaseResponseDocument;
import de.fzJuelich.unicore.xuudb.ListDatabaseDocument;
import de.fzJuelich.unicore.xuudb.ListDatabaseResponseDocument;
import de.fzJuelich.unicore.xuudb.LoginDataType;
import de.fzJuelich.unicore.xuudb.RemoveCertificateDocument;
import de.fzJuelich.unicore.xuudb.RemoveCertificateResponseDocument;
import de.fzJuelich.unicore.xuudb.UpdateCertificateDocument;
import de.fzJuelich.unicore.xuudb.UpdateCertificateResponseDocument;
import de.fzJuelich.unicore.xuudb.UpdateDataType;
import de.fzj.unicore.xuudb.client.wsapi.IAdminExtInterface;
import de.fzj.unicore.xuudb.client.wsapi.XUUDBResponse;
import de.fzj.unicore.xuudb.interfaces.IAdmin;
import de.fzj.unicore.xuudb.Log;

public class IAdminExtImpl implements IAdminExtInterface {
	private static final Logger logger = Log.getLogger(
			Log.XUUDB_CLIENT, IAdminExtImpl.class);

	private IAdmin proxy;

	public IAdminExtImpl(IAdmin p) {
		proxy = p;
	}

	@Override
	public XUUDBResponse adddn(String gcId, String dn, String xlogin,
			String role, String projects) throws Exception {

		AddCertificateDocument outXML = AddCertificateDocument.Factory
				.newInstance();
		LoginDataType addUloginXML = LoginDataType.Factory.newInstance();
		addUloginXML.setGcID(gcId);
		addUloginXML.setToken(dn);
		addUloginXML.setXlogin(xlogin);
		addUloginXML.setRole(role);
		if (projects != null)
			addUloginXML.setProjects(projects);
		else
			addUloginXML.setProjects("");
		outXML.setAddCertificate(addUloginXML);

		logger.debug("Invoking service");
		AddCertificateResponseDocument inXML = null;
		try {
			inXML = proxy.addCertificate(outXML);
		} catch (Exception xfe) {
			String msg = "Error invoking service";
			logger.error(msg, xfe);
			throw new Exception(msg, xfe);
		}
		if (inXML.getAddCertificateResponse().indexOf("constraint") != -1) {
			String msg = "This dataset already exists. Constraint: GCID and DN must be unique";
			logger.info(msg);
			throw new Exception(msg);
		}

		return new XUUDBResponse(inXML.getAddCertificateResponse(), null, null);

	}

	public XUUDBResponse list(LoginDataType arg) throws Exception {

		ListDatabaseDocument outXML = ListDatabaseDocument.Factory
				.newInstance();
		try {
			outXML.setListDatabase(arg);
		} catch (Exception e) {
			String msg = "Cannot build query (check parameters?)";
			logger.error(msg, e);
			throw new Exception(msg, e);
		}

		logger.debug("Invoking service");
		ListDatabaseResponseDocument inXML = null;
		try {
			inXML = proxy.listDatabase(outXML);
		} catch (Exception xfe) {
			String msg = "Error invoking service";
			logger.error(msg, xfe);
			throw new Exception(msg, xfe);
		}

		XUUDBResponse resp = new XUUDBResponse("OK", inXML
				.getListDatabaseResponse().getXUUDBInfo(), inXML
				.getListDatabaseResponse().getDatabaseArray());

		return resp;
	}

	@Override
	public XUUDBResponse remove(LoginDataType data) throws Exception {

		RemoveCertificateDocument outXML = RemoveCertificateDocument.Factory
				.newInstance();
		outXML.setRemoveCertificate(data);
		logger.debug("Invoking service");
		RemoveCertificateResponseDocument inXML = null;
		try {
			inXML = proxy.removeCertificate(outXML);
		} catch (Exception xfe) {
			String msg = "Error invoking service";
			logger.error(msg, xfe);
			throw new Exception(msg, xfe);
		}

		XUUDBResponse resp = new XUUDBResponse(inXML
				.getRemoveCertificateResponse(), null, null);
		return resp;

	}

	@Override
	public XUUDBResponse add(String gcId, String certPem, String xlogin,
			String role, String projects) throws Exception {
		AddCertificateDocument outXML = AddCertificateDocument.Factory
				.newInstance();
		LoginDataType addUloginXML = LoginDataType.Factory.newInstance();
		addUloginXML.setGcID(gcId);
		addUloginXML.setToken(certPem);
		addUloginXML.setRole(role);
		addUloginXML.setXlogin(xlogin);
		if (projects != null)
			addUloginXML.setProjects(projects);
		else
			addUloginXML.setProjects("");
		outXML.setAddCertificate(addUloginXML);
		logger.debug("Invoking service");
		AddCertificateResponseDocument inXML = null;
		try {
			inXML = proxy.addCertificate(outXML);
		} catch (Exception xfe) {
			String msg = "Error invoking service";
			logger.error(msg, xfe);
			throw new Exception(msg, xfe);
		}

		if (inXML.getAddCertificateResponse().indexOf(
				"Unique constraint violation") != -1) {
			String msg = "This dataset already exists. Constraint: GCID and Certificate must be unique";
			logger.info(msg);
			throw new Exception(msg);
		}

		XUUDBResponse resp = new XUUDBResponse(inXML
				.getAddCertificateResponse(), null, null);
		return resp;
	}

	@Override
	public XUUDBResponse update(String gcId, String token, LoginDataType data)
			throws Exception {

		UpdateCertificateDocument outXMLDoc = UpdateCertificateDocument.Factory
				.newInstance();
		UpdateDataType outXML = outXMLDoc.addNewUpdateCertificate();
		outXML.setGcID(gcId);
		outXML.setToken(token);
		outXML.setData(data);

		logger.debug("Invoking service");
		UpdateCertificateResponseDocument inXML = null;
		try {
			inXML = proxy.updateCertificate(outXMLDoc);
		} catch (Exception xfe) {
			String msg = "Error invoking service";
			logger.error(msg, xfe);
			throw new Exception(msg, xfe);

		}

		XUUDBResponse resp = new XUUDBResponse(inXML
				.getUpdateCertificateResponse(), null, null);
		return resp;

	}

	@Override
	public XUUDBResponse exportCsv() throws Exception {

		ListDatabaseDocument outXML = ListDatabaseDocument.Factory
				.newInstance();
		LoginDataType data0 = LoginDataType.Factory.newInstance();
		outXML.setListDatabase(data0);
		ListDatabaseResponseDocument inXML = null;

		logger.debug("Invoking service");
		try {
			inXML = proxy.listDatabase(outXML);
		} catch (Exception xfe) {
			String msg = "Error invoking service";
			logger.error(msg, xfe);
			throw new Exception(msg, xfe);
		}

		XUUDBResponse resp = new XUUDBResponse("OK", inXML
				.getListDatabaseResponse().getXUUDBInfo(), inXML
				.getListDatabaseResponse().getDatabaseArray());
		return resp;
	}

	@Override
	public XUUDBResponse importCsv(LoginDataType[] data, boolean clear)
			throws Exception {

		ImportDatabaseDocument ldd = ImportDatabaseDocument.Factory
				.newInstance();
		DatabaseType dt = DatabaseType.Factory.newInstance();
		if (clear)
			dt.setClean(clear);

		dt.setDatabaseArray(data);
		ldd.setImportDatabase(dt);

		ImportDatabaseDocument outXML = ldd;

		logger.debug("Invoking service");
		ImportDatabaseResponseDocument inXML = null;
		try {
			inXML = proxy.importDatabase(outXML);
		} catch (Exception xfe) {
			String msg = "Error invoking service";
			logger.error(msg, xfe);
			throw new Exception(msg, xfe);
		}

		XUUDBResponse resp = new XUUDBResponse(inXML
				.getImportDatabaseResponse(), null, null);
		return resp;

	}

}
