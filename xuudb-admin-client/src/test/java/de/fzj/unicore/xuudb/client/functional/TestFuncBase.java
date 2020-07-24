package de.fzj.unicore.xuudb.client.functional;

import java.io.File;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.concurrent.Executors;

import junit.framework.TestCase;
import de.fzJuelich.unicore.xuudb.LoginDataType;
import de.fzj.unicore.xuudb.X509Utils;
import de.fzj.unicore.xuudb.client.CLCExecutor;
import de.fzj.unicore.xuudb.client.ClientConfiguration;
import de.fzj.unicore.xuudb.client.ServiceFactory;
import de.fzj.unicore.xuudb.client.wsapi.IAdminExtInterface;
import de.fzj.unicore.xuudb.client.wsapi.IDAPAdminExtInterface;
import de.fzj.unicore.xuudb.client.wsapi.IPublicExtInterface;
import de.fzj.unicore.xuudb.server.HttpsServer;
import eu.unicore.util.configuration.FilePropertiesHelper;

public abstract class TestFuncBase extends TestCase {
	protected String serverConfigFile = "src/test/resources/server.conf";
	protected  String clientConfigFile = "src/test/resources/client.conf";

	protected CLCExecutor clc;

	protected String gcIdDN = "TestDN";
	protected String gcId = "Test";
	protected String dn = "EMAILADDRESS=unicore-support@lists.sf.net,C=DE,O=unicore.eu,OU=Testing,CN=UNICORE demo user";
	protected String xlogin1 = "test";
	protected String xlogin2 = "test2";
	protected String project = "test";
	protected String role = "user";
	protected String pemFile = "src/test/resources/demouser.pem";
	protected String certPem;

	protected IAdminExtInterface admin;
	protected IPublicExtInterface query;
	protected IDAPAdminExtInterface dap;
	protected HttpsServer server;
	
	protected void setUp() throws Exception {

		Properties p = FilePropertiesHelper.load(serverConfigFile);
		//CHECK 
		server=new HttpsServer(p,Executors.newScheduledThreadPool(5));
		server.start();

		ClientConfiguration conf = new ClientConfiguration(new File(
				clientConfigFile));
		ServiceFactory serviceFac = new ServiceFactory(conf);

		admin = serviceFac.getAdminAPI();
		query = serviceFac.getPublicAPI();
		dap = serviceFac.getDAPAdminAPI();
		
		
		X509Certificate x509 = null;
		
		x509 = X509Utils.loadCertificate(pemFile);
		certPem = X509Utils.getPEMStringFromX509(x509);
		

	}
	
	@Override
	protected void tearDown() throws Exception {
		System.out.println("Clear database");
		admin.remove(LoginDataType.Factory.newInstance());
		server.shutdown();
	}

}
