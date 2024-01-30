package de.fzj.unicore.xuudb.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import eu.unicore.security.canl.AuthnAndTrustProperties;
import eu.unicore.security.canl.CredentialProperties;
import eu.unicore.util.configuration.ConfigurationException;
import eu.unicore.util.jetty.HttpServerProperties;
import eu.unicore.util.jetty.JettyServerBase;

/**
 *
 * @author schuller
 */
public class JettyServer extends JettyServerBase {

	private final CXFNonSpringServlet wsServlet=new CXFNonSpringServlet();
	private final CXFNonSpringServlet restServlet=new CXFNonSpringServlet();

	private final AuthnAndTrustProperties securityCfg;

	public JettyServer(URL listenUrl, AuthnAndTrustProperties securityCfg, 
			HttpServerProperties jettyCfg) throws Exception {
		super(listenUrl, securityCfg, jettyCfg);
		this.securityCfg = securityCfg;
		initServer();
	}

	@Override
	protected void initServer() throws ConfigurationException{
		super.initServer();
		if(securityCfg!=null) {
			CredentialProperties cProps = securityCfg.getCredentialProperties(); 
			if(cProps!=null && cProps.isDynamicalReloadEnabled()) {
				String path = cProps.getValue(CredentialProperties.PROP_LOCATION);
				try{
					FileWatcher fw = new FileWatcher(new File(path), () -> {
						securityCfg.reloadCredential();
						reloadCredential();
					});
					fw.schedule(10, TimeUnit.SECONDS);	
				}catch(FileNotFoundException fe) {
					throw new ConfigurationException("", fe);
				}
			}
		}
	}

	@Override
	protected Handler createRootHandler() throws ConfigurationException {
		ServletContextHandler root = new ServletContextHandler(getServer(), "/", ServletContextHandler.SESSIONS);
		ServletHolder sh=new ServletHolder(wsServlet);
		root.addServlet(sh, "/*");
		ServletHolder sh2=new ServletHolder(restServlet);
		root.addServlet(sh2, "/rest/*");
		return root;
	}
	
	public CXFNonSpringServlet getWSServlet(){
		return wsServlet;
	}
	
	
	public CXFNonSpringServlet getRESTServlet(){
		return restServlet;
	}
	
}
