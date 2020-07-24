package de.fzj.unicore.xuudb.client.wsapi;

public interface IPublicExtInterface {

	XUUDBResponse checkDN(String gcId, String dn) throws Exception;
	XUUDBResponse checkCert(String gcId, String certPem) throws Exception;

}
