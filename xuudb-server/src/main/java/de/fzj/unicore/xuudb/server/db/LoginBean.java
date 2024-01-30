package de.fzj.unicore.xuudb.server.db;

import org.json.JSONObject;

import de.fzJuelich.unicore.xuudb.LoginDataType;
import de.fzj.unicore.xuudb.server.SecurityToken;

/**
 * Used as MyBatis DAO object
 * @author K. Benedyczak
 */
public class LoginBean
{
	private String token;
	private String gcid;
	private String role;
	private String projects;
	private String xlogin;
	
	public LoginBean()
	{
	}

	/**
	 * Converts {@link LoginDataType} object into a POJO bean.
	 * The token string is being normalized if is present.
	 * @param src
	 */
	public LoginBean(LoginDataType src)
	{
		if (src.getToken() != null)
		{
			SecurityToken stok = new SecurityToken(src.getToken());
			this.token = stok.toString();
		}
		this.gcid = src.getGcID();
		this.role = src.getRole();
		this.projects = src.getProjects();
		this.xlogin = src.getXlogin();
	}
	
	public LoginBean(JSONObject src)
	{
		if (src.optString("token", null) != null)
		{
			SecurityToken stok = new SecurityToken(src.getString("token"));
			this.token = stok.toString();
		}
		this.gcid = src.optString("gcid", null);
		this.role = src.optString("role", null);
		this.projects = src.optString("projects", null);
		this.xlogin = src.optString("xlogin", null);
	}

	public LoginDataType getAsLoginDataType()
	{
		LoginDataType ret = LoginDataType.Factory.newInstance();
		ret.setGcID(gcid);
		ret.setProjects(projects);
		ret.setRole(role);
		ret.setToken(token);
		ret.setXlogin(xlogin);
		return ret;
	}

	public JSONObject getAsJSON()
	{
		JSONObject ret = new JSONObject();
		ret.put("gcid", gcid);
		ret.put("projects", projects);
		ret.put("role", role);
		ret.put("token", token);
		ret.put("xlogin", xlogin);
		return ret;
	}

	/**
	 * @return the token
	 */
	public String getToken()
	{
		return token;
	}

	/**
	 * @param token the token to set
	 */
	public void setToken(String token)
	{
		this.token = token;
	}

	/**
	 * @return the gcid
	 */
	public String getGcid()
	{
		return gcid;
	}

	/**
	 * @param gcid the gcid to set
	 */
	public void setGcid(String gcid)
	{
		this.gcid = gcid;
	}

	/**
	 * @return the role
	 */
	public String getRole()
	{
		return role;
	}

	/**
	 * @param role the role to set
	 */
	public void setRole(String role)
	{
		this.role = role;
	}

	/**
	 * @return the projects
	 */
	public String getProjects()
	{
		return projects;
	}

	/**
	 * @param projects the projects to set
	 */
	public void setProjects(String projects)
	{
		this.projects = projects;
	}

	/**
	 * @return the xlogin
	 */
	public String getXlogin()
	{
		return xlogin;
	}

	/**
	 * @param xlogin the xlogin to set
	 */
	public void setXlogin(String xlogin)
	{
		this.xlogin = xlogin;
	}
}
