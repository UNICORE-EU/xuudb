package eu.unicore.xuudb.client.functional;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import eu.unicore.xuudb.client.wsapi.XUUDBResponse;
import eu.unicore.xuudb.xbeans.LoginDataType;

public class TestFuncQuery extends TestFuncBase {

	@Test
	public void testCheckCert() throws Exception {
		System.out.println("Test check-cert");
		XUUDBResponse resp = admin.add(gcId, certPem, xlogin1, role,
				project);
		if (!resp.getStatus().contains("OK"))
			fail();

		XUUDBResponse resp2 = query.checkCert(gcId, certPem);

		if (!resp2.getStatus().contains("OK"))
			fail();

		if (resp2.getData().length != 1)
			fail();

		if (!resp2.getData()[0].getRole().equals(role))
			fail();
		if (!resp2.getData()[0].getXlogin().equals(xlogin1))
			fail();
		if (!resp2.getData()[0].getProjects().equals(project))
			fail();
	}

	@Test
	public void testCheckDN() throws Exception {

		System.out.println("Test check-dn");
		XUUDBResponse resp = admin.adddn(gcId, dn, xlogin1, role, project);
		if (!resp.getStatus().contains("OK"))
			fail();

		XUUDBResponse resp2 = query.checkDN(gcId, dn);

		if (!resp2.getStatus().contains("OK"))
			fail();

		if (resp2.getData().length != 1)
			fail();

		if (!resp2.getData()[0].getRole().equals(role))
			fail();
		if (!resp2.getData()[0].getXlogin().equals(xlogin1))
			fail();
		if (!resp2.getData()[0].getProjects().equals(project))
			fail();

	}
}
