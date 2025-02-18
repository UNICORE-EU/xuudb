package eu.unicore.xuudb.client.unit;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.junit5.JUnit5Mockery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.unicore.xuudb.xbeans.GetAttributesResponseType;
import eu.unicore.xuudb.xbeans.LoginDataType;
import eu.unicore.xuudb.xbeans.MappingDataType;
import eu.unicore.xuudb.xbeans.PoolInfoType;
import eu.unicore.xuudb.xbeans.SimplifiedAttributeType;
import eu.unicore.xuudb.X509Utils;
import eu.unicore.xuudb.client.CLCExecutor;
import eu.unicore.xuudb.client.actions.ConnectionManager;
import eu.unicore.xuudb.client.wsapi.IAdminExtInterface;
import eu.unicore.xuudb.client.wsapi.IDAPAdminExtInterface;
import eu.unicore.xuudb.client.wsapi.IDAPPublicExtInterface;
import eu.unicore.xuudb.client.wsapi.IPublicExtInterface;
import eu.unicore.xuudb.client.wsapi.XUUDBResponse;

public class ActionsTest {

	Mockery context;
	IPublicExtInterface query;
	IAdminExtInterface admin;
	IDAPAdminExtInterface dapAdmin;
	IDAPPublicExtInterface dapPublic;

	CLCExecutor clcExecutor;

	@BeforeEach
	public void setUp() throws Exception {
		context = new JUnit5Mockery();
		query = context.mock(IPublicExtInterface.class);
		admin = context.mock(IAdminExtInterface.class);
		dapAdmin = context.mock(IDAPAdminExtInterface.class);
		dapPublic = context.mock(IDAPPublicExtInterface.class);

		ConnectionManager connectionManager = new ConnectionManager(admin,
				query, dapAdmin, dapPublic);
		clcExecutor = new CLCExecutor(connectionManager,
				"src/test/resources/client.conf");
		clcExecutor.readConfig();
		clcExecutor.registerAllActions();
		clcExecutor.registerHelpActions();

	}

	String[] getTokensFromCmd(String line) {

		ArrayList<String> ret = new ArrayList<String>();
		char[] c = line.toCharArray();
		char context = ' ';
		StringBuffer curW = new StringBuffer();

		for (int i = 0; i < c.length; i++) {
			if (context != '-') {
				switch (context) {
				case ' ':
					if (c[i] == ' ')
						break;
					if (c[i] == '\'' || c[i] == '"')
						context = c[i];
					else {
						context = '-';
						curW.append(c[i]);
					}
					break;
				case '\'':
				case '"':
					if (c[i] == context) {
						context = '-';
						ret.add(curW.toString());
						curW.setLength(0);
					} else
						curW.append(c[i]);
					break;
				}
			} else {
				switch (c[i]) {
				case ' ':
					if (curW.length() > 0) {
						ret.add(curW.toString());
						curW.setLength(0);
					}
					context = c[i];
					break;
				default:
					curW.append(c[i]);
				}
			}
		}
		if (curW.length() > 0)
			ret.add(curW.toString());
		return ret.toArray(new String[ret.size()]);
	}

	@Test
	public void testAddDNAction() throws Exception {
		final String gcId = "demo";
		final String role = "drole";
		final String project = "dproject";
		final String dn = "CN=John Doe,O=Test";
		final String xlogin = "xlogin";


		context.checking(new Expectations() {
			{
				oneOf(admin).adddn(with(equal(gcId)), with(equal(dn)),
						with(equal(xlogin)), with(equal(role)),
						with(equal(project)));
				will(returnValue(new XUUDBResponse("ok", null, null)));

			}
		});

		clcExecutor.parseLine(getTokensFromCmd("adddn " + gcId + " '" + dn
				+ "' " + xlogin + " " + role + " " + project));
		context.assertIsSatisfied();
	}

