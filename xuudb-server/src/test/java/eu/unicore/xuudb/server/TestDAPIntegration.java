package eu.unicore.xuudb.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.mvel2.PropertyAccessException;

import eu.unicore.xuudb.xbeans.FindMappingRequestDocument;
import eu.unicore.xuudb.xbeans.FindMappingRequestType;
import eu.unicore.xuudb.xbeans.FindReverseMappingRequestDocument;
import eu.unicore.xuudb.xbeans.FreezeMappingRequestDocument;
import eu.unicore.xuudb.xbeans.FreezeRemoveMappingRequestType;
import eu.unicore.xuudb.xbeans.GetAttributesRequestDocument;
import eu.unicore.xuudb.xbeans.GetAttributesRequestType;
import eu.unicore.xuudb.xbeans.GetAttributesResponseType;
import eu.unicore.xuudb.xbeans.ListMappingRequestDocument;
import eu.unicore.xuudb.xbeans.ListMappingRequestType;
import eu.unicore.xuudb.xbeans.MappingDataType;
import eu.unicore.xuudb.xbeans.MappingListDataType;
import eu.unicore.xuudb.xbeans.PoolInfoType;
import eu.unicore.xuudb.xbeans.RemoveMappingRequestDocument;
import eu.unicore.xuudb.xbeans.RemovePoolRequestDocument;
import eu.unicore.xuudb.xbeans.SimulateGetAttributesRequestDocument;
import eu.unicore.xuudb.xbeans.SimulateGetAttributesResponseDocument;
import eu.unicore.xuudb.interfaces.IDAPAdmin;
import eu.unicore.xuudb.interfaces.IDynamicAttributesPublic;
import eu.unicore.xuudb.server.dynamic.ScriptMapping;

public class TestDAPIntegration {
	
