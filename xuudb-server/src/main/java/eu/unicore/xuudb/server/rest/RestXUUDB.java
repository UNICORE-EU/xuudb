package eu.unicore.xuudb.server.rest;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONObject;

import eu.unicore.xuudb.server.db.IRESTClassicStorage;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * @author schuller
 */
@Path("/")
public class RestXUUDB extends RestBase {
	
	private IRESTClassicStorage storage;

	public RestXUUDB() {}
	
	public void setStorage(IRESTClassicStorage storage) {
		this.storage = storage;
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

	public static class XUUDBApplication extends Application {
		@Override
		public Set<Class<?>> getClasses() {
			Set<Class<?>>classes = new HashSet<>();
			classes.add(RestXUUDB.class);
			return classes;
		}
	}
}
