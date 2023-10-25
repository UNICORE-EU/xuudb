package de.fzj.unicore.xuudb.server.db;

import java.util.List;

import org.json.JSONObject;

public interface IRESTClassicStorage {

	/**
	 * add a new entry to the storage
	 * 
	 * @param addRequest
	 * @return OK or error message
	 */
	public String add(JSONObject addRequest);

	/**
	 * import a list of entries
	 * 
	 * @param idd
	 * @return
	 */
	public String import_csv(JSONObject idd);

	/**
	 * list the database
	 * @param query
	 * @return
	 */
	public List<JSONObject> listDB(JSONObject query);

	/**
	 * remove an entry
	 * @param query
	 * @return
	 */
	public String remove(JSONObject query);

	/**
	 * reads login info from the database, by matching the given token to the database
	 * entries
	 * 
	 * @param gcid
	 * @param token
	 * @return login info
	 */
	public JSONObject checkToken(String gcid, String token);

	/**
	 * reads login info from the database by checking DN
	 * 
	 * @param gcid
	 * @param dn
	 * @return login info
	 */
	public JSONObject checkDN(String gcid, String dn);

	/**
	 * update an entry
	 * @param gcid
	 * @param token
	 * @param login
	 * @return
	 */
	public String update(String gcid, String token, JSONObject login);

	/**
	 * retrieve a list of all GCIDs currently in use by the XUUDB
	 * 
	 * @return
	 */
	public List<String>listGCIDs();

}