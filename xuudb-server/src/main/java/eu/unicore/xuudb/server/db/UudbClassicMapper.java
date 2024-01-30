package eu.unicore.xuudb.server.db;

import java.util.List;

public interface UudbClassicMapper
{
	public LoginBean getRecord(String gcid, String token);
	public void insertRecord(LoginBean arg);
	public List<LoginBean> queryRecords(LoginBean arg);
	public int updateRecord(String gcid, String token, LoginBean arg);
	public int removeRecords(LoginBean arg);
	public List<String> listGcids();
}
