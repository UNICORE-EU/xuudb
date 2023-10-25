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
 ********************************************************************************/

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
