/*
 * Copyright (c) 2011-2012 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package de.fzj.unicore.xuudb.server.db;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.logging.log4j.Logger;

import de.fzj.unicore.xuudb.Log;

public class MyBatisSessionFactory
{
	private static final Logger log = Log.getLogger(Log.XUUDB_DB, MyBatisSessionFactory.class);
	
	private SqlSessionFactory sqlMapFactory;
	private Map<SqlSession, Thread> openSessions = new HashMap<SqlSession, Thread>();
	private boolean shutdown = false;
	
	public MyBatisSessionFactory(InputStream configuration, Properties properties)
	{
		SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
		sqlMapFactory = builder.build(configuration, properties);
	}
	
	public synchronized SqlSession openSession(boolean transactional)
	{
		if (shutdown)
			throw new IllegalStateException("Server is being shut down. No new operations can be served.");
		SqlSession ret = sqlMapFactory.openSession(!transactional);
		openSessions.put(ret, Thread.currentThread());
		return ret;
	}

	public synchronized SqlSession openBatchSession(boolean transactional)
	{
		if (shutdown)
			throw new IllegalStateException("Server is being shut down. No new operations can be served.");
		SqlSession ret = sqlMapFactory.openSession(ExecutorType.BATCH, !transactional);
		openSessions.put(ret, Thread.currentThread());
		return ret;
	}
	
	public synchronized void closeSession(SqlSession session)
	{
		if (openSessions.remove(session) == null)
			throw new IllegalStateException("Can not close session which was " +
					"not retrieved from this factory.");
		session.close();
	}
	
	public synchronized void shutdown()
	{
		shutdown = true;
		int c = 0;
		int maxIter = 50;
		int wait = 100;
		while (openSessions.size() > 0)
		{
			try
			{
				wait(wait);
			} catch (InterruptedException e) { /*ignored*/ }
			c++;
			if (c > maxIter)
			{
				log.fatal("Waited " + maxIter*wait + "ms and there are still open " +
						"(stalled?) DB connections. The faulty threads dump is following. " +
						"Closing the server forcefully, DB might be corrupted.");
				for (Thread t: openSessions.values())
					log.fatal("Stalled thread: " + t.getName() + "\n" + 
							Arrays.toString(t.getStackTrace()));
				break;
			}
		}
		((PooledDataSource)sqlMapFactory.getConfiguration().getEnvironment().getDataSource()).forceCloseAll();
	}
}
