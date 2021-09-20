package de.fzj.unicore.xuudb.client.unit;

import java.util.Date;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;

import de.fzJuelich.unicore.xuudb.AddCertificateDocument;
import de.fzJuelich.unicore.xuudb.CheckCertificateDocument;
import de.fzJuelich.unicore.xuudb.CheckDNDocument;
import de.fzJuelich.unicore.xuudb.FindMappingRequestDocument;
import de.fzJuelich.unicore.xuudb.FindReverseMappingRequestDocument;
import de.fzJuelich.unicore.xuudb.FreezeMappingRequestDocument;
import de.fzJuelich.unicore.xuudb.ImportDatabaseDocument;
import de.fzJuelich.unicore.xuudb.ListDatabaseDocument;
import de.fzJuelich.unicore.xuudb.ListMappingRequestDocument;
import de.fzJuelich.unicore.xuudb.LoginDataType;
import de.fzJuelich.unicore.xuudb.RemoveCertificateDocument;
import de.fzJuelich.unicore.xuudb.RemoveMappingRequestDocument;
import de.fzJuelich.unicore.xuudb.RemovePoolRequestDocument;
import de.fzJuelich.unicore.xuudb.SimulateGetAttributesRequestDocument;
import de.fzJuelich.unicore.xuudb.UpdateCertificateDocument;
import de.fzj.unicore.xuudb.client.wsapi.IAdminExtInterface;
import de.fzj.unicore.xuudb.client.wsapi.IDAPAdminExtInterface;
import de.fzj.unicore.xuudb.client.wsapi.IPublicExtInterface;
import de.fzj.unicore.xuudb.client.wsapi.impl.IAdminExtImpl;
import de.fzj.unicore.xuudb.client.wsapi.impl.IDAPAdminExtImpl;
import de.fzj.unicore.xuudb.client.wsapi.impl.IDAPPublicExtImpl;
import de.fzj.unicore.xuudb.client.wsapi.impl.IPublicExtImpl;
import de.fzj.unicore.xuudb.interfaces.IAdmin;
import de.fzj.unicore.xuudb.interfaces.IDAPAdmin;
import de.fzj.unicore.xuudb.interfaces.IDynamicAttributesPublic;
import de.fzj.unicore.xuudb.interfaces.IPublic;

public class ApiImplTest {

	@Test
	public void testPublic() throws Exception {

		Mockery context = new Mockery();
		final IPublic proxy = context.mock(IPublic.class);
		IPublicExtInterface query = new IPublicExtImpl(proxy);

		context.checking(new Expectations() {
			{
				oneOf(proxy).checkCertificate(
						with(aNonNull(CheckCertificateDocument.class)));
				oneOf(proxy).checkDN(with(aNonNull(CheckDNDocument.class)));

			}
		});
		query.checkCert("test", "test");
		query.checkDN("test", "test");
		context.assertIsSatisfied();

	}

	@Test
	public void testAdmin() throws Exception {
		Mockery context = new Mockery();
		final IAdmin proxy = context.mock(IAdmin.class);
		IAdminExtInterface admin = new IAdminExtImpl(proxy);
		context.checking(new Expectations() {
			{
				exactly(2).of(proxy).addCertificate(
						with(aNonNull(AddCertificateDocument.class)));
				oneOf(proxy).updateCertificate(with(aNonNull(UpdateCertificateDocument.class)));
				oneOf(proxy).removeCertificate(with(aNonNull(RemoveCertificateDocument.class)));
				exactly(2).of(proxy).listDatabase(with(aNonNull(ListDatabaseDocument.class)));
				exactly(2).of(proxy).importDatabase(with(aNonNull(ImportDatabaseDocument.class)));
			}
		});
		admin.add("test", "test", "test", "test", "test");
		admin.adddn("test", "test", "test", "test",null);
		admin.list(LoginDataType.Factory.newInstance());
		admin.remove(LoginDataType.Factory.newInstance());
		admin.exportCsv();
		admin.importCsv(new LoginDataType[0], true);
		admin.importCsv(new LoginDataType[0], false);

		admin.update("test", "test", LoginDataType.Factory.newInstance());
		context.assertIsSatisfied();
	}


	@Test
	public void testDAP() throws Exception {
		Mockery context = new Mockery();
		final IDAPAdmin proxy = context.mock(IDAPAdmin.class);
		IDAPAdminExtInterface admin = new IDAPAdminExtImpl(proxy);

		final IDynamicAttributesPublic proxy2 = context.mock(IDynamicAttributesPublic.class);
		IDAPPublicExtImpl dpublic = new IDAPPublicExtImpl(proxy2);

		context.checking(new Expectations() {
			{
				oneOf(proxy).findMapping(with(aNonNull(FindMappingRequestDocument.class)));
				oneOf(proxy).findReverseMapping(with(aNonNull(FindReverseMappingRequestDocument.class)));
				exactly(2).of(proxy).freezeMapping(with(aNonNull(FreezeMappingRequestDocument.class)));
				exactly(2).of(proxy).removeFrozenMapping(with(aNonNull(RemoveMappingRequestDocument.class)));
				oneOf(proxy).listMappings(with(aNonNull(ListMappingRequestDocument.class)));
				oneOf(proxy).removePool(with(aNonNull(RemovePoolRequestDocument.class)));
				oneOf(proxy).listPools();
				oneOf(proxy2).simulateGetAttributes(with(aNonNull(SimulateGetAttributesRequestDocument.class)));
			}
		});
		admin.list("type", "po");
		admin.find("t","v");
		admin.findReverse("t", "v");
		admin.freeze(new Date(),"f");
		admin.remove(new Date(),"r");
		admin.freeze("map","f");
		admin.remove("map","r");
		admin.removePool("id");
		admin.listPools();
		dpublic.simulateGetAttributes("CN=John Doe,O=Test", "CN=John Doe,O=Test", "x", "vo", "xlogin", "gid", null, null);
		context.assertIsSatisfied();
	}
}
