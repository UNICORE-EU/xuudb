from ucfg_updater import *
from ucfg_utils import info, error, getFile

from ucfg_main import ConfigUtilities

import os
import re

class UUpdater(UUpdaterBase):
	
	def printDescription(self, options):
		info(options, "Updates UNICORE XUUDB from pre-2.0.0 to 2.0.0 version syntax (2.0.0 was released with the base UNICORE 6.6.0)")

	def run(self, options):
		info(options, "UNICORE XUUDB configuration syntax updater")
		cfgUtil = ConfigUtilities(options, "xuudb")
		
		control = cfgUtil.getJavaProperty('xuudb_client.conf', 'xuudb.truststore.type')
		if control != None:
			error('It seems that your configuration file xuudb_client.conf is already in the new format. The update will be skipped.')
			return
		control = cfgUtil.getJavaProperty('xuudb_server.conf', 'xuudb.truststore.type')
		if control != None:
			error('It seems that your configuration file xuudb_server.conf is already in the new format. The update will be skipped.')
			return
		
		#xuudb_client.conf
		cfgUtil.updateJavaPropertyNames('xuudb_client.conf', {			
			'xuudb_keystore_file' 				: 'xuudb.credential.path',
			'xuudb_keystore_password' 			: 'xuudb.credential.password',
			'xuudb_keystore_type' 				: 'xuudb.credential.format',
			'xuudb_truststore_file' 			: 'xuudb.truststore.keystorePath',
			'xuudb_truststore_password' 		: 'xuudb.truststore.keystorePassword',
			'xuudb_truststore_type' 			: 'xuudb.truststore.keystoreFormat',
		})
		addr1 = cfgUtil.getJavaProperty('xuudb_client.conf', 'xuudb_http_host')
		addr2 = cfgUtil.getJavaProperty('xuudb_client.conf', 'xuudb_http_port')
		if addr1 != None and addr2 != None:
			serverUrl = addr1 + ":" + addr2
		else:
			serverUrl = "https://localhost:34463"
		
		cfgUtil.commentJavaProperties('xuudb_client.conf', {
			'xuudb_http_host' 					: 'This property is not used anymore, xuudb.address is used instead.',
			'xuudb_use_ssl' 					: 'This property is not used anymore, control of SSL usage is done in xuudb.address by using http or https protocol.',
			'xuudb_http_port' 					: 'This property is not used anymore, xuudb.address is used instead.',
		});	
		
		cfgUtil.setJavaProperties('xuudb_client.conf', {
			'xuudb.truststore.type'				: 'keystore',
			'xuudb.address'						: serverUrl
		})

		#xuudb_server.conf
		cfgUtil.updateJavaPropertyNames('xuudb_server.conf', {			
			'xuudb_acl_file' 				: 'xuudb.aclFile',
			'xuudb_type' 					: 'xuudb.type',
			'xuudb_keystore_file' 			: 'xuudb.credential.path',
			'xuudb_keystore_password' 		: 'xuudb.credential.password',
			'xuudb_keystore_type' 			: 'xuudb.credential.format',
			'xuudb_truststore_file' 		: 'xuudb.truststore.keystorePath',
			'xuudb_truststore_password' 	: 'xuudb.truststore.keystorePassword',
			'xuudb_truststore_type' 		: 'xuudb.truststore.keystoreFormat',
		})
		addr1 = cfgUtil.getJavaProperty('xuudb_server.conf', 'xuudb_http_host')
		addr2 = cfgUtil.getJavaProperty('xuudb_server.conf', 'xuudb_http_port')
		if addr1 != None and addr2 != None:
			serverUrl = addr1 + ":" + addr2
		else:
			serverUrl = "https://localhost:34463"
		cfgUtil.commentJavaProperties('xuudb_server.conf', {
			'xuudb_http_host' 				: 'This property is not used anymore, xuudb.address is used instead.',
			'xuudb_use_ssl' 				: 'This property is not used anymore, control of SSL usage is done in xuudb.address by using http or https protocol.',
			'xuudb_http_port' 				: 'This property is not used anymore, xuudb.address is used instead.',
			'xuudb_data_file'				: 'This property is not used anymore, database setup is given with xuudb.db.* properties'
		});	
		
		dapFilepath = getFile(options, 'xuudb', 'dap-configuration.xml');
		if options.systemInstall:
			h2url = 'jdbc:h2:/var/lib/unicore/xuudb/data/xuudb2'
		else:
			h2url = 'jdbc:h2:data/xuudb2'
		cfgUtil.setJavaProperties('xuudb_server.conf', {
			'xuudb.truststore.type'			: 'keystore',
			'xuudb.address'					: serverUrl,
			'xuudb.dynamicAttributesConfig'	: dapFilepath,
			'xuudb.db.dialect'				: 'h2',
			'xuudb.db.driver'				: 'org.h2.Driver',
			'xuudb.db.jdbcUrl'				: h2url,
			'xuudb.db.username'				: 'sa',
			'xuudb.db.password'				: '',
		})
		
		cfgUtil.appendLinesIfNotAlreadyExist('xuudb_server.conf', [
			'#  Be sure to enable only ONE database, and configure it correctly.',
			'#  MySQL - external, needs additional configuration (on RDBMS side) ',
			'#xuudb.db.dialect=mysql',
			'#xuudb.db.driver=com.mysql.jdbc.Driver',
			'#xuudb.db.jdbcUrl=jdbc:mysql://localhost/xuudb2',
			'#xuudb.db.username=xuudbuser',
			'#xuudb.db.password=pass'
		]);


		dapContents = '''<?xml version="1.0" encoding="UTF-8"?>
<dynamicAttributes xmlns="http://unicore.eu/xuudb/dynamicAttributesRules"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <poolMonitoringDelay>300</poolMonitoringDelay>
  <rules>

    <!-- Some example rules -->
    <!-- 
    <rule>
      <condition>vo.matches("/biology/.*")</condition>
      <mapping type="pool">biology-uids-pool</mapping>
      <mapping type="fixed" maps="gid">biologists</mapping>
    </rule>
    -->

  </rules>

  <!-- Some example pools -->
  <!-- 
  <pools>
    <pool id="biology-uids-pool" type="uid" key="dn" precreated="true">
      <file>/opt/xuudb/externalUidsPool</file>
    </pool>
  -->
</dynamicAttributes>
'''
		if not os.path.exists(dapFilepath):
			f = open(dapFilepath, "w")
			info(options, "Creating a default " + dapFilepath + " DAP configuration");
			try:
				f.write(dapContents)
			finally:
				f.close()
		
		info(options, "IMPORTANT: Security properties were updated to the new format. Note that you should review the new settings and consider MANY new available options, described in documentation.")
		info(options, "Finished update of configuration of UNICORE XUUDB")

