package eu.unicore.xuudb.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.security.cert.X509Certificate;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import de.fzJuelich.unicore.xuudb.AddCertificateDocument;
import de.fzJuelich.unicore.xuudb.AddCertificateResponseDocument;
import de.fzJuelich.unicore.xuudb.CheckCertificateDocument;
import de.fzJuelich.unicore.xuudb.CheckCertificateResponseDocument;
import de.fzJuelich.unicore.xuudb.CheckDNDataType;
import de.fzJuelich.unicore.xuudb.CheckDNDocument;
import de.fzJuelich.unicore.xuudb.CheckDNResponseDocument;
import de.fzJuelich.unicore.xuudb.CheckDataType;
import de.fzJuelich.unicore.xuudb.DatabaseType;
import de.fzJuelich.unicore.xuudb.ImportDatabaseDocument;
import de.fzJuelich.unicore.xuudb.ListDatabaseDocument;
import de.fzJuelich.unicore.xuudb.ListDatabaseResponseDocument;
import de.fzJuelich.unicore.xuudb.LoginDataType;
import de.fzJuelich.unicore.xuudb.RemoveCertificateDocument;
import de.fzJuelich.unicore.xuudb.UpdateCertificateDocument;
import de.fzJuelich.unicore.xuudb.UpdateDataType;
import eu.unicore.xuudb.X509Utils;
import eu.unicore.xuudb.interfaces.IAdmin;
import eu.unicore.xuudb.interfaces.IPublic;

public class TestAdminAndPublic {

	@Test
	public void testAdminActions() {
		HttpsServer server = null;
		try {
			File dir = new File("target/data");
			FileUtils.deleteDirectory(dir);
			Properties p = new Properties();
			p.load(new FileInputStream(
					"src/test/resources/xuudb_server.conf"));
			server = new HttpsServer(p);
			server.start();

			
			IAdmin admin=server.getAdminImpl();
			IPublic pub=server.getPublicImpl();
			
			
			X509Certificate x509 = X509Utils.loadCertificate("src/test/resources/demo-user.pem");
			String certPem = X509Utils.getPEMStringFromX509(x509);
			
			AddCertificateDocument addCertDoc=AddCertificateDocument.Factory.newInstance();
			LoginDataType addCert = addCertDoc.addNewAddCertificate();
			addCert.setGcID("test");
			addCert.setProjects("testProject");
			addCert.setRole("xrole");
			addCert.setToken(certPem);
			addCert.setXlogin("xtest");
			AddCertificateResponseDocument addResp = admin.addCertificate(addCertDoc);
			assertEquals("OK.", addResp.getAddCertificateResponse());
			
			ListDatabaseDocument listDoc=ListDatabaseDocument.Factory.newInstance();
			LoginDataType listDatabase = listDoc.addNewListDatabase();
			listDatabase.setGcID("test");
			ListDatabaseResponseDocument listDatabaseResp = admin.listDatabase(listDoc);
			assertEquals(1,listDatabaseResp.getListDatabaseResponse().getDatabaseArray().length);
			
			
			UpdateCertificateDocument updateCertDoc=UpdateCertificateDocument.Factory.newInstance();
			UpdateDataType updateCert = updateCertDoc.addNewUpdateCertificate();
			updateCert.setGcID("test");
			LoginDataType data=LoginDataType.Factory.newInstance();
			data.setProjects("testProject");
			updateCert.setToken(certPem);
			data.setXlogin("updatetest");	
			updateCert.setData(data);
			admin.updateCertificate(updateCertDoc);
			
			
			listDoc=ListDatabaseDocument.Factory.newInstance();
			listDatabase = listDoc.addNewListDatabase();
			listDatabase.setGcID("test");
			listDatabaseResp = admin.listDatabase(listDoc);
			assertEquals(1,listDatabaseResp.getListDatabaseResponse().getDatabaseArray().length);
			assertEquals("updatetest",listDatabaseResp.getListDatabaseResponse().getDatabaseArray()[0].getXlogin());
			
			
			ImportDatabaseDocument importDoc=ImportDatabaseDocument.Factory.newInstance();
			DatabaseType importDatabase = importDoc.addNewImportDatabase();
			LoginDataType login = importDatabase.addNewDatabase();
			login.setGcID("test1");
			login.setRole("role");
			login.setToken(certPem);
			admin.importDatabase(importDoc);
			
			listDoc=ListDatabaseDocument.Factory.newInstance();
			listDatabase = listDoc.addNewListDatabase();
			listDatabase.setGcID("test1");
			listDatabaseResp = admin.listDatabase(listDoc);
			assertEquals(1,listDatabaseResp.getListDatabaseResponse().getDatabaseArray().length);
			
			
			addCertDoc=AddCertificateDocument.Factory.newInstance();
			addCert = addCertDoc.addNewAddCertificate();
			addCert.setGcID("test");
			addCert.setProjects("testProject");
			addCert.setToken("CN=test");
			addCert.setXlogin("xtest");
			addCert.setRole("xrole");
			addResp = admin.addCertificate(addCertDoc);
			assertEquals("OK.", addResp.getAddCertificateResponse());
			
			listDoc=ListDatabaseDocument.Factory.newInstance();
			listDatabase = listDoc.addNewListDatabase();
			listDatabase.setGcID("test");
			listDatabaseResp = admin.listDatabase(listDoc);
			assertEquals(2,listDatabaseResp.getListDatabaseResponse().getDatabaseArray().length);
			
			
			CheckCertificateDocument checkCert=CheckCertificateDocument.Factory.newInstance();
			CheckDataType checkCertificate = checkCert.addNewCheckCertificate();
			checkCertificate.setGcID("test");
			checkCertificate.setCertInPEM(certPem);
			CheckCertificateResponseDocument checkCertificateResp = pub.checkCertificate(checkCert);
			assertEquals("updatetest",checkCertificateResp.getCheckCertificateResponse().getXlogin());
			assertEquals("xrole",checkCertificateResp.getCheckCertificateResponse().getRole());

			
			CheckDNDocument checkDNdoc=CheckDNDocument.Factory.newInstance();
			CheckDNDataType checkDN = checkDNdoc.addNewCheckDN();
			checkDN.setGcID("test");
			checkDN.setDistinguishedName("CN=test");
			CheckDNResponseDocument checkDNResp = pub.checkDN(checkDNdoc);
			assertEquals("xtest",checkDNResp.getCheckDNResponse().getXlogin());
			assertEquals("xrole",checkDNResp.getCheckDNResponse().getRole());
			
			RemoveCertificateDocument remDoc=RemoveCertificateDocument.Factory.newInstance();
			remDoc.addNewRemoveCertificate();
			admin.removeCertificate(remDoc);
			
			listDoc=ListDatabaseDocument.Factory.newInstance();
			listDatabase = listDoc.addNewListDatabase();
			listDatabase.setGcID("test");
			listDatabaseResp = admin.listDatabase(listDoc);
			assertEquals(0,listDatabaseResp.getListDatabaseResponse().getDatabaseArray().length);
			
			listDoc=ListDatabaseDocument.Factory.newInstance();
			listDatabase = listDoc.addNewListDatabase();
			listDatabase.setGcID("test1");
			listDatabaseResp = admin.listDatabase(listDoc);
			assertEquals(0,listDatabaseResp.getListDatabaseResponse().getDatabaseArray().length);
			
			
			
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		} finally {
			try {
				server.shutdown();
			} catch (Exception e) { /* ignored */
			}
		}
	}
}
