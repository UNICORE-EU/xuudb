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
 

package de.fzj.unicore.xuudb.server;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.service.invoker.BeanInvoker;
import org.apache.logging.log4j.Logger;

import de.fzj.unicore.xuudb.CommonConfiguration;
import de.fzj.unicore.xuudb.Log;
import de.fzj.unicore.xuudb.interfaces.IAdmin;
import de.fzj.unicore.xuudb.interfaces.IDAPAdmin;
import de.fzj.unicore.xuudb.interfaces.IDynamicAttributesPublic;
import de.fzj.unicore.xuudb.interfaces.IPublic;
import de.fzj.unicore.xuudb.server.db.IStorage;
import de.fzj.unicore.xuudb.server.db.StorageFactory;
import de.fzj.unicore.xuudb.server.dynamic.DAPConfiguration;
import eu.unicore.security.canl.AuthnAndTrustProperties;
import eu.unicore.security.canl.CredentialProperties;
import eu.unicore.security.canl.TruststoreProperties;
import eu.unicore.security.wsutil.cxf.XmlBeansDataBinding;
import eu.unicore.util.jetty.HttpServerProperties;

public class HttpsServer implements IShutdownable {

	private static final Logger logger = Log.getLogger(Log.XUUDB_SERVER, HttpsServer.class);
	
	private JettyServer server;
	private ServerConfiguration config;
	private ScheduledExecutorService executor;
	
	private IAdmin adminImpl;
	private IDAPAdmin dapAdminImpl;
	private IPublic publicImpl;
	private IDynamicAttributesPublic dapPublicImpl;

	/**
	 * creates a XUUDB Http(s)Server like defined in the Properties prop
	 * @param p
	 * @throws Exception
	 */
	public HttpsServer(Properties p, ScheduledExecutorService executor) throws Exception {
		config = new ServerConfiguration(p);
		this.executor = executor;
	}
	
	/**
	 * creates a XUUDB Http(s)Server like defined in the PropertiesFile cf
	 * @param cf
	 * @throws Exception
	 */
	public HttpsServer(String cf, ScheduledExecutorService executor) throws Exception {
		this.executor = executor;
		try {
			config = new ServerConfiguration(new File(cf));
		} catch (IOException e) {
			throw new Exception("Cannot get configuration from " + cf + 
				"\n*** Create a default configuration with tha 'init' admin command", e);
		}
		String v = CommonConfiguration.class.getPackage().getImplementationVersion();
		
		logger.info("");
		logger.info("*********************************************");
		logger.info("*    UNICORE XUUDB USER ATTRIBUTES SERVICE");
		if(v!=null){
			logger.info("*    Version "+v);
		}
		logger.info("*    https://www.unicore.eu");
		logger.info("**********************************************");
	}	
	
	public void start() throws Exception {
		// Start the HTTP/HTTPS server		
		HttpServerProperties jettyProperties = new HttpServerProperties(config.getProperties(), 
				CommonConfiguration.PROP_PREFIX+HttpServerProperties.DEFAULT_PREFIX);
		
		String address = config.getValue(CommonConfiguration.PROP_ADDRESS);
		URL serverUrl = new URL(address);

		AuthnAndTrustProperties secProperties = null;
		
		if (serverUrl.getProtocol().equals("https")) {
			logger.info("Setting up Jetty server in SSL mode.");
			secProperties = new AuthnAndTrustProperties(config.getProperties(), 
					CommonConfiguration.PROP_PREFIX + TruststoreProperties.DEFAULT_PREFIX,
					CommonConfiguration.PROP_PREFIX + CredentialProperties.DEFAULT_PREFIX);
		} else {
			logger.info("Setting up Jetty server in non-SSL mode.");
		}
		
		server = new JettyServer(serverUrl, secProperties, jettyProperties);
		server.start();

		//register with shutdownhook
		ShutdownHook hook = new ShutdownHook();
		hook.register(this);
		
		IStorage storage = StorageFactory.getDatabase(config, hook);

		String acl=config.getValue(ServerConfiguration.PROP_ACL_FILE);
		ACLHandler aclHandler = acl!=null ?
			    new ACLHandler(new File(acl), executor):
				new ACLHandler(executor);
		
	
		createPublicService(aclHandler, storage);
	
		createAdminService(aclHandler,storage);		
		
		File dapConfigFile = config.getFileValue(ServerConfiguration.PROP_DAP_FILE, false);
		DAPConfiguration dapConfiguration = new DAPConfiguration(dapConfigFile, storage.getPoolStorage(), 
				executor);
		
		createDAPPublicService(dapConfiguration,aclHandler);
		
		createDAPAdminService(dapConfiguration,aclHandler,storage);
				
	}
	
