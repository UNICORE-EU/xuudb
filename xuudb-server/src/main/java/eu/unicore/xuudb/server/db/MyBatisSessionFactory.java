package eu.unicore.xuudb.server.db;

import java.io.InputStream;
import java.util.Properties;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class MyBatisSessionFactory
{
	private SqlSessionFactory sqlMapFactory;
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
		return sqlMapFactory.openSession(!transactional);
	}

	public synchronized SqlSession openBatchSession(boolean transactional)
	{
		if (shutdown)
			throw new IllegalStateException("Server is being shut down. No new operations can be served.");
		return sqlMapFactory.openSession(ExecutorType.BATCH, !transactional);
	}
	
	public synchronized void shutdown()
	{
		shutdown = true;
		((PooledDataSource)sqlMapFactory.getConfiguration().getEnvironment().getDataSource()).forceCloseAll();
	}
}
