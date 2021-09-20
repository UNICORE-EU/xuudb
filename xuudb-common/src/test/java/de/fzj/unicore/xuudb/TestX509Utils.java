package de.fzj.unicore.xuudb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.cert.X509Certificate;

import org.junit.Test;

import eu.emi.security.authn.x509.impl.X500NameUtils;

public class TestX509Utils {

	@Test
	public void testX509Utils() throws Exception {

		String pem = "src/test/resources/demouser.pem";
		String s = X509Utils.getStringFromPEMFile(pem);
		assertEquals(
				s,
				"MIICSDCCAbGgAwIBAgIBATANBgkqhkiG9w0BAQQFADBXMQswCQYDVQQGEwJERTEhMB8GA1UEAxMYRGVtbyBDQSBmb3IgdGVzdGluZyBvbmx5MRMwEQYDVQQKEwpVTklDT1JFLkVVMRAwDgYDVQQLEwdUZXN0aW5nMB4XDTA3MTIzMTIzMDAwMFoXDTEyMTIzMDIzMDAwMFowfTEaMBgGA1UEAwwRVU5JQ09SRSBkZW1vIHVzZXIxEDAOBgNVBAsMB1Rlc3RpbmcxEzARBgNVBAoMCnVuaWNvcmUuZXUxCzAJBgNVBAYMAkRFMSswKQYJKoZIhvcNAQkBFhx1bmljb3JlLXN1cHBvcnRAbGlzdHMuc2YubmV0MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCVrCNiyUyVXMDJHWxB7Pt3sDROCUn6msnOmXg0/1MKyAaJaVw/eZACreOa8Tyqg+F/BFr1LdeikAjkZraYv4STCLDHlDV9KJI9XDB46S8SqRlt+FsLsEgS23jtWYgZ2/GKidGEG7CfJ/TJIlsAk2Tc054FA3HXzJPdH/6yaQIWuQIDAQABMA0GCSqGSIb3DQEBBAUAA4GBAA+obQz9DSnTu4Kp6s23dWU3l7JgoliMfINy6Omj9HXDQusY7G+hZ1t/HXwSB8fhiRp2G5AurvcwgFYQscCRgvewOTUNI+3IRWfpa3rtlsdiTzO+r2Kt/QJ+Y4WQmSDkqct05tGPy+8a6PSlaDJfSAEE/O3lDlwhxwqtDaUlq/Hp");

		X509Certificate cert = X509Utils.getX509FromPEMString(s);
		assertTrue(X500NameUtils.equal(cert.getSubjectX500Principal(),
				"EMAILADDRESS=unicore-support@lists.sf.net, C=DE, O=unicore.eu, OU=Testing, CN=UNICORE demo user"));

		String s2 = X509Utils.getPEMStringFromX509(cert);
		assertEquals(s, s2);

		// bad pem file
		pem = "src/test/resources/test.conf";
		try {
			s = X509Utils.getStringFromPEMFile(pem);
		} catch (IOException e) {
			assertTrue(e.getMessage().contains("PEM data not found in the stream"));
		}
	}

}
