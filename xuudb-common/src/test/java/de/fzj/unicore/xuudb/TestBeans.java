package de.fzj.unicore.xuudb;

import de.fzJuelich.unicore.xuudb.CheckCertificateChainDocument;
import junit.framework.TestCase;

public class TestBeans extends TestCase {

	public void test1(){
		CheckCertificateChainDocument ccd=CheckCertificateChainDocument.Factory.newInstance();
		ccd.addNewCheckCertificateChain().setEncodedChain("foo");
		ccd.getCheckCertificateChain().setGcID("bar");
		System.out.println(ccd);
	}
}
