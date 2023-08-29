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
import de.fzj.unicore.xuudb.interfaces.IAdmin;
import de.fzj.unicore.xuudb.server.db.IClassicStorage;

public class AdminImpl implements IAdmin {

	private final IClassicStorage xuudb;
	private final String version; 

	public AdminImpl(ServerConfiguration co, IClassicStorage backend) throws Exception {
		this.xuudb = backend;
		this.version = getClass().getPackage().getImplementationVersion()!=null?
				getClass().getPackage().getImplementationVersion() : "DEVELOPMENT";
	}

	public AddCertificateResponseDocument addCertificate(AddCertificateDocument xml) {
		AddCertificateResponseDocument ret = AddCertificateResponseDocument.Factory.newInstance();
		String ans = xuudb.add(xml.getAddCertificate()); 
		ret.setAddCertificateResponse(ans);
		return ret;
	}

	public ListDatabaseResponseDocument listDatabase(ListDatabaseDocument xml) {
		ListDatabaseResponseDocument ret = ListDatabaseResponseDocument.Factory.newInstance();
		LoginDataType[] ans = xuudb.listDB(xml.getListDatabase());
		DatabaseType data = DatabaseType.Factory.newInstance();
		data.setDatabaseArray(ans);
		data.setXUUDBInfo(getInfo());
		data.setIsDNMode(true);
		ret.setListDatabaseResponse(data);
		return ret;
	}

	public RemoveCertificateResponseDocument removeCertificate(RemoveCertificateDocument xml) {
		RemoveCertificateResponseDocument ret = RemoveCertificateResponseDocument.Factory.newInstance();
		String ans = xuudb.remove(xml.getRemoveCertificate());
		ret.setRemoveCertificateResponse(ans);
		return ret;
	}

	public UpdateCertificateResponseDocument updateCertificate(UpdateCertificateDocument xml) {
		String gcid=xml.getUpdateCertificate().getGcID();
		String token=xml.getUpdateCertificate().getToken();
		UpdateCertificateResponseDocument ret = UpdateCertificateResponseDocument.Factory.newInstance();
		String ans = xuudb.update(gcid, token, xml.getUpdateCertificate().getData());
		ret.setUpdateCertificateResponse(ans);
		return ret;
	}

	public ImportDatabaseResponseDocument importDatabase(ImportDatabaseDocument xml) {
		String erg = xuudb.import_csv(xml);
		ImportDatabaseResponseDocument ret = ImportDatabaseResponseDocument.Factory.newInstance();
		ret.setImportDatabaseResponse(erg);
		return ret;
	}

	protected String getInfo(){
		return "XUUDB Server version "+version;
	}
}
