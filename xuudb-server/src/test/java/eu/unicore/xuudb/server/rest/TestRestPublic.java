package eu.unicore.xuudb.server.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Test;

import eu.unicore.xuudb.server.HttpsServer;

public class TestRestPublic {

	@Test
	public void testSetup() throws Exception {
		HttpsServer server = null;
		try {
			File dir = new File("target/data");
			FileUtils.deleteDirectory(dir);
			Properties p = new Properties();
			p.load(new FileInputStream(
					"src/test/resources/xuudb_server.conf"));
			server = new HttpsServer(p);
			server.start();

			URL u = new URL("http://localhost:34463/rest/xuudb/info");
			HttpURLConnection conn = (HttpURLConnection)u.openConnection();
			try(InputStream in = conn.getInputStream()){
				System.out.println(new JSONObject(IOUtils.toString(in, "UTF-8"))
						.toString(2));
			}
		} finally {
			try {
				server.shutdown();
			} catch (Exception e) { /* ignored */
			}
		}
	}
}
