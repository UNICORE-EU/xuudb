package de.fzj.unicore.xuudb.server.db;

import java.util.Date;
import java.util.List;

import de.fzj.unicore.xuudb.server.dynamic.MappingStatus;
import de.fzj.unicore.xuudb.server.dynamic.Pool;

public interface IPoolStorage {
	public void initializePools(Iterable<Pool> pools);
	public void removePool(String poolName);
	public List<PoolInfoBean> listPools();
	
	public String getOrCreateMapping(Pool pool, String key, boolean dryRun);
	public void freezeInactive(Pool pool, Date inactiveFrom);
	public void freezeSpecified(Pool pool, String value);
	public void deleteOld(Pool pool, Date inactiveFrom);
	public void deleteSpecified(Pool pool, String value);
	public void checkEmptiness(Pool pool, int warningThreshold);
	
	public List<MappingBean> listMappings(Integer poolId, MappingStatus status);
	public List<MappingBean> listMappingsByValue(String valueType, String value);
	public List<MappingBean> listMappingsByKey(String keyType, String keyValue);
	
}