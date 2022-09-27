package de.fzj.unicore.xuudb.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.logging.log4j.Logger;

import de.fzj.unicore.xuudb.Log;
import eu.emi.security.authn.x509.impl.X500NameUtils;


/**
 * use an ACL file to limit access
 *
 * @author schuller
 */
public class ACLHandler extends AbstractSoapInterceptor {

	private static final Logger logger=Log.getLogger(Log.XUUDB_SERVER, ACLHandler.class);

	private final File aclFile;
	private final FileWatcher watchDog;
	private final Set<String>acceptedDNs = new HashSet<>();

	public ACLHandler(File aclFile, ScheduledExecutorService executor)throws IOException{
		super(Phase.PRE_INVOKE);
		this.aclFile=aclFile;
		if(!aclFile.exists()){
			throw new FileNotFoundException("ACL file <"+aclFile.getPath()+"> not found!");
		}
		else{
			logger.info("XUUDB using ACL file {}", aclFile);
			readACL();
			watchDog=new FileWatcher(aclFile, new Runnable(){
				public void run(){
					readACL();
				}
			});
			executor.schedule(watchDog, 5, TimeUnit.SECONDS);
		}
	}

	protected void readACL(){
		synchronized(acceptedDNs){
			try(BufferedReader br = new BufferedReader(new FileReader(aclFile))){
				acceptedDNs.clear();
				while(true){
					String theLine=br.readLine();
					if(theLine==null)break;
					String line=theLine.trim();
					if(line.startsWith("#"))continue;
					if(line.trim().length()>0){
						try{
							String canonical = X500NameUtils.getComparableForm(line);
							acceptedDNs.add(canonical);
							logger.info("Allowing admin access for <{}>", line);
						}catch(Exception ex){
							logger.warn("Invalid entry <{}>", line, ex);
						}
					}
				}
			}catch(Exception ex){
				logger.fatal("ACL file read error!",ex);
			}
		}
	}

	public void handleMessage(SoapMessage message) {
		String userName="anonymous";
		try{
			X509Certificate[] certPath = getSSLCertPath(message);
			if (certPath != null){
				X509Certificate userCert=certPath[0];
				userName=userCert.getSubjectX500Principal().getName();
			}
		}catch(Exception ex){
			logger.error("Can't get user name from request. No ssl?",ex);
		}
		logger.info("Admin access from {}", userName);

		try{
			checkAccess(userName);
		}catch(Exception ex){
			throw new Fault(ex);
		}
	}
	
	protected X509Certificate[] getSSLCertPath(SoapMessage message)
	{
		HttpServletRequest req =(HttpServletRequest)message.get(AbstractHTTPDestination.HTTP_REQUEST);
		return (X509Certificate[])req.getAttribute("javax.servlet.request.X509Certificate");
	}

	protected void checkAccess(String userName)throws Exception{
		synchronized (acceptedDNs) {
			if(!acceptedDNs.contains(X500NameUtils.getComparableForm(userName))){
				String msg="Admin access denied!\n\nTo allow access for this " +
						"certificate, the distinguished name \n" +userName+
						"\nneeds to be entered into the ACL file."
						+"\nPlease check the XUUDB's ACL file!\n\n" ;
				throw new Exception(msg);
			}
		}
	}

}
