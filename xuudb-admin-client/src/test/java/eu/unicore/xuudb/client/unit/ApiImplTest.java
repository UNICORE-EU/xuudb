package eu.unicore.xuudb.client.unit;

import java.util.Date;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.junit5.JUnit5Mockery;
import org.junit.jupiter.api.Test;

import eu.unicore.xuudb.xbeans.AddCertificateDocument;
import eu.unicore.xuudb.xbeans.CheckCertificateDocument;
import eu.unicore.xuudb.xbeans.CheckDNDocument;
import eu.unicore.xuudb.xbeans.FindMappingRequestDocument;
import eu.unicore.xuudb.xbeans.FindReverseMappingRequestDocument;
import eu.unicore.xuudb.xbeans.FreezeMappingRequestDocument;
import eu.unicore.xuudb.xbeans.ImportDatabaseDocument;
import eu.unicore.xuudb.xbeans.ListDatabaseDocument;
import eu.unicore.xuudb.xbeans.ListMappingRequestDocument;
import eu.unicore.xuudb.xbeans.LoginDataType;
import eu.unicore.xuudb.xbeans.RemoveCertificateDocument;
import eu.unicore.xuudb.xbeans.RemoveMappingRequestDocument;
import eu.unicore.xuudb.xbeans.RemovePoolRequestDocument;
import eu.unicore.xuudb.xbeans.SimulateGetAttributesRequestDocument;
import eu.unicore.xuudb.xbeans.UpdateCertificateDocument;
import eu.unicore.xuudb.client.wsapi.IAdminExtInterface;
import eu.unicore.xuudb.client.wsapi.IDAPAdminExtInterface;
import eu.unicore.xuudb.client.wsapi.IPublicExtInterface;
import eu.unicore.xuudb.client.wsapi.impl.IAdminExtImpl;
import eu.unicore.xuudb.client.wsapi.impl.IDAPAdminExtImpl;
import eu.unicore.xuudb.client.wsapi.impl.IDAPPublicExtImpl;
import eu.unicore.xuudb.client.wsapi.impl.IPublicExtImpl;
import eu.unicore.xuudb.interfaces.IAdmin;
import eu.unicore.xuudb.interfaces.IDAPAdmin;
import eu.unicore.xuudb.interfaces.IDynamicAttributesPublic;
import eu.unicore.xuudb.interfaces.IPublic;

public class ApiImplTest {

	@Test
	public void testPublic() throws Exception {

		Mockery context = new JUnit5Mockery();
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