	@Test
	public void test() throws Exception {
		HttpsServer server = null;
		try {
			File dir = new File("target/data");
			FileUtils.deleteDirectory(dir);
			
			Properties p = new Properties();
			p.load(new FileInputStream(
					"src/test/resources/xuudb_server.conf"));
			//for sure
			p.setProperty("xuudb.db.jdbcUrl","jdbc:h2:./target/data/xuudb222");
			server = new HttpsServer(p);
			server.start();

			IDAPAdmin admin = server.getDapAdminImpl();
			IDynamicAttributesPublic pub = server.getDapPublicImpl();

			// check if pools are initialized correctly
			PoolInfoType[] pools = admin.listPools().getListPoolsResponse().getPoolArray();
			PoolInfoType p0 = getPool("biology-dynamic-gids-pool", pools);
			assertEquals("vo", p0.getPoolKeyType());
			assertEquals("gid", p0.getPoolType());
			assertEquals(0, p0.getActiveMappings());
			assertEquals(0, p0.getFrozenMappings());
			assertEquals(901, p0.getFreeSlots());

			PoolInfoType p2 = getPool("biology-uids-pool", pools);
			assertEquals("dn", p2.getPoolKeyType());
			assertEquals("uid", p2.getPoolType());
			assertEquals(0, p2.getActiveMappings());
			assertEquals(0, p2.getFrozenMappings());
			assertEquals(3, p2.getFreeSlots());

			// get a first mapping
			GetAttributesRequestDocument reqDoc = GetAttributesRequestDocument.Factory
					.newInstance();
			GetAttributesRequestType req = reqDoc.addNewGetAttributesRequest();
			req.setVo("/biology/dynamic/foo");
			req.setRole("user");
			req.setUserDN("CN=foo");
			GetAttributesResponseType getResp = pub.getAttributes(reqDoc)
					.getGetAttributesResponse();
			assertTrue(getResp.getGid().startsWith("grid-dyn"));
			String assignedGid = getResp.getGid();

			// ask again - should get the same
			System.out.println("Got gid: " + assignedGid);
			getResp = pub.getAttributes(reqDoc).getGetAttributesResponse();
			assertEquals(assignedGid, getResp.getGid());

			// list mappings
			ListMappingRequestDocument listReqDoc = ListMappingRequestDocument.Factory
					.newInstance();
			ListMappingRequestType listReq = listReqDoc
					.addNewListMappingRequest();
			listReq.setPoolId("biology-dynamic-gids-pool");
			listReq.setMappingType("any");
			MappingListDataType listResp = admin.listMappings(listReqDoc)
					.getListMappingResponse();
			assertEquals(1, listResp.getMappingArray().length);
			assertNull(listResp.getMappingArray(0).getFreezeTime());
			assertNotNull(listResp.getMappingArray(0).getId());
			assertEquals("/biology/dynamic/foo", listResp.getMappingArray(0)
					.getKey());
			assertEquals("vo", listResp.getMappingArray(0).getKeyType());
			assertNotNull(listResp.getMappingArray(0).getLastAccess());
			assertEquals("biology-dynamic-gids-pool", listResp.getMappingArray(
					0).getPoolName());
			assertEquals(assignedGid, listResp.getMappingArray(0).getValue());

			// and a second one - should be different
			GetAttributesRequestDocument reqDoc2 = GetAttributesRequestDocument.Factory
					.newInstance();
			GetAttributesRequestType req2 = reqDoc2
					.addNewGetAttributesRequest();
			req2.setVo("/biology/dynamic/bar");
			req2.setRole("user");
			req2.setUserDN("CN=foo");
			GetAttributesResponseType getResp2 = pub.getAttributes(reqDoc2)
					.getGetAttributesResponse();
			assertTrue(getResp2.getGid().startsWith("grid-dyn"));
			assertNotSame(assignedGid, getResp2.getGid());

			// check pool stats
			pools = admin.listPools().getListPoolsResponse().getPoolArray();
			p0 = getPool("biology-dynamic-gids-pool", pools);
			assertEquals("vo", p0.getPoolKeyType());
			assertEquals("gid", p0.getPoolType());
			assertEquals(2, p0.getActiveMappings());
			assertEquals(0, p0.getFrozenMappings());
			assertEquals(899, p0.getFreeSlots());

			// list mappings
			ListMappingRequestDocument listReqDoc2 = ListMappingRequestDocument.Factory
					.newInstance();
			ListMappingRequestType listReq2 = listReqDoc2
					.addNewListMappingRequest();
			listReq2.setPoolId("biology-dynamic-gids-pool");
			listReq2.setMappingType("live");
			MappingListDataType listResp2 = admin.listMappings(listReqDoc2)
					.getListMappingResponse();
			assertEquals(2, listResp2.getMappingArray().length);

			listReq2.unsetPoolId();
			listReq2.setMappingType("frozen");
			listResp2 = admin.listMappings(listReqDoc2)
					.getListMappingResponse();
			assertEquals(0, listResp2.getMappingArray().length);

			listReq2.setMappingType("live");
			listResp2 = admin.listMappings(listReqDoc2)
					.getListMappingResponse();
			assertEquals(3, listResp2.getMappingArray().length);

			// find it
			FindMappingRequestDocument findReqDoc = FindMappingRequestDocument.Factory
					.newInstance();
			FindMappingRequestType findReq = findReqDoc
					.addNewFindMappingRequest();
			findReq.setType("gid");
			findReq.setValue(assignedGid);
			MappingDataType[] found = admin.findMapping(findReqDoc)
					.getFindMappingResponse().getMappingArray();
			assertEquals(1, found.length);
			assertNull(found[0].getFreezeTime());
			assertNotNull(found[0].getId());
			assertEquals("/biology/dynamic/foo", found[0].getKey());
			assertEquals("vo", found[0].getKeyType());
			assertNotNull(found[0].getLastAccess());
			assertEquals("biology-dynamic-gids-pool", found[0].getPoolName());
			assertEquals(assignedGid, found[0].getValue());

			// reverse find it
			FindReverseMappingRequestDocument revFindReqDoc = FindReverseMappingRequestDocument.Factory
					.newInstance();
			FindMappingRequestType findReq2 = revFindReqDoc
					.addNewFindReverseMappingRequest();
			findReq2.setType("vo");
			findReq2.setValue("/biology/dynamic/foo");
			MappingDataType[] found2 = admin.findReverseMapping(revFindReqDoc)
					.getFindReverseMappingResponse().getMappingArray();
			assertEquals(1, found2.length);
			assertNull(found2[0].getFreezeTime());
			assertNotNull(found2[0].getId());
			assertEquals("/biology/dynamic/foo", found2[0].getKey());
			assertEquals("vo", found2[0].getKeyType());
			assertNotNull(found2[0].getLastAccess());
			assertEquals("biology-dynamic-gids-pool", found2[0].getPoolName());
			assertEquals(assignedGid, found2[0].getValue());

			// freeze the first
			FreezeMappingRequestDocument freezeReqDoc = FreezeMappingRequestDocument.Factory
					.newInstance();
			FreezeRemoveMappingRequestType freezeReq = freezeReqDoc
					.addNewFreezeMappingRequest();
			freezeReq.setId("/biology/dynamic/foo");
			freezeReq.setPoolId("biology-dynamic-gids-pool");
			admin.freezeMapping(freezeReqDoc);

			// check with list
			listReq2.setPoolId("biology-dynamic-gids-pool");
			listReq2.setMappingType("frozen");
			listResp2 = admin.listMappings(listReqDoc2)
					.getListMappingResponse();
			assertEquals(1, listResp2.getMappingArray().length);

			// check pool stats
			pools = admin.listPools().getListPoolsResponse().getPoolArray();
			p0 = getPool("biology-dynamic-gids-pool", pools);
			assertEquals(1, p0.getActiveMappings());
			assertEquals(1, p0.getFrozenMappings());
			assertEquals(899, p0.getFreeSlots());

			// remove the frozen
			RemoveMappingRequestDocument removeReqDoc = RemoveMappingRequestDocument.Factory
					.newInstance();
			FreezeRemoveMappingRequestType removeReq = removeReqDoc
					.addNewRemoveMappingRequest();
			removeReq.setId("/biology/dynamic/foo");
			removeReq.setPoolId("biology-dynamic-gids-pool");
			admin.removeFrozenMapping(removeReqDoc);

			// check pool stats
			pools = admin.listPools().getListPoolsResponse().getPoolArray();
			p0 = getPool("biology-dynamic-gids-pool", pools);
			assertEquals(1, p0.getActiveMappings());
			assertEquals(0, p0.getFrozenMappings());
			assertEquals(900, p0.getFreeSlots());

			// try to remove a pool (should fail as one mapping is still active)
			RemovePoolRequestDocument removePoolReqDoc = RemovePoolRequestDocument.Factory
					.newInstance();
			removePoolReqDoc.addNewRemovePoolRequest().setPoolId(
					"biology-dynamic-gids-pool");
			assertThrows(IllegalArgumentException.class,()->{
				admin.removePool(removePoolReqDoc);
			});

			// freeze the 2nd one
			FreezeMappingRequestDocument freezeReqDoc2 = FreezeMappingRequestDocument.Factory
					.newInstance();
			FreezeRemoveMappingRequestType freezeReq2 = freezeReqDoc2
					.addNewFreezeMappingRequest();
			freezeReq2.setId("/biology/dynamic/bar");
			freezeReq2.setPoolId("biology-dynamic-gids-pool");
			admin.freezeMapping(freezeReqDoc2);

			// try to remove a pool (should fail as one mapping is frozen)
			assertThrows(IllegalArgumentException.class,()->{
				admin.removePool(removePoolReqDoc);
			});

			// remove the 2nd one
			RemoveMappingRequestDocument removeReqDoc2 = RemoveMappingRequestDocument.Factory
					.newInstance();
			FreezeRemoveMappingRequestType removeReq2 = removeReqDoc2
					.addNewRemoveMappingRequest();
			removeReq2.setId("/biology/dynamic/bar");
			removeReq2.setPoolId("biology-dynamic-gids-pool");
			admin.removeFrozenMapping(removeReqDoc2);

			// simulate get attr
			SimulateGetAttributesRequestDocument simReqDoc = SimulateGetAttributesRequestDocument.Factory
					.newInstance();
			GetAttributesRequestType simReq = simReqDoc
					.addNewSimulateGetAttributesRequest();
			simReq.setVo("/biology/dynamic/foo");
			simReq.setRole("user");
			simReq.setUserDN("CN=foo");
			SimulateGetAttributesResponseDocument simRespDoc = pub
					.simulateGetAttributes(simReqDoc);
			GetAttributesResponseType simResp = simRespDoc
					.getSimulateGetAttributesResponse();
			assertTrue(simResp.getGid().startsWith("grid-dyn"));
			assertTrue(simResp.getXlogin().startsWith("uid"));

			// times test

			GetAttributesRequestDocument treqDoc = GetAttributesRequestDocument.Factory
					.newInstance();
			GetAttributesRequestType treq = treqDoc
					.addNewGetAttributesRequest();
			treq.setVo("/times/test");
			treq.setRole("user");
			treq.setUserDN("CN=foo");
			GetAttributesResponseType tgetResp = pub.getAttributes(treqDoc)
					.getGetAttributesResponse();
			assertTrue(tgetResp.getGid().startsWith("times"));

			ListMappingRequestDocument tlistReqDoc2 = ListMappingRequestDocument.Factory
					.newInstance();
			ListMappingRequestType tlistReq2 = tlistReqDoc2
					.addNewListMappingRequest();
			tlistReq2.setPoolId("times-test-gids-pool");
			tlistReq2.setMappingType("live");
			MappingListDataType tlistResp2 = admin.listMappings(tlistReqDoc2)
					.getListMappingResponse();
			assertEquals(1, tlistResp2.getMappingArray().length);

			tlistReq2.setMappingType("frozen");
			listResp2 = admin.listMappings(listReqDoc2)
					.getListMappingResponse();
			assertEquals(0, listResp2.getMappingArray().length);

			Thread.sleep(6000);

			tlistReq2.setMappingType("live");
			tlistResp2 = admin.listMappings(tlistReqDoc2)
					.getListMappingResponse();
			assertEquals(0, tlistResp2.getMappingArray().length);

			tlistReq2.setMappingType("frozen");
			tlistResp2 = admin.listMappings(tlistReqDoc2)
					.getListMappingResponse();
			assertEquals(1, tlistResp2.getMappingArray().length);

			Thread.sleep(6000);

			tlistReq2.setMappingType("live");
			tlistResp2 = admin.listMappings(tlistReqDoc2)
					.getListMappingResponse();
			assertEquals(0, tlistResp2.getMappingArray().length);

			tlistReq2.setMappingType("frozen");
			tlistResp2 = admin.listMappings(tlistReqDoc2)
					.getListMappingResponse();
			assertEquals(0, tlistResp2.getMappingArray().length);

			// won't work as pool is not removed from the configuration
			// admin.removePool(removePoolReqDoc);
		} finally {
			try { server.shutdown(); } catch (Exception e) {}
		}
	}

	private PoolInfoType getPool(String name, PoolInfoType[] pools){
		for(PoolInfoType p: pools){
			if(name.equals(p.getPoolId()))return p;
		}
		return null;
	}
	
	@Test
	public void testTemplateExpansion() throws Exception {
		String t = "Hello, ${name}";
		var vars = new HashMap<String, Object>();
		vars.put("name", "Johnny");
		assertEquals("Hello, Johnny", ScriptMapping.evaluateTemplate(t, vars));
		// check missing variables lead to an error
		final String t2 = "Hello, ${foo}";
		assertThrows(PropertyAccessException.class,
				()->ScriptMapping.evaluateTemplate(t2, vars));
	}
}
