package de.fzj.unicore.xuudb.server;

import java.security.cert.X509Certificate;

import de.fzj.unicore.xuudb.X509Utils;
import eu.emi.security.authn.x509.impl.X500NameUtils;

/**
 *
 * this class is a wrapper for tokens in comparable DN format
 *
 * @author tweddell
 * @author schuller
 * @since 1.0.1
 */
public class SecurityToken {

	private final String token;

	/**
	 * create a new security token from the supplied PEM or DN
	 *
	 * @param rawToken - PEM or DN
	 */
	public SecurityToken(String rawToken) {
		this.token = convertToDN(rawToken);
	}

	protected String convertToDN(String source) {
		String dn = null;
		if (source.length() > 200) {
			dn = tryCertificate(source);
		}
		if (dn==null) {
			dn = X500NameUtils.getComparableForm(source);
		}
		return dn;
	}

	protected String tryCertificate(String source) {
		String dn = null;
		try {
			X509Certificate cert = X509Utils.getX509FromPEMString(source);
			dn = X500NameUtils.getComparableForm(cert.getSubjectX500Principal().getName());
		} catch (Exception ce) {}
		return dn;
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

	@Override
	public int hashCode() {
		return token.toLowerCase().hashCode()^0x36593265;
	}

}
