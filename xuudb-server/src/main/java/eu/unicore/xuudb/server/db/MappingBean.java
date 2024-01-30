package eu.unicore.xuudb.server.db;

import java.util.Date;

public class MappingBean {
	private Date lastAccess;
	private Date freezeTime;
	private String entry;
	private String mappingKey;
	private String poolName;
	private Integer id;
	private String mappingKeyType;
	private String mappingValueType;
	
	public String getEntry() {
		return entry;
	}
	public void setEntry(String entry) {
		this.entry = entry;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	/**
	 * @return the lastAccess
	 */
	public Date getLastAccess()
	{
		return lastAccess;
	}
	/**
	 * @param lastAccess the lastAccess to set
	 */
	public void setLastAccess(Date lastAccess)
	{
		this.lastAccess = lastAccess;
	}
	/**
	 * @return the freezeTime
	 */
	public Date getFreezeTime()
	{
		return freezeTime;
	}
	/**
	 * @param freezeTime the freezeTime to set
	 */
	public void setFreezeTime(Date freezeTime)
	{
		this.freezeTime = freezeTime;
	}
	/**
	 * @return the mappingKey
	 */
	public String getMappingKey()
	{
		return mappingKey;
	}
	/**
	 * @param mappingKey the mappingKey to set
	 */
	public void setMappingKey(String mappingKey)
	{
		this.mappingKey = mappingKey;
	}
	/**
	 * @return the poolName
	 */
	public String getPoolName()
	{
		return poolName;
	}
	/**
	 * @param poolName the poolName to set
	 */
	public void setPoolName(String poolName)
	{
		this.poolName = poolName;
	}
	/**
	 * @return the mappingKeyType
	 */
	public String getMappingKeyType()
	{
		return mappingKeyType;
	}
	/**
	 * @param mappingKeyType the mappingKeyType to set
	 */
	public void setMappingKeyType(String mappingKeyType)
	{
		this.mappingKeyType = mappingKeyType;
	}
	/**
	 * @return the mappingValueType
	 */
	public String getMappingValueType()
	{
		return mappingValueType;
	}
	/**
	 * @param mappingValueType the mappingValueType to set
	 */
	public void setMappingValueType(String mappingValueType)
	{
		this.mappingValueType = mappingValueType;
	}
}
