package de.fzj.unicore.xuudb.server;

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
import de.fzj.unicore.xuudb.interfaces.IAdmin;
import de.fzj.unicore.xuudb.server.db.IClassicStorage;

public class AdminImpl implements IAdmin {

	private final IClassicStorage xuudb;
	private final String version; 

	public AdminImpl(ServerConfiguration co, IClassicStorage backend) throws Exception {
		this.xuudb = backend;
		this.version = getClass().getPackage().getImplementationVersion()!=null?
				getClass().getPackage().getImplementationVersion() : "DEVELOPMENT";
	}

	public AddCertificateResponseDocument addCertificate(AddCertificateDocument xml) {
		AddCertificateResponseDocument ret = AddCertificateResponseDocument.Factory.newInstance();
		String ans = xuudb.add(xml.getAddCertificate()); 
		ret.setAddCertificateResponse(ans);
		return ret;
	}

	public ListDatabaseResponseDocument listDatabase(ListDatabaseDocument xml) {
		ListDatabaseResponseDocument ret = ListDatabaseResponseDocument.Factory.newInstance();
		LoginDataType[] ans = xuudb.listDB(xml.getListDatabase());
		DatabaseType data = DatabaseType.Factory.newInstance();
		data.setDatabaseArray(ans);
		data.setXUUDBInfo(getInfo());
		data.setIsDNMode(true);
		ret.setListDatabaseResponse(data);
		return ret;
	}

	public RemoveCertificateResponseDocument removeCertificate(RemoveCertificateDocument xml) {
		RemoveCertificateResponseDocument ret = RemoveCertificateResponseDocument.Factory.newInstance();
		String ans = xuudb.remove(xml.getRemoveCertificate());
		ret.setRemoveCertificateResponse(ans);
		return ret;
	}

	public UpdateCertificateResponseDocument updateCertificate(UpdateCertificateDocument xml) {
		String gcid=xml.getUpdateCertificate().getGcID();
		String token=xml.getUpdateCertificate().getToken();
		UpdateCertificateResponseDocument ret = UpdateCertificateResponseDocument.Factory.newInstance();
		String ans = xuudb.update(gcid, token, xml.getUpdateCertificate().getData());
		ret.setUpdateCertificateResponse(ans);
		return ret;
	}

	public ImportDatabaseResponseDocument importDatabase(ImportDatabaseDocument xml) {
		String erg = xuudb.import_csv(xml);
		ImportDatabaseResponseDocument ret = ImportDatabaseResponseDocument.Factory.newInstance();
		ret.setImportDatabaseResponse(erg);
		return ret;
	}

	protected String getInfo(){
		return "XUUDB Server version "+version;
	}
}
