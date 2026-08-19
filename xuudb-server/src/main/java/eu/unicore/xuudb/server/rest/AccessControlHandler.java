package eu.unicore.xuudb.server.rest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.logging.log4j.Logger;

import eu.emi.security.authn.x509.impl.X500NameUtils;
import eu.unicore.security.Client;
import eu.unicore.security.SecurityException;
import eu.unicore.xuudb.Log;
import eu.unicore.xuudb.server.FileWatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;

/**
 * Limit access to the API via an ACL
 *
 * @author schuller
 */
public class AccessControlHandler implements ContainerRequestFilter {

	private static final Logger logger=Log.getLogger(Log.XUUDB_SERVER, AccessControlHandler.class);

	private final File aclFile;
	private final FileWatcher watchDog;
	private final Set<String>acceptedDNs = new HashSet<>();

	public AccessControlHandler(File aclFile)throws IOException{
		this.aclFile=aclFile;
		if(!aclFile.exists()){
			throw new FileNotFoundException("ACL file <"+aclFile.getPath()+"> not found!");
		}
		else{
			readACL();
			watchDog = new FileWatcher(aclFile, ()->readACL());
			watchDog.schedule(5, TimeUnit.SECONDS);
			logger.info("Using ACL file {}", aclFile);
		}
	}

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		Message message = PhaseInterceptorChain.getCurrentMessage();
		final OperationResourceInfo ori = message.getExchange().get(OperationResourceInfo.class);
		String action = ori.getHttpMethod();
		if(!"GET".equalsIgnoreCase(action)) {
			try{
				String userName = checkAccess(getDN(message));
				logger.info("Admin access from <{}>", userName );
			}
			catch(Exception ex){
				throw new WebApplicationException(ex, HttpStatus.SC_FORBIDDEN);
			}
		}
	}

	private String getDN(Message message) throws Exception {
		String userName = Client.ANONYMOUS_CLIENT_DN;
		X509Certificate[] certPath = getSSLCertPath(message);
		if (certPath != null){
			X509Certificate userCert = certPath[0];
			userName = userCert.getSubjectX500Principal().getName();
		}
		return userName ;
	}

	private X509Certificate[] getSSLCertPath(Message message) {
		HttpServletRequest req =(HttpServletRequest)message.get(AbstractHTTPDestination.HTTP_REQUEST);
		return (X509Certificate[])req.getAttribute("jakarta.servlet.request.X509Certificate");
	}

	private String checkAccess(String userName)throws Exception {
		synchronized (acceptedDNs) {
			if(!acceptedDNs.contains(X500NameUtils.getComparableForm(userName))){
				String msg = "Admin access for <" + userName + "> denied! Please check the XUUDB's ACL file.";
				logger.debug(msg);
				throw new SecurityException(msg);
			}
			return userName;
		}
	}

	private void readACL(){
		try {
			List<String> lines = FileUtils.readLines(aclFile, "UTF-8");
			List<String> newDNs = new ArrayList<>();
			for(String line: lines) {
				line = line.trim();
				if(line.startsWith("#") || line.length()==0)continue;
				try{
					String canonical = X500NameUtils.getComparableForm(line);
					newDNs.add(canonical);
					logger.info("Allowing admin access for <{}>", line);
				}catch(Exception ex){
					logger.warn("Invalid entry <{}>", line, ex);
				}
			}
			synchronized (acceptedDNs) {
				acceptedDNs.clear();
				acceptedDNs.addAll(newDNs);
			}
		}catch(Exception ex){
			logger.fatal("ACL file read error!",ex);
		}
	}
}