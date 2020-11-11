/*
 * Copyright (c) 2011-2012 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package de.fzj.unicore.xuudb.server.db;

import java.io.IOException;
import java.io.InputStream;

import org.apache.ibatis.io.Resources;
import org.apache.logging.log4j.Logger;

import de.fzj.unicore.xuudb.Log;
import de.fzj.unicore.xuudb.server.IShutdownable;
import de.fzj.unicore.xuudb.server.ShutdownHook;


public class MyBatisDatabase implements IShutdownable, IStorage
{
	public static final Logger log = Log.getLogger(Log.XUUDB_DB, MyBatisDatabase.class);
	public static final String DEF_CONFIG_LOCATION = "mybatis/sqlconfig.xml";
	
	private MyBatisClassicDB classicDB;
	private MyBatisPoolDB poolDB;
	private MyBatisSessionFactory factory;
	
	public MyBatisDatabase(DatabaseProperties properties, ShutdownHook shutdown) 
			throws IOException
	{
		this(Resources.getResourceAsStream(DEF_CONFIG_LOCATION), properties, shutdown);
	}
	
	public MyBatisDatabase(InputStream configuration, DatabaseProperties properties, 
			ShutdownHook shutdown)
	{
		factory = new MyBatisSessionFactory(configuration, properties.getProperties());
		classicDB = new MyBatisClassicDB(factory);
		poolDB = new MyBatisPoolDB(factory);
	}
	
	@Override
	public IClassicStorage getClassicStorage() {
		return classicDB;
	}

	@Override
	public IPoolStorage getPoolStorage() {
		return poolDB;
	}

	@Override
	public void shutdown()
	{
		factory.shutdown();
	}

	@Override
	public String getNameOfService()
	{
		return "XUUDB MyBatis Database";
	}
}
