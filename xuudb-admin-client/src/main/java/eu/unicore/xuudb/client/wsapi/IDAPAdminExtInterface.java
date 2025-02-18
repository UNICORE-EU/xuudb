package eu.unicore.xuudb.client.wsapi;

import java.util.Date;

import eu.unicore.xuudb.xbeans.MappingDataType;
import eu.unicore.xuudb.xbeans.PoolInfoType;

public interface IDAPAdminExtInterface {

	public void freeze(String id,String pool) throws Exception;

	public void freeze(Date date, String pool) throws Exception;

	public void remove(String id,String pool) throws Exception;

	public void remove(Date date, String pool) throws Exception;

	public MappingDataType[] list(String type, String pool)
			throws Exception;

	public MappingDataType[] findReverse(String type, String value) throws Exception;

	public MappingDataType[] find(String type, String value)
			throws Exception;

	public PoolInfoType[] listPools() throws Exception;
	
	public void removePool(String id) throws Exception;

}
