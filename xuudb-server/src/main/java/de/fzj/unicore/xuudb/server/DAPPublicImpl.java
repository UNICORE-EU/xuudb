/*
 * Copyright (c) 2012 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package de.fzj.unicore.xuudb.server;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;

import de.fzJuelich.unicore.xuudb.GetAttributesRequestDocument;
import de.fzJuelich.unicore.xuudb.GetAttributesRequestType;
import de.fzJuelich.unicore.xuudb.GetAttributesResponseDocument;
import de.fzJuelich.unicore.xuudb.GetAttributesResponseType;
import de.fzJuelich.unicore.xuudb.SimulateGetAttributesRequestDocument;
import de.fzJuelich.unicore.xuudb.SimulateGetAttributesResponseDocument;
import de.fzj.unicore.xuudb.Log;
import de.fzj.unicore.xuudb.interfaces.IDynamicAttributesPublic;
import de.fzj.unicore.xuudb.server.dynamic.EvaluationContext;
import de.fzj.unicore.xuudb.server.dynamic.EvaluationEngine;
import de.fzj.unicore.xuudb.server.dynamic.Rule;
import de.fzj.unicore.xuudb.server.dynamic.DAPConfiguration;

/**
 * Dynamic attributes query endpoint
 * @author K. Benedyczak
 */
public class DAPPublicImpl implements IDynamicAttributesPublic
{
	private static final Logger log = Log.getLogger(Log.XUUDB_SERVER, DAPPublicImpl.class);
	private final DAPConfiguration config;
	private final EvaluationEngine engine;
	
	public DAPPublicImpl(DAPConfiguration config) 
			throws IOException, XmlException, ParseException
	{
		this.config = config;
		engine = new EvaluationEngine();
	}
	
	@Override
	public GetAttributesResponseDocument getAttributes(GetAttributesRequestDocument reqDoc) {
		log.debug("getAttributes invoked for DN: {}", reqDoc.getGetAttributesRequest().getUserDN());
		GetAttributesResponseDocument respDoc = GetAttributesResponseDocument.Factory.newInstance();
		commonGet(reqDoc.getGetAttributesRequest(), respDoc.addNewGetAttributesResponse(), false);
		return respDoc;
	}

	@Override
	public SimulateGetAttributesResponseDocument simulateGetAttributes(
			SimulateGetAttributesRequestDocument xml)
	{
		log.debug("simulateGetAttributes invoked for DN: {}", xml.getSimulateGetAttributesRequest().getUserDN());
		SimulateGetAttributesResponseDocument respDoc = SimulateGetAttributesResponseDocument.Factory.newInstance();
		commonGet(xml.getSimulateGetAttributesRequest(), respDoc.addNewSimulateGetAttributesResponse(), true);
		log.debug("RESP: " + respDoc.xmlText(new XmlOptions().setSavePrettyPrint()));
		return respDoc;
	}
	
	private void commonGet(GetAttributesRequestType req, GetAttributesResponseType resp, boolean dryRun)
	{
		if (req == null)
			throw new IllegalArgumentException("Request can not be empty");
		if (req.getUserDN() == null)
			throw new IllegalArgumentException("User distinguished name must be provided");
		if (req.getRole() == null)
			throw new IllegalArgumentException("User role must be provided");
		EvaluationContext ctx = new EvaluationContext(req.getUserDN(), 
				req.getIssuerDN(), 
				req.getRole(), 
				req.getVo(), 
				req.getExtraAttributesArray(), 
				req.getXlogin(), 
				req.getGid(), 
				req.getSupplementaryGidsArray());
		ctx.setDryRun(dryRun);
		
		List<Rule> rules = config.getRules();
		engine.evaluate(rules, ctx, dryRun);
		
		resp.setGid(ctx.getGid());
		resp.setXlogin(ctx.getXlogin());
		resp.setSupplementaryGidsArray(ctx.getSupplementaryGids().toArray(new String[0]));		
	}
}
