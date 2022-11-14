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
 

package de.fzj.unicore.xuudb;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;

import eu.emi.security.authn.x509.impl.CertificateUtils;
import eu.emi.security.authn.x509.impl.CertificateUtils.Encoding;

public class X509Utils {
	
	private static final String X509BEGIN_TOKEN = "-----BEGIN CERTIFICATE-----\n";
	private static final String X509END_TOKEN = "\n-----END CERTIFICATE-----";
	
	/**
	 * Loads a certificate from a given file.
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static X509Certificate loadCertificate(String file) throws IOException
	{
		BufferedInputStream bis = null;
		try {
			bis = new BufferedInputStream(new FileInputStream(file));
			return CertificateUtils.loadCertificate(bis , Encoding.PEM);
		} finally {
			if (bis != null)
				bis.close();
		}
	}
	
	/**
	 * Same as invoking loadCertificate and then getPEMStringFromX509
	 * @param pemFile
	 * @return a string which contains the BASE64 encoded cert
	 * @throws CertificateEncodingException 
	 * @throws FileNotFoundException
	 * @throws link IllegalArgumentException
	 */
	public static String getStringFromPEMFile(String pemFile) throws IOException {
		X509Certificate cert = loadCertificate(pemFile);
		return getPEMStringFromX509(cert);
	}
	
	/**
	 * Reads certificate from a string, which must be a Base64 encoded PEM, without start and end delimiter lines.
	 * @param pemstr
	 * @return instance of X509Certificate from pemstr
	 * @throws CertificateException
	 */
	public static X509Certificate getX509FromPEMString(String pemstr) throws IOException {
		if( pemstr == null )
			return null;
		String work = X509BEGIN_TOKEN + pemstr + X509END_TOKEN;
		ByteArrayInputStream bis = new ByteArrayInputStream(work.getBytes()); 
		try {
			return CertificateUtils.loadCertificate(bis , Encoding.PEM);
		} finally {
			if (bis != null)
				bis.close();
		}
	}

	/**
	 * @param x509
	 * @return a certificate base64 encoded (i.e. not a real PEM!)
	 * @throws CertificateEncodingException
	 */
	public static String getPEMStringFromX509(Certificate x509) throws IOException {
		try {
			byte[] base64 = Base64.getEncoder().encode(x509.getEncoded());
			return new String(base64);
		} catch (CertificateEncodingException e) {
			throw new IOException("Can't encode the certificate, shouldn't happen", e);
		}
	}
}