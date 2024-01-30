package eu.unicore.xuudb.client.wsapi;

import de.fzJuelich.unicore.xuudb.LoginDataType;


public interface IAdminExtInterface {

		
	public XUUDBResponse add(String gcId,String certPem,String xlogin,String role,String projects) throws Exception;

	public XUUDBResponse adddn(String gcId,String dn,String xlogin,String role,String projects) throws Exception;

	public XUUDBResponse  list(LoginDataType data)  throws Exception;
	
	public XUUDBResponse remove(LoginDataType data) throws Exception;
	
	public XUUDBResponse update(String gcId,String token,LoginDataType data) throws Exception;
	
	public XUUDBResponse exportCsv() throws Exception;

	public XUUDBResponse importCsv(LoginDataType[] data,boolean clear) throws Exception;

}
