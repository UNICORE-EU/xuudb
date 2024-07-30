package eu.unicore.xuudb.client.functional;

import java.io.File;
import java.security.cert.X509Certificate;
import java.util.Properties;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

import de.fzJuelich.unicore.xuudb.LoginDataType;
import eu.unicore.util.configuration.FilePropertiesHelper;
import eu.unicore.xuudb.X509Utils;
import eu.unicore.xuudb.client.CLCExecutor;
import eu.unicore.xuudb.client.ClientConfiguration;
import eu.unicore.xuudb.client.ServiceFactory;
import eu.unicore.xuudb.client.wsapi.IAdminExtInterface;
import eu.unicore.xuudb.client.wsapi.IDAPAdminExtInterface;
import eu.unicore.xuudb.client.wsapi.IPublicExtInterface;
import eu.unicore.xuudb.server.HttpsServer;

public abstract class TestFuncBase {

	protected static String serverConfigFile = "src/test/resources/server.conf";
	protected static String clientConfigFile = "src/test/resources/client.conf";

	protected static CLCExecutor clc;

	protected String gcIdDN = "TestDN";
	protected String gcId = "Test";
	protected String dn = "EMAILADDRESS=unicore-support@lists.sf.net,C=DE,O=unicore.eu,OU=Testing,CN=UNICORE demo user";
	protected String xlogin1 = "test";
	protected String xlogin2 = "test2";
	protected String project = "test";
	protected String role = "user";
	protected static String pemFile = "src/test/resources/demouser.pem";
	protected static String certPem;

	protected static IAdminExtInterface admin;
	protected static IPublicExtInterface query;
	protected static IDAPAdminExtInterface dap;
	protected static HttpsServer server;
	
	@BeforeAll
	public static void setUp() throws Exception {
		Properties p = FilePropertiesHelper.load(serverConfigFile);
		server = new HttpsServer(p);
		server.start();
		ClientConfiguration conf = new ClientConfiguration(new File(clientConfigFile));
		ServiceFactory serviceFac = new ServiceFactory(conf);
		admin = serviceFac.getAdminAPI();
		query = serviceFac.getPublicAPI();
		dap = serviceFac.getDAPAdminAPI();
		X509Certificate x509 = X509Utils.loadCertificate(pemFile);
		certPem = X509Utils.getPEMStringFromX509(x509);
	}

	@AfterEach
	public void cleanup() throws Exception {
		System.out.println("Clear database");
		admin.remove(LoginDataType.Factory.newInstance());
	}

	@AfterAll
	public static void tearDown() throws Exception {
		server.shutdown();
	}

}
