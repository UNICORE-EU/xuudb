package eu.unicore.xuudb.server.dynamic;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import eu.unicore.util.configuration.ConfigurationException;
import eu.unicore.xuudb.Log;
import eu.unicore.xuudb.server.FileWatcher;
import eu.unicore.xuudb.server.db.IPoolStorage;
import eu.unicore.xuudb.server.dynamic.xbeans.Configuration;
import eu.unicore.xuudb.server.dynamic.xbeans.DynamicAttributesDocument;
import eu.unicore.xuudb.server.dynamic.xbeans.DynamicAttributesDocument.DynamicAttributes;
import eu.unicore.xuudb.server.dynamic.xbeans.Pools;
import eu.unicore.xuudb.server.dynamic.xbeans.Rules;


/**
 * Loads the configuration of DAP module.
 * @author K. Benedyczak
 */
public class DAPConfiguration
{
	private static final int DEF_UPDATE_CHECK = 10000;
	private static final Logger log = Log.getLogger(Log.XUUDB_SERVER, DAPConfiguration.class);

	private final File file;
	private final ExpressionParser spelParser;
	private List<Rule> rules;
	private Map<String, Pool> pools;
	private ScheduledExecutorService poolsWatchdogExecutor;
	private final IPoolStorage storage;

	public DAPConfiguration(File file, IPoolStorage storage) 
			throws IOException, ConfigurationException {
		this(file, storage, DEF_UPDATE_CHECK);
	}
	
	public DAPConfiguration(File file, IPoolStorage storage, int updateInterval) 
			throws IOException, ConfigurationException {
		this.file = file;
		poolsWatchdogExecutor = Executors.newSingleThreadScheduledExecutor();
		spelParser = new SpelExpressionParser();
		this.storage = storage;
		try {
			parse();
		} catch (XmlException e) {
			throw new ConfigurationException("Problem loading dynamic attributes " +
					"configuration file: " + e.getMessage(), e);
		} catch (ParseException e) {
			throw new ConfigurationException("Problem loading dynamic attributes " +
					"configuration file: " + e.getMessage(), e);
		}
		if (updateInterval > 0)
			startConfigWatcher(updateInterval);
	}

	private void startConfigWatcher(int interval) throws FileNotFoundException
	{
		FileWatcher r = new FileWatcher(file, ()->
		{
			try {
				log.debug("Configuration file change detected, reloading...");
				parse();
				log.info("Successfully reloaded the configuration file");
			} catch (Exception e) {
				Log.logException("Updated configuration file is invalid, " +
						"using the old configuration.", e, log);
			}
		});
		r.schedule(interval, TimeUnit.MILLISECONDS);
		log.info("Config file monitoring enabled with interval: " + (interval/1000) + "s");
	}

	
	private void parse() throws IOException, XmlException, ParseException 
	{
		DynamicAttributesDocument mainDoc = DynamicAttributesDocument.Factory.parse(
				new BufferedInputStream(new FileInputStream(file)));
		List<?> validationErrors = new ArrayList<>();
		boolean valid = mainDoc.validate(new XmlOptions().setErrorListener(validationErrors));
		if (!valid)
			throw new ParseException(validationErrors.toString(), -1);
		DynamicAttributes main = mainDoc.getDynamicAttributes();

		
		Configuration generalConfiguration = main.getDefaultConfiguration();
		int timeout = ProcessInvoker.MAX_TIMEOUT;
		if (generalConfiguration == null)
			generalConfiguration = Configuration.Factory.newInstance();
		if (generalConfiguration.isSetHandlerInvocationTimeLimit())
			timeout = generalConfiguration.getHandlerInvocationTimeLimit();
		
		Pools pools = main.getPools();
		Map<String, Pool> newPools = parsePools(pools, generalConfiguration);
		
		Rules rules = main.getRules();
		List<Rule> newRules = parseRules(rules, newPools, timeout);
		
		
		int updateDelay = main.isSetPoolMonitoringDelay() ? main.getPoolMonitoringDelay() : 24*3600;
		
		update(newRules, newPools, updateDelay);
	}
	
	private synchronized void update(List<Rule> newRules, Map<String, Pool> pools, long period)
	{
		storage.initializePools(pools.values());
		poolsWatchdogExecutor.shutdownNow();
		if (period > 0)
		{
			poolsWatchdogExecutor = Executors.newSingleThreadScheduledExecutor();
			poolsWatchdogExecutor.scheduleAtFixedRate(new PoolsWatchdog(storage, pools.values()), 
				period, period, TimeUnit.SECONDS);
			log.info("Started a pools monitoring task with the interval " + period + "s");
		} else
			log.info("Pools monitoring task is disabled");
		rules = newRules;
		this.pools = pools;
	}

	public synchronized List<Rule> getRules()
	{
		return rules;
	}

	public synchronized Map<String, Pool> getPools()
	{
		return pools;
	}
	
