package eu.unicore.xuudb;

import org.junit.jupiter.api.Test;

import eu.unicore.xuudb.xbeans.CheckCertificateChainDocument;	

public class TestBeans {

	@Test
	public void test1(){
		CheckCertificateChainDocument ccd=CheckCertificateChainDocument.Factory.newInstance();
		ccd.addNewCheckCertificateChain().setEncodedChain("foo");
		ccd.getCheckCertificateChain().setGcID("bar");
		System.out.println(ccd);
	}
}
