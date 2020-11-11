/*
 * Copyright (c) 2012 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package de.fzj.unicore.xuudb.server.db;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.Logger;

import de.fzJuelich.unicore.xuudb.DatabaseType;
import de.fzJuelich.unicore.xuudb.ImportDatabaseDocument;
import de.fzJuelich.unicore.xuudb.LoginDataType;
import de.fzj.unicore.xuudb.Log;
import de.fzj.unicore.xuudb.server.SecurityToken;
import de.fzj.unicore.xuudb.server.Xlogin;

public class MyBatisClassicDB implements IClassicStorage {
	
	public static final Logger log = Log.getLogger(Log.XUUDB_DB, MyBatisClassicDB.class);
	private MyBatisSessionFactory factory;
	private static final String ST_CREATE_TABLE = "create-records-table";
	
	public MyBatisClassicDB(MyBatisSessionFactory factory) {
		this.factory = factory;
	
		SqlSession session = factory.openSession(false);
		try
		{
			session.update(ST_CREATE_TABLE);
		} finally 
		{
			factory.closeSession(session);
		}
	}
	
	@Override
	public String add(LoginDataType addRequest)
			throws IllegalArgumentException, PersistenceException
	{
		log.debug("Adding into UUDB");
		LoginBean bean = new LoginBean(addRequest);
		SqlSession session = factory.openSession(true);
		if (bean.getGcid() == null || bean.getToken() == null)
			throw new IllegalArgumentException("Token and GCID must be set when adding an entry");
		if (bean.getGcid() == "*")
			throw new IllegalArgumentException("Token and GCID must not be '*'");
		try
		{
			UudbClassicMapper mapper = session.getMapper(UudbClassicMapper.class);
			LoginBean existing = mapper.getRecord(bean.getGcid(), bean.getToken());

			if (existing != null) 
			{
				log.debug("Found existing entry, will update it instead of adding a duplicate");
				
				Xlogin xlogin = new Xlogin(existing.getXlogin());
				boolean added = xlogin.addLogin(bean.getXlogin());
				String ret = "OK";
				if (added)
				{
					log.debug("Adding new xlogin <" + bean.getXlogin() + "> to existing entry.");
					bean.setXlogin(xlogin.getEncoded());
					int updated = mapper.updateRecord(bean.getGcid(), bean.getToken(), bean);
					ret = "Updated " + updated + " rows";
				} else {
					log.debug("Xlogin <" + bean.getXlogin() +
							"> already exists in entry, skipping update.");
				}
				session.commit();
				return ret;
			}
			mapper.insertRecord(bean);
			session.commit();
		} finally 
		{
			factory.closeSession(session);
		}
		return "OK.";
	}

	@Override
	public String import_csv(ImportDatabaseDocument idd)
			throws IllegalArgumentException, PersistenceException
	{
		DatabaseType db = idd.getImportDatabase();
		LoginDataType[] logins = db.getDatabaseArray();
		int occuredErrors=0;
		boolean clear=false;
		if (idd.getImportDatabase().isSetClean())
			clear=idd.getImportDatabase().getClean();
		
		SqlSession session = factory.openSession(true);
		try
		{
			UudbClassicMapper mapper = session.getMapper(UudbClassicMapper.class);
			if(clear)
			{
				log.info("Clearing database before import.");
				mapper.removeRecords(new LoginBean());
			}
			
			for(int i = 0; i<logins.length; i++) {
				try
				{
					LoginBean bean = new LoginBean(logins[i]);
					mapper.insertRecord(bean);
				} catch (Exception e)
				{
					log.warn("Error importing entry: " + e);
					occuredErrors++;
				}
			}
			session.commit();
			return (logins.length-occuredErrors) + 
					"   certificates from   "  + logins.length + 
					"   were imported into the XUUDB.";
		} finally 
		{
			factory.closeSession(session);
		}
	}

	@Override
	public LoginDataType[] listDB(LoginDataType query) 
			throws IllegalArgumentException, PersistenceException
	{
		log.debug("Listing UUDB contents");
		LoginBean bean = new LoginBean(query);
		SqlSession session = factory.openSession(false);
		try
		{
			UudbClassicMapper mapper = session.getMapper(UudbClassicMapper.class);
			List<LoginBean> result = mapper.queryRecords(bean);
			LoginDataType[] ret = new LoginDataType[result.size()];
			for (int i=0; i<ret.length; i++)
				ret[i] = result.get(i).getAsLoginDataType();
			return ret;
		} finally 
		{
			factory.closeSession(session);
		}
	}

	@Override
	public String remove(LoginDataType removeArg)
			throws IllegalArgumentException, PersistenceException
	{
		log.debug("Removing records or xlogins from UUDB");
		LoginBean bean = new LoginBean(removeArg);
		SqlSession session = factory.openSession(true);
		try
		{
			UudbClassicMapper mapper = session.getMapper(UudbClassicMapper.class);
			
			List<LoginBean> existing = new ArrayList<LoginBean>();
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
						boolean removedL = xlogin.removeLogin(login);
						if (!removedL)
							log.debug("Xlogin " + login + " not found in entry " +
									xlogin + ", ignoring.");
					}
					if (xlogin.getNumberOfLogins() == 0)
					{
						log.debug("Removing entry");
						removed += mapper.removeRecords(lb);
					} else
					{
						log.debug("Updating xlogin to <" + xlogin + ">.");
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
		} finally 
		{
			factory.closeSession(session);
		}
	}

	@Override
	public LoginDataType checkToken(String gcid, String token)
			throws IllegalArgumentException, PersistenceException
	{
		log.debug("Checking if token is present in UUDB");
		SecurityToken stok = new SecurityToken(token);
		SqlSession session = factory.openSession(false);
		try
		{
			UudbClassicMapper mapper = session.getMapper(UudbClassicMapper.class);
			LoginBean existing = mapper.getRecord(gcid, stok.getToken());
			if (existing == null)
				return LoginDataType.Factory.newInstance();
			return existing.getAsLoginDataType();
		} finally 
		{
			factory.closeSession(session);
		}
	}

	@Override
	public LoginDataType checkDN(String gcid, String dn)
			throws IllegalArgumentException, PersistenceException
	{
		return checkToken(gcid, dn);
	}

	@Override
	public String update(String gcid, String token, LoginDataType login)
			throws IllegalArgumentException, PersistenceException
	{
		log.debug("Updateing UUDB");
		SecurityToken stok;
		stok = new SecurityToken(token);

		SqlSession session = factory.openSession(true);
		try
		{
			LoginBean bean = new LoginBean(login);
			UudbClassicMapper mapper = session.getMapper(UudbClassicMapper.class);
			int updated = mapper.updateRecord(gcid, stok.getToken(), bean);
			session.commit();
			return "Updated   " + updated + "   rows." ;
		} finally 
		{
			factory.closeSession(session);
		}
	}

	@Override
	public List<String> listGCIDs()
			throws IllegalArgumentException, PersistenceException
	{
		log.debug("Listing GCIDs");
		SqlSession session = factory.openSession(true);
		try
		{
			UudbClassicMapper mapper = session.getMapper(UudbClassicMapper.class);
			return mapper.listGcids();
		} finally 
		{
			factory.closeSession(session);
		}
	}
}
