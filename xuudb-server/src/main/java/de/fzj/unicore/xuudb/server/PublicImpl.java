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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.logging.log4j.Logger;

import de.fzJuelich.unicore.xuudb.CheckCertChainResponseDocument;
import de.fzJuelich.unicore.xuudb.CheckCertificateChainDocument;
import de.fzJuelich.unicore.xuudb.CheckCertificateDocument;
import de.fzJuelich.unicore.xuudb.CheckCertificateResponseDocument;
import de.fzJuelich.unicore.xuudb.CheckDNDocument;
import de.fzJuelich.unicore.xuudb.CheckDNResponseDocument;
import de.fzJuelich.unicore.xuudb.LoginDataType;
import de.fzj.unicore.xuudb.CommonConfiguration;
import de.fzj.unicore.xuudb.Log;
import de.fzj.unicore.xuudb.X509Utils;
import de.fzj.unicore.xuudb.interfaces.IPublic;
import de.fzj.unicore.xuudb.server.db.IClassicStorage;

public class PublicImpl implements IPublic {

	private static final Logger logger=Log.getLogger(Log.XUUDB_SERVER, PublicImpl.class);
	
	private IClassicStorage db;
	
	public PublicImpl(CommonConfiguration co, IClassicStorage backend) throws Exception {
		this.db = backend;
	}

	public CheckCertificateResponseDocument checkCertificate(CheckCertificateDocument xml) {
		try{
			CheckCertificateResponseDocument ret =  CheckCertificateResponseDocument.Factory.newInstance();
			String certinpem = xml.getCheckCertificate().getCertInPEM();
			String gcid = xml.getCheckCertificate().getGcID();
			LoginDataType data = db.checkToken(gcid, certinpem);
			if(data==null){
				data=LoginDataType.Factory.newInstance();
			}
			ret.setCheckCertificateResponse(data);
			return ret;
		} catch (IllegalArgumentException e) {
			logger.warn("Got wrong/unparsable argument: " + xml.xmlText() + 
					"\nError is: " + e.toString());
			throw e;
		} catch (PersistenceException e) {
			Log.logException("Error in database code", e, logger);
			String msg = Log.createFaultMessage("Internal server error, database related", e);
			throw new RuntimeException(msg);
		}
	}

	public CheckCertChainResponseDocument checkCertificateChain(CheckCertificateChainDocument xml) {
		try{
			CheckCertChainResponseDocument ret =  CheckCertChainResponseDocument.Factory.newInstance();
			String gcid = xml.getCheckCertificateChain().getGcID();
			String base64 = xml.getCheckCertificateChain().getEncodedChain();

			byte[] cpb = Base64.getDecoder().decode(base64.getBytes());
			InputStream is = new ByteArrayInputStream(cpb);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			CertPath cp=cf.generateCertPath(is);                    
			LoginDataType data=null;
			Certificate o = cp.getCertificates().get(0);
			data = db.checkToken(gcid, X509Utils.getPEMStringFromX509((X509Certificate)o));
			ret.setCheckCertChainResponse(data);
			return ret;
		} catch (IllegalArgumentException e) {
			logger.warn("Got wrong/unparsable argument: " + xml.xmlText() + 
					"\nError is: " + e.toString());
			throw e;
		} catch (PersistenceException e) {
			Log.logException("Error in database code", e, logger);
			String msg = Log.createFaultMessage("Internal server error, database related", e);
			throw new RuntimeException(msg);
		} catch (Exception e)
		{
			String msg = "Got wrong/unparsable argument: " + xml.xmlText() + 
					"\nError is: " + e.toString();
			logger.warn(msg);
			throw new IllegalArgumentException(msg);
		}
	}

	public CheckDNResponseDocument checkDN(CheckDNDocument xml) {
		try{
			CheckDNResponseDocument ret =  CheckDNResponseDocument.Factory.newInstance();
			String gcid = xml.getCheckDN().getGcID();
			String dn = xml.getCheckDN().getDistinguishedName();
			LoginDataType data = db.checkDN(gcid, dn);
			ret.setCheckDNResponse(data);
			return ret;
		} catch (IllegalArgumentException e) {
			logger.warn("Got wrong/unparsable argument: " + xml.xmlText() + 
					"\nError is: " + e.toString());
			throw e;
		} catch (PersistenceException e) {
			Log.logException("Error in database code", e, logger);
			String msg = Log.createFaultMessage("Internal server error, database related", e);
			throw new RuntimeException(msg);
		}
	}
}
