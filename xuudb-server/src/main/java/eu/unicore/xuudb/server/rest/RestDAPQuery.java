package eu.unicore.xuudb.server.rest;

import java.util.HashSet;
import java.util.Set;

import org.apache.cxf.jaxrs.impl.ResponseBuilderImpl;
import org.json.JSONObject;

import eu.unicore.util.Log;
import eu.unicore.xuudb.server.db.IRESTClassicStorage;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * @author schuller
 */
@Path("/")
public class RestDAPQuery {
	
	private IRESTClassicStorage storage;

	public RestDAPQuery() {}
	
	public void setStorage(IRESTClassicStorage storage) {
		this.storage = storage;
	}
	
	@GET
	@Path("/info")
	@Produces("application/json")
	public String info() {
		JSONObject info = new JSONObject();
		String v = getClass().getPackage().getSpecificationVersion();
		info.put("version", v!=null? v : "dev");
		return info.toString();
	}

	@GET
	@Path("/query/{gcid}")
	@Produces("application/json")
	public Response query(@PathParam("gcid") String gcid, @QueryParam("dn") String dn) {
		try {
			if(dn==null) {
				return handleError(400, "Query parameter 'dn' must be given", null);
			}
			JSONObject res = storage.checkDN(gcid, dn);
			return Response.ok(res.toString(), MediaType.APPLICATION_JSON).build();
		}catch(Exception e) {
			return handleError("Query error", e);
		}
	}

	/**
	 * Create an error (500) Response. 
	 * The error info will be placed in the response body as JSON.
	 */
	protected static Response handleError(String message, Throwable cause) 
			throws WebApplicationException{
		return handleError(500, message, cause);
	}
	/**
	 * Create an error Response with the given HTTP status. 
	 * The error info will be placed in the response body as JSON.
	 */
	protected static Response handleError(int status, String message, Throwable cause) 
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
	protected static Response createErrorResponse(int status, String message) {
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
	
	public static class DAPApplication extends Application {
		@Override
		public Set<Class<?>> getClasses() {
			Set<Class<?>>classes = new HashSet<>();
			classes.add(RestDAPQuery.class);
			return classes;
		}
	}
}