	protected void createPublicService(ACLHandler aclHandler, IStorage storage)throws Exception{
		JaxWsServerFactoryBean factory=getFactory();
		factory.setAddress(IPublic.SERVICE_NAME);
		factory.setServiceClass(IPublic.class);
		publicImpl = new PublicImpl(config, storage.getClassicStorage());
		factory.setInvoker(new BeanInvoker(publicImpl));
		if (config.getBooleanValue(ServerConfiguration.PROP_PROTECT_ALL)){
			factory.getInInterceptors().add(aclHandler);
		}
		factory.create();	
	}
	
	protected void createAdminService(ACLHandler aclHandler, IStorage storage)throws Exception{
		JaxWsServerFactoryBean factory=getFactory();
		factory.setAddress(IAdmin.SERVICE_NAME);
		factory.setServiceClass(IAdmin.class);
		adminImpl = new AdminImpl(config, storage.getClassicStorage());
		factory.setInvoker(new BeanInvoker(adminImpl));
		factory.getInInterceptors().add(aclHandler);
		factory.create();
	}

	protected void createDAPPublicService(DAPConfiguration dapConfiguration, ACLHandler aclHandler)throws Exception{
		JaxWsServerFactoryBean factory=getFactory();
		factory.setAddress(IDynamicAttributesPublic.SERVICE_NAME);
		factory.setServiceClass(IDynamicAttributesPublic.class);
		dapPublicImpl = new DAPPublicImpl(dapConfiguration);
		factory.setInvoker(new BeanInvoker(dapPublicImpl));
		if (config.getBooleanValue(ServerConfiguration.PROP_PROTECT_ALL)){
			factory.getInInterceptors().add(aclHandler);
		}
		factory.create();	
	}

	protected void createDAPAdminService(DAPConfiguration dapConfiguration, ACLHandler aclHandler, IStorage storage)throws Exception{
		JaxWsServerFactoryBean factory=getFactory();
		factory.setAddress(IDAPAdmin.SERVICE_NAME);
		factory.setServiceClass(IDAPAdmin.class);
		dapAdminImpl = new DAPAdminImpl(storage.getPoolStorage(), dapConfiguration);
		factory.setInvoker(new BeanInvoker(dapAdminImpl));
		factory.getInInterceptors().add(aclHandler);
		factory.create();
	}
	
	private JaxWsServerFactoryBean getFactory(){
		JaxWsServerFactoryBean factory=new JaxWsServerFactoryBean();
		factory.setDataBinding(new XmlBeansDataBinding());
		factory.setBus(server.getServlet().getBus());
		return factory;
	}
	
	@Override
	public void shutdown() throws Exception {
		server.stop();
	}

	@Override
	public String getNameOfService() {
		return "XUUDB Server";
	}

	/**
	 * @return the adminImpl
	 */
	public IAdmin getAdminImpl()
	{
		return adminImpl;
	}

	/**
	 * @return the dapAdminImpl
	 */
	public IDAPAdmin getDapAdminImpl()
	{
		return dapAdminImpl;
	}

	/**
	 * @return the publicImpl
	 */
	public IPublic getPublicImpl()
	{
		return publicImpl;
	}

	/**
	 * @return the dapPublicImpl
	 */
	public IDynamicAttributesPublic getDapPublicImpl()
	{
		return dapPublicImpl;
	}
}
