package eu.unicore.xuudb.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Represents the user's remote login<br/>
 * 
 * Users may have multiple xlogins mapped to the same authentication
 * token.
 * 
 * @author schuller
 */
public class Xlogin implements Serializable, Iterable<String>{

	private static final long serialVersionUID = 1L;

	private final List<String> logins=new ArrayList<>();
	
	/**
	 * constructs a new Xlogin instance from the supplied ":"-separated
	 * list
	 * @see #getEncoded()
	 * @param encoded
	 */
	public Xlogin(String encoded){
		//quick&dirty parsing...
		for(String l: encoded.split(":"))logins.add(l);
	}

	/**
	 * adds a login
	 * 
	 * @param login
	 * @return true if a new login was added
	 */
	public boolean addLogin(String login){
		if(logins.contains(login))return false;
		return logins.add(login);
	}
	
	/**
	 * removes a login
	 * 
	 * @param login
	 * @return true if a login was removed
	 */
	public boolean removeLogin(String login){
		return logins.remove(login);
	}
	
	/**
	 * return the number of logins
	 */
	public int getNumberOfLogins(){
		return logins.size();
	}
	
	/**
	 * returns the list of xlogins as ":" separated String
	 * @return the encoded list of xlogins, or an empty string if empty
	 */
	public String getEncoded(){
		StringBuilder sb=new StringBuilder();
		int i=0;
		if(logins!=null){
			for(String s: logins){
				if(i>0)sb.append(":");
				sb.append(s);
				i++;
			}
		}
		return sb.toString();
	}
	
	/**
	 * returns a pretty-printed form
	 */
	public String toString(){
		return "["+getEncoded()+"]";
	}
	
	public Iterator<String> iterator(){
		return logins.iterator();
	}
}
