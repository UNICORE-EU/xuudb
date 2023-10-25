package de.fzj.unicore.xuudb.server.rest;

import org.json.JSONObject;

import de.fzj.unicore.xuudb.server.db.IRESTClassicStorage;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

/**
 * @author schuller
 */
@Path("/")
public class XUUDBPublic {
	
	private IRESTClassicStorage storage;

	public XUUDBPublic() {}
	
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
		info.put("storage", storage.toString());
		
		return info.toString();
	}

}
