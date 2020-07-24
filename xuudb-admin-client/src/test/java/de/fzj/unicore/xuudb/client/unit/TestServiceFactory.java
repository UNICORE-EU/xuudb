package de.fzj.unicore.xuudb.client.unit;

import static eu.unicore.security.canl.TrustedIssuersProperties.PROP_KS_PASSWORD;
import static eu.unicore.security.canl.TrustedIssuersProperties.PROP_KS_PATH;
import static eu.unicore.security.canl.TrustedIssuersProperties.PROP_KS_TYPE;
import static eu.unicore.security.canl.TrustedIssuersProperties.PROP_TYPE;
import static eu.unicore.security.canl.TruststoreProperties.DEFAULT_PREFIX;

import java.util.Properties;

import junit.framework.TestCase;
import de.fzj.unicore.xuudb.AbstractConfiguration;
import de.fzj.unicore.xuudb.client.ClientConfiguration;
import de.fzj.unicore.xuudb.client.ServiceFactory;
import de.fzj.unicore.xuudb.interfaces.IAdmin;
import de.fzj.unicore.xuudb.interfaces.IDAPAdmin;
import de.fzj.unicore.xuudb.interfaces.IPublic;
import eu.unicore.security.canl.CredentialProperties;
import eu.unicore.util.httpclient.ClientProperties;

public class TestServiceFactory extends TestCase {
	private static final String P = AbstractConfiguration.PROP_PREFIX;
	
	public void testServiceFactory() {
		Properties newone = new Properties();
		newone.setProperty(P + AbstractConfiguration.PROP_ADDRESS, "http://localhost:9999");
		
		newone.setProperty(P + CredentialProperties.DEFAULT_PREFIX + 
				CredentialProperties.PROP_LOCATION, "src/test/resources/xuudb.p12");
		newone.setProperty(P + CredentialProperties.DEFAULT_PREFIX + 
				CredentialProperties.PROP_FORMAT, "PKCS12");
		newone.setProperty(P + CredentialProperties.DEFAULT_PREFIX + 
				CredentialProperties.PROP_PASSWORD, "the!xuudb");
		newone.setProperty(P + DEFAULT_PREFIX + PROP_TYPE, "keystore");
		newone.setProperty(P + DEFAULT_PREFIX + PROP_KS_PATH, "src/test/resources/truststore.jks");
		newone.setProperty(P + DEFAULT_PREFIX + PROP_KS_TYPE, "JKS");
		newone.setProperty(P + DEFAULT_PREFIX + PROP_KS_PASSWORD, "unicore");
		newone.setProperty(P + ClientProperties.DEFAULT_PREFIX + 
				ClientProperties.PROP_SSL_ENABLED, "true");
		
		ClientConfiguration config = new ClientConfiguration(newone);
		ServiceFactory serviceFactory;
		try
		{
			serviceFactory = new ServiceFactory(config);
		} catch (Exception e)
		{
			e.printStackTrace();
			fail("Error creating ServiceFactory " + e);
			return;
		}

		IAdmin admin = null;
		admin = serviceFactory.getAdminEndpoint();
		assertNotNull(admin);
		IPublic pub=null;
		pub=serviceFactory.getPublicEndpoint();
		assertNotNull(pub);
		
		
		IDAPAdmin dap=null;
		dap=serviceFactory.getDapAdminEndpoint();
		assertNotNull(dap);
	}
}
