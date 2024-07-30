package eu.unicore.xuudb.client.functional;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import de.fzJuelich.unicore.xuudb.LoginDataType;
import eu.unicore.xuudb.client.wsapi.XUUDBResponse;

public class TestFuncAdmin extends TestFuncBase {

	@Test
	public void testAddAction() throws Exception {
		System.out.println("Test add user");

		XUUDBResponse resp = admin.add(gcId, certPem, xlogin1, role,
				project);
		if (!resp.getStatus().contains("OK"))
			fail();
	}

	@Test
	public void testAddDNAction() throws Exception {
		System.out.println("Test addDN user");
		XUUDBResponse resp = admin.adddn(gcId, dn, xlogin2, role, project);
		if (!resp.getStatus().contains("OK"))
			fail();
	}

	@Test
	public void testImportExport() throws Exception {

		System.out.println("Test export");

		XUUDBResponse resp = admin.adddn(gcId, dn, xlogin2, role, project);
		if (!resp.getStatus().contains("OK"))
			fail();

		resp = admin.exportCsv();
		if (!resp.getStatus().contains("OK"))
			fail();

		if (resp.getData().length != 1)
			fail();



		XUUDBResponse resp2 = admin.importCsv(resp.getData(), true);
		if (!resp2.getStatus().contains(
				"1   certificates from   1   were imported into the XUUDB"))
			fail();

	}

	@Test
	public void testList() throws Exception {
		
		System.out.println("Test list");
		XUUDBResponse resp = admin.add(gcId, certPem, xlogin1, role,
				project);
		if (!resp.getStatus().contains("OK"))
			fail();

		LoginDataType lg = LoginDataType.Factory.newInstance();
		lg.setGcID(gcId);
		XUUDBResponse resp2 = admin.list(lg);
		if (resp2.getData().length != 1)
			fail();
	}

	@Test
	public void testUpdate() throws Exception {

		System.out.println("Test update");

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
	}

	@Test
	public void testRemove() throws Exception {
		System.out.println("Test idividual remove");
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

	}

}
