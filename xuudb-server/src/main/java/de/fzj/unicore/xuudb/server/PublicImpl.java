package de.fzj.unicore.xuudb.server;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

import de.fzJuelich.unicore.xuudb.CheckCertChainResponseDocument;
import de.fzJuelich.unicore.xuudb.CheckCertificateChainDocument;
import de.fzJuelich.unicore.xuudb.CheckCertificateDocument;
import de.fzJuelich.unicore.xuudb.CheckCertificateResponseDocument;
import de.fzJuelich.unicore.xuudb.CheckDNDocument;
import de.fzJuelich.unicore.xuudb.CheckDNResponseDocument;
import de.fzJuelich.unicore.xuudb.LoginDataType;
import de.fzj.unicore.xuudb.CommonConfiguration;
import de.fzj.unicore.xuudb.X509Utils;
import de.fzj.unicore.xuudb.interfaces.IPublic;
import de.fzj.unicore.xuudb.server.db.IClassicStorage;

public class PublicImpl implements IPublic {

	private final IClassicStorage db;

	public PublicImpl(CommonConfiguration co, IClassicStorage backend) throws Exception {
		this.db = backend;
	}

	public CheckCertificateResponseDocument checkCertificate(CheckCertificateDocument xml) {
		CheckCertificateResponseDocument ret =  CheckCertificateResponseDocument.Factory.newInstance();
		String certinpem = xml.getCheckCertificate().getCertInPEM();
		String gcid = xml.getCheckCertificate().getGcID();
		LoginDataType data = db.checkToken(gcid, certinpem);
		if(data==null){
			data=LoginDataType.Factory.newInstance();
		}
		ret.setCheckCertificateResponse(data);
		return ret;
	}

	public CheckCertChainResponseDocument checkCertificateChain(CheckCertificateChainDocument xml) {
		CheckCertChainResponseDocument ret =  CheckCertChainResponseDocument.Factory.newInstance();
		String gcid = xml.getCheckCertificateChain().getGcID();
		String base64 = xml.getCheckCertificateChain().getEncodedChain();
		byte[] cpb = Base64.getDecoder().decode(base64.getBytes());
		InputStream is = new ByteArrayInputStream(cpb);
		LoginDataType data=null;
		try {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			CertPath cp=cf.generateCertPath(is);                    
			Certificate o = cp.getCertificates().get(0);
			data = db.checkToken(gcid, X509Utils.getPEMStringFromX509((X509Certificate)o));
		}catch(Exception ce) {
			throw new RuntimeException(ce);
		}
		ret.setCheckCertChainResponse(data);
		return ret;
	}

	public CheckDNResponseDocument checkDN(CheckDNDocument xml) {
		CheckDNResponseDocument ret =  CheckDNResponseDocument.Factory.newInstance();
		String gcid = xml.getCheckDN().getGcID();
		String dn = xml.getCheckDN().getDistinguishedName();
		LoginDataType data = db.checkDN(gcid, dn);
		ret.setCheckDNResponse(data);
		return ret;
	}
}