	private Map<String, Pool> parsePools(Pools xmlPools, Configuration generalConfiguration) 
			throws ParseException, IOException
	{
		Map<String, Pool> ret = new HashMap<>();
		if (xmlPools == null)
			return ret;
		eu.unicore.xuudb.server.dynamic.xbeans.Pool[] xmlPoolA = xmlPools.getPoolArray();
		if (xmlPoolA == null || xmlPoolA.length == 0)
			return ret;
		
		for (eu.unicore.xuudb.server.dynamic.xbeans.Pool xmlPool: xmlPoolA)
		{
			MappingType type = getMappingType(xmlPool.getType());
			boolean isPrecreated = xmlPool.getPrecreated();
			List<String> entries = new ArrayList<String>();
			String[] ids = xmlPool.getIdArray();
			if (ids != null)
				for (int i=0; i<ids.length; i++)
					entries.add(ids[i]);
			String[] files = xmlPool.getFileArray();
			if (files != null)
				for (int i=0; i<files.length; i++)
					loadFileIds(entries, files[i]);
			Configuration poolCfg = xmlPool.getConfiguration();
			if (poolCfg == null)
				poolCfg = Configuration.Factory.newInstance();
			Pool p = new Pool(xmlPool.getId2(), type, xmlPool.getKey(), isPrecreated, entries, 
					new PoolConfiguration(poolCfg, generalConfiguration));
			ret.put(p.getId(), p);
		}
		return ret;
	}
	
	
	private List<Rule> parseRules(Rules rules, Map<String, Pool> newPools, int timeout) throws ParseException
	{
		List<Rule> ret = new ArrayList<Rule>();
		if (rules == null)
			return ret;
		eu.unicore.xuudb.server.dynamic.xbeans.Rule[] rulesA = rules.getRuleArray();
		if (rulesA == null || rulesA.length == 0)
			return ret;
		for (eu.unicore.xuudb.server.dynamic.xbeans.Rule xmlRule: rulesA)
		{
			String rawCond = xmlRule.getCondition();
			if (rawCond == null)
				throw new ParseException("Rule without a condition found", -1);
			Expression e = parseSpEL(rawCond);
			boolean overwriteExisting = xmlRule.isSetOverwriteExisting() ? 
					xmlRule.getOverwriteExisting() : false;
			eu.unicore.xuudb.server.dynamic.xbeans.Mapping xmlMappings[]  = xmlRule.getMappingArray();
			if (xmlMappings == null || xmlMappings.length == 0)
				throw new ParseException("Rule without mapping found", -1);
			List<eu.unicore.xuudb.server.dynamic.Mapping> mappings = 
					new ArrayList<eu.unicore.xuudb.server.dynamic.Mapping>();
			for (eu.unicore.xuudb.server.dynamic.xbeans.Mapping xmlMapping: xmlMappings)
			{
				mappings.add(parseMapping(xmlMapping, newPools, timeout));
			}
			ret.add(new Rule(e, overwriteExisting, mappings));
		}
		return ret;
	}

	private Mapping parseMapping(eu.unicore.xuudb.server.dynamic.xbeans.Mapping xmlMapping, 
			Map<String, Pool> newPools, int timeout) throws ParseException
	{
		String contents = xmlMapping.getStringValue();
		String type = xmlMapping.getType();
		String maps = xmlMapping.getMaps();
		if (FixedMapping.ID.equalsIgnoreCase(type))
		{
			if (maps == null)
				throw new ParseException("The 'maps' parameter is required for " + type + " mapping", -1);
			return new FixedMapping(contents, getMappingType(maps));
		} else if (ScriptMapping.ID.equalsIgnoreCase(type))
		{
			if (maps == null)
				throw new ParseException("The 'maps' parameter is required for " + type + " mapping", -1);
			return new ScriptMapping(contents, getMappingType(maps), timeout);
		} else if (PoolMapping.ID.equalsIgnoreCase(type))
		{
			Pool pool = newPools.get(contents.trim());
			if (pool == null)
				throw new ParseException("Unknown pool used in mapping: " + 
						contents.trim(), -1);
			PoolMapping ret = new PoolMapping(pool, storage);
			if (maps != null && ret.getType() != getMappingType(maps))
				throw new ParseException("Inconsistent pool type (" + ret.getType() 
						+ ") and mapping 'maps' attribute (" + maps + ")." +
						" Those values must be the same, or the 'maps' attribute " +
						"can be removed for pool mapping.", -1);
			return ret;
		} else
		{
			throw new ParseException("Unknown mapping type found: " + type, -1);
		}
	}
	
	private void loadFileIds(List<String> where, String file) throws IOException
	{
		try(BufferedReader br = new BufferedReader(new FileReader(file))){
			String line;
			while ((line = br.readLine()) != null)
				where.add(line.trim());
		}
	}
	
	private MappingType getMappingType(String s) throws ParseException
	{
		try
		{
			return MappingType.valueOf(s);
		} catch (IllegalArgumentException e)
		{
			throw new ParseException("Got wrong value of what is generated by a mapping (or pool): " 
					+ s + ", must be one of " + Arrays.toString(MappingType.values()), -1);
		}
	}
	
	private Expression parseSpEL(String expr) throws ParseException
	{
		try
		{
			return spelParser.parseExpression(expr);
		} catch (org.springframework.expression.ParseException e)
		{
			throw new ParseException("Problem parsing SpEL '" + expr + "': " + e.getMessage(), 
					e.getPosition());
		} catch (Exception ee)
		{
			throw new ParseException("Other problem parsing SpEL expression '" + 
					expr + "': " + ee.toString(), -1);
		}
	}

}
