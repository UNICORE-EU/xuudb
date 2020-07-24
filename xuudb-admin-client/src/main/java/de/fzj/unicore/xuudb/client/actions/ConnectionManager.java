package de.fzj.unicore.xuudb.client.actions;

import de.fzj.unicore.xuudb.client.wsapi.IAdminExtInterface;
import de.fzj.unicore.xuudb.client.wsapi.IDAPAdminExtInterface;
import de.fzj.unicore.xuudb.client.wsapi.IDAPPublicExtInterface;
import de.fzj.unicore.xuudb.client.wsapi.IPublicExtInterface;

public class ConnectionManager {
	IAdminExtInterface admin;
	IPublicExtInterface query;
	IDAPAdminExtInterface dapAdmin;
	IDAPPublicExtInterface dapPublic;

	public ConnectionManager(IAdminExtInterface adm, IPublicExtInterface q,
			IDAPAdminExtInterface da, IDAPPublicExtInterface dp) {
		super();
		admin = adm;
		query = q;
		dapAdmin = da;
		dapPublic = dp;

	}

}
