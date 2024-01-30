package eu.unicore.xuudb.server;

import java.io.File;
import java.net.URL;
import java.util.Properties;

import org.apache.cxf.jaxrs.JAXRSInvoker;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.utils.ResourceUtils;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.service.invoker.BeanInvoker;
import org.apache.logging.log4j.Logger;

import eu.unicore.security.canl.AuthnAndTrustProperties;
import eu.unicore.security.canl.CredentialProperties;
import eu.unicore.security.canl.TruststoreProperties;
import eu.unicore.security.wsutil.cxf.XmlBeansDataBinding;
import eu.unicore.util.jetty.HttpServerProperties;
import eu.unicore.xuudb.CommonConfiguration;
import eu.unicore.xuudb.Log;
import eu.unicore.xuudb.interfaces.IAdmin;
import eu.unicore.xuudb.interfaces.IDAPAdmin;
import eu.unicore.xuudb.interfaces.IDynamicAttributesPublic;
import eu.unicore.xuudb.interfaces.IPublic;
import eu.unicore.xuudb.server.db.IStorage;
import eu.unicore.xuudb.server.db.StorageFactory;
import eu.unicore.xuudb.server.dynamic.DAPConfiguration;
import eu.unicore.xuudb.server.rest.RestDAPQuery;
import eu.unicore.xuudb.server.rest.RestXUUDB;

public class HttpsServer implements IShutdownable {

	private static final Logger logger = Log.getLogger(Log.XUUDB_SERVER, HttpsServer.class);
	
	private JettyServer server;
	private final ServerConfiguration config;
	private IAdmin adminImpl;
	private IDAPAdmin dapAdminImpl;

	private IPublic publicImpl;
	private IDynamicAttributesPublic dapPublicImpl;

	private RestXUUDB publicRESTImpl;
	private RestDAPQuery publicRESTDAP;
	
	/**
	 * creates a XUUDB Http(s)Server configured using the given properties
	 *
	 * @param p
	 * @throws Exception
	 */
	public HttpsServer(Properties p) throws Exception {
		config = new ServerConfiguration(p);
	}
	
	/**
	 * creates a XUUDB Http(s)Server configured using the given properties file
	 *
	 * @param cf
	 * @throws Exception
	 */
	public HttpsServer(String cf) throws Exception {
		config = new ServerConfiguration(new File(cf));
		logger.info("*********************************************");
		logger.info("*    UNICORE XUUDB USER ATTRIBUTES SERVICE");
		logger.info("*    Version {}", CommonConfiguration.class.getPackage().getImplementationVersion());
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

		ShutdownHook hook = new ShutdownHook();
		hook.register(this);

		IStorage storage = StorageFactory.getDatabase(config, hook);

		String acl=config.getValue(ServerConfiguration.PROP_ACL_FILE);
		ACLHandler aclHandler = new ACLHandler(new File(acl));

		createPublicService(aclHandler, storage);
		createAdminService(aclHandler,storage);		
		createRESTPublic(storage, aclHandler);

		File dapConfigFile = config.getFileValue(ServerConfiguration.PROP_DAP_FILE, false);
		DAPConfiguration dapConfiguration = new DAPConfiguration(dapConfigFile, storage.getPoolStorage());
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
		factory.setBus(server.getWSServlet().getBus());
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
	
	protected void createRESTPublic(IStorage storage, ACLHandler aclHandler)throws Exception{
		JAXRSServerFactoryBean factory = ResourceUtils.createApplication(
				new RestXUUDB.XUUDBApplication(), true, false, false,
				server.getRESTServlet().getBus());
		factory.setAddress("/xuudb");
		if (config.getBooleanValue(ServerConfiguration.PROP_PROTECT_ALL)){
			factory.setProvider(aclHandler);
		}
		publicRESTImpl = new RestXUUDB();
		publicRESTImpl.setStorage(storage.getRESTClassicStorage());
		factory.setInvoker(new 
				JAXRSInvoker() {
				@Override
			public Object getServiceObject(Exchange e) {
					return publicRESTImpl;
				}
		});
		factory.create();	
	}

	protected void createRESTDAPPublic(IStorage storage, ACLHandler aclHandler)throws Exception{
		JAXRSServerFactoryBean factory = ResourceUtils.createApplication(
				new RestDAPQuery.DAPApplication(), true, false, false,
				server.getRESTServlet().getBus());
		factory.setAddress("/dap");
		if (config.getBooleanValue(ServerConfiguration.PROP_PROTECT_ALL)){
			factory.setProvider(aclHandler);
		}
		publicRESTDAP = new RestDAPQuery();
		publicRESTDAP.setStorage(storage.getRESTClassicStorage());
		factory.setInvoker(new 
				JAXRSInvoker() {
				@Override
			public Object getServiceObject(Exchange e) {
					return publicRESTDAP;
				}
		});
		factory.create();	
	}

}
