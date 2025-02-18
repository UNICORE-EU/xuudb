package eu.unicore.xuudb.server.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import eu.unicore.xuudb.server.HttpsServer;

public class TestRestDAP {

	@Test
	public void testSetup() throws Exception {
		HttpsServer server = setup();
		try {
			URL u = new URL("http://localhost:34463/rest/dap/info");
			HttpURLConnection conn = (HttpURLConnection)u.openConnection();
			try(InputStream in = conn.getInputStream()){
				System.out.println(new JSONObject(IOUtils.toString(in, "UTF-8"))
						.toString(2));
			}
		} finally {
			try {
				server.shutdown();
			} catch (Exception e) {}
		}
	}

	@Test
	public void testQuery() throws Exception {
		HttpsServer server = setup();
		try {
			URIBuilder ub = new URIBuilder();
			ub.setScheme("http");
			ub.setHost("localhost").setPort(34463);
			ub.setPath("/rest/dap/query");
			ub.addParameter("dn", "CN=demouser");
			ub.addParameter("role", "user");
			ub.addParameter("vo", "/biology/dynamic/foo");
			URL u = ub.build().toURL();
			HttpURLConnection conn = (HttpURLConnection)u.openConnection();
			try(InputStream in = conn.getInputStream()){
				JSONObject res = new JSONObject(IOUtils.toString(in, "UTF-8"));
				System.out.println(res.toString(2));
				assertEquals("uid1", res.getString("xlogin"));
				assertEquals("grid-dyn1", res.getString("group"));
				
			}
		} finally {
			try {
				server.shutdown();
			} catch (Exception e) { /* ignored */
			}
		}
	}

	@Test
	public void testQueryError() throws Exception {
		HttpsServer server = setup();
		try {
			URL u = new URL("http://localhost:34463/rest/dap/query");
			// no DN/role- expect 400 error
			Exception e = assertThrows(Exception.class, ()->{
				HttpURLConnection conn = (HttpURLConnection)u.openConnection();
				try(InputStream in = conn.getInputStream()){
					new JSONObject(IOUtils.toString(in, "UTF-8"));
				}
			});
			assertTrue(e.toString().contains("400"));
		} finally {
			try {
				server.shutdown();
			} catch (Exception e) { /* ignored */
			}
		}
	}

	private HttpsServer setup() throws Exception {
		File dir = new File("target/data");
		FileUtils.deleteDirectory(dir);
		Properties p = new Properties();
		p.load(new FileInputStream(
				"src/test/resources/xuudb_server.conf"));
		HttpsServer server = new HttpsServer(p);
		server.start();
		return server;
	}
}