	@Test
	public void testAddAction() throws Exception {
		final String gcId = "demo";
		final String role = "drole";
		final String project = "dproject";
		final String pem = "src/test/resources/demouser.pem";
		final String xlogin = "xlogin";

		X509Certificate x509 = X509Utils.loadCertificate(pem);
		final String certPem = X509Utils.getPEMStringFromX509(x509);
		context.checking(new Expectations() {
			{
				oneOf(admin).add(with(equal(gcId)), with(equal(certPem)),
						with(equal(xlogin)), with(equal(role)),
						with(equal(project)));
				will(returnValue(new XUUDBResponse("ok", null, null)));

			}
		});
		clcExecutor.parseLine(getTokensFromCmd("add " + gcId + " " + pem
				+ " " + xlogin + " " + role + " " + project));
		context.assertIsSatisfied();
	}

	@Test
	public void testUpdateAction() throws Exception {
		final String gcId = "demo";
		final String role = "drole";
		final String project = "dproject";
		final String pem = "src/test/resources/demouser.pem";
		final String xlogin = "xlogin";

		X509Certificate x509 = X509Utils.loadCertificate(pem);
		final String certPem = X509Utils.getPEMStringFromX509(x509);
		context.checking(new Expectations() {
			{

				oneOf(admin).update(with(equal(gcId)),
						with(equal(certPem)),
						with((aNonNull(LoginDataType.class))));
				will(returnValue(new XUUDBResponse("ok", null, null)));

			}
		});

		clcExecutor.parseLine(getTokensFromCmd("update " + gcId + " " + pem
				+ " xlogin=" + xlogin + " role=" + role + " project="
				+ project));
		context.assertIsSatisfied();
	}

	@Test
	public void testRemoveAction() throws Exception {
		final String gcId = "demo";
		final String role = "drole";
		final String project = "dproject";
		final String pem = "src/test/resources/demouser.pem";
		final String xlogin = "xlogin";
		context.checking(new Expectations() {
			{

				exactly(2).of(admin).remove(
						with((aNonNull(LoginDataType.class))));
				will(returnValue(new XUUDBResponse("ok", null, null)));

			}
		});

		clcExecutor.parseLine(getTokensFromCmd("remove gcID=" + gcId
				+ " pemfile=" + pem + " xlogin=" + xlogin + " role=" + role
				+ " project=" + project));

		clcExecutor.parseLine(getTokensFromCmd("remove ALL"));
		context.assertIsSatisfied();
	}

	@Test
	public void testListAction() throws Exception {
		final String gcId = "demo";
		final String role = "drole";
		final String project = "dproject";
		final String dn = "CN=John Doe,O=Test";
		final String xlogin = "xlogin";

		LoginDataType data = LoginDataType.Factory.newInstance();
		data.setGcID(gcId);
		data.setRole(role);
		data.setToken(dn);
		data.setXlogin(xlogin);
		data.setProjects(project);
		final LoginDataType[] dd = new LoginDataType[1];
		dd[0] = data;

		context.checking(new Expectations() {
			{

				oneOf(admin).list(with((aNonNull(LoginDataType.class))));
				will(returnValue(new XUUDBResponse("ok", "test", dd)));

			}
		});

		clcExecutor.parseLine(getTokensFromCmd("list gcID=" + gcId));
		context.assertIsSatisfied();
	}

	@Test
	public void testExportAndImportAction() throws Exception {
		final String gcId = "demo";
		final String role = "drole";
		final String project = "dproject";
		final String dn = "CN=John Doe,O=Test";
		final String xlogin = "xlogin";

		LoginDataType data = LoginDataType.Factory.newInstance();
		data.setGcID(gcId);
		data.setRole(role);
		data.setToken(dn);
		data.setXlogin(xlogin);
		data.setProjects(project);
		final ArrayList<LoginDataType> dda = new ArrayList<LoginDataType>();
		dda.add(data);

		final LoginDataType[] dd = new LoginDataType[1];
		dd[0] = data;

		context.checking(new Expectations() {
			{
				exactly(2).of(admin).exportCsv();
				will(returnValue(new XUUDBResponse("ok", "test", dd)));

				oneOf(admin).importCsv(
						with(aNonNull(LoginDataType[].class)),
						with(equal(true)));
				will(returnValue(new XUUDBResponse("ok", null, null)));

			}
		});
		File f = new File("target/test.csv");
		clcExecutor.parseLine(getTokensFromCmd("export "
				+ f.getAbsolutePath().replace(".csv", "") + " force"));

		clcExecutor.parseLine(getTokensFromCmd("export "
				+ f.getAbsolutePath() + " force"));

		clcExecutor.parseLine(getTokensFromCmd("export "
				+ f.getAbsolutePath()));

		if (!f.exists())
			fail();
		clcExecutor.parseLine(getTokensFromCmd("import "
				+ f.getAbsolutePath() + " clearDB"));
		f.delete();

		context.assertIsSatisfied();
	}

