package de.fzj.unicore.xuudb.server.db;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import de.fzj.unicore.xuudb.Log;
import de.fzj.unicore.xuudb.server.SecurityToken;
import de.fzj.unicore.xuudb.server.Xlogin;

public class MyBatisJSONClassicDB implements IRESTClassicStorage {
	
	public static final Logger log = Log.getLogger(Log.XUUDB_DB, MyBatisJSONClassicDB.class);
	private MyBatisSessionFactory factory;
	private static final String ST_CREATE_TABLE = "create-records-table";
	
	public MyBatisJSONClassicDB(MyBatisSessionFactory factory, String charset) {
		this.factory = factory;
		try(SqlSession session = factory.openSession(false))
		{
			session.update(ST_CREATE_TABLE, charset);
		}
	}
	
	@Override
	public String add(JSONObject addRequest)
			throws IllegalArgumentException, PersistenceException
	{
		LoginBean bean = new LoginBean(addRequest);
		if (bean.getGcid() == null || bean.getToken() == null)
			throw new IllegalArgumentException("Token and GCID must be set when adding an entry");
		if (bean.getGcid() == "*")
			throw new IllegalArgumentException("Token and GCID must not be '*'");
		try(SqlSession session = factory.openSession(true))
		{
			UudbClassicMapper mapper = session.getMapper(UudbClassicMapper.class);
			LoginBean existing = mapper.getRecord(bean.getGcid(), bean.getToken());
			if (existing != null) 
			{
				Xlogin xlogin = new Xlogin(existing.getXlogin());
				boolean added = xlogin.addLogin(bean.getXlogin());
				String ret = "OK";
				if (added)
				{
					log.debug("Adding new xlogin <{}> to existing entry", bean.getXlogin());
					bean.setXlogin(xlogin.getEncoded());
					int updated = mapper.updateRecord(bean.getGcid(), bean.getToken(), bean);
					ret = "Updated " + updated + " rows";
				} else {
					log.debug("Xlogin <{}> already exists in entry, skipping update", bean.getXlogin());
				}
				session.commit();
				return ret;
			}
			mapper.insertRecord(bean);
			session.commit();
		}
		return "OK.";
	}

	@Override
	public String import_csv(JSONObject idd)
			throws IllegalArgumentException, PersistenceException
	{
		JSONArray logins = idd.optJSONArray("database");
		if(logins==null)logins = new JSONArray();
		int occuredErrors = 0;
		boolean clear = idd.optBoolean("clear", false);
		try(SqlSession session = factory.openSession(true))
		{
			UudbClassicMapper mapper = session.getMapper(UudbClassicMapper.class);
			if(clear)
			{
				log.info("Clearing database before import.");
				mapper.removeRecords(new LoginBean());
			}
			for(int i = 0; i<logins.length(); i++) {
				JSONObject login = logins.getJSONObject(i);
				try
				{
					LoginBean bean = new LoginBean(login);
					mapper.insertRecord(bean);
				} catch (Exception e)
				{
					log.warn("Error importing entry: " + e);
					occuredErrors++;
				}
			}
			session.commit();
			return (logins.length()-occuredErrors) + 
					"   certificates from   "  + logins.length() + 
					"   were imported into the XUUDB.";
		}
	}

	@Override
	public List<JSONObject> listDB(JSONObject query) 
			throws IllegalArgumentException, PersistenceException
	{
		LoginBean bean = new LoginBean(query);
		try(SqlSession session = factory.openSession(false))
		{
			UudbClassicMapper mapper = session.getMapper(UudbClassicMapper.class);
			List<LoginBean> result = mapper.queryRecords(bean);
			List<JSONObject> ret = new ArrayList<>();
			for (LoginBean r: result)
				ret.add(r.getAsJSON());
			return ret;
		}
	}

	@Override
	public String remove(JSONObject removeArg)
			throws IllegalArgumentException, PersistenceException
	{
		LoginBean bean = new LoginBean(removeArg);
		try(SqlSession session = factory.openSession(true))
		{
			UudbClassicMapper mapper = session.getMapper(UudbClassicMapper.class);
			List<LoginBean> existing = new ArrayList<>();
			if (bean.getToken() != null && bean.getXlogin() != null)
			{
				if (bean.getGcid() == null || bean.getGcid().equals("*"))
				{
					LoginBean query = new LoginBean();
					query.setToken(bean.getToken());
					existing = mapper.queryRecords(query);
				} else
				{
					existing.add(mapper.getRecord(bean.getGcid(), 
							bean.getToken()));
				}
			}

			int removed = 0;
			int updated = 0;
			//edit or delete
			if (existing.size() > 0)
			{
				for (LoginBean lb: existing)
				{
					Xlogin xlogin = new Xlogin(lb.getXlogin());
					Xlogin toRemove = new Xlogin(bean.getXlogin());
					for (String login: toRemove)
					{
						xlogin.removeLogin(login);
					}
					if (xlogin.getNumberOfLogins() == 0)
					{
						removed += mapper.removeRecords(lb);
					} else
					{
						log.debug("Updating xlogin to <{}>", xlogin);
						lb.setXlogin(xlogin.getEncoded());
						updated += mapper.updateRecord(lb.getGcid(), lb.getToken(), lb);
					}
				}
			} else
			{
				removed = mapper.removeRecords(bean);
			}
			session.commit();
			return "Removed records: " + removed + "; updated records: " + updated;
		}
	}

	@Override
	public JSONObject checkDN(String gcid, String dn)
			throws IllegalArgumentException, PersistenceException
	{
		SecurityToken stok = new SecurityToken(dn);
		try(SqlSession session = factory.openSession(false))
		{
			UudbClassicMapper mapper = session.getMapper(UudbClassicMapper.class);
			LoginBean existing = mapper.getRecord(gcid, stok.toString());
			if (existing == null)
				return new JSONObject();
			return existing.getAsJSON();
		}
	}

	@Override
	public String update(String gcid, String dn, JSONObject login)
			throws IllegalArgumentException, PersistenceException
	{
		SecurityToken stok = new SecurityToken(dn);
		try(SqlSession session = factory.openSession(true))
		{
			LoginBean bean = new LoginBean(login);
			UudbClassicMapper mapper = session.getMapper(UudbClassicMapper.class);
			int updated = mapper.updateRecord(gcid, stok.toString(), bean);
			session.commit();
			return "Updated   " + updated + "   rows." ;
		}
	}

	@Override
	public List<String> listGCIDs()
			throws IllegalArgumentException, PersistenceException
	{
		try(SqlSession session = factory.openSession(true))
		{
			UudbClassicMapper mapper = session.getMapper(UudbClassicMapper.class);
			return mapper.listGcids();
		}
	}
	
	public String toString() {
		return getClass().getSimpleName();
	}
}
