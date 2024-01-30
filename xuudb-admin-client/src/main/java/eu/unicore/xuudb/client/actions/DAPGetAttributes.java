package eu.unicore.xuudb.client.actions;

import de.fzJuelich.unicore.xuudb.GetAttributesResponseType;
import de.fzJuelich.unicore.xuudb.SimplifiedAttributeType;

public class DAPGetAttributes extends DAPSimulateGetAttributes {

	public DAPGetAttributes(ConnectionManager cm) {
		super(		cm,
				"getDynamicAttributes",
				"Performs a normal dynamic mapping operation for a specified user\n"
						+ " Syntax:\n        getDynamicAttributes <dn='DN'> <role='role'> vo=x|xlogin=x|issuerDN=x|supplementaryGids=v1,v2,v3...|extraAttrName=extraAttrValue \n"
						+ " Example:\n"
						+ "        getDynamicAttributes dn='CN=John Doe, O=Test Inc' role=role1 xlogin=xx",
				2, 99, "getDyn");
		setCategory(AbstractAction.Category.dynamic.toString());
	}

	@Override
	protected GetAttributesResponseType wsCall(String dn, String issuerDN, String role,
			String vo, String xlogin, String gid, String[] supplementaryGids,
			SimplifiedAttributeType[] extraAttributes) throws Exception {
		return cm.dapPublic.getAttributes(dn,
				issuerDN, role, vo, xlogin, gid, supplementaryGids,
				extraAttributes);
	}
}
