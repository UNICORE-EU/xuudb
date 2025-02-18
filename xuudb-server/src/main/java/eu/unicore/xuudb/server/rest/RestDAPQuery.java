package eu.unicore.xuudb.server.rest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;

import eu.unicore.xuudb.server.dynamic.DAPConfiguration;
import eu.unicore.xuudb.server.dynamic.EvaluationContext;
import eu.unicore.xuudb.server.dynamic.EvaluationEngine;
import eu.unicore.xuudb.server.dynamic.Rule;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * @author schuller
 */
@Path("/")
public class RestDAPQuery extends RestBase {

	private DAPConfiguration config;

	private final EvaluationEngine engine;

	public RestDAPQuery() {
		this.engine = new EvaluationEngine();
	}

	public void setConfig(DAPConfiguration config) {
		this.config = config;
	}

	@GET
	@Path("/query")
	@Produces("application/json")
	public Response query(@QueryParam("dn") String dn, @QueryParam("role") String role,
			@QueryParam("xlogin") String xlogin, @QueryParam("group")String group,
			@QueryParam("vo") String vo) {
		try {
			if(dn==null || role==null) {
				return handleError(400, "Query parameters 'dn' and 'role' must be given", null);
			}
			return Response.ok(evaluate(dn, role, xlogin, group, vo).toString(),
					MediaType.APPLICATION_JSON).build();
		}catch(Exception e) {
			return handleError(400, "Query error", e);
		}
	}

	private JSONObject evaluate(String dn, String role, String xlogin, String gid, String vo) {
		EvaluationContext ctx = new EvaluationContext(dn, 
				null, 
				role, 
				vo, 
				null, 
				xlogin, 
				gid, 
				null);
		ctx.setDryRun(false);
		List<Rule> rules = config.getRules();
		engine.evaluate(rules, ctx, false);
		JSONObject resp = new JSONObject();
		resp.put("group", ctx.getGid());
		resp.put("xlogin", ctx.getXlogin());
		resp.put("supplementaryGroups", ctx.getSupplementaryGids().toArray(new String[0]));
		return resp;
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
