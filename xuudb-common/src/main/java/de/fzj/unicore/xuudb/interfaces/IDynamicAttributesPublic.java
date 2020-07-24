/*
 * Copyright (c) 2012 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */ 
package de.fzj.unicore.xuudb.interfaces;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;

import de.fzJuelich.unicore.xuudb.GetAttributesRequestDocument;
import de.fzJuelich.unicore.xuudb.GetAttributesResponseDocument;
import de.fzJuelich.unicore.xuudb.SimulateGetAttributesRequestDocument;
import de.fzJuelich.unicore.xuudb.SimulateGetAttributesResponseDocument;

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
