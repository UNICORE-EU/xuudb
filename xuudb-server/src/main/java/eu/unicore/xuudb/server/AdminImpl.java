package eu.unicore.xuudb.server;

import eu.unicore.xuudb.xbeans.AddCertificateDocument;
import eu.unicore.xuudb.xbeans.AddCertificateResponseDocument;
import eu.unicore.xuudb.xbeans.DatabaseType;
import eu.unicore.xuudb.xbeans.ImportDatabaseDocument;
import eu.unicore.xuudb.xbeans.ImportDatabaseResponseDocument;
import eu.unicore.xuudb.xbeans.ListDatabaseDocument;
import eu.unicore.xuudb.xbeans.ListDatabaseResponseDocument;
import eu.unicore.xuudb.xbeans.LoginDataType;
import eu.unicore.xuudb.xbeans.RemoveCertificateDocument;
import eu.unicore.xuudb.xbeans.RemoveCertificateResponseDocument;
import eu.unicore.xuudb.xbeans.UpdateCertificateDocument;
import eu.unicore.xuudb.xbeans.UpdateCertificateResponseDocument;
import eu.unicore.xuudb.interfaces.IAdmin;
import eu.unicore.xuudb.server.db.IClassicStorage;

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
