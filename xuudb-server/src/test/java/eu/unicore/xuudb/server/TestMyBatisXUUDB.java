package eu.unicore.xuudb.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileInputStream;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import de.fzJuelich.unicore.xuudb.LoginDataType;
import eu.emi.security.authn.x509.impl.CertificateUtils;
import eu.emi.security.authn.x509.impl.CertificateUtils.Encoding;
import eu.unicore.util.configuration.FilePropertiesHelper;
import eu.unicore.xuudb.X509Utils;
import eu.unicore.xuudb.server.db.DatabaseProperties;
import eu.unicore.xuudb.server.db.IClassicStorage;
import eu.unicore.xuudb.server.db.IStorage;
import eu.unicore.xuudb.server.db.MyBatisDatabase;

public class TestMyBatisXUUDB {
	
	protected IClassicStorage xuudb;
	protected IStorage db;
	
	@BeforeEach
	public void setUp()throws Exception{
		File dir = new File("target/data");
		FileUtils.deleteDirectory(dir);
		ShutdownHook hook = new ShutdownHook();
		DatabaseProperties dbProps = new DatabaseProperties(FilePropertiesHelper.load(
				"src/test/resources/xuudb_server.conf"));
		db = new MyBatisDatabase(dbProps, hook);
		xuudb = db.getClassicStorage();
		xuudb.remove(LoginDataType.Factory.newInstance());
	}

	@AfterEach
	public void tearDown() {
		db.shutdown();
	}
	
	@Test
	public void testRemoveByDN()throws Exception{
		LoginDataType addRequest=getAddRequest("xlogin1:xlogin2:xlogin3");
		xuudb.add(addRequest);
		assertEquals(1, getEntries().length);
		
		//remove last one
		LoginDataType removeRequest=getRemoveByDNRequest();
		xuudb.remove(removeRequest);
		LoginDataType[]os=getEntries();
		assertEquals(0,os.length);
	}
	
	protected LoginDataType getRemoveByDNRequest()throws Exception{
		LoginDataType add=LoginDataType.Factory.newInstance();
		X509Certificate cert = CertificateUtils.loadCertificate(new FileInputStream("src/test/resources/demo-user.pem"), 
				Encoding.PEM);
		add.setToken(cert.getSubjectX500Principal().getName());
		return add;
	}

	@Test
	public void testAddRemoveIndividualXlogins()throws Exception{
		
		LoginDataType addRequest=getAddRequest("xlogin1");
		xuudb.add(addRequest);
		
		assertEquals(1,getEntries().length);

		//add a second xlogin
		addRequest=getAddRequest("xlogin2");
		xuudb.add(addRequest);
		
		LoginDataType[]os=getEntries();
		assertEquals(1,os.length);
		LoginDataType x=os[0];
		assertEquals("xlogin1:xlogin2",x.getXlogin());
		
		//add a third xlogin
		addRequest=getAddRequest("xlogin3");
		xuudb.add(addRequest);
		
		os=getEntries();
		assertEquals(1,os.length);
		x=os[0];
		assertEquals("xlogin1:xlogin2:xlogin3",x.getXlogin());
		
		//remove one
		LoginDataType removeRequest=getRemoveRequest("xlogin2");
		xuudb.remove(removeRequest);
		os=getEntries();
		assertEquals(1,os.length);
		x=os[0];
		assertEquals("xlogin1:xlogin3",x.getXlogin());
		//remove 2nd one
		removeRequest=getRemoveRequest("xlogin3");
		xuudb.remove(removeRequest);
		os=getEntries();
		assertEquals(1,os.length);
		x=os[0];
		assertEquals("xlogin1",x.getXlogin());
		
		//remove last one
		removeRequest=getRemoveRequest("xlogin1");
		xuudb.remove(removeRequest);
		os=getEntries();
		assertEquals(0,os.length);
		
	}

	@Test
	public void testDoNotAddSameXlogin()throws Exception{
		
		LoginDataType addRequest=getAddRequest("xlogin1");
		xuudb.add(addRequest);
		
		assertEquals(1,getEntries().length);

		Xlogin x=new Xlogin(getEntries()[0].getXlogin());
		assertEquals(1, x.getNumberOfLogins());

		addRequest=getAddRequest("xlogin2");
		xuudb.add(addRequest);

		x=new Xlogin(getEntries()[0].getXlogin());
		assertEquals(2, x.getNumberOfLogins());
		
		//add the same xlogin
		addRequest=getAddRequest("xlogin1");
		xuudb.add(addRequest);

		x=new Xlogin(getEntries()[0].getXlogin());
		assertEquals(2, x.getNumberOfLogins());
		
	}

	@Test
	public void testRemoveFullXlogin()throws Exception{
		LoginDataType addRequest=getAddRequest("xlogin1:xlogin2:xlogin3");
		xuudb.add(addRequest);
		assertEquals(1,getEntries().length);
		
		//remove last one
		LoginDataType removeRequest=getRemoveRequest("xlogin1:xlogin2:xlogin3");
		xuudb.remove(removeRequest);
		LoginDataType[]os=getEntries();
		assertEquals(0,os.length);
	}

