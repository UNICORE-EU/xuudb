package de.fzj.unicore.xuudb.client.wsapi;

import de.fzJuelich.unicore.xuudb.LoginDataType;

public class XUUDBResponse {

	String status;

	public String getStatus() {
		return status;
	}

	public String getInfo() {
		return info;
	}

	public LoginDataType[] getData() {
		return data;
	}

	String info;
	LoginDataType[] data;

	public XUUDBResponse(String st, String inf, LoginDataType[] d) {
		status = st;
		info = inf;
		data = d;
	}

}
