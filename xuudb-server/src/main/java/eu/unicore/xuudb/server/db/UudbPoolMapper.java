package eu.unicore.xuudb.server.db;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;


public interface UudbPoolMapper
{
	public MappingBean checkPoolExistence(@Param("pool") String pool);
	public int createPool(MappingBean bean);
	public void populatePool(@Param("pool") int pool, @Param("entry") String entry);
	public void removePool(@Param("poolName") String pool);
	public List<PoolInfoBean> listPools();
	
	public MappingBean getAliveMapping(@Param("pool") int pool, @Param("key") String key);
	public List<MappingBean> getFrozenMappings(@Param("pool") int pool, @Param("key") String key);
	public void updateAccessTime(@Param("id") int id);
	public MappingBean getFreeMapping(@Param("pool") int pool);
	public void addMapping(@Param("key") String key, @Param("id") int id);
	
	public List<MappingBean> getInactiveMappings(@Param("pool") int pool, @Param("since") Date since);
	public void freezeMapping(@Param("id") int id);

	public List<MappingBean> getOldMappings(@Param("pool") int pool, @Param("since") Date since);
	public void removeMapping(@Param("id") int id);
	
	public long countOccupiedMappings(@Param("pool") int pool);
	
	public List<MappingBean> getMappings(@Param("pool") Integer pool, @Param("status") String status);
	
	public List<MappingBean> findMappingsByValue(@Param("value") String value, @Param("valueType") String valueType);
	public List<MappingBean> findMappingsByKey(@Param("keyType") String keyType, @Param("key") String key);
}
