package de.fzj.unicore.xuudb.client.functional;

import de.fzj.unicore.xuudb.client.wsapi.XUUDBResponse;

public class TestFuncQuery extends TestFuncBase {

	public void testCheckCert() {
		System.out.println("Test check-cert");
		try {
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

		} catch (Exception e) {
			fail(e.toString());
		}

	}

	public void testCheckDN() {

		System.out.println("Test check-dn");
		try {
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

		} catch (Exception e) {
			fail(e.toString());
		}

	}
}
