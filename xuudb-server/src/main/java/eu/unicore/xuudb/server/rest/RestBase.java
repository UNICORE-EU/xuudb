package eu.unicore.xuudb.server.rest;

import org.apache.cxf.jaxrs.impl.ResponseBuilderImpl;
import org.json.JSONObject;

import eu.unicore.util.Log;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * @author schuller
 */
public abstract class RestBase {

	@GET
	@Path("/info")
	@Produces("application/json")
	public String info() {
		return doGetInfo().toString();
	}

	protected JSONObject doGetInfo() {
		JSONObject info = new JSONObject();
		String v = getClass().getPackage().getSpecificationVersion();
		info.put("version", v!=null? v : "dev");
		return info;
	}

	/**
	 * Create an error (500) Response. 
	 * The error info will be placed in the response body as JSON.
	 */
	protected Response handleError(String message, Throwable cause) 
			throws WebApplicationException{
		return handleError(500, message, cause);
	}
	/**
	 * Create an error Response with the given HTTP status. 
	 * The error info will be placed in the response body as JSON.
	 */
	protected Response handleError(int status, String message, Throwable cause) 
			throws WebApplicationException{
		if(cause!=null && cause instanceof WebApplicationException){
			// special case: no need to wrap these
			throw (WebApplicationException)cause;
		}
		String msg = null;
		if(cause!=null) {
			msg  = Log.createFaultMessage(message, cause);
		}
		else msg = message;
		return createErrorResponse(status, msg);
	}

	/**
	 * Create a Response with the given HTTP status. 
	 * The error info will be placed in the response body as JSON.
	 */
	protected Response createErrorResponse(int status, String message) {
		ResponseBuilderImpl res = new ResponseBuilderImpl();
		res.status(status);
		res.type(MediaType.APPLICATION_JSON);
		JSONObject json = new JSONObject();
		try{
			json.put("errorMessage", message);
			json.put("status", status);
		}catch(Exception ex){}
		res.entity(json.toString());
		return res.build();
	}
	
}
