package eu.unicore.xuudb.client.unit;

import static eu.unicore.security.canl.TrustedIssuersProperties.PROP_KS_PASSWORD;
import static eu.unicore.security.canl.TrustedIssuersProperties.PROP_KS_PATH;
import static eu.unicore.security.canl.TrustedIssuersProperties.PROP_KS_TYPE;
import static eu.unicore.security.canl.TrustedIssuersProperties.PROP_TYPE;
import static eu.unicore.security.canl.TruststoreProperties.DEFAULT_PREFIX;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Properties;

import org.junit.jupiter.api.Test;

import eu.unicore.security.canl.CredentialProperties;
import eu.unicore.util.httpclient.ClientProperties;
import eu.unicore.xuudb.CommonConfiguration;
import eu.unicore.xuudb.client.ClientConfiguration;
import eu.unicore.xuudb.client.ServiceFactory;
import eu.unicore.xuudb.interfaces.IAdmin;
import eu.unicore.xuudb.interfaces.IDAPAdmin;
import eu.unicore.xuudb.interfaces.IPublic;

public class TestServiceFactory {

	private static final String P = CommonConfiguration.PROP_PREFIX;

	@Test
	public void testServiceFactory() throws Exception {
		Properties newone = new Properties();
		newone.setProperty(P + CommonConfiguration.PROP_ADDRESS, "http://localhost:9999");
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
		serviceFactory = new ServiceFactory(config);

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