	@Test
	public void testCheckCertAction() throws Exception {
		final String gcId = "demo";
		final String role = "drole";
		final String project = "dproject";
		final String xlogin = "xlogin";
		final String pem = "src/test/resources/demouser.pem";

		final LoginDataType data = LoginDataType.Factory.newInstance();
		data.setGcID(gcId);
		data.setRole(role);
		data.setXlogin(xlogin);
		data.setProjects(project);
		X509Certificate x509 = X509Utils.loadCertificate(pem);
		final String certPem = X509Utils.getPEMStringFromX509(x509);
		data.setToken(certPem);

		final LoginDataType[] dd = new LoginDataType[1];
		dd[0] = data;
		context.checking(new Expectations() {
			{

				oneOf(query).checkCert(with(equal(gcId)),
						with(equal(data.getToken())));
				will(returnValue(new XUUDBResponse("ok", null, dd)));

			}
		});
		clcExecutor.parseLine(getTokensFromCmd("check-cert " + gcId + " "
				+ pem));
		context.assertIsSatisfied();
	}

	@Test
	public void testCheckDNAction() throws Exception {
		final String gcId = "demo";
		final String role = "drole";
		final String project = "dproject";
		final String xlogin = "xlogin";
		final String dn = "CN=John Doe,O=Test";

		final LoginDataType data = LoginDataType.Factory.newInstance();
		data.setGcID(gcId);
		data.setRole(role);
		data.setXlogin(xlogin);
		data.setProjects(project);
		data.setToken(dn);
		final LoginDataType[] dd = new LoginDataType[1];
		dd[0] = data;
		context.checking(new Expectations() {
			{

				oneOf(query).checkDN(with(equal(gcId)), with(equal(dn)));
				will(returnValue(new XUUDBResponse("ok", null, dd)));

			}
		});
		clcExecutor.parseLine(getTokensFromCmd("check-dn " + gcId + " "
				+ "'" + dn + "'"));
		context.assertIsSatisfied();
	}

	@Test
	public void testFreezeMappingByIdAction() throws Exception {
		context.checking(new Expectations() {
			{

				oneOf(dapAdmin).freeze(with(aNonNull(String.class)),
						with(aNonNull(String.class)));

			}
		});

		clcExecutor.parseLine(getTokensFromCmd("freezeMappings map1 2"));
		context.assertIsSatisfied();
	}

	@Test
	public void testFreezeMappingAction() throws Exception {
		context.checking(new Expectations() {
			{

				oneOf(dapAdmin).freeze(with(aNonNull(Date.class)),
						with(aNonNull(String.class)));

			}
		});
		clcExecutor
		.parseLine(getTokensFromCmd("freezeMappings 2011-12-12-11-12-12 pool"));
		context.assertIsSatisfied();
	}

	@Test
	public void testRemoveMappingByIdAction() throws Exception {
		context.checking(new Expectations() {
			{

				oneOf(dapAdmin).remove(with(aNonNull(String.class)),
						with(aNonNull(String.class)));

			}
		});

		clcExecutor.parseLine(getTokensFromCmd("removeMappings map1 2"));
		context.assertIsSatisfied();
	}

	@Test
	public void testRemoveMappingAction() throws Exception {
		context.checking(new Expectations() {
			{

				oneOf(dapAdmin).remove(with(aNonNull(Date.class)),
						with(aNonNull(String.class)));

			}
		});
		clcExecutor
		.parseLine(getTokensFromCmd("removeMappings 12-12-2011 pool"));
		context.assertIsSatisfied();
	}

