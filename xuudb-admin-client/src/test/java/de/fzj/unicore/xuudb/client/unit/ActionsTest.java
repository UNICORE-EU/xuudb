package de.fzj.unicore.xuudb.client.unit;

import java.io.File;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

import org.jmock.Expectations;
import org.jmock.Mockery;

import de.fzJuelich.unicore.xuudb.GetAttributesResponseType;
import de.fzJuelich.unicore.xuudb.LoginDataType;
import de.fzJuelich.unicore.xuudb.MappingDataType;
import de.fzJuelich.unicore.xuudb.PoolInfoType;
import de.fzJuelich.unicore.xuudb.SimplifiedAttributeType;
import de.fzj.unicore.xuudb.X509Utils;
import de.fzj.unicore.xuudb.client.CLCExecutor;
import de.fzj.unicore.xuudb.client.actions.ConnectionManager;
import de.fzj.unicore.xuudb.client.wsapi.IAdminExtInterface;
import de.fzj.unicore.xuudb.client.wsapi.IDAPAdminExtInterface;
import de.fzj.unicore.xuudb.client.wsapi.IDAPPublicExtInterface;
import de.fzj.unicore.xuudb.client.wsapi.IPublicExtInterface;
import de.fzj.unicore.xuudb.client.wsapi.XUUDBResponse;

public class ActionsTest extends TestCase {
	Mockery context;
	IPublicExtInterface query;
	IAdminExtInterface admin;
	IDAPAdminExtInterface dapAdmin;
	IDAPPublicExtInterface dapPublic;

	CLCExecutor clcExecutor;

