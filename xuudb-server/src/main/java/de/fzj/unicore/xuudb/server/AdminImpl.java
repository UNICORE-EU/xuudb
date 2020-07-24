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

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.log4j.Logger;

import de.fzJuelich.unicore.xuudb.AddCertificateDocument;
import de.fzJuelich.unicore.xuudb.AddCertificateResponseDocument;
import de.fzJuelich.unicore.xuudb.DatabaseType;
import de.fzJuelich.unicore.xuudb.ImportDatabaseDocument;
import de.fzJuelich.unicore.xuudb.ImportDatabaseResponseDocument;
import de.fzJuelich.unicore.xuudb.ListDatabaseDocument;
import de.fzJuelich.unicore.xuudb.ListDatabaseResponseDocument;
import de.fzJuelich.unicore.xuudb.LoginDataType;
import de.fzJuelich.unicore.xuudb.RemoveCertificateDocument;
import de.fzJuelich.unicore.xuudb.RemoveCertificateResponseDocument;
import de.fzJuelich.unicore.xuudb.UpdateCertificateDocument;
import de.fzJuelich.unicore.xuudb.UpdateCertificateResponseDocument;
import de.fzj.unicore.xuudb.Log;
import de.fzj.unicore.xuudb.interfaces.IAdmin;
import de.fzj.unicore.xuudb.server.db.IClassicStorage;

public class AdminImpl implements IAdmin {
	private static final Logger log = Log.getLogger(Log.XUUDB_SERVER, AdminImpl.class);
	
	private IClassicStorage xuudb;
	private boolean isDNMode;
	private String version; 
	
	public AdminImpl(ServerConfiguration co, IClassicStorage backend) throws Exception {
		this.xuudb = backend;
		isDNMode=true;
		version=getClass().getPackage().getImplementationVersion();
		if(version==null)
			version="DEVELOPMENT";
	}
	
	public AddCertificateResponseDocument addCertificate(AddCertificateDocument xml) {
		AddCertificateResponseDocument ret = AddCertificateResponseDocument.Factory.newInstance();
		try {
			String ans = xuudb.add(xml.getAddCertificate()); 
			ret.setAddCertificateResponse(ans);
		} catch (IllegalArgumentException e) {
			log.warn("Got wrong/unparsable argument: " + xml.xmlText() + 
					"\nError is: " + e.toString());
			throw e;
		} catch (PersistenceException e) {
			Log.logException("Error in database code", e, log);
			String msg = Log.createFaultMessage("Internal server error, database related", e);
			throw new RuntimeException(msg);
		}
		return ret;
	}

	public ListDatabaseResponseDocument listDatabase(ListDatabaseDocument xml) {
		ListDatabaseResponseDocument ret = ListDatabaseResponseDocument.Factory.newInstance();
		LoginDataType[] ans = null;
		try {
			ans = xuudb.listDB(xml.getListDatabase());
		} catch (IllegalArgumentException e) {
			log.warn("Got wrong/unparsable argument: " + xml.xmlText() + 
					"\nError is: " + e.toString());
			throw e;
		} catch (PersistenceException e) {
			Log.logException("Error in database code", e, log);
			String msg = Log.createFaultMessage("Internal server error, database related", e);
			throw new RuntimeException(msg);
		}
		DatabaseType data = DatabaseType.Factory.newInstance();
		data.setDatabaseArray(ans);
		data.setXUUDBInfo(getInfo());
		data.setIsDNMode(isDNMode);
		ret.setListDatabaseResponse(data);
		return ret;
	}

	public RemoveCertificateResponseDocument removeCertificate(RemoveCertificateDocument xml) {
		RemoveCertificateResponseDocument ret = RemoveCertificateResponseDocument.Factory.newInstance();
		String ans = null;
		try {
			ans = xuudb.remove(xml.getRemoveCertificate());
		} catch (IllegalArgumentException e) {
			log.warn("Got wrong/unparsable argument: " + xml.xmlText() + 
					"\nError is: " + e.toString());
			throw e;
		} catch (PersistenceException e) {
			Log.logException("Error in database code", e, log);
			String msg = Log.createFaultMessage("Internal server error, database related", e);
			throw new RuntimeException(msg);
		}
		ret.setRemoveCertificateResponse(ans);
		return ret;
	}

	public UpdateCertificateResponseDocument updateCertificate(UpdateCertificateDocument xml) {
		String gcid=xml.getUpdateCertificate().getGcID();
		String token=xml.getUpdateCertificate().getToken();
		UpdateCertificateResponseDocument ret = UpdateCertificateResponseDocument.Factory.newInstance();
		try {
			String ans = xuudb.update(gcid, token, xml.getUpdateCertificate().getData());
			ret.setUpdateCertificateResponse(ans);
		} catch (IllegalArgumentException e) {
			log.warn("Got wrong/unparsable argument: " + xml.xmlText() + 
					"\nError is: " + e.toString());
			throw e;
		} catch (PersistenceException e) {
			Log.logException("Error in database code", e, log);
			String msg = Log.createFaultMessage("Internal server error, database related", e);
			throw new RuntimeException(msg);
		}
		return ret;
	}

	public ImportDatabaseResponseDocument importDatabase(ImportDatabaseDocument xml) {
		try {
			String erg = xuudb.import_csv(xml);
			ImportDatabaseResponseDocument ret = ImportDatabaseResponseDocument.Factory.newInstance();
			ret.setImportDatabaseResponse(erg);
			return ret;
		} catch (IllegalArgumentException e) {
			log.warn("Got wrong/unparsable argument: " + xml.xmlText() + 
					"\nError is: " + e.toString());
			throw e;
		} catch (PersistenceException e) {
			Log.logException("Error in database code", e, log);
			String msg = Log.createFaultMessage("Internal server error, database related", e);
			throw new RuntimeException(msg);
		}
	}
	
	protected String getInfo(){
		StringBuilder sb=new StringBuilder();
		sb.append("XUUDB Server version ").append(version);
		sb.append(" ").append("running in ").append(isDNMode?"DN":"normal").append(" mode");
		return sb.toString();
	}
}
