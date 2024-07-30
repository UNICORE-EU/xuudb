package eu.unicore.xuudb.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import eu.unicore.util.configuration.FilePropertiesHelper;
import eu.unicore.xuudb.server.db.DatabaseProperties;
import eu.unicore.xuudb.server.db.IPoolStorage;
import eu.unicore.xuudb.server.db.IStorage;
import eu.unicore.xuudb.server.db.MyBatisDatabase;
import eu.unicore.xuudb.server.db.MyBatisPoolDB;
import eu.unicore.xuudb.server.dynamic.EvaluationContext;
import eu.unicore.xuudb.server.dynamic.MappingType;
import eu.unicore.xuudb.server.dynamic.Pool;
import eu.unicore.xuudb.server.dynamic.PoolConfiguration;
import eu.unicore.xuudb.server.dynamic.PoolMapping;
import eu.unicore.xuudb.server.dynamic.xbeans.Configuration;

public class TestPools 
{
	
	@Test
	public void testExtrapolate()
	{
		String template = "xlogin";
		List<String> result = MyBatisPoolDB.extrapolateEntry(template);
		assertEquals(1, result.size());
		
		template = "xlogin[3-3]";
		result = MyBatisPoolDB.extrapolateEntry(template);
		assertEquals(1, result.size());

		template = "xlogin[100-300]";
		result = MyBatisPoolDB.extrapolateEntry(template);
		assertEquals(201, result.size());

		template = "ddd[100-300]ddd";
		result = MyBatisPoolDB.extrapolateEntry(template);
		assertEquals(201, result.size());

		template = "[100-300]ddd";
		result = MyBatisPoolDB.extrapolateEntry(template);
		assertEquals(201, result.size());
	}
	
	public void testMappingWithPersistence() throws Exception
	{
		File dir = new File("target/data");
		FileUtils.deleteDirectory(dir);
		
		ShutdownHook hook = new ShutdownHook();
		DatabaseProperties dbProps = new DatabaseProperties(FilePropertiesHelper.load(
				"src/test/resources/xuudb_server.conf"));
		IStorage db = new MyBatisDatabase(dbProps, hook);
		IPoolStorage storage = db.getPoolStorage();
		
		List<String> entries = new ArrayList<String>();
		Collections.addAll(entries, "xlogin1", "xlogin2", "pool[2-5]", "xlogin3", 
				"po[1-10]ol", "[100-100]pool");
		PoolConfiguration pc = new PoolConfiguration(Configuration.Factory.newInstance(), 
				Configuration.Factory.newInstance());
		Pool pool = new Pool("pool1", MappingType.uid, "dn", true, entries, pc);
		
		storage.initializePools(Collections.singleton(pool));
		
		PoolMapping mapping = new PoolMapping(pool, storage);

		EvaluationContext context = new EvaluationContext(" CN=Some User, C=PL", "foo", "foo", "foo", 
				null, null, null, null);
		
		mapping.applyAttributes(context, true, false);
		
		assertNotNull(context.getXlogin());
		assertFalse(context.getXlogin().contains("["));
		assertFalse(context.getXlogin().contains("]"));
		System.out.println(context.getXlogin());
		
		String saved = context.getXlogin();
		for (int i=0; i<10; i++)
		{
			context.setXlogin(null);
			mapping.applyAttributes(context, true, false);
			assertNotNull(context.getXlogin());
			assertEquals(saved, context.getXlogin());
		}

		EvaluationContext context2 = new EvaluationContext(" CN=Other User, C=PL", "foo", "foo", "foo", 
				null, null, null, null);
		mapping.applyAttributes(context2, true, false);
		
		assertNotNull(context2.getXlogin());
		assertFalse(context2.getXlogin().contains("["));
		assertFalse(context2.getXlogin().contains("]"));
		System.out.println(context2.getXlogin());
		assertNotSame(saved, context2.getXlogin());

		
		db.shutdown();
		
		db = new MyBatisDatabase(dbProps, hook);
		storage = db.getPoolStorage();
		mapping = new PoolMapping(pool, storage);
		context.setXlogin(null);
		mapping.applyAttributes(context, true, false);
		assertNotNull(context.getXlogin());
		assertEquals(saved, context.getXlogin());
		
		db.shutdown();
		
		FileUtils.deleteDirectory(dir);
	}
}
