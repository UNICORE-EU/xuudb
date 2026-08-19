package eu.unicore.xuudb.server.rest;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.net.URIBuilder;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import eu.unicore.util.httpclient.DefaultClientConfiguration;
import eu.unicore.util.httpclient.HttpUtils;
import eu.unicore.xuudb.server.HttpsServer;

public class TestRestPublic {

	@Test
	public void testSetup() throws Exception {
		HttpsServer server = setup();
		try {
			URL u = new URL("http://localhost:34463/rest/xuudb/info");
			var get = new HttpGet(u.toString());
			try(var hc = client(u);
				var res = hc.executeOpen(null, get, HttpClientContext.create()))
			{
				System.out.println(new JSONObject(EntityUtils.toString(res.getEntity(), "UTF-8"))
						.toString(2));
			} 
		} finally {
			server.shutdown();
		}
	}

	@Test
	public void testQuery() throws Exception {
		HttpsServer server = setup();
		try {
			URIBuilder ub = new URIBuilder();
			ub.setScheme("http");
			ub.setHost("localhost").setPort(34463);
			ub.setPath("/rest/xuudb/query/test");
			ub.addParameter("dn", "CN=demouser");
			URL u = ub.build().toURL();
			var get = new HttpGet(u.toString());
			try(var hc = client(u);
				var res = hc.executeOpen(null, get, HttpClientContext.create()))
			{
				System.out.println(new JSONObject(EntityUtils.toString(res.getEntity(), "UTF-8"))
						.toString(2));
			} 
		}finally {
			server.shutdown();
		}
	}

	@Test
	public void testQueryError() throws Exception {
		HttpsServer server = setup();
		try {
			URL u = new URL("http://localhost:34463/rest/xuudb/query/test");
			// no DN - expect 400 error
			var get = new HttpGet(u.toString());
			try(var hc = client(u); 
				var res = hc.executeOpen(null, get, HttpClientContext.create()))
			{
				System.out.println(new JSONObject(EntityUtils.toString(res.getEntity(), "UTF-8")).toString(2));
				assertTrue(res.getCode()==400);
			} 
		}finally {
			server.shutdown();
		}
	}
	
	@Test
	public void testPUTACLCheck() throws Exception {
		HttpsServer server = setup();
		try {
			URL u = new URL("http://localhost:34463/rest/xuudb/update");
			// no auth - expect 403 error
			var put = new HttpPut(u.toString());
			put.setHeader("Content-Type", "application/json");
			JSONObject j = new JSONObject();
			put.setEntity(new StringEntity(j.toString()));
			try(var hc = client(u); 
				var res = hc.executeOpen(null, put, HttpClientContext.create()))
			{
				System.out.println("Got: " + res.getReasonPhrase()+" "
						+ EntityUtils.toString(res.getEntity(),"UTF-8"));
				assertTrue(res.getCode()==403);
			} 
		} finally {
			server.shutdown();
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

	private CloseableHttpClient client(URL url) throws Exception {
		return HttpUtils.client(url.toString(), new DefaultClientConfiguration());
	}

}