	@Test
	public void testBasicCRUD()throws Exception{
		LoginDataType addRequest=getAddRequest("xlogin1");
		xuudb.add(addRequest);
		assertEquals(1,getEntries().length);

		LoginDataType queryResponse=doQuery();
		assertEquals("xlogin1",queryResponse.getXlogin());
		
		List<String>gcids=xuudb.listGCIDs();
		assertNotNull(gcids);
		assertEquals(1,gcids.size());
		assertEquals("foo",gcids.get(0));
		
		
		LoginDataType removeRequest=getRemoveByXloginRequest("xlogin1");
		xuudb.remove(removeRequest);
		assertEquals(0,getEntries().length);

		//removeRequest=getRemoveByXloginRequest("\'; drop table uudb; --");
		//xuudb.remove(removeRequest);

		queryResponse=doQuery();
		assertNull(queryResponse.getXlogin());
		
	
	}

	@Test
	@Disabled
	public void testPerformance() throws Exception{
		final int ITERATIONS = 10, DIFFERENT_QUERIES = 100, MULTIPLIER = 5;
		final int RECORD_COUNT = DIFFERENT_QUERIES*MULTIPLIER;
		for(int i=0; i<RECORD_COUNT; i++){
			xuudb.add(getAddRequest(i));
		}
		LoginDataType x;
		long time = System.currentTimeMillis();
		for(int j=0; j<ITERATIONS; j++){
			long time0 = System.currentTimeMillis();
			for(int i=0; i<DIFFERENT_QUERIES; i++){
				int no=(i*MULTIPLIER*(j*10+3) + (j^0xffff)) % RECORD_COUNT;
				try {
					x = doQuery("foo" + no);
					assertEquals("user"+no, x.getRole());
					assertEquals("login"+no, x.getXlogin());
					assertEquals("projects"+no, x.getProjects());
				} catch (Exception ex) {
					ex.printStackTrace();
					fail();
				}
			}
			time0 = System.currentTimeMillis() - time0;
			System.out.println("Speed in iteration #"+(j+1)+": " + (1000.0*DIFFERENT_QUERIES/time0) + " queries/s");
		}
		time = System.currentTimeMillis() - time;
		System.out.println("Avarage speed: " + (ITERATIONS*1000.0*DIFFERENT_QUERIES/time) + " queries/s");

		LoginDataType removeRequest=LoginDataType.Factory.newInstance();
		xuudb.remove(removeRequest);
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
							LoginDataType queryResponse = doQuery("foo" + copyOfI);
							assertEquals("user" + copyOfI, queryResponse.getRole());
							assertEquals("login" + copyOfI, queryResponse.getXlogin());
							assertEquals("projects" + copyOfI, queryResponse.getProjects());
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
							LoginDataType addRequest=getAddRequest(ID);
							xuudb.add(addRequest);
							
							LoginDataType queryResponse=doQuery();
							assertEquals("user" + ID, queryResponse.getRole());
							assertEquals("login" + ID, queryResponse.getXlogin());
							assertEquals("projects" + ID, queryResponse.getProjects());

							LoginDataType removeRequest=getRemoveByXloginRequest("xlogin"+ID);
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
		LoginDataType removeRequest=LoginDataType.Factory.newInstance();
		xuudb.remove(removeRequest);
		assertEquals(0, errors.size());

		System.out.println("Average speed: " +
				(ITERATIONS*1000.0*(QUERY_THREADS+CRUD_THREADS*3)/time) +
				" operations/s");
	}
	
	protected LoginDataType[] getEntries()throws Exception{
		LoginDataType query=LoginDataType.Factory.newInstance();
		query.setGcID("foo");
		return xuudb.listDB(query);
	}
	
	protected LoginDataType getAddRequest(String login)throws Exception{
		String cert=X509Utils.getStringFromPEMFile("src/test/resources/demo-user.pem");
		LoginDataType add=LoginDataType.Factory.newInstance();
		add.setGcID("foo");
		add.setRole("user");
		add.setXlogin(login);
		add.setToken(cert);
		return add;
	}

	private LoginDataType doQuery()throws Exception{
		String cert=X509Utils.getStringFromPEMFile("src/test/resources/demo-user.pem");
		return xuudb.checkToken("foo", cert);
	}

	protected LoginDataType getRemoveByXloginRequest(String login)throws Exception{
		LoginDataType add=LoginDataType.Factory.newInstance();
		add.setXlogin(login);
		return add;
	}
	

	private LoginDataType getRemoveRequest(String login)throws Exception{
		String cert=X509Utils.getStringFromPEMFile("src/test/resources/demo-user.pem");
		LoginDataType add=LoginDataType.Factory.newInstance();
		add.setGcID("foo");
		add.setXlogin(login);
		add.setToken(cert);
		return add;
	}
	
	protected LoginDataType getAddRequest(int id)throws Exception{
		LoginDataType add=LoginDataType.Factory.newInstance();
		add.setGcID("foo"+id);
		add.setRole("user"+id);
		add.setXlogin("login"+id);
		add.setProjects("projects"+id);
		add.setToken(X509Utils.getStringFromPEMFile("src/test/resources/demo-user.pem"));
		return add;
	}


	private LoginDataType doQuery(String gcid)throws Exception{
		return xuudb.checkToken(gcid, X509Utils.getStringFromPEMFile("src/test/resources/demo-user.pem"));
	}

}
