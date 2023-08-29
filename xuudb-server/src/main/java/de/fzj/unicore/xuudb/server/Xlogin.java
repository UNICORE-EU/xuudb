/*********************************************************************************
 * Copyright (c) 2008 Forschungszentrum Juelich GmbH 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * (1) Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the disclaimer at the end. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 * 
 * (2) Neither the name of Forschungszentrum Juelich GmbH nor the names of its 
 * contributors may be used to endorse or promote products derived from this 
 * software without specific prior written permission.
 * 
 * DISCLAIMER
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *********************************************************************************/

package de.fzj.unicore.xuudb.server;

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
