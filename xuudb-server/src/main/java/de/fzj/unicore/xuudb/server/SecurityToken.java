package de.fzj.unicore.xuudb.server;

import java.security.cert.X509Certificate;

import de.fzj.unicore.xuudb.X509Utils;
import eu.emi.security.authn.x509.impl.X500NameUtils;

/**
 * 
 * this class is a wrapper for managing tokens in pem or dn format.
 * 
 * @author tweddell
 * @author schuller
 * @since 1.0.1
 */

public class SecurityToken {
	private String token;
	private String source;
	private boolean certificateToken;
	private X509Certificate certificate = null;
	
	/**
	 * create a new security token
	 * 
	 * @param token - the raw token
	 * @param dnMode - if the DB is running in dnMode
	 * @throws Exception
	 */
	public SecurityToken(String rawToken) throws IllegalArgumentException {
		this.source = rawToken;
		resolveType(rawToken);
		this.token = buildDNToken(rawToken);
	}
	
	protected void resolveType(String source) {
		if (source.length() > 200) {
			if (!tryCertificate(source, false))
				tryDN(source, true);
		} else {
			if (!tryDN(source, false))
				tryCertificate(source, true);			
		}
	}
	
	protected boolean tryCertificate(String source, boolean fail) {
		try {
			certificate = X509Utils.getX509FromPEMString(source);
			certificateToken = true;
			return true;
		} catch (Exception ce) {
			if (fail) {
				throw new IllegalArgumentException("Token is not a DN or x509 cert.");				
			}
			return false;
		}
	}

	protected boolean tryDN(String source, boolean fail) {
		try {
			X500NameUtils.getX500Principal(source);
			certificateToken = false;
			return true;
		} catch (Exception ce) {
			if (fail) {
				throw new IllegalArgumentException("Token is not a DN or x509 cert.");				
			}
			return false;
		}
	}
	
	protected String buildDNToken(String token) throws IllegalArgumentException {
		if (!certificateToken)
			return X500NameUtils.getComparableForm(token);
		return X500NameUtils.getComparableForm(certificate.getSubjectX500Principal().getName());
	}

	@Override
	public String toString() {
		return this.token;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(! (obj instanceof SecurityToken)) {
			return false;
		}
		return this.token.equalsIgnoreCase(obj.toString());
	}
	
	public int hashCode() {
		//equal tokens (see equals() method) should have equal hashcodes
		return token.toLowerCase().hashCode()^0x36593265;
	}

	/**
	 * @return the token
	 */
	public String getToken() {
		return token;
	}

	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * @return the certificateToken
	 */
	public boolean isCertificateToken() {
		return certificateToken;
	}

	/**
	 * @return the certificate or null if DN was set.
	 */
	public X509Certificate getCertificate()	{
		return certificate;
	}
}
