package eu.unicore.xuudb.server.dynamic;

import org.apache.logging.log4j.Logger;

import eu.unicore.xuudb.xbeans.SimplifiedAttributeType;
import eu.emi.security.authn.x509.impl.X500NameUtils;
import eu.unicore.xuudb.Log;
import eu.unicore.xuudb.server.db.IPoolStorage;

/**
 * Sets attributes using a configured pool
 * 
 * @author K. Benedyczak
 */
public class PoolMapping extends Mapping
{
	public static final Logger log = Log.getLogger(Log.XUUDB_SERVER, PoolMapping.class);
	public static final String ID = "pool";
	private IPoolStorage storage;
	private Pool pool;
	
	public PoolMapping(Pool pool, IPoolStorage storage)
	{
		super(pool.getId(), pool.getType());
		this.storage = storage;
		this.pool = pool;
	}
	
	@Override
	public void applyAttributes(EvaluationContext context, boolean overwrite, boolean dryRun)
	{
		String dbKey = getKey(context);
		if (dbKey == null)
			return;

		String poolValue = storage.getOrCreateMapping(pool, dbKey, dryRun);
		if (poolValue == null)
		{
			log.warn("Pool " + pool.getId() + " is empty. The mapping is skipped.");
			return;
		}
		log.debug("Value of the key of type {} used by the pool {} is: {}",
				pool.getKeyType(), pool.getId(), poolValue);
		if (pool.getType().equals(MappingType.uid))
		{
			log.debug("Setting xlogin to {}", poolValue);
			context.setXlogin(poolValue);
		} else if (pool.getType().equals(MappingType.gid))
		{
			log.debug("Setting gid to {}", poolValue);
			context.setGid(poolValue);			
		} else 
			throw new RuntimeException("Bug: pool configured with a type which is " +
					"neither uid nor gid: " + pool.getType());
	}
	
	
	private String getKey(EvaluationContext context)
	{
		String key = null;
		
		switch (pool.getKeyType())
		{
		case dn:
			key = X500NameUtils.getComparableForm(context.getUserDN());
			break;
		case issuer:
			key = context.getIssuerDN();
			break;
		case role:
			key = context.getRole();
			break;
		case vo:
			key = context.getVo();
			break;
		case generic:
			for (SimplifiedAttributeType extra: context.getExtraAttributes())
			{
				if (extra.getName().equals(pool.getGenericKey()))
				{
					if (extra.getValueArray() != null && extra.getValueArray().length == 1)
						key = extra.getValueArray(0);
					else 
					{
						int numVals = extra.getValueArray() == null ? 0 : extra.getValueArray().length;
						log.warn("Generic attribute " + extra.getName() + 
								" which is used as a key for " + pool.getId() + " pool" +
								" has " + numVals + " values and should have exactly one. " +
								"Either it shouldn't be used as a key or your attribute " +
								"sources are misconfigured. The mapping is skipped.");
						return null;
					}
				}
			}
			break;
		}
		if (key == null || key.equals(""))
		{
			log.debug("Key of type {} used by pool {} is not available. The mapping is skipped.",
					pool.getKeyType(), pool.getId());
			return null;
		}
		log.debug("Key of type {} used by pool {} value is: {}",
				pool.getKeyType(), pool.getId(), key);
		return key;
	}
}
