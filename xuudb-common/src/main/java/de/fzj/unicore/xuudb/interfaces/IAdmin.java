package de.fzj.unicore.xuudb.interfaces;

import de.fzJuelich.unicore.xuudb.AddCertificateDocument;
import de.fzJuelich.unicore.xuudb.AddCertificateResponseDocument;
import de.fzJuelich.unicore.xuudb.ImportDatabaseDocument;
import de.fzJuelich.unicore.xuudb.ImportDatabaseResponseDocument;
import de.fzJuelich.unicore.xuudb.ListDatabaseDocument;
import de.fzJuelich.unicore.xuudb.ListDatabaseResponseDocument;
import de.fzJuelich.unicore.xuudb.RemoveCertificateDocument;
import de.fzJuelich.unicore.xuudb.RemoveCertificateResponseDocument;
import de.fzJuelich.unicore.xuudb.UpdateCertificateDocument;
import de.fzJuelich.unicore.xuudb.UpdateCertificateResponseDocument;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import jakarta.jws.soap.SOAPBinding.ParameterStyle;
import jakarta.jws.soap.SOAPBinding.Style;
import jakarta.jws.soap.SOAPBinding.Use;

@WebService
@SOAPBinding(parameterStyle=ParameterStyle.BARE, use=Use.LITERAL, style=Style.DOCUMENT)
public interface IAdmin {


	public static final String SERVICE_NAME="XUUDBAdmin";

	/**
	 * Remove entry that maps to securityToken from UUUDB
	 */
	@WebMethod()
	@WebResult(targetNamespace="http://www.fz-juelich.de/unicore/xuudb", name="RemoveCertificateResponse")
	public RemoveCertificateResponseDocument removeCertificate(
			@WebParam(targetNamespace="http://www.fz-juelich.de/unicore/xuudb", name="RemoveCertificate")
			RemoveCertificateDocument xml);
	
	/**
	 * Add entry (securityToken,role,xlogin) to UUDB
	 */
	@WebMethod()
	@WebResult(targetNamespace="http://www.fz-juelich.de/unicore/xuudb", name="AddCertificateResponse")
	public AddCertificateResponseDocument addCertificate(
			@WebParam(targetNamespace="http://www.fz-juelich.de/unicore/xuudb", name="AddCertificate")
			AddCertificateDocument xml);
	
	/**
	 * Update new securityToken for an existing entry 
	 */
	@WebMethod()
	@WebResult(targetNamespace="http://www.fz-juelich.de/unicore/xuudb", name="UpdateCertificateResponse")
	public UpdateCertificateResponseDocument updateCertificate(
			@WebParam(targetNamespace="http://www.fz-juelich.de/unicore/xuudb", name="UpdateCertificate")
			UpdateCertificateDocument xml); 
	
	/**
	 * List all entries that matches to 'search'
	 */
	@WebMethod()
	@WebResult(targetNamespace="http://www.fz-juelich.de/unicore/xuudb", name="ListDatabaseResponse")
	public ListDatabaseResponseDocument listDatabase(
			@WebParam(targetNamespace="http://www.fz-juelich.de/unicore/xuudb", name="ListDatabase")
			ListDatabaseDocument xml);

	/**
	 * Import database from csv
	 */
	@WebMethod()
	@WebResult(targetNamespace="http://www.fz-juelich.de/unicore/xuudb", name="ImportDatabaseResponse")
	public ImportDatabaseResponseDocument importDatabase(
			@WebParam(targetNamespace="http://www.fz-juelich.de/unicore/xuudb", name="ImportDatabase")
			ImportDatabaseDocument xml);
	

}
