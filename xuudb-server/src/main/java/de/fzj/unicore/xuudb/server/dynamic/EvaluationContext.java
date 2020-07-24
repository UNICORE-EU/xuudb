/*
 * Copyright (c) 2012 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package de.fzj.unicore.xuudb.server.dynamic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.fzJuelich.unicore.xuudb.SimplifiedAttributeType;

/**
 * Contains input-output attributes which are being evaluated and modified
 * during rules chain traversal. Additionally also contains constant information about evaluated subject.
 * @author K. Benedyczak
 */
public class EvaluationContext
{
	private final String userDN;
	private final String issuerDN;
	private final String role;
	private final String vo;
	private final SimplifiedAttributeType[] extraAttributes;
	
	private String xlogin;
	private String gid;
	private List<String> supplementaryGids;
	private boolean xloginSet = false;
	private boolean gidSet = false;
	
	//for convenience - used by SpEl context when preparing arguments to scripts
	private boolean dryRun = false;

	public EvaluationContext(String userDN, String issuerDN, String role, String vo,
			SimplifiedAttributeType[] extraAttributes, String xlogin, String gid,
			String[] supplementaryGids) {
		this.userDN = userDN;
		this.issuerDN = issuerDN;
		this.role = role;
		this.vo = vo;
		if (extraAttributes != null)
			this.extraAttributes = extraAttributes;
		else
			this.extraAttributes = new SimplifiedAttributeType[0];
		this.xlogin = xlogin;
		this.gid = gid;
		this.supplementaryGids = new ArrayList<String>();
		if (supplementaryGids != null)
			Collections.addAll(this.supplementaryGids, supplementaryGids);
	}
	
	public String getXlogin() {
		return xlogin;
	}
	public void setXlogin(String xlogin) {
		this.xlogin = xlogin;
		xloginSet = true;
	}
	public String getGid() {
		return gid;
	}
	public void setGid(String gid) {
		this.gid = gid;
		gidSet = true;
	}
	public List<String> getSupplementaryGids() {
		return supplementaryGids;
	}
	public void setSupplementaryGids(List<String> supplementaryGids) {
		this.supplementaryGids = supplementaryGids;
	}
	public String getUserDN() {
		return userDN;
	}
	public String getIssuerDN() {
		return issuerDN;
	}
	public String getRole() {
		return role;
	}
	public String getVo() {
		return vo;
	}
	public SimplifiedAttributeType[] getExtraAttributes() {
		return extraAttributes;
	}
	public boolean isXloginSet() {
		return xloginSet;
	}
	public void setXloginSet(boolean xloginSet) {
		this.xloginSet = xloginSet;
	}
	public boolean isGidSet() {
		return gidSet;
	}
	public void setGidSet(boolean gidSet) {
		this.gidSet = gidSet;
	}
	public boolean isDryRun() {
		return dryRun;
	}
	public void setDryRun(boolean dryRun) {
		this.dryRun = dryRun;
	}
}
