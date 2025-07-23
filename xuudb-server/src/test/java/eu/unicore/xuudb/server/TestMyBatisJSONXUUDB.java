package eu.unicore.xuudb.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.io.FileInputStream;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import eu.emi.security.authn.x509.impl.CertificateUtils;
import eu.emi.security.authn.x509.impl.CertificateUtils.Encoding;
import eu.unicore.util.configuration.FilePropertiesHelper;
import eu.unicore.xuudb.X509Utils;
import eu.unicore.xuudb.server.db.DatabaseProperties;
import eu.unicore.xuudb.server.db.IRESTClassicStorage;
import eu.unicore.xuudb.server.db.IStorage;
import eu.unicore.xuudb.server.db.MyBatisDatabase;

public class TestMyBatisJSONXUUDB {

	protected IRESTClassicStorage xuudb;
	protected IStorage db;

	@BeforeEach
	public void setUp()throws Exception{
		File dir = new File("target/data");
		FileUtils.deleteDirectory(dir);
		ShutdownHook hook = new ShutdownHook();
		DatabaseProperties dbProps = new DatabaseProperties(FilePropertiesHelper.load(
				"src/test/resources/xuudb_server.conf"));
		db = new MyBatisDatabase(dbProps, hook);
		xuudb = db.getRESTClassicStorage();
		xuudb.remove(new JSONObject());
	}

	@AfterEach
	public void tearDown() {
		db.shutdown();
	}

	@Test
	public void testRemoveByDN()throws Exception{
		JSONObject addRequest = getAddRequest("xlogin1:xlogin2:xlogin3");
		xuudb.add(addRequest);
		assertEquals(1, getEntries().size());
		//remove last one
		JSONObject removeRequest = getRemoveByDNRequest();
		xuudb.remove(removeRequest);
		assertEquals(0, getEntries().size());
	}

	protected JSONObject getRemoveByDNRequest()throws Exception{
		JSONObject req = new JSONObject();
		X509Certificate cert = CertificateUtils.loadCertificate(new FileInputStream("src/test/resources/demo-user.pem"), 
				Encoding.PEM);
		req.put("token", cert.getSubjectX500Principal().getName());
		return req;
	}

	@Test
	public void testAddRemoveIndividualXlogins()throws Exception{
		JSONObject addRequest = getAddRequest("xlogin1");
		xuudb.add(addRequest);
		assertEquals(1,getEntries().size());

		//add a second xlogin
		addRequest = getAddRequest("xlogin2");
		xuudb.add(addRequest);

		List<JSONObject>os = getEntries();
		assertEquals(1,os.size());
		JSONObject x=os.get(0);
		assertEquals("xlogin1:xlogin2",x.get("xlogin"));

		//add a third xlogin
		addRequest=getAddRequest("xlogin3");
		xuudb.add(addRequest);

		os=getEntries();
		assertEquals(1,os.size());
		x = os.get(0);
		assertEquals("xlogin1:xlogin2:xlogin3",x.get("xlogin"));

		//remove one
		JSONObject removeRequest = getRemoveRequest("xlogin2");
		xuudb.remove(removeRequest);
		os=getEntries();
		assertEquals(1,os.size());
		x=os.get(0);
		assertEquals("xlogin1:xlogin3",x.get("xlogin"));
		//remove 2nd one
		removeRequest=getRemoveRequest("xlogin3");
		xuudb.remove(removeRequest);
		os=getEntries();
		assertEquals(1,os.size());
		x=os.get(0);
		assertEquals("xlogin1",x.get("xlogin"));

		//remove last one
		removeRequest=getRemoveRequest("xlogin1");
		xuudb.remove(removeRequest);
		os=getEntries();
		assertEquals(0,os.size());
	}

