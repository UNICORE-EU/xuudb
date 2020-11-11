/*********************************************************************************
 * Copyright (c) 2006 Forschungszentrum Juelich GmbH 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * (1) Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the disclaimer at the end. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 * 
 * (2) Neither the name of Forschungszentrum Juelich GmbH nor the names of its 
 * contributors may be used to endorse or promote products derived from this 
 * software without specific prior written permission.
 * 
 * DISCLAIMER
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *********************************************************************************/


package de.fzj.unicore.xuudb.client;

import java.net.URL;

import org.apache.logging.log4j.Logger;

import de.fzj.unicore.xuudb.AbstractConfiguration;
import de.fzj.unicore.xuudb.Log;
import de.fzj.unicore.xuudb.client.wsapi.IAdminExtInterface;
import de.fzj.unicore.xuudb.client.wsapi.IDAPAdminExtInterface;
import de.fzj.unicore.xuudb.client.wsapi.IDAPPublicExtInterface;
import de.fzj.unicore.xuudb.client.wsapi.IPublicExtInterface;
import de.fzj.unicore.xuudb.client.wsapi.impl.IAdminExtImpl;
import de.fzj.unicore.xuudb.client.wsapi.impl.IDAPAdminExtImpl;
import de.fzj.unicore.xuudb.client.wsapi.impl.IDAPPublicExtImpl;
import de.fzj.unicore.xuudb.client.wsapi.impl.IPublicExtImpl;
import de.fzj.unicore.xuudb.interfaces.IAdmin;
import de.fzj.unicore.xuudb.interfaces.IDAPAdmin;
import de.fzj.unicore.xuudb.interfaces.IDynamicAttributesPublic;
import de.fzj.unicore.xuudb.interfaces.IPublic;
import eu.unicore.security.canl.CredentialProperties;
import eu.unicore.security.canl.TruststoreProperties;
import eu.unicore.security.wsutil.client.WSClientFactory;
import eu.unicore.util.httpclient.ClientProperties;
import eu.unicore.util.httpclient.IClientConfiguration;

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
				AbstractConfiguration.PROP_PREFIX + TruststoreProperties.DEFAULT_PREFIX,
				AbstractConfiguration.PROP_PREFIX + CredentialProperties.DEFAULT_PREFIX,
				AbstractConfiguration.PROP_PREFIX + ClientProperties.DEFAULT_PREFIX);
		WSClientFactory factory = new WSClientFactory(secProperties);
		
		URL adminEndpointURL = new URL(config.getValue(AbstractConfiguration.PROP_ADDRESS) + 
			"/" + IAdmin.SERVICE_NAME);
		URL publicEndpointURL = new URL(config.getValue(AbstractConfiguration.PROP_ADDRESS) + 
			"/" + IPublic.SERVICE_NAME);

		URL dapAdminEndpointURL = new URL(config.getValue(AbstractConfiguration.PROP_ADDRESS) + 
				"/" + IDAPAdmin.SERVICE_NAME);
		
		URL dapPublicEndpointURL = new URL(config.getValue(AbstractConfiguration.PROP_ADDRESS) + 
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