	@Test
	public void testListMappingAction() throws Exception {
		final MappingDataType d = MappingDataType.Factory.newInstance();
		d.setKey("test");
		d.setValue("testV");
		d.setId("1001");
		d.setPoolName("Name1");
		d.setFreezeTime(Calendar.getInstance());
		d.setLastAccess(Calendar.getInstance());

		final MappingDataType[] resp = new MappingDataType[1];
		resp[0] = d;
		context.checking(new Expectations() {
			{

				oneOf(dapAdmin).list(with(aNonNull(String.class)),
						with(aNonNull(String.class)));
				will(returnValue(resp));

			}
		});
		clcExecutor
		.parseLine(getTokensFromCmd("listMappings frozen pool1"));
		context.assertIsSatisfied();
	}

	@Test
	public void testFindMappingAction() throws Exception {
		final MappingDataType f = MappingDataType.Factory.newInstance();
		f.setId("1");
		f.setPoolName("100");
		f.setKey("key");
		f.setKeyType("keyType");

		final MappingDataType[] resp = new MappingDataType[1];
		resp[0] = f;
		context.checking(new Expectations() {
			{

				oneOf(dapAdmin).find(with(aNonNull(String.class)),
						with(aNonNull(String.class)));
				will(returnValue(resp));

			}
		});
		clcExecutor.parseLine(getTokensFromCmd("findMapping vo cl"));
		context.assertIsSatisfied();
	}

	@Test
	public void testFindReverseMappingAction() throws Exception {
		final MappingDataType f = MappingDataType.Factory.newInstance();
		f.setId("1");
		f.setPoolName("100");
		f.setKey("key");
		f.setKeyType("keyType");

		final MappingDataType[] resp = new MappingDataType[1];
		resp[0] = f;
		context.checking(new Expectations() {
			{

				oneOf(dapAdmin).findReverse(with(aNonNull(String.class)),
						with(aNonNull(String.class)));
				will(returnValue(resp));

			}
		});

		clcExecutor
		.parseLine(getTokensFromCmd("findReverseMapping tt val"));
		context.assertIsSatisfied();
	}

	@Test
	public void testRemovePoolAction() throws Exception {
		context.checking(new Expectations() {
			{

				oneOf(dapAdmin).removePool(with(aNonNull(String.class)));

			}
		});
		clcExecutor.parseLine(getTokensFromCmd("removePool pool1"));
		context.assertIsSatisfied();
	}

	@Test
	public void testListPoolsAction() throws Exception {
		final PoolInfoType f = PoolInfoType.Factory.newInstance();
		f.setPoolId("11");
		f.setFreeSlots(1000);
		f.setActiveMappings(12);
		f.setFrozenMappings(20);
		f.setPoolKeyType("vo");
		f.setPoolType("gid");

		final PoolInfoType[] resp = new PoolInfoType[1];
		resp[0] = f;
		context.checking(new Expectations() {
			{

				oneOf(dapAdmin).listPools();
				will(returnValue(resp));

			}
		});

		clcExecutor.parseLine(getTokensFromCmd("listPools"));
		context.assertIsSatisfied();
	}

	@Test
	public void testSimulateAction() throws Exception {
		final String dn = "CN=John Doe,O=Test";
		final GetAttributesResponseType resp = GetAttributesResponseType.Factory
				.newInstance();
		resp.setGid("gid1");
		resp.setXlogin("xlogin");
		String[] sub=new String[2];
		sub[0]="sub1";
		sub[1]="sub2";
		resp.setSupplementaryGidsArray(sub);

		context.checking(new Expectations() {
			{

				oneOf(dapPublic).simulateGetAttributes(
						with(aNonNull(String.class)),
						with(aNonNull(String.class)),
						with(aNonNull(String.class)),
						with(aNonNull(String.class)),
						with(aNonNull(String.class)),
						with(aNonNull(String.class)),
						with(aNonNull(String[].class)),
						with(aNonNull(SimplifiedAttributeType[].class)));
				will(returnValue(resp));
			}
		});
		clcExecutor
		.parseLine(getTokensFromCmd("simulate "
				+ "'dn="
				+ dn
				+ "' "
				+ "'issuerdn="
				+ dn
				+ "'"
				+ "role=role1 xlogin=x vo=x gid=g supplementaryGids=g1,g2 another=vl"));
		context.assertIsSatisfied();
	}

}