	@Test
	public void testDoNotAddSameXlogin()throws Exception{
		JSONObject addRequest = getAddRequest("xlogin1");
		xuudb.add(addRequest);
		assertEquals(1,getEntries().size());
		Xlogin x=new Xlogin(getEntries().get(0).getString("xlogin"));
		assertEquals(1, x.getNumberOfLogins());
		addRequest=getAddRequest("xlogin2");
		xuudb.add(addRequest);
		x=new Xlogin(getEntries().get(0).getString("xlogin"));
		assertEquals(2, x.getNumberOfLogins());
		//add the same xlogin
		addRequest=getAddRequest("xlogin1");
		xuudb.add(addRequest);
		x=new Xlogin(getEntries().get(0).getString("xlogin"));
		assertEquals(2, x.getNumberOfLogins());
	}

	@Test
	public void testRemoveFullXlogin()throws Exception{
		JSONObject addRequest = getAddRequest("xlogin1:xlogin2:xlogin3");
		xuudb.add(addRequest);
		assertEquals(1,getEntries().size());
		//remove last one
		JSONObject removeRequest=getRemoveRequest("xlogin1:xlogin2:xlogin3");
		xuudb.remove(removeRequest);
		assertEquals(0,getEntries().size());
	}

	@Test
	public void testImportCSV()throws Exception{
		JSONObject csv = new JSONObject();
		csv.put("clear", true);
		JSONArray db = new JSONArray();
		csv.put("database", db);
		db.put(getAddRequest("xlogin1"));
		xuudb.import_csv(csv);
		assertEquals(1,getEntries().size());
	}
	@Test
	public void testBasicCRUD()throws Exception{
		JSONObject addRequest = getAddRequest("xlogin1");
		xuudb.add(addRequest);
		assertEquals(1, getEntries().size());

		JSONObject queryResponse = doQuery("foo");
		assertEquals("xlogin1", queryResponse.get("xlogin"));

		List<String>gcids=xuudb.listGCIDs();
		assertNotNull(gcids);
		assertEquals(1,gcids.size());
		assertEquals("foo",gcids.get(0));

		JSONObject update = getAddRequest("xlogin2");
		xuudb.update("foo", update.getString("token"), update);
		assertEquals(1, getEntries().size());

		JSONObject removeRequest = getRemoveByXloginRequest("xlogin2");
		xuudb.remove(removeRequest);
		assertEquals(0,getEntries().size());

		queryResponse = doQuery();
		assertNull(queryResponse.optString("xlogin", null));
	}

	@Test
	@Disabled
	public void testPerformance() throws Exception{
		final int ITERATIONS = 10, DIFFERENT_QUERIES = 100, MULTIPLIER = 5;
		final int RECORD_COUNT = DIFFERENT_QUERIES*MULTIPLIER;
		for(int i=0; i<RECORD_COUNT; i++){
			xuudb.add(getAddRequest(i));
		}
		JSONObject x;
		long time = System.currentTimeMillis();
		for(int j=0; j<ITERATIONS; j++){
			long time0 = System.currentTimeMillis();
			for(int i=0; i<DIFFERENT_QUERIES; i++){
				int no=(i*MULTIPLIER*(j*10+3) + (j^0xffff)) % RECORD_COUNT;
				x = doQuery("foo" + no);
				assertEquals("user"+no, x.get("role"));
				assertEquals("login"+no, x.get("xlogin"));
				assertEquals("projects"+no, x.get("projects"));
			}
			time0 = System.currentTimeMillis() - time0;
			System.out.println("Speed in iteration #"+(j+1)+": " + (1000.0*DIFFERENT_QUERIES/time0) + " queries/s");
		}
		time = System.currentTimeMillis() - time;
		System.out.println("Avarage speed: " + (ITERATIONS*1000.0*DIFFERENT_QUERIES/time) + " queries/s");
	}

