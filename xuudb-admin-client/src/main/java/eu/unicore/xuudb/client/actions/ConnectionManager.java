package eu.unicore.xuudb.client.actions;

import eu.unicore.xuudb.client.wsapi.IAdminExtInterface;
import eu.unicore.xuudb.client.wsapi.IDAPAdminExtInterface;
import eu.unicore.xuudb.client.wsapi.IDAPPublicExtInterface;
import eu.unicore.xuudb.client.wsapi.IPublicExtInterface;

public class ConnectionManager {
	final IAdminExtInterface admin;
	final IPublicExtInterface query;
	final IDAPAdminExtInterface dapAdmin;
	final IDAPPublicExtInterface dapPublic;

	public ConnectionManager(IAdminExtInterface adm, IPublicExtInterface q,
			IDAPAdminExtInterface da, IDAPPublicExtInterface dp) {
		super();
		admin = adm;
		query = q;
		dapAdmin = da;
		dapPublic = dp;
	}

}
