package de.fzj.unicore.xuudb.server;

import junit.framework.TestCase;

public class TestSecurityToken extends TestCase {


	public void test1()throws Exception{
		String dn="CN=foo, C=de, OU=bar.org, emailAddress=asd@q.org";
		SecurityToken t1=new SecurityToken(dn);
		System.out.println(t1);
		String dn2="cn=foo, C=de,ou=bar.org,emailAddress=asd@q.org";
		SecurityToken t2=new SecurityToken(dn2);
		System.out.println(t2);
		assertEquals(t2.toString(),t1.toString());
	} 

}
