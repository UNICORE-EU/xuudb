package eu.unicore.xuudb.interfaces;

import de.fzJuelich.unicore.xuudb.GetAttributesRequestDocument;
import de.fzJuelich.unicore.xuudb.GetAttributesResponseDocument;
import de.fzJuelich.unicore.xuudb.SimulateGetAttributesRequestDocument;
import de.fzJuelich.unicore.xuudb.SimulateGetAttributesResponseDocument;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import jakarta.jws.soap.SOAPBinding.ParameterStyle;
import jakarta.jws.soap.SOAPBinding.Style;
import jakarta.jws.soap.SOAPBinding.Use;

/**
 * XUUDB dynamic attributes query interface
 */
@WebService
@SOAPBinding(parameterStyle=ParameterStyle.BARE, use=Use.LITERAL, style=Style.DOCUMENT)
public interface IDynamicAttributesPublic {
	
	public static final String SERVICE_NAME="XUUDBDynamicAttributesQuery";

	/**
	 * Returns dynamic attributes for the entity described by the already established attributes
	 */
	@WebMethod()
	@WebResult(targetNamespace="http://www.fz-juelich.de/unicore/xuudb", name="GetAttributesResponse")
	public GetAttributesResponseDocument getAttributes(
			@WebParam(targetNamespace="http://www.fz-juelich.de/unicore/xuudb", name="GetAttributesRequest")
			GetAttributesRequestDocument xml);
	
	/**
	 * Performs a dry run of normal operation, checking to what a specified user would be mapped to.
	 * @return dynamic attributes 
	 */
	@WebMethod()
	@WebResult(targetNamespace="http://www.fz-juelich.de/unicore/xuudb", name="SimulateGetAttributesResponse")
	public SimulateGetAttributesResponseDocument simulateGetAttributes(
			@WebParam(targetNamespace="http://www.fz-juelich.de/unicore/xuudb", name="SimulateGetAttributes")
			SimulateGetAttributesRequestDocument xml);
}
