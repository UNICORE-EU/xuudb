package de.fzj.unicore.xuudb.server.db;

import java.util.List;

import de.fzJuelich.unicore.xuudb.ImportDatabaseDocument;
import de.fzJuelich.unicore.xuudb.LoginDataType;

public interface IClassicStorage {

	/**
	 * add a new entry to the storage
	 * 
	 * @param addRequest
	 * @return OK or error message
	 */
	public String add(LoginDataType addRequest);

	/**
	 * import a list of entries
	 * 
	 * @param idd
	 * @return
	 */
	public String import_csv(ImportDatabaseDocument idd);

	/**
	 * list the database
	 * @param query
	 * @return
	 */
	public LoginDataType[] listDB(LoginDataType query);

	/**
	 * remove an entry
	 * @param query
	 * @return
	 */
	public String remove(LoginDataType query);

	/**
	 * reads login info from the database, by matching the given token to the database
	 * entries
	 * 
	 * @param gcid
	 * @param token
	 * @return login info
	 */
	public LoginDataType checkToken(String gcid, String token);

	/**
	 * reads login info from the database by checking DN
	 * 
	 * @param gcid
	 * @param dn
	 * @return login info
	 */
	public LoginDataType checkDN(String gcid, String dn);

	/**
	 * update an entry
	 * @param gcid
	 * @param token
	 * @param login
	 * @return
	 */
	public String update(String gcid, String token, LoginDataType login);

	/**
	 * retrieve a list of all GCIDs currently in use by the XUUDB
	 * 
	 * @return
	 */
	public List<String>listGCIDs();

}