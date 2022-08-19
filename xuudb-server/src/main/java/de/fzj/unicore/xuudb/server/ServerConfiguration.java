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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.Logger;

import de.fzj.unicore.xuudb.CommonConfiguration;
import de.fzj.unicore.xuudb.Log;
import eu.unicore.security.canl.CredentialProperties;
import eu.unicore.security.canl.TruststoreProperties;
import eu.unicore.util.configuration.DocumentationReferenceMeta;
import eu.unicore.util.configuration.FilePropertiesHelper;
import eu.unicore.util.configuration.PropertyMD;
import eu.unicore.util.db.DBPropertiesHelper;
import eu.unicore.util.jetty.HttpServerProperties;

/**
 * Server configuration
 *
 * @author K. Benedyczak
 */
public class ServerConfiguration extends CommonConfiguration {

	private static final Logger logger = Log.getLogger(Log.CONFIGURATION, ServerConfiguration.class);

	public enum XuudbModes {dn};
	/**
	 * type of XUUDB: "dn" checks DNs only, while "normal" checks the certificate
	 */
	public static final String PROP_XUUDBTYPE = "type";
	public static final String PROP_ACL_FILE = "aclFile";
	public static final String PROP_PROTECT_ALL = "protectAll";
	public static final String PROP_DAP_FILE = "dynamicAttributesConfig";
	
	@DocumentationReferenceMeta
	public final static Map<String, PropertyMD> DEFAULTS = new HashMap<String, PropertyMD>();
	static 
	{
		DEFAULTS.put(PROP_ADDRESS, new PropertyMD(DEFAULT_ADDRESS).
				setDescription("HTTPS or HTTP URL where the server should listen."));
		DEFAULTS.put(PROP_XUUDBTYPE, new PropertyMD(XuudbModes.dn).
				setDescription("DEPRECATED and no longer required."));
		DEFAULTS.put(PROP_DAP_FILE, new PropertyMD("conf/dynamicAttributesCfg.xml").setPath().
				setDescription("File with configuration of the dynamic part of the XUUDB."));
		DEFAULTS.put(PROP_ACL_FILE, new PropertyMD().setPath().
				setDescription("File with DNs of clients authorised to access protected XUUDB services."));
		DEFAULTS.put(PROP_PROTECT_ALL, new PropertyMD("false").
				setDescription("If true then access to both query and modify operations are protected by ACL. If false then only modification operations are protected."));
		DEFAULTS.put(DBPropertiesHelper.PREFIX, new PropertyMD().setCanHaveSubkeys().
				setDescription("Properties with this prefix are used to configure database backend, used by XUUDB. See separate documentation for details."));
		DEFAULTS.put(HttpServerProperties.DEFAULT_PREFIX, new PropertyMD().setCanHaveSubkeys().setHidden().
				setDescription("Properties with this prefix are used to configure advanced Jetty HTTP server settings. See separate documentation for details."));
		DEFAULTS.put(TruststoreProperties.DEFAULT_PREFIX, new PropertyMD().setCanHaveSubkeys().setHidden().
				setDescription("Properties with this prefix are used to configure the server's credential. See separate documentation for details."));
		DEFAULTS.put(CredentialProperties.DEFAULT_PREFIX, new PropertyMD().setCanHaveSubkeys().setHidden().
				setDescription("Properties with this prefix are used to configure trust and certificate validation settings. See separate documentation for details."));
	}

	public ServerConfiguration(File config) throws IOException {
		super(PROP_PREFIX, FilePropertiesHelper.load(config), DEFAULTS, logger);
	}
	
	public ServerConfiguration(Properties p) {
		super(PROP_PREFIX, p, DEFAULTS, logger);
	}

}







