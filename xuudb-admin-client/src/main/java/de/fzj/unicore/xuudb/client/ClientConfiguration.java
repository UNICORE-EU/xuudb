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


package de.fzj.unicore.xuudb.client;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import de.fzj.unicore.xuudb.AbstractConfiguration;
import de.fzj.unicore.xuudb.Log;

import eu.unicore.security.canl.CredentialProperties;
import eu.unicore.security.canl.TruststoreProperties;
import eu.unicore.util.configuration.FilePropertiesHelper;
import eu.unicore.util.configuration.PropertyMD;
import eu.unicore.util.httpclient.ClientProperties;

/**
 * Provides access to client configuration and for generating hints on converting 
 * the old configuration syntax to the new one.
 * @author K. Benedyczak
 */
public class ClientConfiguration extends AbstractConfiguration {
	private static final Logger logger = Log.getLogger(Log.CONFIGURATION, ClientConfiguration.class);

	public static final String PROP_BATCH = "batch";
	
	public final static Map<String, PropertyMD> DEFAULTS = new HashMap<String, PropertyMD>();
	static 
	{
		DEFAULTS.put(PROP_ADDRESS, new PropertyMD(DEFAULT_ADDRESS));
		DEFAULTS.put(PROP_BATCH, new PropertyMD("false"));
		DEFAULTS.put(TruststoreProperties.DEFAULT_PREFIX, new PropertyMD().setCanHaveSubkeys().setHidden().
				setDescription("Properties with this prefix are used to configure the server's credential. See separate documentation for details."));
		DEFAULTS.put(CredentialProperties.DEFAULT_PREFIX, new PropertyMD().setCanHaveSubkeys().setHidden().
				setDescription("Properties with this prefix are used to configure trust and certificate validation settings. See separate documentation for details."));
		DEFAULTS.put(ClientProperties.DEFAULT_PREFIX, new PropertyMD().setCanHaveSubkeys().setHidden().
				setDescription("Properties with this prefix are used to configure advanced HTTP client settings. See separate documentation for details."));
	}

	public ClientConfiguration(File config) throws IOException {
		super(PROP_PREFIX, FilePropertiesHelper.load(config), DEFAULTS, logger);
		checkConfigFile(config, logger);
	}
	
	public ClientConfiguration(Properties p) {
		super(PROP_PREFIX, p, DEFAULTS, logger);
	}

	protected String createConfigUpdateHint() {
		StringBuilder newOnes = new StringBuilder();
		StringBuilder oldOnes = new StringBuilder();

		createCommonConfigUpdateHint(newOnes, oldOnes);
		
		if (oldOnes.length() > 0)
			return "Entries to be removed from the config file:\n" + oldOnes + 
					"\nEntries to be added:\n" + newOnes;
		else
			return "";
	}
	
	public boolean isBatch() {
		return getBooleanValue(PROP_BATCH);
	}
}







