package eu.unicore.xuudb.client.wsapi;

import eu.unicore.xuudb.xbeans.GetAttributesResponseType;
import eu.unicore.xuudb.xbeans.SimplifiedAttributeType;

public interface IDAPPublicExtInterface {

	public GetAttributesResponseType simulateGetAttributes(String userDN,
			String issuerDN, String role, String vo, String xlogin, String gid,
			String[] supplementaryGids,
			SimplifiedAttributeType[] extraAttributes) throws Exception;

	public GetAttributesResponseType getAttributes(String userDN,
			String issuerDN, String role, String vo, String xlogin, String gid,
			String[] supplementaryGids,
			SimplifiedAttributeType[] extraAttributes) throws Exception;

}
