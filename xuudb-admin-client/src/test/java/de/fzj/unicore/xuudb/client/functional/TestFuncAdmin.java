package de.fzj.unicore.xuudb.client.functional;

import de.fzJuelich.unicore.xuudb.LoginDataType;
import de.fzj.unicore.xuudb.client.wsapi.XUUDBResponse;

public class TestFuncAdmin extends TestFuncBase {
	public void testAddAction() {
		System.out.println("Test add user");

		try {
			XUUDBResponse resp = admin.add(gcId, certPem, xlogin1, role,
					project);
			if (!resp.getStatus().contains("OK"))
				fail();
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void testAddDNAction() {
		System.out.println("Test addDN user");
		try {
			XUUDBResponse resp = admin.adddn(gcId, dn, xlogin2, role, project);
			if (!resp.getStatus().contains("OK"))
				fail();

		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void testImportExport() {

		System.out.println("Test export");

		try {
			XUUDBResponse resp = admin.adddn(gcId, dn, xlogin2, role, project);
			if (!resp.getStatus().contains("OK"))
				fail();

		} catch (Exception e) {
			fail(e.toString());
		}

		try {
			XUUDBResponse resp = admin.exportCsv();
			if (!resp.getStatus().contains("OK"))
				fail();

			if (resp.getData().length != 1)
				fail();

			

			XUUDBResponse resp2 = admin.importCsv(resp.getData(), true);
			if (!resp2.getStatus().contains(
					"1   certificates from   1   were imported into the XUUDB"))
				fail();

		} catch (Exception e) {
			fail(e.toString());
		}

	}

	public void testList() {

		System.out.println("Test list");
		try {
			XUUDBResponse resp = admin.add(gcId, certPem, xlogin1, role,
					project);
			if (!resp.getStatus().contains("OK"))
				fail();

			LoginDataType lg = LoginDataType.Factory.newInstance();
			lg.setGcID(gcId);
			XUUDBResponse resp2 = admin.list(lg);
			if (resp2.getData().length != 1)
				fail();

		} catch (Exception e) {
			fail(e.toString());
		}

	}

	public void testUpdate() {

		System.out.println("Test update");

		try {
			XUUDBResponse resp = admin.add(gcId, certPem, xlogin1, role,
					project);
			if (!resp.getStatus().contains("OK"))
				fail();

			LoginDataType lg = LoginDataType.Factory.newInstance();
			lg.setGcID(gcId + "xxx");
			lg.setRole("newRole");
			XUUDBResponse resp2 = admin.update(gcId, certPem, lg);
			if (!resp2.getStatus().contains("Updated   1   rows."))
				fail();

		} catch (Exception e) {
			fail(e.toString());
		}

	}

	public void testRemove() {
		System.out.println("Test idividual remove");
		try {
			XUUDBResponse resp = admin.add(gcId, certPem, xlogin1, role,
					project);
			if (!resp.getStatus().contains("OK"))
				fail();

			LoginDataType lg = LoginDataType.Factory.newInstance();
			lg.setGcID(gcId);
			lg.setRole(role);
			lg.setToken(certPem);
			XUUDBResponse resp2 = admin.remove(lg);
			if (!resp2.getStatus().contains("Removed records: 1"))
				fail();

		} catch (Exception e) {
			fail(e.toString());
		}

	}

}
