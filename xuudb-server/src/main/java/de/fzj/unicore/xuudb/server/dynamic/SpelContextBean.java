/*
 * Copyright (c) 2012 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package de.fzj.unicore.xuudb.server.dynamic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Bean used to initialize SpEL {@link StandardEvaluationContext}, used to evaluate rules' conditions.
 * @author K. Benedyczak
 */
public class SpelContextBean {
	private String dn;
	private String issuer;
	private String vo;
	private String xlogin;
	private String role;
	private String gid;
	private Set<String> gids;
	private Map<String, Set<String>> attributes;

	
	public String getIssuer() {
		return issuer;
	}
	public void setIssuer(String issuer) {
		this.issuer = (issuer == null) ? "" : issuer;
	}
	public String getDn() {
		return dn;
	}
	public void setDn(String dn) {
		this.dn = (dn == null) ? "" : dn;
	}
	public String getVo() {
		return vo;
	}
	public void setVo(String vo) {
		this.vo = (vo == null) ? "" : vo;
	}
	public String getXlogin() {
		return xlogin;
	}
	public void setXlogin(String xlogin) {
		this.xlogin = (xlogin == null) ? "" : xlogin;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = (role == null) ? "" : role;
	}
	public String getGid() {
		return gid;
	}
	public void setGid(String gid) {
		this.gid = (gid == null) ? "" : gid;
	}
	public Set<String> getGids() {
		return gids;
	}
	public void setGids(Set<String> gids) {
		this.gids = (gids == null) ? new HashSet<>() : gids;
	}
	public Map<String, Set<String>> getAttributes() {
		return attributes;
	}
	public void setAttributes(Map<String, Set<String>> attributes) {
		this.attributes = (attributes == null) ? new HashMap<>() : attributes;
	}
}