	@Override
	protected void setUp() throws Exception {
		context = new Mockery();
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

	public void testAddDNAction() {
		final String gcId = "demo";
		final String role = "drole";
		final String project = "dproject";
		final String dn = "CN=John Doe,O=Test";
		final String xlogin = "xlogin";

		try {
			context.checking(new Expectations() {
				{
					oneOf(admin).adddn(with(equal(gcId)), with(equal(dn)),
							with(equal(xlogin)), with(equal(role)),
							with(equal(project)));
					will(returnValue(new XUUDBResponse("ok", null, null)));

				}
			});

		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}

		try {
			clcExecutor.parseLine(getTokensFromCmd("adddn " + gcId + " '" + dn
					+ "' " + xlogin + " " + role + " " + project));
		} catch (Exception e) {
			e.printStackTrace();
			fail("AddDN action fail");

		}
		context.assertIsSatisfied();
	}

	public void testAddAction() {
		final String gcId = "demo";
		final String role = "drole";
		final String project = "dproject";
		final String pem = "src/test/resources/demouser.pem";
		final String xlogin = "xlogin";

		try {
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

		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}

		try {

			clcExecutor.parseLine(getTokensFromCmd("add " + gcId + " " + pem
					+ " " + xlogin + " " + role + " " + project));
		} catch (Exception e) {
			e.printStackTrace();
			fail("Add action fail");

		}
		context.assertIsSatisfied();
	}

	public void testUpdateAction() {
		final String gcId = "demo";
		final String role = "drole";
		final String project = "dproject";
		final String pem = "src/test/resources/demouser.pem";
		final String xlogin = "xlogin";

		try {
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

		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}

		try {

			clcExecutor.parseLine(getTokensFromCmd("update " + gcId + " " + pem
					+ " xlogin=" + xlogin + " role=" + role + " project="
					+ project));
		} catch (Exception e) {
			e.printStackTrace();
			fail("Update action fail");

		}
		context.assertIsSatisfied();
	}

	public void testRemoveAction() {
		final String gcId = "demo";
		final String role = "drole";
		final String project = "dproject";
		final String pem = "src/test/resources/demouser.pem";
		final String xlogin = "xlogin";

		try {

			context.checking(new Expectations() {
				{

					exactly(2).of(admin).remove(
							with((aNonNull(LoginDataType.class))));
					will(returnValue(new XUUDBResponse("ok", null, null)));

				}
			});

		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}

		try {

			clcExecutor.parseLine(getTokensFromCmd("remove gcID=" + gcId
					+ " pemfile=" + pem + " xlogin=" + xlogin + " role=" + role
					+ " project=" + project));

			clcExecutor.parseLine(getTokensFromCmd("remove ALL"));

		} catch (Exception e) {
			e.printStackTrace();
			fail("Remove action fail");

		}
		context.assertIsSatisfied();
	}

	public void testListAction() {
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

		try {

			context.checking(new Expectations() {
				{

					oneOf(admin).list(with((aNonNull(LoginDataType.class))));
					will(returnValue(new XUUDBResponse("ok", "test", dd)));

				}
			});

		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}

		try {

			clcExecutor.parseLine(getTokensFromCmd("list gcID=" + gcId));
		} catch (Exception e) {
			e.printStackTrace();
			fail("List action fail");

		}
		context.assertIsSatisfied();
	}

	public void testExportAndImportAction() {
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

		try {

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

		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}

		File f = new File("target/test.csv");

		try {

			clcExecutor.parseLine(getTokensFromCmd("export "
					+ f.getAbsolutePath().replace(".csv", "") + " force"));

			clcExecutor.parseLine(getTokensFromCmd("export "
					+ f.getAbsolutePath() + " force"));

			clcExecutor.parseLine(getTokensFromCmd("export "
					+ f.getAbsolutePath()));

			if (!f.exists())
				fail();

		} catch (Exception e) {
			e.printStackTrace();
			fail("Export action fail");

		}

		try {

			clcExecutor.parseLine(getTokensFromCmd("import "
					+ f.getAbsolutePath() + " clearDB"));

		} catch (Exception e) {
			e.printStackTrace();
			fail("Import action fail");

		}
		f.delete();

		context.assertIsSatisfied();
	}

	public void testCheckCertAction() {
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

		try {
			X509Certificate x509 = X509Utils.loadCertificate(pem);
			final String certPem = X509Utils.getPEMStringFromX509(x509);
			data.setToken(certPem);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

		final LoginDataType[] dd = new LoginDataType[1];
		dd[0] = data;

		try {

			context.checking(new Expectations() {
				{

					oneOf(query).checkCert(with(equal(gcId)),
							with(equal(data.getToken())));
					will(returnValue(new XUUDBResponse("ok", null, dd)));

				}
			});

		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}

		try {

			clcExecutor.parseLine(getTokensFromCmd("check-cert " + gcId + " "
					+ pem));
		} catch (Exception e) {
			e.printStackTrace();
			fail("Check cert action fail");

		}
		context.assertIsSatisfied();
	}

	public void testCheckDNAction() {
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

		try {

			context.checking(new Expectations() {
				{

					oneOf(query).checkDN(with(equal(gcId)), with(equal(dn)));
					will(returnValue(new XUUDBResponse("ok", null, dd)));

				}
			});

		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}

		try {

			clcExecutor.parseLine(getTokensFromCmd("check-dn " + gcId + " "
					+ "'" + dn + "'"));
		} catch (Exception e) {
			e.printStackTrace();
			fail("Check DN action fail");

		}
		context.assertIsSatisfied();
	}

	public void testFreezeMappingByIdAction() {

		try {

			context.checking(new Expectations() {
				{

					oneOf(dapAdmin).freeze(with(aNonNull(String.class)),
							with(aNonNull(String.class)));

				}
			});

		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}

		try {

			clcExecutor.parseLine(getTokensFromCmd("freezeMappings map1 2"));
		} catch (Exception e) {
			e.printStackTrace();
			fail("Freeze Mapping by id action fail");

		}
		context.assertIsSatisfied();
	}

	public void testFreezeMappingAction() {

		try {

			context.checking(new Expectations() {
				{

					oneOf(dapAdmin).freeze(with(aNonNull(Date.class)),
							with(aNonNull(String.class)));

				}
			});

		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}

		try {

			clcExecutor
					.parseLine(getTokensFromCmd("freezeMappings 2011-12-12-11-12-12 pool"));
		} catch (Exception e) {
			e.printStackTrace();
			fail("Freeze Mapping action fail");

		}
		context.assertIsSatisfied();
	}

	public void testRemoveMappingByIdAction() {

		try {

			context.checking(new Expectations() {
				{

					oneOf(dapAdmin).remove(with(aNonNull(String.class)),
							with(aNonNull(String.class)));

				}
			});

		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}

		try {

			clcExecutor.parseLine(getTokensFromCmd("removeMappings map1 2"));
		} catch (Exception e) {
			e.printStackTrace();
			fail("Remove Mapping by id action fail");

		}
		context.assertIsSatisfied();
	}

	public void testRemoveMappingAction() {

		try {

			context.checking(new Expectations() {
				{

					oneOf(dapAdmin).remove(with(aNonNull(Date.class)),
							with(aNonNull(String.class)));

				}
			});

		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}

		try {

			clcExecutor
					.parseLine(getTokensFromCmd("removeMappings 12-12-2011 pool"));

		} catch (Exception e) {
			e.printStackTrace();
			fail("Remove Mapping action fail");

		}
		context.assertIsSatisfied();
	}

	public void testListMappingAction() {
		try {

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

		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}

		try {

			clcExecutor
					.parseLine(getTokensFromCmd("listMappings frozen pool1"));

		} catch (Exception e) {
			e.printStackTrace();
			fail("List Mapping action fail");

		}
		context.assertIsSatisfied();
	}

	public void testFindMappingAction() {
		try {

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

		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}

		try {

			clcExecutor.parseLine(getTokensFromCmd("findMapping vo cl"));
		} catch (Exception e) {
			e.printStackTrace();
			fail("Find Mapping action fail");

		}
		context.assertIsSatisfied();
	}

	public void testFindReverseMappingAction() {
		try {

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

		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}

		try {

			clcExecutor
					.parseLine(getTokensFromCmd("findReverseMapping tt val"));
		} catch (Exception e) {
			e.printStackTrace();
			fail("Find reverse Mapping action fail");

		}
		context.assertIsSatisfied();
	}

	public void testRemovePoolAction() {
		try {

			context.checking(new Expectations() {
				{

					oneOf(dapAdmin).removePool(with(aNonNull(String.class)));

				}
			});

		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}

		try {

			clcExecutor.parseLine(getTokensFromCmd("removePool pool1"));
		} catch (Exception e) {
			e.printStackTrace();
			fail("Remove pool action fail");

		}
		context.assertIsSatisfied();
	}

	public void testListPoolsAction() {
		try {

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

		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}

		try {

			clcExecutor.parseLine(getTokensFromCmd("listPools"));
		} catch (Exception e) {
			e.printStackTrace();
			fail("List pools Mapping action fail");

		}
		context.assertIsSatisfied();
	}

	public void testSimulateAction() {
		final String dn = "CN=John Doe,O=Test";

		try {

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

		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}

		try {

			clcExecutor
					.parseLine(getTokensFromCmd("simulate "
							+ "'dn="
							+ dn
							+ "' "
							+ "'issuerdn="
							+ dn
							+ "'"
							+ "role=role1 xlogin=x vo=x gid=g supplementaryGids=g1,g2 another=vl"));
		} catch (Exception e) {
			e.printStackTrace();
			fail("Simulate Mapping action fail");

		}
		context.assertIsSatisfied();
	}

}
