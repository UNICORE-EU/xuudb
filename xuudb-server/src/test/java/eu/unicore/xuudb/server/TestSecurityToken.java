package eu.unicore.xuudb.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestSecurityToken {

	@Test
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
