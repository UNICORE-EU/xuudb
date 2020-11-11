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

import static eu.unicore.security.canl.TruststoreProperties.DEFAULT_PREFIX;
import static eu.unicore.security.canl.TruststoreProperties.PROP_KS_PASSWORD;
import static eu.unicore.security.canl.TruststoreProperties.PROP_KS_PATH;
import static eu.unicore.security.canl.TruststoreProperties.PROP_KS_TYPE;
import static eu.unicore.security.canl.TruststoreProperties.PROP_TYPE;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.Logger;

import eu.unicore.security.canl.CredentialProperties;
import eu.unicore.util.configuration.ConfigurationException;
import eu.unicore.util.configuration.PropertiesHelper;
import eu.unicore.util.configuration.PropertyMD;

/**
 * Defines common things for client's and server's configuration classes.
 * 
 * @author K. Benedyczak
 */
public abstract class AbstractConfiguration extends PropertiesHelper {
	public static final String PROP_PREFIX = "xuudb.";
	
	public static final String PROP_ADDRESS = "address";
	public static final String DEFAULT_ADDRESS = "http://localhost:34463";

	public AbstractConfiguration(String prefix, Properties properties,
			Map<String, PropertyMD> defaults, Logger log)
			throws ConfigurationException
	{
		super(prefix, properties, defaults, log);
	}
	
	protected void checkConfigFile(File config, Logger logger) throws IOException {
		String updateInfo = createConfigUpdateHint();
		if (updateInfo.length() > 0) {
			logger.error("The configuration in the " + config + " file uses the obsolete syntax." +
					" Please consult the documentation and perform the suggested updates:\n" +
					updateInfo);
			throw new IOException("Obsolete syntax in the configuration file was detected");
		}
		logger.info("Loaded Configuration from file: " + config.getPath());
	}
	
	protected abstract String createConfigUpdateHint();
	
	protected void createCommonConfigUpdateHint(StringBuilder newOnes, StringBuilder oldOnes) {
		final String KEYSTORE = "xuudb_keystore_file";
		final String KEYSTOREPASSWORD = "xuudb_keystore_password";
		final String KEYSTORETYPE = "xuudb_keystore_type";
		final String TRUSTSTORE = "xuudb_truststore_file";
		final String TRUSTSTOREPASSWORD = "xuudb_truststore_password";
		final String TRUSTSTORETYPE = "xuudb_truststore_type";
		
		final String HTTPPORT = "xuudb_http_port";
		final String HTTPHOST = "xuudb_http_host";
		final String USESSL = "xuudb_use_ssl";
		
		if (properties.getProperty(HTTPHOST) != null) {
			oldOnes.append(HTTPPORT).append("\n");
			oldOnes.append(HTTPHOST).append("\n");
			oldOnes.append(USESSL).append("\n");

			newOnes.append(PROP_PREFIX + PROP_ADDRESS + "=" + 
					properties.getProperty(HTTPHOST) + ":" + properties.getProperty(HTTPPORT));
		}
		
		if (properties.getProperty(KEYSTORE) != null) {
			oldOnes.append(KEYSTORE).append("\n");
			oldOnes.append(KEYSTOREPASSWORD).append("\n");
			oldOnes.append(KEYSTORETYPE).append("\n");

			newOnes.append(CredentialProperties.DEFAULT_PREFIX + 
				CredentialProperties.PROP_KEY_LOCATION + "=" + properties.getProperty(KEYSTORE));
			String oldType = properties.getProperty(KEYSTORETYPE);
			if (oldType == null)
				oldType = "JKS";
			newOnes.append(CredentialProperties.DEFAULT_PREFIX + 
				CredentialProperties.PROP_FORMAT + "=" + oldType);
			newOnes.append(CredentialProperties.DEFAULT_PREFIX + 
				CredentialProperties.PROP_PASSWORD + "=<YOUR_CURRENT_KEYSTORE_PASSWORD " +
						"(not written to log for security reasons)>");
		}
		
		if (properties.getProperty(TRUSTSTORE) != null) {
			oldOnes.append(TRUSTSTORE).append("\n");
			oldOnes.append(TRUSTSTORETYPE).append("\n");
			oldOnes.append(TRUSTSTOREPASSWORD).append("\n");
			
			newOnes.append(DEFAULT_PREFIX + PROP_TYPE + "=keystore");
			newOnes.append(DEFAULT_PREFIX + PROP_KS_PATH + "=" + properties.getProperty(TRUSTSTORE));
			String oldType = properties.getProperty(TRUSTSTORETYPE);
			if (oldType == null)
				oldType = "JKS";
			newOnes.append(DEFAULT_PREFIX + PROP_KS_TYPE + "=" + oldType);
			newOnes.append(DEFAULT_PREFIX + PROP_KS_PASSWORD + "=<YOUR_CURRENT_TRUSTSTORE_PASSWORD " +
						"(not written to log for security reasons)>");
			
		}
	}
	
	
	public Properties getProperties() {
		return properties;
	}
}







