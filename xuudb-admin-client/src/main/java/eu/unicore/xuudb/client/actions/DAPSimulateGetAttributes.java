package eu.unicore.xuudb.client.actions;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;

import de.fzJuelich.unicore.xuudb.GetAttributesResponseType;
import de.fzJuelich.unicore.xuudb.SimplifiedAttributeType;
import eu.emi.security.authn.x509.impl.X500NameUtils;

public class DAPSimulateGetAttributes extends AbstractAction {
	
	protected DAPSimulateGetAttributes(ConnectionManager cm, String cmd, String help,
			int minArgs, int maxArgs, String alias) {
		super(cm, cmd, help, minArgs, maxArgs, alias);
	}

	public DAPSimulateGetAttributes(ConnectionManager cm) {
		super(
				cm,
				"simulate",
				"Performs a dry run of a normal dynamic mapping operation, checking to what a specified user would be mapped to\n"
						+ " Syntax:\n        simulate <dn='DN'> <role='role'> vo=x|xlogin=x|issuerDN=x|supplementaryGids=v1,v2,v3...|extraAttrName=val1,val2 \n"
						+ " Example:\n"
						+ "        simulate dn='CN=John Doe, O=Test Inc' role=role1 xlogin=xx",
				2, 99, "sim");
		setCategory(AbstractAction.Category.dynamic.toString());
	}

	@Override
	public boolean invoke(String[] args, boolean isBatch) throws Exception {
		logArguments(args);

		String dn = null;
		String issuerDN = null;
		String role = null;
		String vo = null;
		String xlogin = null;
		String gid = null;
		String[] supplementaryGids = null;
		HashMap<String, String> attrs = new HashMap<>();

		for (int i = 0; i < args.length; ++i) {
			if (args[i].toLowerCase().startsWith("vo="))
				vo = args[i].substring(3);
			else if (args[i].toLowerCase().startsWith("xlogin="))
				xlogin = args[i].substring(7);
			else if (args[i].toLowerCase().startsWith("gid=")) {
				gid = args[i].substring(4);

			} else if (args[i].toLowerCase().startsWith("dn=")) {
				String d = args[i].substring(3);
				try {
					dn = X500NameUtils.getReadableForm(d);
				} catch (IllegalArgumentException e) {
					String msg = ("DN format error");
					logger.error(msg, e);
					throw new IllegalArgumentException(msg);
				}

			} else if (args[i].toLowerCase().startsWith("issuerdn=")) {
				String d = args[i].substring(9);
				try {
					issuerDN = X500NameUtils.getReadableForm(d);
				} catch (IllegalArgumentException e) {
					String msg = ("IssuerDN format error");
					logger.error(msg, e);
					throw new IllegalArgumentException(msg);
				}

			}

			else if (args[i].toLowerCase().startsWith("role="))
				role = args[i].substring(5);

			else if (args[i].toLowerCase().startsWith("supplementarygids=")) {
				String g = args[i].substring(18);
				supplementaryGids = g.split(",");

			}

			else {
				String[] attribute = args[i].split("=");
				if (attribute.length != 2)
					throw new Exception("Extra attribute parameter is incorect");
				else {
					attrs.put(attribute[0], attribute[1]);
				}

			}
		}

		SimplifiedAttributeType[] extraAttributes = null;

		if (attrs.size() > 0) {

			extraAttributes = new SimplifiedAttributeType[attrs.size()];
			Enumeration<String> strEnum = Collections.enumeration(attrs
					.keySet());
			int i=0;
			while (strEnum.hasMoreElements()) {
				String name = strEnum.nextElement();
				String value = attrs.get(name);
				String[] values = value.split(",");

				SimplifiedAttributeType sm = SimplifiedAttributeType.Factory
						.newInstance();
				sm.setName(name);
				sm.setValueArray(values);
				String msg = "Set extra attributes " + name + " with values: ";
				for (String v : values) {
					msg = msg + v + " ";
				}
				logger.debug(msg);
				extraAttributes[i] = sm;
			}

		}

		if (dn == null || role == null)
			throw new Exception("DN and role must be specified");
		GetAttributesResponseType resp;
		resp = wsCall(dn, issuerDN, role, vo, 
				xlogin, gid, supplementaryGids, extraAttributes);
		System.out.println("Done. Received:\n");

		System.out.printf("%-15s|%-15s|%-40s|\n", "    Xlogin", "     Gid", 
				"        Supplementary gids");
		System.out.println("------------------------------------------------------------------------");

		String supgids = "";
		for (String sgid : resp.getSupplementaryGidsArray())
			supgids = supgids + sgid + ",";
		if (supgids.length() > 0)
			supgids = supgids.substring(0, supgids.length() - 1);
		String ruid = resp.getXlogin() == null ? "" : resp.getXlogin();
		String rgid = resp.getGid() == null ? "" : resp.getGid();
		System.out.printf("%15s|%15s|%40s|\n", ruid, rgid, supgids);

		return true;
	}
	
	protected GetAttributesResponseType wsCall(String dn, String issuerDN, String role,
			String vo, String xlogin, String gid, String[] supplementaryGids,
			SimplifiedAttributeType[] extraAttributes) throws Exception {
		return cm.dapPublic.simulateGetAttributes(dn,
				issuerDN, role, vo, xlogin, gid, supplementaryGids,
				extraAttributes);
	}
}
