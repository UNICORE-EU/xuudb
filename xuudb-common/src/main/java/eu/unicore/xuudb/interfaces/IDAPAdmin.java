package eu.unicore.xuudb.interfaces;

import eu.unicore.xuudb.xbeans.FindMappingRequestDocument;
import eu.unicore.xuudb.xbeans.FindMappingResponseDocument;
import eu.unicore.xuudb.xbeans.FindReverseMappingRequestDocument;
import eu.unicore.xuudb.xbeans.FindReverseMappingResponseDocument;
import eu.unicore.xuudb.xbeans.FreezeMappingRequestDocument;
import eu.unicore.xuudb.xbeans.ListMappingRequestDocument;
import eu.unicore.xuudb.xbeans.ListMappingResponseDocument;
import eu.unicore.xuudb.xbeans.ListPoolsResponseDocument;
import eu.unicore.xuudb.xbeans.RemoveMappingRequestDocument;
import eu.unicore.xuudb.xbeans.RemovePoolRequestDocument;
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
public interface IDAPAdmin {
	public static final String SERVICE_NAME = "XUUDBDAPAdmin";

	/**
	 * List mappings using optional filters.
	 * @param xml
	 * @return
	 */
	@WebMethod()
	@WebResult(targetNamespace="http://www.fz-juelich.de/unicore/xuudb", name="ListMappingResponse")
	public ListMappingResponseDocument listMappings(
			@WebParam(targetNamespace="http://www.fz-juelich.de/unicore/xuudb", name="ListMappingRequest")
			ListMappingRequestDocument xml);

	/**
	 * Finds a mapping by providing a mapping value. E.g. can return a mapping(s) which maps to a specified uid 
	 * @param xml
	 * @return
	 */
	@WebMethod()
	@WebResult(targetNamespace="http://www.fz-juelich.de/unicore/xuudb", name="FindMappingResponse")
	public FindMappingResponseDocument findMapping(
			@WebParam(targetNamespace="http://www.fz-juelich.de/unicore/xuudb", name="FindMappingRequest")
			FindMappingRequestDocument xml);

	/**
	 * Finds a mapping by providing an attribute which is the mapping key. E.g. can 
	 * return all mappings which are bound to a specified VO.
	 * @param xml
	 * @return
	 */
	@WebMethod()
	@WebResult(targetNamespace="http://www.fz-juelich.de/unicore/xuudb", name="FindReverseMappingResponse")
	public FindReverseMappingResponseDocument findReverseMapping(
			@WebParam(targetNamespace="http://www.fz-juelich.de/unicore/xuudb", name="FindReverseMappingRequest")
			FindReverseMappingRequestDocument xml);

	/**
	 * Freezes an active mapping
	 * @param xml
	 */
	@WebMethod()
	public void freezeMapping(
			@WebParam(targetNamespace="http://www.fz-juelich.de/unicore/xuudb", name="FreezeMappingRequest")
			FreezeMappingRequestDocument xml);

	/**
	 * Removes a previously frozen mapping.
	 * @param xml
	 */
	@WebMethod()
	public void removeFrozenMapping(
			@WebParam(targetNamespace="http://www.fz-juelich.de/unicore/xuudb", name="RemoveMappingRequest")
			RemoveMappingRequestDocument xml);

	/**
	 * Removes a pool by its name. The pool must be empty and inactive.
	 * @param xml
	 */
	@WebMethod()
	public void removePool(RemovePoolRequestDocument xml);
	
	/**
	 * Allows to get a real list off all existing pools, including those removed from configuration file
	 * @return list of all pools defined in database, both active and not
	 */
	@WebMethod()
	@WebResult(targetNamespace="http://www.fz-juelich.de/unicore/xuudb", name="ListPoolsResponse")
	public ListPoolsResponseDocument listPools();
}
