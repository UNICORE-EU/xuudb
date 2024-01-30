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
	private MyBatisJSONClassicDB classicJSONDB;
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
		String charset = properties.getValue(DatabaseProperties.CHARSET);
		log.debug("Using charset '{}'", charset);
		classicDB = new MyBatisClassicDB(factory, charset);
		poolDB = new MyBatisPoolDB(factory, charset);

		classicJSONDB = new MyBatisJSONClassicDB(factory, charset);
	}
	
	@Override
	public IClassicStorage getClassicStorage() {
		return classicDB;
	}

	@Override
	public IRESTClassicStorage getRESTClassicStorage() {
		return classicJSONDB;
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