	@Test
	@Disabled
	public void testScalability() throws Exception{
		final int ITERATIONS = 200, QUERY_THREADS = 40, CRUD_THREADS = 10;
		final int RECORD_COUNT = 100;
		for(int i=0; i<RECORD_COUNT; i++){
			xuudb.add(getAddRequest(i));
		}
		final Map<Integer, Throwable> errors = Collections.synchronizedMap(new TreeMap<Integer, Throwable>());
		final Thread[] threads = new Thread[QUERY_THREADS + CRUD_THREADS];
		for(int i=0; i<QUERY_THREADS; i++){
			final int copyOfI = i;
			threads[copyOfI]=new Thread(new Runnable() {
				@Override
				public void run() {
					for(int j=0; j<ITERATIONS; j++){
						try {
							JSONObject queryResponse = doQuery("foo" + copyOfI);
							assertEquals("user" + copyOfI, queryResponse.get("role"));
							assertEquals("login" + copyOfI, queryResponse.get("xlogin"));
							assertEquals("projects" + copyOfI, queryResponse.get("projects"));
						} catch (AssertionError ex) {
							errors.put(copyOfI, ex);
							return;
						} catch (Exception ex) {
							errors.put(copyOfI, ex);
							return;
						}
					}
				}
			}, "query-thread-"+i);
		}
		for(int i=QUERY_THREADS; i<threads.length; i++){
			final int copyOfI = i;
			threads[copyOfI]=new Thread(new Runnable() {
				@Override
				public void run() {
					final int ID = 9000 + copyOfI;
					for(int j=0; j<ITERATIONS; j++){
						try {
							JSONObject addRequest=getAddRequest(ID);
							xuudb.add(addRequest);
							JSONObject queryResponse = doQuery("foo"+ID);
							assertEquals("user" + ID, queryResponse.get("role"));
							assertEquals("login" + ID, queryResponse.get("xlogin"));
							assertEquals("projects" + ID, queryResponse.get("projects"));
							JSONObject removeRequest = getRemoveByXloginRequest("xlogin"+ID);
							xuudb.remove(removeRequest);
						} catch (AssertionError ex) {
							errors.put(copyOfI, ex);
							return;
						} catch (Exception ex) {
							errors.put(copyOfI, ex);
							return;
						}
					}
				}
			}, "crud-thread-"+i);
		}

		long time = System.currentTimeMillis();
		for(Thread th:threads) th.start();
		for(Thread th:threads) th.join();
		time = System.currentTimeMillis() - time;

		for(Map.Entry<Integer,Throwable> error: errors.entrySet()){
			System.out.println("Thread #"+error.getKey()+" caused an exception: "+error.getValue().getMessage());
		}
		JSONObject removeRequest = new JSONObject();
		xuudb.remove(removeRequest);
		assertEquals(0, errors.size());

		System.out.println("Average speed: " +
				(ITERATIONS*1000.0*(QUERY_THREADS+CRUD_THREADS*3)/time) +
				" operations/s");
	}

	protected List<JSONObject> getEntries()throws Exception{
		JSONObject query = new JSONObject();
		query.put("gcid", "foo");
		return xuudb.listDB(query);
	}
	
	protected JSONObject getAddRequest(String login)throws Exception{
		String cert=X509Utils.getStringFromPEMFile("src/test/resources/demo-user.pem");
		JSONObject add = new JSONObject();
		add.put("gcid", "foo");
		add.put("role", "user");
		add.put("xlogin", login);
		add.put("token", cert);
		return add;
	}

	private JSONObject doQuery()throws Exception{
		return doQuery("foo");
	}

	private JSONObject doQuery(String gcid)throws Exception{
		String dn=X509Utils.loadCertificate("src/test/resources/demo-user.pem").getSubjectX500Principal().getName();
		return xuudb.checkDN(gcid, dn);
	}

	protected JSONObject getRemoveByXloginRequest(String login)throws Exception{
		JSONObject req = new JSONObject();
		req.put("xlogin", login);
		return req;
	}

	private JSONObject getRemoveRequest(String login)throws Exception{
		String cert = X509Utils.getStringFromPEMFile("src/test/resources/demo-user.pem");
		JSONObject req = new JSONObject();
		req.put("gcid", "foo");
		req.put("xlogin", login);
		req.put("token", cert);
		return req;
	}

	private JSONObject getAddRequest(int id)throws Exception{
		JSONObject add = new JSONObject();
		add.put("gcid", "foo"+id);
		add.put("role", "user"+id);
		add.put("xlogin", "login"+id);
		add.put("projects", "projects"+id);
		add.put("token", X509Utils.getStringFromPEMFile("src/test/resources/demo-user.pem"));
		return add;
	}
}
