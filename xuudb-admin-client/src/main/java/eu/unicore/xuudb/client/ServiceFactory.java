package eu.unicore.xuudb.client;

import java.net.URL;

import org.apache.logging.log4j.Logger;

import eu.unicore.security.canl.CredentialProperties;
import eu.unicore.security.canl.TruststoreProperties;
import eu.unicore.security.wsutil.client.WSClientFactory;
import eu.unicore.util.httpclient.ClientProperties;
import eu.unicore.util.httpclient.IClientConfiguration;
import eu.unicore.xuudb.CommonConfiguration;
import eu.unicore.xuudb.Log;
import eu.unicore.xuudb.client.wsapi.IAdminExtInterface;
import eu.unicore.xuudb.client.wsapi.IDAPAdminExtInterface;
import eu.unicore.xuudb.client.wsapi.IDAPPublicExtInterface;
import eu.unicore.xuudb.client.wsapi.IPublicExtInterface;
import eu.unicore.xuudb.client.wsapi.impl.IAdminExtImpl;
import eu.unicore.xuudb.client.wsapi.impl.IDAPAdminExtImpl;
import eu.unicore.xuudb.client.wsapi.impl.IDAPPublicExtImpl;
import eu.unicore.xuudb.client.wsapi.impl.IPublicExtImpl;
import eu.unicore.xuudb.interfaces.IAdmin;
import eu.unicore.xuudb.interfaces.IDAPAdmin;
import eu.unicore.xuudb.interfaces.IDynamicAttributesPublic;
import eu.unicore.xuudb.interfaces.IPublic;

public class ServiceFactory {

	private static final Logger logger = Log.getLogger(Log.XUUDB_CLIENT, ServiceFactory.class);

	private IAdmin adminEndpoint;
	private IPublic publicEndpoint;
	private IDAPAdmin dapAdminEndpoint;
	private IDynamicAttributesPublic dapPublicEndpoint;

	public IDAPAdmin getDapAdminEndpoint() {
		return dapAdminEndpoint;
	}

	public ServiceFactory(ClientConfiguration c) throws Exception {
		doCreate(c);
	}

	private void doCreate(ClientConfiguration config) throws Exception {
		logger.info("Creating ServiceFactory ... ");

		IClientConfiguration secProperties = new ClientProperties(config.getProperties(), 
				CommonConfiguration.PROP_PREFIX + TruststoreProperties.DEFAULT_PREFIX,
				CommonConfiguration.PROP_PREFIX + CredentialProperties.DEFAULT_PREFIX,
				CommonConfiguration.PROP_PREFIX + ClientProperties.DEFAULT_PREFIX);
		WSClientFactory factory = new WSClientFactory(secProperties);
		
		URL adminEndpointURL = new URL(config.getValue(CommonConfiguration.PROP_ADDRESS) + 
			"/" + IAdmin.SERVICE_NAME);
		URL publicEndpointURL = new URL(config.getValue(CommonConfiguration.PROP_ADDRESS) + 
			"/" + IPublic.SERVICE_NAME);

		URL dapAdminEndpointURL = new URL(config.getValue(CommonConfiguration.PROP_ADDRESS) + 
				"/" + IDAPAdmin.SERVICE_NAME);
		
		URL dapPublicEndpointURL = new URL(config.getValue(CommonConfiguration.PROP_ADDRESS) + 
				"/" + IDynamicAttributesPublic.SERVICE_NAME);
		

		
		logger.info("Creating XUUDB Admin Endpoint at " + adminEndpointURL.toString());
		adminEndpoint = (IAdmin) factory.createPlainWSProxy(IAdmin.class, adminEndpointURL.toString());

		logger.info("Creating XUUDB Query Endpoint at " + publicEndpointURL.toString());
		publicEndpoint = (IPublic) factory.createPlainWSProxy(IPublic.class, publicEndpointURL.toString());
		
		logger.info("Creating XUUDB DAPAdmin Endpoint at " + dapAdminEndpointURL.toString());
		dapAdminEndpoint = (IDAPAdmin) factory.createPlainWSProxy(IDAPAdmin.class, dapAdminEndpointURL.toString());
		
		logger.info("Creating XUUDB DAPPublic Endpoint at " + dapPublicEndpointURL.toString());
		dapPublicEndpoint = (IDynamicAttributesPublic) factory.createPlainWSProxy(IDynamicAttributesPublic.class, dapPublicEndpointURL.toString());
		
		logger.info("    Done");
	}

	public IAdmin getAdminEndpoint() { 
		return adminEndpoint; 
	}
	
	public IPublic getPublicEndpoint() { 
		return publicEndpoint; 
	}
	
	public IAdminExtInterface getAdminAPI() { 
		return new IAdminExtImpl(adminEndpoint); 
	}
	
	public IPublicExtInterface getPublicAPI() { 
		return new IPublicExtImpl(publicEndpoint); 
	}
	
	public IDAPAdminExtInterface getDAPAdminAPI() { 
		return new IDAPAdminExtImpl(dapAdminEndpoint); 
	}
	
	public IDAPPublicExtInterface getDAPPublicAPI() { 
		return new IDAPPublicExtImpl(dapPublicEndpoint); 
	}
	
	
	
	
	
}
