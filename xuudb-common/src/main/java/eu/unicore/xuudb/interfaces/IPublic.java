package eu.unicore.xuudb.interfaces;

import de.fzJuelich.unicore.xuudb.CheckCertChainResponseDocument;
import de.fzJuelich.unicore.xuudb.CheckCertificateChainDocument;
import de.fzJuelich.unicore.xuudb.CheckCertificateDocument;
import de.fzJuelich.unicore.xuudb.CheckCertificateResponseDocument;
import de.fzJuelich.unicore.xuudb.CheckDNDocument;
import de.fzJuelich.unicore.xuudb.CheckDNResponseDocument;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import jakarta.jws.soap.SOAPBinding.ParameterStyle;
import jakarta.jws.soap.SOAPBinding.Style;
import jakarta.jws.soap.SOAPBinding.Use;

/**
 * XUUDB query interface
 */
@WebService
@SOAPBinding(parameterStyle=ParameterStyle.BARE, use=Use.LITERAL, style=Style.DOCUMENT)
public interface IPublic {
	
	public static final String SERVICE_NAME="XUUDBQuery";

	/**
	 * check whether the given certificate is in the XUUDB, and return attributes
	 */
	@WebMethod()
	@WebResult(targetNamespace="http://www.fz-juelich.de/unicore/xuudb", name="checkCertificateResponse")
	public CheckCertificateResponseDocument checkCertificate(
			@WebParam(targetNamespace="http://www.fz-juelich.de/unicore/xuudb", name="CheckCertificate")
			CheckCertificateDocument xml);
	
	/**
	 * check whether the chain contains a certificate that is in the UUDB, and return attributes
	 */
	@WebMethod()
	@WebResult(targetNamespace="http://www.fz-juelich.de/unicore/xuudb", name="CheckCertificateChainResponse")
	public CheckCertChainResponseDocument checkCertificateChain(
			@WebParam(targetNamespace="http://www.fz-juelich.de/unicore/xuudb", name="CheckCertificateChain")
			CheckCertificateChainDocument xml);
	
	
	/**
	 * check whether the distinguished name is in the UUDB, and return attributes
	 */
	@WebMethod()
	@WebResult(targetNamespace="http://www.fz-juelich.de/unicore/xuudb", name="CheckDNResponse")
	public CheckDNResponseDocument checkDN(
			@WebParam(targetNamespace="http://www.fz-juelich.de/unicore/xuudb", name="CheckDN")
			CheckDNDocument xml);
	
}
