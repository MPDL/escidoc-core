/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at license/ESCIDOC.LICENSE
 * or http://www.escidoc.de/license.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at license/ESCIDOC.LICENSE.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 */

/*
 * Copyright 2006-2008 Fachinformationszentrum Karlsruhe Gesellschaft
 * fuer wissenschaftlich-technische Information mbH and Max-Planck-
 * Gesellschaft zur Foerderung der Wissenschaft e.V.  
 * All rights reserved.  Use is subject to license terms.
 */
package de.escidoc.core.test.sb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.escidoc.core.test.EscidocRestSoapTestsBase;
import de.escidoc.core.test.common.client.servlet.HttpHelper;
import de.escidoc.core.test.security.client.PWCallback;

/**
 * Test the implementation of the admin search for items and containers.
 * 
 * @author MIH
 * 
 */
public class ItemContainerAdminSearchTest extends SearchTestBase {

    private static final String INDEX_NAME = "item_container_admin";
    
    private static final ArrayList<String> RESULT_XPATHS = 
        new ArrayList<String>() {
            private static final long serialVersionUID = 1L;
            { 
                add("/searchRetrieveResponse/records/record/"
                        + "recordData/search-result-record/item");
                add("/searchRetrieveResponse/records/record/"
                        + "recordData/search-result-record/container");
            }
            };

    private static String[] itemIds = null;

    private static String[] containerIds = null;

    private static String[] adminTestContainerIds = null;

    private static String[][] componentIds = null;

    private static int methodCounter = 0;

    private static String startTime = "";

    /**
     * @param transport
     *            The transport identifier.
     * @throws Exception
     *             e
     */
    public ItemContainerAdminSearchTest(final int transport) throws Exception {
        super(transport);
        item = new ItemHelper(transport);
        container = new ContainerHelper(transport);
        grant = new GrantHelper(
                transport, GrantHelper.getUserAccountHandlerCode());
    }

    /**
     * Set up servlet test.
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Before
    public void initialize() throws Exception {
        if (methodCounter == 0) {
            prepare();
//            int c = 3287;
//            containerIds = new String[14];
//            adminTestContainerIds = new String[20];
//            itemIds = new String[84];
//            componentIds = new String[84][2];
//            for (int i = 0; i < 14; i++) {
//                containerIds[i] = "escidoc:" + c;
//                c++;
//            }
//            c--;
//            for (int i = 0; i < 84; i++) {
//                c += 3;
//                itemIds[i] = "escidoc:" + c;
//                componentIds[i][0] = "escidoc:" + (c - 2);
//                componentIds[i][1] = "escidoc:" + (c - 1);
//            }
//            c++;
//            for (int i = 0; i < 20; i++) {
//                adminTestContainerIds[i] = "escidoc:" + c;
//                c++;
//            }
        }
    }

    /**
     * Clean up after servlet test.
     * 
     * @throws Exception
     *             If anything fails.
     */
    @After
    public void deinitialize() throws Exception {
        methodCounter++;
        // if (methodCounter == getTestAnnotationsCount()) {
        // deprepare();
        // }
    }

    /**
     * insert item(s) into system for the tests.
     * 
     * @test.name prepare
     * @test.id PREPARE
     * @test.input
     * @test.inputDescription
     * @test.expected
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    private void prepare() throws Exception {
        log.info("starting SearchTest at "
                + new DateTime(System.currentTimeMillis()
                        + (60 * 60 * 1000), DateTimeZone.UTC).toString());
        // create empty indices/////////////////////////////////////////////////
        String urlParameters =
                "?operation=updateIndex" + "&action=createEmpty"
                        + "&repositoryName=escidocrepository" + "&indexName=";
        String httpUrl =
                HttpHelper
                        .createUrl(
                                de.escidoc.core.test.common.client.servlet.Constants.PROTOCOL,
                                de.escidoc.core.test.common.client.servlet.Constants.HOST_PORT,
                                de.escidoc.core.test.common.client.servlet.Constants.FEDORAGSEARCH_BASE_URI
                                        + urlParameters);
        HttpHelper
                .executeHttpMethod(
                        de.escidoc.core.test.common.client.servlet.Constants.HTTP_METHOD_GET,
                        httpUrl, null, null, null, null);
        // /////////////////////////////////////////////////////////////////////

        startTime =
                new DateTime(System.currentTimeMillis(), DateTimeZone.UTC)
                        .toString();
        // Create
        // Containers/////////////////////////////////////////////////////
        // Build Container-Hierarchy with one parent,
        // two members with each again having two members.
        // Build this hierarchy twice, onnce with context escidoc:persistent3
        // and once with context escidoc:persistent10
        containerIds = new String[14];
        for (int i = 0; i < 2; i++) {
            String contextId = null;
            String handle = null;
            if (i == 0) {
                contextId = CONTEXT_ID;
                handle = PWCallback.DEPOSITOR_HANDLE;
            } else {
                contextId = CONTEXT_ID3;
                handle = PWCallback.DEPOSITOR_WALS_HANDLE;
            }
            containerIds[0 + (i * 7)] = prepareContainer(
                    handle,
                    contextId,
                    null,
                    null,
                    "escidoc_search_container0_" + getTransport(false) + ".xml",
                    STATUS_PENDING);
            containerIds[1 + (i * 7)] = prepareContainer(
                    handle,
                    contextId,
                    null,
                    containerIds[0 + (i * 7)],
                    "escidoc_search_container0_" + getTransport(false) + ".xml",
                    STATUS_PENDING);
            containerIds[2 + (i * 7)] = prepareContainer(
                    handle,
                    contextId,
                    null,
                    containerIds[0 + (i * 7)],
                    "escidoc_search_container0_" + getTransport(false) + ".xml",
                    STATUS_PENDING);
            containerIds[3 + (i * 7)] = prepareContainer(
                    handle,
                    contextId,
                    null,
                    containerIds[1 + (i * 7)],
                    "escidoc_search_container0_" + getTransport(false) + ".xml",
                    STATUS_PENDING);
            containerIds[4 + (i * 7)] = prepareContainer(
                    handle,
                    contextId,
                    null,
                    containerIds[1 + (i * 7)],
                    "escidoc_search_container0_" + getTransport(false) + ".xml",
                    STATUS_PENDING);
            containerIds[5 + (i * 7)] = prepareContainer(
                    handle,
                    contextId,
                    null,
                    containerIds[2 + (i * 7)],
                    "escidoc_search_container0_" + getTransport(false) + ".xml",
                    STATUS_PENDING);
            containerIds[6 + (i * 7)] = prepareContainer(
                    handle,
                    contextId,
                    null,
                    containerIds[2 + (i * 7)],
                    "escidoc_search_container0_" + getTransport(false) + ".xml",
                    STATUS_PENDING);
        }
        // /////////////////////////////////////////////////////////////////////

        // Create Items/////////////////////////////////////////////////////
        // Under each container in the hierarchy, put 6 items in 6 different
        // stati.
        //item in status pending under container[5] is also added to container[4]
        itemIds = new String[84];
        componentIds = new String[84][2];
        for (int i = 0; i < 2; i++) {
            String contextId = null;
            String handle = null;
            if (i == 0) {
                contextId = CONTEXT_ID;
                handle = PWCallback.DEPOSITOR_HANDLE;
            } else {
                contextId = CONTEXT_ID3;
                handle = PWCallback.DEPOSITOR_WALS_HANDLE;
            }
            for (int j = 0; j < 7; j++) {
                for (int k = 0; k < 6; k++) {
                    String[] parentContainerIds = null;
                    String status = null;
                    switch (k) {
                    case 0:
                        status = STATUS_PENDING;
                        if (j == 5) {
                            parentContainerIds = 
                                new String[]{containerIds[j + (i * 7)], 
                                    containerIds[j - 1 + (i * 7)]};
                        } else {
                            parentContainerIds = 
                                new String[]{containerIds[j + (i * 7)]};
                        }
                        break;
                    case 1:
                        status = STATUS_SUBMITTED;
                        parentContainerIds = 
                            new String[]{containerIds[j + (i * 7)]};
                        break;
                    case 2:
                        status = STATUS_RELEASED;
                        parentContainerIds = 
                            new String[]{containerIds[j + (i * 7)]};
                        break;
                    case 3:
                        status = STATUS_WITHDRAWN;
                        parentContainerIds = 
                            new String[]{containerIds[j + (i * 7)]};
                        break;
                    case 4:
                        status = "postreleased";
                        parentContainerIds = 
                            new String[]{containerIds[j + (i * 7)]};
                        break;
                    case 5:
                        status = STATUS_IN_REVISION;
                        parentContainerIds = 
                            new String[]{containerIds[j + (i * 7)]};
                        break;
                    }
                    HashMap<String, String> itemHash =
                            prepareItem(handle, contextId, 
                                    parentContainerIds,
                                    "escidoc_search_item"
                                            + ((i * 42) + (j * 6) + k) + "_"
                                            + getTransport(false) + ".xml",
                                    status);
                    itemIds[(i * 42) + (j * 6) + k] = itemHash.get("itemId");
                    for (int l = 0;; l++) {
                        if (itemHash.get("componentId" + (l + 1)) == null) {
                            break;
                        }
                        componentIds[(i * 42) + (j * 6) + k][l] =
                                        itemHash.get("componentId" + (l + 1));
                    }
                }
            }
        }
        // /////////////////////////////////////////////////////////////////////
        adminTestContainerIds = new String[20];
        for (int i = 0; i < 2; i++) {
            String contextId = null;
            String handle = null;
            if (i == 0) {
                contextId = CONTEXT_ID;
                handle = PWCallback.DEPOSITOR_HANDLE;
            } else {
                contextId = CONTEXT_ID3;
                handle = PWCallback.DEPOSITOR_WALS_HANDLE;
            }
            adminTestContainerIds[0 + (i * 10)] = prepareContainer(
                    handle,
                    contextId,
                    null,
                    null,
                    "escidoc_search_container0_" + getTransport(false) + ".xml",
                    STATUS_PENDING);
            adminTestContainerIds[1 + (i * 10)] = prepareContainer(
                    handle,
                    contextId,
                    null,
                    adminTestContainerIds[0 + (i * 10)],
                    "escidoc_search_container0_" + getTransport(false) + ".xml",
                    STATUS_PENDING);
            adminTestContainerIds[2 + (i * 10)] = prepareContainer(
                    handle,
                    contextId,
                    null,
                    adminTestContainerIds[0 + (i * 10)],
                    "escidoc_search_container0_" + getTransport(false) + ".xml",
                    STATUS_PENDING);
            adminTestContainerIds[3 + (i * 10)] = prepareContainer(
                    handle,
                    contextId,
                    null,
                    adminTestContainerIds[1 + (i * 10)],
                    "escidoc_search_container0_" + getTransport(false) + ".xml",
                    STATUS_PENDING);
            adminTestContainerIds[4 + (i * 10)] = prepareContainer(
                    handle,
                    contextId,
                    null,
                    adminTestContainerIds[1 + (i * 10)],
                    "escidoc_search_container0_" + getTransport(false) + ".xml",
                    STATUS_PENDING);
            adminTestContainerIds[5 + (i * 10)] = prepareContainer(
                    handle,
                    contextId,
                    null,
                    adminTestContainerIds[2 + (i * 10)],
                    "escidoc_search_container0_" + getTransport(false) + ".xml",
                    STATUS_PENDING);
            adminTestContainerIds[6 + (i * 10)] = prepareContainer(
                    handle,
                    contextId,
                    null,
                    adminTestContainerIds[2 + (i * 10)],
                    "escidoc_search_container0_" + getTransport(false) + ".xml",
                    STATUS_PENDING);
            adminTestContainerIds[7 + (i * 10)] = prepareContainer(
                    handle,
                    contextId,
                    null,
                    null,
                    "escidoc_search_container0_" + getTransport(false) + ".xml",
                    STATUS_WITHDRAWN);
            adminTestContainerIds[8 + (i * 10)] = prepareContainer(
                    handle,
                    contextId,
                    null,
                    null,
                    "escidoc_search_container0_" + getTransport(false) + ".xml",
                    "postreleased");
            adminTestContainerIds[9 + (i * 10)] = prepareContainer(
                handle,
                contextId,
                null,
                null,
                "escidoc_search_container0_" + getTransport(false) + ".xml",
                STATUS_IN_REVISION);
            prepareContainer(
                    handle,
                    contextId,
                    adminTestContainerIds[0 + (i * 10)],
                    null,
                    null,
                    STATUS_PENDING);
            prepareContainer(
                    handle,
                    contextId,
                    adminTestContainerIds[1 + (i * 10)],
                    null,
                    null,
                    STATUS_SUBMITTED);
            prepareContainer(
                    handle,
                    contextId,
                    adminTestContainerIds[2 + (i * 10)],
                    null,
                    null,
                    STATUS_SUBMITTED);
            prepareContainer(
                    handle,
                    contextId,
                    adminTestContainerIds[3 + (i * 10)],
                    null,
                    null,
                    STATUS_RELEASED);
            prepareContainer(
                    handle,
                    contextId,
                    adminTestContainerIds[4 + (i * 10)],
                    null,
                    null,
                    STATUS_RELEASED);
            prepareContainer(
                    handle,
                    contextId,
                    adminTestContainerIds[5 + (i * 10)],
                    null,
                    null,
                    STATUS_RELEASED);
            prepareContainer(
                    handle,
                    contextId,
                    adminTestContainerIds[6 + (i * 10)],
                    null,
                    null,
                    STATUS_RELEASED);
        }
        // /////////////////////////////////////////////////////////////////////
        waitForIndexerToAppear(itemIds[83], INDEX_NAME);
        Thread.sleep(60000);
    }

    /**
     * explain operation without parameters for existing database xyz.
     * 
     * @test.name explain (1)
     * @test.id SB_EX-1
     * @test.input
     * @test.inputDescription
     * @test.expected explain plan for the corresponding database according
     *                ZeeRex Schema
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSBEX1() throws Exception {
        HashMap<String, String> parameters = new HashMap<String, String>();
        String response = explain(parameters, INDEX_NAME);
        assertXmlValidExplainPlan(response);
        assertEquals("srw/search/" + INDEX_NAME, getDatabase(response));
        assertEquals(Constants.ITEM_CONTAINER_ADMIN_INDEX_FIELD_COUNT,
                                            getIndexFieldCount(response));
        assertEquals(Constants.ITEM_CONTAINER_ADMIN_SORT_FIELD_COUNT,
                                            getSortFieldCount(response));
    }

    /**
     * Test searching as anonymous user.
     * 
     * @test.name Anonymous User Search
     * @test.id SB_AnonymousUserSearch
     * @test.input anonymous user searching all objects
     * @test.expected 54 hits.
     *              Anonymous may see items/containers in version-status released
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsAnonymousUser() throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("handle", PWCallback.ANONYMOUS_HANDLE);
                put("expectedHits", "54");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (3 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 9 || i == 19) {
                                    continue;
                                }
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        for (int i = 0; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as Systemadministrator user.
     * 
     * @test.name Systemadministrator User Search
     * @test.id SB_SystemadministratorUserSearch
     * @test.input Systemadministrator user searching all objects
     * @test.expected 118 hits.
     *              Systemadministrator may see all items/containers
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsSystemadministratorUser() throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0",
                               GrantHelper.ROLE_HREF_SYSTEM_ADMINISTRATOR);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "118");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int i = 0; i < containerIds.length; i++) {
                            put(containerIds[i], null);
                        }
                        for (int i = 0; i < 20; i++) {
                            if (i == 8 || i == 18) {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "pending"));
                            } else {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                            }
                        }
                        for (int i = 0; i < 84; i++) {
                            if (i % 6 == 4) {
                                put(itemIds[i], getItemXpathList(i, "pending"));
                            } else {
                                put(itemIds[i], getItemXpathList(i, null));
                            }
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as Systeminspector user.
     * 
     * @test.name Systeminspector User Search
     * @test.id SB_SysteminspectorUserSearch
     * @test.input Systeminspector user searching all objects
     * @test.expected 118 hits.
     *              Systeminspector may see all items/containers
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsSysteminspectorUser() throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0",
                               GrantHelper.ROLE_HREF_SYSTEM_INSPECTOR);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "118");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int i = 0; i < containerIds.length; i++) {
                            put(containerIds[i], null);
                        }
                        for (int i = 0; i < 20; i++) {
                            if (i == 8 || i == 18) {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "pending"));
                            } else {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                            }
                        }
                        for (int i = 0; i < 84; i++) {
                            if (i % 6 == 4) {
                                put(itemIds[i], getItemXpathList(i, "pending"));
                            } else {
                                put(itemIds[i], getItemXpathList(i, null));
                            }
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as Depositor user.
     * 
     * @test.name Depositor User Search
     * @test.id SB_DepositorUserSearch
     * @test.input Depositor user searching all objects.
     *              scope on CONTEXT_ID.
     *              no object created
     * @test.expected 54 hits.
     *              Depositor may see all items/containers (s)he created
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsDepositorUser() throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper.ROLE_HREF_DEPOSITOR);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTEXT_BASE_URI
                        + "/" + CONTEXT_ID);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "54");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (3 + j * 10); i < (9 + j * 10); i++) {
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        for (int i = 0; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as Depositor user.
     * 
     * @test.name Depositor User Search
     * @test.id SB_DepositorUserSearch
     * @test.input Depositor user searching all objects.
     *              scope on CONTEXT_ID3.
     *              no object created
     * @test.expected 54 hits.
     *              Depositor may see all items/containers (s)he created
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsDepositorUser1() throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper.ROLE_HREF_DEPOSITOR);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTEXT_BASE_URI
                        + "/" + CONTEXT_ID3);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "54");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (3 + j * 10); i < (9 + j * 10); i++) {
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        for (int i = 0; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as Depositor user.
     * 
     * @test.name Depositor User Search
     * @test.id SB_DepositorUserSearch
     * @test.input Depositor user searching all objects.
     *              scope on CONTEXT_ID.
     *              all objects in this context created
     * @test.expected 86 hits.
     *              Depositor may see all items/containers (s)he created
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsDepositorUser2() throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("handle", PWCallback.DEPOSITOR_HANDLE);
                put("expectedHits", "86");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int i = 0; i < 10; i++) {
                            if (i == 8) {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "pending"));
                            } else {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                            }
                            
                        }
                        for (int i = 13; i < 19; i++) {
                            if (i == 18) {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                            } else {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                            }
                            
                        }
                        for (int i = 0; i < 7; i++) {
                            put(containerIds[i], null);
                        }
                        for (int i = 0; i < 42; i++) {
                            if (i % 6 == 4) {
                                put(itemIds[i], getItemXpathList(i, "pending"));
                            } else {
                                put(itemIds[i], getItemXpathList(i, null));
                            }
                        }
                        for (int i = 42; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as Depositor user.
     * 
     * @test.name Depositor User Search
     * @test.id SB_DepositorUserSearch
     * @test.input Depositor user searching all objects.
     *              scope on CONTEXT_ID3.
     *              all objects in this context created
     * @test.expected 86 hits.
     *              Depositor may see all items/containers (s)he created
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsDepositorUser3() throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("handle", PWCallback.DEPOSITOR_WALS_HANDLE);
                put("expectedHits", "86");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int i = 3; i < 9; i++) {
                            if (i == 8) {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                            } else {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                            }
                            
                        }
                        for (int i = 10; i < 20; i++) {
                            if (i == 18) {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "pending"));
                            } else {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                            }
                            
                        }
                        for (int i = 7; i < 14; i++) {
                            put(containerIds[i], null);
                        }
                        for (int i = 42; i < 84; i++) {
                            if (i % 6 == 4) {
                                put(itemIds[i], getItemXpathList(i, "pending"));
                            } else {
                                put(itemIds[i], getItemXpathList(i, null));
                            }
                        }
                        for (int i = 0; i < 42; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as Administrator user.
     * 
     * @test.name Administrator User Search
     * @test.id SB_AdministratorUserSearch
     * @test.input Administrator user searching all objects.
     *              scope on CONTEXT_ID.
     * @test.expected 86 hits.
     *              Administrator may see all items/containers of the scoped context
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsAdministratorUser() throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper.ROLE_HREF_ADMINISTRATOR);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTEXT_BASE_URI
                        + "/" + CONTEXT_ID);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "86");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int i = 0; i < 10; i++) {
                            if (i == 8) {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "pending"));
                            } else {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                            }
                            
                        }
                        for (int i = 13; i < 19; i++) {
                            if (i == 18) {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                            } else {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                            }
                            
                        }
                        for (int i = 0; i < 7; i++) {
                            put(containerIds[i], null);
                        }
                        for (int i = 0; i < 42; i++) {
                            if (i % 6 == 4) {
                                put(itemIds[i], getItemXpathList(i, "pending"));
                            } else {
                                put(itemIds[i], getItemXpathList(i, null));
                            }
                        }
                        for (int i = 42; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as Administrator user.
     * 
     * @test.name Administrator User Search
     * @test.id SB_AdministratorUserSearch
     * @test.input Administrator user searching all objects.
     *              scope on CONTEXT_ID3.
     * @test.expected 86 hits.
     *              Administrator may see all items/containers of the scoped context
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsAdministratorUser1() throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper.ROLE_HREF_ADMINISTRATOR);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTEXT_BASE_URI
                        + "/" + CONTEXT_ID3);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "86");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int i = 3; i < 9; i++) {
                            if (i == 8) {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                            } else {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                            }
                            
                        }
                        for (int i = 10; i < 20; i++) {
                            if (i == 18) {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "pending"));
                            } else {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                            }
                            
                        }
                        for (int i = 7; i < 14; i++) {
                            put(containerIds[i], null);
                        }
                        for (int i = 42; i < 84; i++) {
                            if (i % 6 == 4) {
                                put(itemIds[i], getItemXpathList(i, "pending"));
                            } else {
                                put(itemIds[i], getItemXpathList(i, null));
                            }
                        }
                        for (int i = 0; i < 42; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as Administrator user.
     * 
     * @test.name Administrator User Search
     * @test.id SB_AdministratorUserSearch
     * @test.input Administrator user searching all objects.
     *              scope on CONTEXT_ID and CONTEXT_ID3.
     * @test.expected 118 hits.
     *              Administrator may see all items/containers of the scoped context
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsAdministratorUser2() throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper.ROLE_HREF_ADMINISTRATOR);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTEXT_BASE_URI
                        + "/" + CONTEXT_ID);
                put("role1", GrantHelper.ROLE_HREF_ADMINISTRATOR);
                put("scope1", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTEXT_BASE_URI
                        + "/" + CONTEXT_ID3);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "118");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int i = 0; i < 20; i++) {
                            if (i == 8 || i == 18) {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "pending"));
                            } else {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                            }
                        }
                        for (int i = 0; i < 14; i++) {
                            put(containerIds[i], null);
                        }
                        for (int i = 0; i < 84; i++) {
                            if (i % 6 == 4) {
                                put(itemIds[i], getItemXpathList(i, "pending"));
                            } else {
                                put(itemIds[i], getItemXpathList(i, null));
                            }
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as CollaboratorModifierAddRemoveAnyMembers user.
     * 
     * @test.name CollaboratorModifierAddRemoveAnyMembers User Search
     * @test.id SB_CollaboratorModifierAddRemoveAnyMembersUserSearch
     * @test.input CollaboratorModifierAddRemoveAnyMembers user searching all objects.
     *              scope on TopLevel Container in CONTEXT_ID.
     * @test.expected 82 hits.
     *              CollaboratorModifierAddRemoveAnyMembers may:
     *              - see all items/containers below the scoped container + the scoped container
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorModifierAddRemoveAnyMembersUser()
            throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_ADD_REMOVE_ANY_MEMBERS);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + containerIds[0]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "82");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (3 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 9 || i == 19) {
                                    continue;
                                }
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        for (int i = 0; i < 7; i++) {
                            put(containerIds[i], null);
                        }
                        for (int i = 0; i < 42; i++) {
                            if (i % 6 == 4) {
                                put(itemIds[i], getItemXpathList(i, "pending"));
                            } else {
                                put(itemIds[i], getItemXpathList(i, null));
                            }
                        }
                        for (int i = 42; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as CollaboratorModifierAddRemoveAnyMembers user.
     * 
     * @test.name CollaboratorModifierAddRemoveAnyMembers User Search
     * @test.id SB_CollaboratorModifierAddRemoveAnyMembersUserSearch
     * @test.input CollaboratorModifierAddRemoveAnyMembers user searching all objects.
     *              scope on TopLevel Container in CONTEXT_ID3.
     * @test.expected 82 hits.
     *              CollaboratorModifierAddRemoveAnyMembers may:
     *              - see all items/containers below the scoped container + the scoped container
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorModifierAddRemoveAnyMembersUser1()
            throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_ADD_REMOVE_ANY_MEMBERS);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + containerIds[7]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "82");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (3 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 9 || i == 19) {
                                    continue;
                                }
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        for (int i = 7; i < 14; i++) {
                            put(containerIds[i], null);
                        }
                        for (int i = 42; i < 84; i++) {
                            if (i % 6 == 4) {
                                put(itemIds[i], getItemXpathList(i, "pending"));
                            } else {
                                put(itemIds[i], getItemXpathList(i, null));
                            }
                        }
                        for (int i = 0; i < 42; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as CollaboratorModifierAddRemoveAnyMembers user.
     * 
     * @test.name CollaboratorModifierAddRemoveAnyMembers User Search
     * @test.id SB_CollaboratorModifierAddRemoveAnyMembersUserSearch
     * @test.input CollaboratorModifierAddRemoveAnyMembers user searching all objects.
     *              scope on TopLevel Container in CONTEXT_ID3
     *              and TopLevel Container in CONTEXT_ID.
     * @test.expected 110 hits.
     *              CollaboratorModifierAddRemoveAnyMembers may:
     *              - see all items/containers below the scoped container + the scoped container
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorModifierAddRemoveAnyMembersUser2()
            throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_ADD_REMOVE_ANY_MEMBERS);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + containerIds[7]);
                put("role1", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_ADD_REMOVE_ANY_MEMBERS);
                put("scope1", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + containerIds[0]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "110");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (3 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 9 || i == 19) {
                                    continue;
                                }
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        for (int i = 0; i < 14; i++) {
                            put(containerIds[i], null);
                        }
                        for (int i = 0; i < 84; i++) {
                            if (i % 6 == 4) {
                                put(itemIds[i], getItemXpathList(i, "pending"));
                            } else {
                                put(itemIds[i], getItemXpathList(i, null));
                            }
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as CollaboratorModifierAddRemoveAnyMembers user.
     * 
     * @test.name CollaboratorModifierAddRemoveAnyMembers User Search
     * @test.id SB_CollaboratorModifierAddRemoveAnyMembersUserSearch
     * @test.input CollaboratorModifierAddRemoveAnyMembers user searching all objects.
     *              scope on 2 FirstLevel Containers in CONTEXT_ID
     *              and 1 second level Container in CONTEXT_ID3.
     * @test.expected 82 hits.
     *              CollaboratorModifierAddRemoveAnyMembers may:
     *              - see all items/containers below the scoped container + the scoped container
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorModifierAddRemoveAnyMembersUser3()
            throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_ADD_REMOVE_ANY_MEMBERS);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + containerIds[1]);
                put("role1", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_ADD_REMOVE_ANY_MEMBERS);
                put("scope1", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + containerIds[2]);
                put("role2", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_ADD_REMOVE_ANY_MEMBERS);
                put("scope2", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + containerIds[10]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "82");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (3 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 9 || i == 19) {
                                    continue;
                                }
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        for (int i = 1; i < 7; i++) {
                            put(containerIds[i], null);
                        }
                        for (int i = 6; i < 42; i++) {
                            if (i % 6 == 4) {
                                put(itemIds[i], getItemXpathList(i, "pending"));
                            } else {
                                put(itemIds[i], getItemXpathList(i, null));
                            }
                        }
                        put(itemIds[2], getItemXpathList(2, null));
                        put(itemIds[3], getItemXpathList(3, null));
                        put(itemIds[4], getItemXpathList(4, "released"));
                        put(containerIds[10], null);
                        for (int i = 42; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                        put(itemIds[60], getItemXpathList(60, null));
                        put(itemIds[61], getItemXpathList(61, null));
                        put(itemIds[64], getItemXpathList(64, "pending"));
                        put(itemIds[65], getItemXpathList(65, null));
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as CollaboratorModifierAddRemoveAnyMembers user.
     * 
     * @test.name CollaboratorModifierAddRemoveAnyMembers User Search
     * @test.id SB_CollaboratorModifierAddRemoveAnyMembersUserSearch
     * @test.input CollaboratorModifierAddRemoveAnyMembers user searching all objects.
     *              scope on 1 FirstLevel Containers in CONTEXT_ID
     * @test.expected 67 hits.
     *              CollaboratorModifierAddRemoveAnyMembers may:
     *              - see all items/containers below the scoped container + the scoped container
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorModifierAddRemoveAnyMembersUser3_1()
            throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_ADD_REMOVE_ANY_MEMBERS);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + containerIds[1]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "67");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (3 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 9 || i == 19) {
                                    continue;
                                }
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        put(containerIds[1], null);
                        put(containerIds[3], null);
                        put(containerIds[4], null);
                        for (int i = 6; i < 12; i++) {
                            if (i % 6 == 4) {
                                put(itemIds[i], getItemXpathList(i, "pending"));
                            } else {
                                put(itemIds[i], getItemXpathList(i, null));
                            }
                        }
                        for (int i = 18; i < 31; i++) {
                            if (i % 6 == 4) {
                                put(itemIds[i], getItemXpathList(i, "pending"));
                            } else {
                                put(itemIds[i], getItemXpathList(i, null));
                            }
                        }
                        put(itemIds[2], getItemXpathList(2, null));
                        put(itemIds[3], getItemXpathList(3, null));
                        put(itemIds[4], getItemXpathList(4, "released"));
                        put(itemIds[14], getItemXpathList(14, null));
                        put(itemIds[15], getItemXpathList(15, null));
                        put(itemIds[16], getItemXpathList(16, "released"));
                        for (int i = 30; i < 42; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                        for (int i = 42; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as CollaboratorModifierAddRemoveAnyMembers user.
     * 
     * @test.name CollaboratorModifierAddRemoveAnyMembers User Search
     * @test.id SB_CollaboratorModifierAddRemoveAnyMembersUserSearch
     * @test.input CollaboratorModifierAddRemoveAnyMembers user searching all objects.
     *              scope on 1 FirstLevel Containers in CONTEXT_ID
     * @test.expected 66 hits.
     *              CollaboratorModifierAddRemoveAnyMembers may:
     *              - see all items/containers below the scoped container + the scoped container
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorModifierAddRemoveAnyMembersUser3_2()
            throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_ADD_REMOVE_ANY_MEMBERS);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + containerIds[2]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "66");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (3 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 9 || i == 19) {
                                    continue;
                                }
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        put(containerIds[2], null);
                        put(containerIds[5], null);
                        put(containerIds[6], null);
                        for (int i = 12; i < 18; i++) {
                            if (i % 6 == 4) {
                                put(itemIds[i], getItemXpathList(i, "pending"));
                            } else {
                                put(itemIds[i], getItemXpathList(i, null));
                            }
                        }
                        for (int i = 30; i < 42; i++) {
                            if (i % 6 == 4) {
                                put(itemIds[i], getItemXpathList(i, "pending"));
                            } else {
                                put(itemIds[i], getItemXpathList(i, null));
                            }
                        }
                        put(itemIds[2], getItemXpathList(2, null));
                        put(itemIds[3], getItemXpathList(3, null));
                        put(itemIds[4], getItemXpathList(4, "released"));
                        put(itemIds[8], getItemXpathList(8, null));
                        put(itemIds[9], getItemXpathList(9, null));
                        put(itemIds[10], getItemXpathList(10, "released"));
                        for (int i = 18; i < 30; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                        for (int i = 42; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as CollaboratorModifierAddRemoveAnyMembers user.
     * 
     * @test.name CollaboratorModifierAddRemoveAnyMembers User Search
     * @test.id SB_CollaboratorModifierAddRemoveAnyMembersUserSearch
     * @test.input CollaboratorModifierAddRemoveAnyMembers user searching all objects.
     *              scope on TopLevel AdminTestContainer in CONTEXT_ID.
     * @test.expected 57 hits.
     *              CollaboratorModifierAddRemoveAnyMembers may:
     *              - see all items/containers below the scoped container + the scoped container
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorModifierAddRemoveAnyMembersUser4()
            throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_ADD_REMOVE_ANY_MEMBERS);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + adminTestContainerIds[0]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "57");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int i = 0; i < 9; i++) {
                            if (i == 8) {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                            } else {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                            }
                            
                        }
                        for (int i = 13; i < 19; i++) {
                            if (i == 18) {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                            } else {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                            }
                            
                        }
                        for (int i = 0; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as CollaboratorModifierAddRemoveAnyMembers user.
     * 
     * @test.name CollaboratorModifierAddRemoveAnyMembers User Search
     * @test.id SB_CollaboratorModifierAddRemoveAnyMembersUserSearch
     * @test.input CollaboratorModifierAddRemoveAnyMembers user searching all objects.
     *              scope on TopLevel AdminTestContainer in CONTEXT_ID3.
     * @test.expected 57 hits.
     *              CollaboratorModifierAddRemoveAnyMembers may:
     *              - see all items/containers below the scoped container + the scoped container
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorModifierAddRemoveAnyMembersUser5()
            throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_ADD_REMOVE_ANY_MEMBERS);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + adminTestContainerIds[10]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "57");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int i = 3; i < 9; i++) {
                            if (i == 8) {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                            } else {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                            }
                            
                        }
                        for (int i = 10; i < 19; i++) {
                            if (i == 18) {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                            } else {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                            }
                            
                        }
                        for (int i = 0; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as CollaboratorModifierAddRemoveAnyMembers user.
     * 
     * @test.name CollaboratorModifierAddRemoveAnyMembers User Search
     * @test.id SB_CollaboratorModifierAddRemoveAnyMembersUserSearch
     * @test.input CollaboratorModifierAddRemoveAnyMembers user searching all objects.
     *              scope on TopLevel AdminTestContainer in CONTEXT_ID and CONTEXT_ID3.
     * @test.expected 60 hits.
     *              CollaboratorModifierAddRemoveAnyMembers may:
     *              - see all items/containers below the scoped container + the scoped container
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorModifierAddRemoveAnyMembersUser6()
            throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_ADD_REMOVE_ANY_MEMBERS);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + adminTestContainerIds[0]);
                put("role1", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_ADD_REMOVE_ANY_MEMBERS);
                put("scope1", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + adminTestContainerIds[10]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "60");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int i = 0; i < 9; i++) {
                            if (i == 8) {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                            } else {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                            }
                            
                        }
                        for (int i = 10; i < 19; i++) {
                            if (i == 18) {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                            } else {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                            }
                            
                        }
                        for (int i = 0; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as CollaboratorModifierAddRemoveAnyMembers user.
     * 
     * @test.name CollaboratorModifierAddRemoveAnyMembers User Search
     * @test.id SB_CollaboratorModifierAddRemoveAnyMembersUserSearch
     * @test.input CollaboratorModifierAddRemoveAnyMembers user searching all objects.
     *              scope on 1 FirstLevel AdminTestContainers in CONTEXT_ID
     *              and 1 second level AdminTestContainer in CONTEXT_ID3.
     * @test.expected 55 hits.
     *              CollaboratorModifierAddRemoveAnyMembers may:
     *              - see all items/containers below the scoped container + the scoped container
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorModifierAddRemoveAnyMembersUser7()
            throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_ADD_REMOVE_ANY_MEMBERS);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + adminTestContainerIds[1]);
                put("role1", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_ADD_REMOVE_ANY_MEMBERS);
                put("scope1", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + adminTestContainerIds[13]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "55");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int i = 3; i < 9; i++) {
                            if (i == 8) {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                            } else {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                            }
                            
                        }
                        put(adminTestContainerIds[1], getAdminTestContainerXpathList(1, null));
                        for (int i = 13; i < 19; i++) {
                            if (i == 18) {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                            } else {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                            }
                            
                        }
                        for (int i = 0; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as CollaboratorModifierAddRemoveAnyMembers user.
     * 
     * @test.name CollaboratorModifierAddRemoveAnyMembers User Search
     * @test.id SB_CollaboratorModifierAddRemoveAnyMembersUserSearch
     * @test.input CollaboratorModifierAddRemoveAnyMembers user searching all objects.
     *              scope on postreleased AdminTestContainer in CONTEXT_ID.
     * @test.expected 54 hits.
     *              CollaboratorModifierAddRemoveAnyMembers may:
     *              - see all items/containers below the scoped container + the scoped container
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorModifierAddRemoveAnyMembersUser8()
            throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_ADD_REMOVE_ANY_MEMBERS);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + adminTestContainerIds[8]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "54");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int i = 3; i < 9; i++) {
                            if (i == 8) {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "pending"));
                            } else {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                            }
                            
                        }
                        for (int i = 13; i < 19; i++) {
                            if (i == 18) {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                            } else {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                            }
                            
                        }
                        for (int i = 0; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as CollaboratorModifierUpdateAnyMembers user.
     * 
     * @test.name CollaboratorModifierUpdateAnyMembers User Search
     * @test.id SB_CollaboratorModifierUpdateAnyMembersUserSearch
     * @test.input CollaboratorModifierUpdateAnyMembers user searching all objects.
     *              scope on TopLevel Container in CONTEXT_ID.
     * @test.expected 82 hits.
     *              CollaboratorModifierUpdateAnyMembers may:
     *              - see all items/containers below the scoped container + the scoped container
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorModifierUpdateAnyMembersUser()
            throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_UPDATE_ANY_MEMBERS);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + containerIds[0]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "82");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (3 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 9 || i == 19) {
                                    continue;
                                }
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        for (int i = 0; i < 7; i++) {
                            put(containerIds[i], null);
                        }
                        for (int i = 0; i < 42; i++) {
                            if (i % 6 == 4) {
                                put(itemIds[i], getItemXpathList(i, "pending"));
                            } else {
                                put(itemIds[i], getItemXpathList(i, null));
                            }
                        }
                        for (int i = 42; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as CollaboratorModifierUpdateAnyMembers user.
     * 
     * @test.name CollaboratorModifierUpdateAnyMembers User Search
     * @test.id SB_CollaboratorModifierUpdateAnyMembersUserSearch
     * @test.input CollaboratorModifierUpdateAnyMembers user searching all objects.
     *              scope on TopLevel Container in CONTEXT_ID3.
     * @test.expected 82 hits.
     *              CollaboratorModifierUpdateAnyMembers may:
     *              - see all items/containers below the scoped container + the scoped container
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorModifierUpdateAnyMembersUser1()
            throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_UPDATE_ANY_MEMBERS);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + containerIds[7]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "82");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (3 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 9 || i == 19) {
                                    continue;
                                }
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        for (int i = 7; i < 14; i++) {
                            put(containerIds[i], null);
                        }
                        for (int i = 42; i < 84; i++) {
                            if (i % 6 == 4) {
                                put(itemIds[i], getItemXpathList(i, "pending"));
                            } else {
                                put(itemIds[i], getItemXpathList(i, null));
                            }
                        }
                        for (int i = 0; i < 42; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as CollaboratorModifierUpdateAnyMembers user.
     * 
     * @test.name CollaboratorModifierUpdateAnyMembers User Search
     * @test.id SB_CollaboratorModifierUpdateAnyMembersUserSearch
     * @test.input CollaboratorModifierUpdateAnyMembers user searching all objects.
     *              scope on TopLevel Container in CONTEXT_ID3
     *              and TopLevel Container in CONTEXT_ID.
     * @test.expected 110 hits.
     *              CollaboratorModifierUpdateAnyMembers may:
     *              - see all items/containers below the scoped container + the scoped container
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorModifierUpdateAnyMembersUser2()
            throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_UPDATE_ANY_MEMBERS);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + containerIds[7]);
                put("role1", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_UPDATE_ANY_MEMBERS);
                put("scope1", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + containerIds[0]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "110");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (3 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 9 || i == 19) {
                                    continue;
                                }
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        for (int i = 0; i < 14; i++) {
                            put(containerIds[i], null);
                        }
                        for (int i = 0; i < 84; i++) {
                            if (i % 6 == 4) {
                                put(itemIds[i], getItemXpathList(i, "pending"));
                            } else {
                                put(itemIds[i], getItemXpathList(i, null));
                            }
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as CollaboratorModifierUpdateAnyMembers user.
     * 
     * @test.name CollaboratorModifierUpdateAnyMembers User Search
     * @test.id SB_CollaboratorModifierUpdateAnyMembersUserSearch
     * @test.input CollaboratorModifierUpdateAnyMembers user searching all objects.
     *              scope on 2 FirstLevel Containers in CONTEXT_ID
     *              and 1 second level Container in CONTEXT_ID3.
     * @test.expected 82 hits.
     *              CollaboratorModifierUpdateAnyMembers may:
     *              - see all items/containers below the scoped container + the scoped container
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorModifierUpdateAnyMembersUser3()
            throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_UPDATE_ANY_MEMBERS);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + containerIds[1]);
                put("role1", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_UPDATE_ANY_MEMBERS);
                put("scope1", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + containerIds[2]);
                put("role2", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_UPDATE_ANY_MEMBERS);
                put("scope2", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + containerIds[10]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "82");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (3 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 9 || i == 19) {
                                    continue;
                                }
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        for (int i = 1; i < 7; i++) {
                            put(containerIds[i], null);
                        }
                        for (int i = 6; i < 42; i++) {
                            if (i % 6 == 4) {
                                put(itemIds[i], getItemXpathList(i, "pending"));
                            } else {
                                put(itemIds[i], getItemXpathList(i, null));
                            }
                        }
                        put(itemIds[2], getItemXpathList(2, null));
                        put(itemIds[3], getItemXpathList(3, null));
                        put(itemIds[4], getItemXpathList(4, "released"));
                        put(containerIds[10], null);
                        for (int i = 42; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                        put(itemIds[60], getItemXpathList(60, null));
                        put(itemIds[61], getItemXpathList(61, null));
                        put(itemIds[64], getItemXpathList(64, "pending"));
                        put(itemIds[65], getItemXpathList(65, null));
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as CollaboratorModifierUpdateAnyMembers user.
     * 
     * @test.name CollaboratorModifierUpdateAnyMembers User Search
     * @test.id SB_CollaboratorModifierUpdateAnyMembersUserSearch
     * @test.input CollaboratorModifierUpdateAnyMembers user searching all objects.
     *              scope on 1 FirstLevel Containers in CONTEXT_ID
     * @test.expected 67 hits.
     *              CollaboratorModifierUpdateAnyMembers may:
     *              - see all items/containers below the scoped container + the scoped container
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorModifierUpdateAnyMembersUser3_1()
            throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_UPDATE_ANY_MEMBERS);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + containerIds[1]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "67");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (3 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 9 || i == 19) {
                                    continue;
                                }
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        put(containerIds[1], null);
                        put(containerIds[3], null);
                        put(containerIds[4], null);
                        for (int i = 6; i < 12; i++) {
                            if (i % 6 == 4) {
                                put(itemIds[i], getItemXpathList(i, "pending"));
                            } else {
                                put(itemIds[i], getItemXpathList(i, null));
                            }
                        }
                        for (int i = 18; i < 31; i++) {
                            if (i % 6 == 4) {
                                put(itemIds[i], getItemXpathList(i, "pending"));
                            } else {
                                put(itemIds[i], getItemXpathList(i, null));
                            }
                        }
                        put(itemIds[2], getItemXpathList(2, null));
                        put(itemIds[3], getItemXpathList(3, null));
                        put(itemIds[4], getItemXpathList(4, "released"));
                        put(itemIds[14], getItemXpathList(14, null));
                        put(itemIds[15], getItemXpathList(15, null));
                        put(itemIds[16], getItemXpathList(16, "released"));
                        for (int i = 30; i < 42; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                        for (int i = 42; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as CollaboratorModifierUpdateAnyMembers user.
     * 
     * @test.name CollaboratorModifierUpdateAnyMembers User Search
     * @test.id SB_CollaboratorModifierUpdateAnyMembersUserSearch
     * @test.input CollaboratorModifierUpdateAnyMembers user searching all objects.
     *              scope on 1 FirstLevel Containers in CONTEXT_ID
     * @test.expected 66 hits.
     *              CollaboratorModifierUpdateAnyMembers may:
     *              - see all items/containers below the scoped container + the scoped container
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorModifierUpdateAnyMembersUser3_2()
            throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_UPDATE_ANY_MEMBERS);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + containerIds[2]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "66");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (3 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 9 || i == 19) {
                                    continue;
                                }
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        put(containerIds[2], null);
                        put(containerIds[5], null);
                        put(containerIds[6], null);
                        for (int i = 12; i < 18; i++) {
                            if (i % 6 == 4) {
                                put(itemIds[i], getItemXpathList(i, "pending"));
                            } else {
                                put(itemIds[i], getItemXpathList(i, null));
                            }
                        }
                        for (int i = 30; i < 42; i++) {
                            if (i % 6 == 4) {
                                put(itemIds[i], getItemXpathList(i, "pending"));
                            } else {
                                put(itemIds[i], getItemXpathList(i, null));
                            }
                        }
                        put(itemIds[2], getItemXpathList(2, null));
                        put(itemIds[3], getItemXpathList(3, null));
                        put(itemIds[4], getItemXpathList(4, "released"));
                        put(itemIds[8], getItemXpathList(8, null));
                        put(itemIds[9], getItemXpathList(9, null));
                        put(itemIds[10], getItemXpathList(10, "released"));
                        for (int i = 18; i < 30; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                        for (int i = 42; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as CollaboratorModifierUpdateAnyMembers user.
     * 
     * @test.name CollaboratorModifierUpdateAnyMembers User Search
     * @test.id SB_CollaboratorModifierUpdateAnyMembersUserSearch
     * @test.input CollaboratorModifierUpdateAnyMembers user searching all objects.
     *              scope on TopLevel AdminTestContainer in CONTEXT_ID.
     * @test.expected 57 hits.
     *              CollaboratorModifierUpdateAnyMembers may:
     *              - see all items/containers below the scoped container + the scoped container
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorModifierUpdateAnyMembersUser4()
            throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_UPDATE_ANY_MEMBERS);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + adminTestContainerIds[0]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "57");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int i = 0; i < 9; i++) {
                            if (i == 8) {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                            } else {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                            }
                            
                        }
                        for (int i = 13; i < 19; i++) {
                            if (i == 18) {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                            } else {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                            }
                            
                        }
                        for (int i = 0; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as CollaboratorModifierUpdateAnyMembers user.
     * 
     * @test.name CollaboratorModifierUpdateAnyMembers User Search
     * @test.id SB_CollaboratorModifierUpdateAnyMembersUserSearch
     * @test.input CollaboratorModifierUpdateAnyMembers user searching all objects.
     *              scope on TopLevel AdminTestContainer in CONTEXT_ID3.
     * @test.expected 57 hits.
     *              CollaboratorModifierUpdateAnyMembers may:
     *              - see all items/containers below the scoped container + the scoped container
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorModifierUpdateAnyMembersUser5()
            throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_UPDATE_ANY_MEMBERS);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + adminTestContainerIds[10]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "57");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int i = 3; i < 9; i++) {
                            if (i == 8) {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                            } else {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                            }
                            
                        }
                        for (int i = 10; i < 19; i++) {
                            if (i == 18) {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                            } else {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                            }
                            
                        }
                        for (int i = 0; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as CollaboratorModifierUpdateAnyMembers user.
     * 
     * @test.name CollaboratorModifierUpdateAnyMembers User Search
     * @test.id SB_CollaboratorModifierUpdateAnyMembersUserSearch
     * @test.input CollaboratorModifierUpdateAnyMembers user searching all objects.
     *              scope on TopLevel AdminTestContainer in CONTEXT_ID and CONTEXT_ID3.
     * @test.expected 60 hits.
     *              CollaboratorModifierUpdateAnyMembers may:
     *              - see all items/containers below the scoped container + the scoped container
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorModifierUpdateAnyMembersUser6()
            throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_UPDATE_ANY_MEMBERS);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + adminTestContainerIds[0]);
                put("role1", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_UPDATE_ANY_MEMBERS);
                put("scope1", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + adminTestContainerIds[10]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "60");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int i = 0; i < 20; i++) {
                            if (i == 9 || i == 19) {
                                continue;
                            }
                            if (i == 8 || i == 18) {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                            } else {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                            }
                            
                        }
                        for (int i = 0; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as CollaboratorModifierUpdateAnyMembers user.
     * 
     * @test.name CollaboratorModifierUpdateAnyMembers User Search
     * @test.id SB_CollaboratorModifierUpdateAnyMembersUserSearch
     * @test.input CollaboratorModifierUpdateAnyMembers user searching all objects.
     *              scope on 1 FirstLevel AdminTestContainer in CONTEXT_ID
     *              and 1 second level AdminTestContainer in CONTEXT_ID3.
     * @test.expected 55 hits.
     *              CollaboratorModifierUpdateAnyMembers may:
     *              - see all items/containers below the scoped container + the scoped container
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorModifierUpdateAnyMembersUser7()
            throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_UPDATE_ANY_MEMBERS);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + adminTestContainerIds[1]);
                put("role1", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_UPDATE_ANY_MEMBERS);
                put("scope1", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + adminTestContainerIds[13]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "55");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int i = 3; i < 9; i++) {
                            if (i == 8) {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                            } else {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                            }
                            
                        }
                        put(adminTestContainerIds[1], getAdminTestContainerXpathList(1, null));
                        for (int i = 13; i < 19; i++) {
                            if (i == 18) {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                            } else {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                            }
                            
                        }
                        for (int i = 0; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as CollaboratorModifierUpdateAnyMembers user.
     * 
     * @test.name CollaboratorModifierUpdateAnyMembers User Search
     * @test.id SB_CollaboratorModifierUpdateAnyMembersUserSearch
     * @test.input CollaboratorModifierUpdateAnyMembers user searching all objects.
     *              scope on postreleased AdminTestContainer in CONTEXT_ID.
     * @test.expected 54 hits.
     *              CollaboratorModifierUpdateAnyMembers may:
     *              - see all items/containers below the scoped container + the scoped container
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorModifierUpdateAnyMembersUser8()
            throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_UPDATE_ANY_MEMBERS);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + adminTestContainerIds[8]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "54");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int i = 3; i < 9; i++) {
                            if (i == 8) {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "pending"));
                            } else {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                            }
                            
                        }
                        for (int i = 13; i < 19; i++) {
                            if (i == 18) {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                            } else {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                            }
                            
                        }
                        for (int i = 0; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as CollaboratorModifierAddRemoveMembers user.
     * 
     * @test.name CollaboratorModifierAddRemoveMembers User Search
     * @test.id SB_CollaboratorModifierAddRemoveMembersUserSearch
     * @test.input CollaboratorModifierAddRemoveMembers user searching all objects.
     *              scope on TopLevel Container in CONTEXT_ID.
     * @test.expected 55 hits.
     *              CollaboratorModifierAddRemoveMembers may:
     *              - see scoped container
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorModifierAddRemoveMembersUser()
            throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_ADD_REMOVE_MEMBERS);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + containerIds[0]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "55");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (3 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 9 || i == 19) {
                                    continue;
                                }
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        put(containerIds[0], null);
                        for (int i = 0; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as CollaboratorModifierAddRemoveMembers user.
     * 
     * @test.name CollaboratorModifierAddRemoveMembers User Search
     * @test.id SB_CollaboratorModifierAddRemoveMembersUserSearch
     * @test.input CollaboratorModifierAddRemoveMembers user searching all objects.
     *              scope on TopLevel AdminTestContainer in CONTEXT_ID.
     * @test.expected 55 hits.
     *              CollaboratorModifierAddRemoveMembers may:
     *              - see scoped container
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorModifierAddRemoveMembersUser1()
            throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_ADD_REMOVE_MEMBERS);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + adminTestContainerIds[0]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "55");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (3 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 9 || i == 19) {
                                    continue;
                                }
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        put(adminTestContainerIds[0], getAdminTestContainerXpathList(0, null));
                        for (int i = 0; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as CollaboratorModifierAddRemoveMembers user.
     * 
     * @test.name CollaboratorModifierAddRemoveMembers User Search
     * @test.id SB_CollaboratorModifierAddRemoveMembersUserSearch
     * @test.input CollaboratorModifierAddRemoveMembers user searching all objects.
     *              scope on postreleased AdminTestContainer in CONTEXT_ID.
     * @test.expected 54 hits.
     *              CollaboratorModifierAddRemoveMembers may:
     *              - see scoped container
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorModifierAddRemoveMembersUser2()
            throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_ADD_REMOVE_MEMBERS);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + adminTestContainerIds[8]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "54");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (3 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 9 || i == 19) {
                                    continue;
                                }
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        put(adminTestContainerIds[8], getAdminTestContainerXpathList(8, "pending"));
                        for (int i = 0; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as CollaboratorModifierUpdateDirectMembers user.
     * 
     * @test.name CollaboratorModifierUpdateDirectMembers User Search
     * @test.id SB_CollaboratorModifierUpdateDirectMembersUserSearch
     * @test.input CollaboratorModifierUpdateDirectMembers user searching all objects.
     *              scope on TopLevel Container in CONTEXT_ID.
     * @test.expected 60 hits.
     *              CollaboratorModifierUpdateDirectMembers may:
     *              - see scoped container
     *              - see items/containers directly below scoped container
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorModifierUpdateDirectMembersUser()
            throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_UPDATE_DIRECT_MEMBERS);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + containerIds[0]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "60");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (3 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 9 || i == 19) {
                                    continue;
                                }
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        put(containerIds[0], null);
                        put(containerIds[1], null);
                        put(containerIds[2], null);
                        for (int i = 0; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                        put(itemIds[0], getItemXpathList(0, null));
                        put(itemIds[1], getItemXpathList(1, null));
                        put(itemIds[4], getItemXpathList(4, "pending"));
                        put(itemIds[5], getItemXpathList(5, null));
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as CollaboratorModifierUpdateDirectMembers user.
     * 
     * @test.name CollaboratorModifierUpdateDirectMembers User Search
     * @test.id SB_CollaboratorModifierUpdateDirectMembersUserSearch
     * @test.input CollaboratorModifierUpdateDirectMembers user searching all objects.
     *              scope on 1 SecondLevel Container in CONTEXT_ID.
     * @test.expected 59 hits.
     *              CollaboratorModifierUpdateDirectMembers may:
     *              - see scoped container
     *              - see items/containers directly below scoped container
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorModifierUpdateDirectMembersUser1()
            throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_UPDATE_DIRECT_MEMBERS);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + containerIds[4]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "59");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (3 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 9 || i == 19) {
                                    continue;
                                }
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        put(containerIds[4], null);
                        for (int i = 0; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                        put(itemIds[24], getItemXpathList(24, null));
                        put(itemIds[25], getItemXpathList(25, null));
                        put(itemIds[28], getItemXpathList(28, "pending"));
                        put(itemIds[29], getItemXpathList(29, null));
                        put(itemIds[30], getItemXpathList(30, null));
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as CollaboratorModifierUpdateDirectMembers user.
     * 
     * @test.name CollaboratorModifierUpdateDirectMembers User Search
     * @test.id SB_CollaboratorModifierUpdateDirectMembersUserSearch
     * @test.input CollaboratorModifierUpdateDirectMembers user searching all objects.
     *              scope on TopLevel AdminTestContainer in CONTEXT_ID3.
     * @test.expected 57 hits.
     *              CollaboratorModifierUpdateDirectMembers may:
     *              - see scoped container
     *              - see items/containers directly below scoped container
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorModifierUpdateDirectMembersUser1_1()
            throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_UPDATE_DIRECT_MEMBERS);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + adminTestContainerIds[10]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "57");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (3 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 9 || i == 19) {
                                    continue;
                                }
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        put(adminTestContainerIds[10], getAdminTestContainerXpathList(10, null));
                        put(adminTestContainerIds[11], getAdminTestContainerXpathList(11, null));
                        put(adminTestContainerIds[12], getAdminTestContainerXpathList(12, null));
                        for (int i = 0; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as CollaboratorModifierUpdateDirectMembers user.
     * 
     * @test.name CollaboratorModifierUpdateDirectMembers User Search
     * @test.id SB_CollaboratorModifierUpdateDirectMembersUserSearch
     * @test.input CollaboratorModifierUpdateDirectMembers user searching all objects.
     *              scope on FirstLevel AdminTestContainer in CONTEXT_ID3.
     * @test.expected 55 hits.
     *              CollaboratorModifierUpdateDirectMembers may:
     *              - see scoped container
     *              - see items/containers directly below scoped container
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorModifierUpdateDirectMembersUser2()
            throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER_UPDATE_DIRECT_MEMBERS);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + adminTestContainerIds[11]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "55");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (3 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 9 || i == 19) {
                                    continue;
                                }
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        put(adminTestContainerIds[11], getAdminTestContainerXpathList(11, null));
                        for (int i = 0; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as CollaboratorModifier user.
     * 
     * @test.name CollaboratorModifier User Search
     * @test.id SB_CollaboratorModifierUserSearch
     * @test.input CollaboratorModifier user searching all objects.
     *              scope on a component of item[0].
     * @test.expected 55 hits.
     *              CollaboratorModifier may have scope component, item, container, context:
     *              - scope: component:
     *                  -see item of scoped component
     *              - scope: item
     *                  -see scoped item
     *              - scope container
     *                  - see scoped container
     *              - scope context
     *                  -see items/containers of scoped context
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorModifierUser() throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.ITEM_BASE_URI
                        + "/" + itemIds[0] + "/components/component/"
                        + componentIds[0][0]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "55");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (3 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 9 || i == 19) {
                                    continue;
                                }
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        for (int i = 0; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                        put(itemIds[0], getItemXpathList(0, null));
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as CollaboratorModifier user.
     * 
     * @test.name CollaboratorModifier User Search
     * @test.id SB_CollaboratorModifierUserSearch
     * @test.input CollaboratorModifier user searching all objects.
     *              scope on item[0].
     * @test.expected 55 hits.
     *              CollaboratorModifier may have scope component, item, container, context:
     *              - scope: component:
     *                  -see item of scoped component
     *              - scope: item
     *                  -see scoped item
     *              - scope container
     *                  - see scoped container
     *              - scope context
     *                  -see items/containers of scoped context
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorModifierUser1() throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.ITEM_BASE_URI
                        + "/" + itemIds[0]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "55");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (3 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 9 || i == 19) {
                                    continue;
                                }
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        for (int i = 0; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                        put(itemIds[0], getItemXpathList(0, null));
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as CollaboratorModifier user.
     * 
     * @test.name CollaboratorModifier User Search
     * @test.id SB_CollaboratorModifierUserSearch
     * @test.input CollaboratorModifier user searching all objects.
     *              scope on item[0],[6],[13],[42],[55],[66],[72].
     * @test.expected 61 hits.
     *              CollaboratorModifier may have scope component, item, container, context:
     *              - scope: component:
     *                  -see item of scoped component
     *              - scope: item
     *                  -see scoped item
     *              - scope container
     *                  - see scoped container
     *              - scope context
     *                  -see items/containers of scoped context
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorModifierUser1_2() throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.ITEM_BASE_URI
                        + "/" + itemIds[0]);
                put("role1", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER);
                put("scope1", de.escidoc.core.test.common.client
                        .servlet.Constants.ITEM_BASE_URI
                        + "/" + itemIds[6]);
                put("role2", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER);
                put("scope2", de.escidoc.core.test.common.client
                        .servlet.Constants.ITEM_BASE_URI
                        + "/" + itemIds[13]);
                put("role3", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER);
                put("scope3", de.escidoc.core.test.common.client
                        .servlet.Constants.ITEM_BASE_URI
                        + "/" + itemIds[42]);
                put("role4", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER);
                put("scope4", de.escidoc.core.test.common.client
                        .servlet.Constants.ITEM_BASE_URI
                        + "/" + itemIds[55]);
                put("role5", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER);
                put("scope5", de.escidoc.core.test.common.client
                        .servlet.Constants.ITEM_BASE_URI
                        + "/" + itemIds[66]);
                put("role6", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER);
                put("scope6", de.escidoc.core.test.common.client
                        .servlet.Constants.ITEM_BASE_URI
                        + "/" + itemIds[72]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "61");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (3 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 9 || i == 19) {
                                    continue;
                                }
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        for (int i = 0; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                        put(itemIds[0], getItemXpathList(0, null));
                        put(itemIds[6], getItemXpathList(6, null));
                        put(itemIds[13], getItemXpathList(13, null));
                        put(itemIds[42], getItemXpathList(42, null));
                        put(itemIds[55], getItemXpathList(55, null));
                        put(itemIds[66], getItemXpathList(66, null));
                        put(itemIds[72], getItemXpathList(72, null));
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as CollaboratorModifier user.
     * 
     * @test.name CollaboratorModifier User Search
     * @test.id SB_CollaboratorModifierUserSearch
     * @test.input CollaboratorModifier user searching all objects.
     *              scope on container[3].
     * @test.expected 55 hits.
     *              CollaboratorModifier may have scope component, item, container, context:
     *              - scope: component:
     *                  -see item of scoped component
     *              - scope: item
     *                  -see scoped item
     *              - scope container
     *                  - see scoped container
     *              - scope context
     *                  -see items/containers of scoped context
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorModifierUser2() throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + containerIds[3]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "55");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (3 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 9 || i == 19) {
                                    continue;
                                }
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        for (int i = 0; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                        put(containerIds[3], null);
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as CollaboratorModifier user.
     * 
     * @test.name CollaboratorModifier User Search
     * @test.id SB_CollaboratorModifierUserSearch
     * @test.input CollaboratorModifier user searching all objects.
     *              scope on container[3] and container[9].
     * @test.expected 56 hits.
     *              CollaboratorModifier may have scope component, item, container, context:
     *              - scope: component:
     *                  -see item of scoped component
     *              - scope: item
     *                  -see scoped item
     *              - scope container
     *                  - see scoped container
     *              - scope context
     *                  -see items/containers of scoped context
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorModifierUser3() throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + containerIds[3]);
                put("role1", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER);
                put("scope1", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + containerIds[9]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "56");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (3 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 9 || i == 19) {
                                    continue;
                                }
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        for (int i = 0; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                        put(containerIds[3], null);
                        put(containerIds[9], null);
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as CollaboratorModifier user.
     * 
     * @test.name CollaboratorModifier User Search
     * @test.id SB_CollaboratorModifierUserSearch
     * @test.input CollaboratorModifier user searching all objects.
     *              scope on adminTestContainer[1] and adminTestContainer[8].
     * @test.expected 55 hits.
     *              CollaboratorModifier may have scope component, item, container, context:
     *              - scope: component:
     *                  -see item of scoped component
     *              - scope: item
     *                  -see scoped item
     *              - scope container
     *                  - see scoped container
     *              - scope context
     *                  -see items/containers of scoped context
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorModifierUser3_1() throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + adminTestContainerIds[1]);
                put("role1", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER);
                put("scope1", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + adminTestContainerIds[8]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "55");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (3 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 9 || i == 19) {
                                    continue;
                                }
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        put(adminTestContainerIds[1], getAdminTestContainerXpathList(1, null));
                        put(adminTestContainerIds[8], getAdminTestContainerXpathList(8, "pending"));
                        for (int i = 0; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as CollaboratorModifier user.
     * 
     * @test.name CollaboratorModifier User Search
     * @test.id SB_CollaboratorModifierUserSearch
     * @test.input CollaboratorModifier user searching all objects.
     *              scope on CONTEXT_ID.
     * @test.expected 86 hits.
     *              CollaboratorModifier may have scope component, item, container, context:
     *              - scope: component:
     *                  -see item of scoped component
     *              - scope: item
     *                  -see scoped item
     *              - scope container
     *                  - see scoped container
     *              - scope context
     *                  -see items/containers of scoped context
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorModifierUser4() throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTEXT_BASE_URI
                        + "/" + CONTEXT_ID);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "86");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int i = 0; i < 10; i++) {
                            if (i == 8) {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "pending"));
                            } else {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                            }
                            
                        }
                        for (int i = 13; i < 19; i++) {
                            if (i == 18) {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                            } else {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                            }
                            
                        }
                        for (int i = 0; i < 7; i++) {
                            put(containerIds[i], null);
                        }
                        for (int i = 0; i < 42; i++) {
                            if (i % 6 == 4) {
                                put(itemIds[i], getItemXpathList(i, "pending"));
                            } else {
                                put(itemIds[i], getItemXpathList(i, null));
                            }
                        }
                        for (int i = 42; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as CollaboratorModifier user.
     * 
     * @test.name CollaboratorModifier User Search
     * @test.id SB_CollaboratorModifierUserSearch
     * @test.input CollaboratorModifier user searching all objects.
     *              scope on CONTEXT_ID and CONTEXT_ID3.
     * @test.expected 118 hits.
     *              CollaboratorModifier may have scope component, item, container, context:
     *              - scope: component:
     *                  -see item of scoped component
     *              - scope: item
     *                  -see scoped item
     *              - scope container
     *                  - see scoped container
     *              - scope context
     *                  -see items/containers of scoped context
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorModifierUser5() throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTEXT_BASE_URI
                        + "/" + CONTEXT_ID);
                put("role1", GrantHelper
                        .ROLE_HREF_COLLABORATOR_MODIFIER);
                put("scope1", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTEXT_BASE_URI
                        + "/" + CONTEXT_ID3);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "118");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (0 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "pending"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        for (int i = 0; i < 14; i++) {
                            put(containerIds[i], null);
                        }
                        for (int i = 0; i < 84; i++) {
                            if (i % 6 == 4) {
                                put(itemIds[i], getItemXpathList(i, "pending"));
                            } else {
                                put(itemIds[i], getItemXpathList(i, null));
                            }
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as Collaborator user.
     * 
     * @test.name Collaborator User Search
     * @test.id SB_CollaboratorUserSearch
     * @test.input Collaborator user searching all objects.
     *              scope on a component of item[43].
     * @test.expected 55 hits.
     *              Collaborator may have scope component, item, container, context:
     *              - scope: component:
     *                  -see item of scoped component
     *              - scope: item
     *                  -see scoped item
     *              - scope container
     *                  - see scoped container
     *              - scope context
     *                  -see items/containers of scoped context
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorUser() throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.ITEM_BASE_URI
                        + "/" + itemIds[43] + "/components/component/"
                        + componentIds[43][0]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "55");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (3 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 9 || i == 19) {
                                    continue;
                                }
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        for (int i = 0; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                        put(itemIds[43], getItemXpathList(43, null));
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as Collaborator user.
     * 
     * @test.name Collaborator User Search
     * @test.id SB_CollaboratorUserSearch
     * @test.input Collaborator user searching all objects.
     *              scope on item[49].
     * @test.expected 55 hits.
     *              Collaborator may have scope component, item, container, context:
     *              - scope: component:
     *                  -see item of scoped component
     *              - scope: item
     *                  -see scoped item
     *              - scope container
     *                  - see scoped container
     *              - scope context
     *                  -see items/containers of scoped context
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorUser1() throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.ITEM_BASE_URI
                        + "/" + itemIds[49]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "55");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (3 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 9 || i == 19) {
                                    continue;
                                }
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        for (int i = 0; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                        put(itemIds[49], getItemXpathList(49, null));
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as Collaborator user.
     * 
     * @test.name Collaborator User Search
     * @test.id SB_CollaboratorUserSearch
     * @test.input Collaborator user searching all objects.
     *              scope on item[0],[6],[13],[42],[55],[66],[72].
     * @test.expected 61 hits.
     *              Collaborator may have scope component, item, container, context:
     *              - scope: component:
     *                  -see item of scoped component
     *              - scope: item
     *                  -see scoped item
     *              - scope container
     *                  - see scoped container
     *              - scope context
     *                  -see items/containers of scoped context
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorUser1_2() throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.ITEM_BASE_URI
                        + "/" + itemIds[0]);
                put("role1", GrantHelper
                        .ROLE_HREF_COLLABORATOR);
                put("scope1", de.escidoc.core.test.common.client
                        .servlet.Constants.ITEM_BASE_URI
                        + "/" + itemIds[6]);
                put("role2", GrantHelper
                        .ROLE_HREF_COLLABORATOR);
                put("scope2", de.escidoc.core.test.common.client
                        .servlet.Constants.ITEM_BASE_URI
                        + "/" + itemIds[13]);
                put("role3", GrantHelper
                        .ROLE_HREF_COLLABORATOR);
                put("scope3", de.escidoc.core.test.common.client
                        .servlet.Constants.ITEM_BASE_URI
                        + "/" + itemIds[42]);
                put("role4", GrantHelper
                        .ROLE_HREF_COLLABORATOR);
                put("scope4", de.escidoc.core.test.common.client
                        .servlet.Constants.ITEM_BASE_URI
                        + "/" + itemIds[55]);
                put("role5", GrantHelper
                        .ROLE_HREF_COLLABORATOR);
                put("scope5", de.escidoc.core.test.common.client
                        .servlet.Constants.ITEM_BASE_URI
                        + "/" + itemIds[66]);
                put("role6", GrantHelper
                        .ROLE_HREF_COLLABORATOR);
                put("scope6", de.escidoc.core.test.common.client
                        .servlet.Constants.ITEM_BASE_URI
                        + "/" + itemIds[72]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "61");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (3 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 9 || i == 19) {
                                    continue;
                                }
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        for (int i = 0; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                        put(itemIds[0], getItemXpathList(0, null));
                        put(itemIds[6], getItemXpathList(6, null));
                        put(itemIds[13], getItemXpathList(13, null));
                        put(itemIds[42], getItemXpathList(42, null));
                        put(itemIds[55], getItemXpathList(55, null));
                        put(itemIds[66], getItemXpathList(66, null));
                        put(itemIds[72], getItemXpathList(72, null));
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as Collaborator user.
     * 
     * @test.name Collaborator User Search
     * @test.id SB_CollaboratorUserSearch
     * @test.input Collaborator user searching all objects.
     *              scope on container[3].
     * @test.expected 55 hits.
     *              Collaborator may have scope component, item, container, context:
     *              - scope: component:
     *                  -see item of scoped component
     *              - scope: item
     *                  -see scoped item
     *              - scope container
     *                  - see scoped container
     *              - scope context
     *                  -see items/containers of scoped context
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorUser2() throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + containerIds[3]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "55");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (3 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 9 || i == 19) {
                                    continue;
                                }
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        for (int i = 0; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                        put(containerIds[3], null);
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as Collaborator user.
     * 
     * @test.name Collaborator User Search
     * @test.id SB_CollaboratorUserSearch
     * @test.input Collaborator user searching all objects.
     *              scope on container[3] and container[9].
     * @test.expected 56 hits.
     *              Collaborator may have scope component, item, container, context:
     *              - scope: component:
     *                  -see item of scoped component
     *              - scope: item
     *                  -see scoped item
     *              - scope container
     *                  - see scoped container
     *              - scope context
     *                  -see items/containers of scoped context
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorUser3() throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + containerIds[3]);
                put("role1", GrantHelper
                        .ROLE_HREF_COLLABORATOR);
                put("scope1", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + containerIds[9]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "56");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (3 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 9 || i == 19) {
                                    continue;
                                }
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        for (int i = 0; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                        put(containerIds[3], null);
                        put(containerIds[9], null);
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as Collaborator user.
     * 
     * @test.name Collaborator User Search
     * @test.id SB_CollaboratorUserSearch
     * @test.input Collaborator user searching all objects.
     *              scope on adminTestContainer[1] and adminTestContainer[8].
     * @test.expected 55 hits.
     *              Collaborator may have scope component, item, container, context:
     *              - scope: component:
     *                  -see item of scoped component
     *              - scope: item
     *                  -see scoped item
     *              - scope container
     *                  - see scoped container
     *              - scope context
     *                  -see items/containers of scoped context
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorUser3_1() throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + adminTestContainerIds[1]);
                put("role1", GrantHelper
                        .ROLE_HREF_COLLABORATOR);
                put("scope1", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTAINER_BASE_URI
                        + "/" + adminTestContainerIds[8]);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "55");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (3 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 9 || i == 19) {
                                    continue;
                                }
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        put(adminTestContainerIds[1], getAdminTestContainerXpathList(1, null));
                        put(adminTestContainerIds[8], getAdminTestContainerXpathList(8, "pending"));
                        for (int i = 0; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as Collaborator user.
     * 
     * @test.name Collaborator User Search
     * @test.id SB_CollaboratorUserSearch
     * @test.input Collaborator user searching all objects.
     *              scope on CONTEXT_ID.
     * @test.expected 86 hits.
     *              Collaborator may have scope component, item, container, context:
     *              - scope: component:
     *                  -see item of scoped component
     *              - scope: item
     *                  -see scoped item
     *              - scope container
     *                  - see scoped container
     *              - scope context
     *                  -see items/containers of scoped context
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorUser4() throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTEXT_BASE_URI
                        + "/" + CONTEXT_ID);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "86");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int i = 0; i < 10; i++) {
                            if (i == 8) {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "pending"));
                            } else {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                            }
                            
                        }
                        for (int i = 13; i < 19; i++) {
                            if (i == 18) {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                            } else {
                                put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                            }
                            
                        }
                        for (int i = 0; i < 7; i++) {
                            put(containerIds[i], null);
                        }
                        for (int i = 0; i < 42; i++) {
                            if (i % 6 == 4) {
                                put(itemIds[i], getItemXpathList(i, "pending"));
                            } else {
                                put(itemIds[i], getItemXpathList(i, null));
                            }
                        }
                        for (int i = 42; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as Collaborator user.
     * 
     * @test.name Collaborator User Search
     * @test.id SB_CollaboratorUserSearch
     * @test.input Collaborator user searching all objects.
     *              scope on CONTEXT_ID and CONTEXT_ID3.
     * @test.expected 118 hits.
     *              Collaborator may have scope component, item, container, context:
     *              - scope: component:
     *                  -see item of scoped component
     *              - scope: item
     *                  -see scoped item
     *              - scope container
     *                  - see scoped container
     *              - scope context
     *                  -see items/containers of scoped context
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsCollaboratorUser5() throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_COLLABORATOR);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTEXT_BASE_URI
                        + "/" + CONTEXT_ID);
                put("role1", GrantHelper
                        .ROLE_HREF_COLLABORATOR);
                put("scope1", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTEXT_BASE_URI
                        + "/" + CONTEXT_ID3);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "118");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (0 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "pending"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        for (int i = 0; i < 14; i++) {
                            put(containerIds[i], null);
                        }
                        for (int i = 0; i < 84; i++) {
                            if (i % 6 == 4) {
                                put(itemIds[i], getItemXpathList(i, "pending"));
                            } else {
                                put(itemIds[i], getItemXpathList(i, null));
                            }
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as MdEditor user.
     * 
     * @test.name MdEditor User Search
     * @test.id SB_MdEditorUserSearch
     * @test.input MdEditor user searching all objects.
     *              scope on CONTEXT_ID.
     * @test.expected 71 hits.
     *              MdEditor has scope on context
     *              - may see items/containers below scope
     *                public-status must be submitted, released, in-revision and withdrawn
     *                version-status must be pending, submitted, released or in-revision
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsMdEditorUser() throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_MD_EDITOR);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTEXT_BASE_URI
                        + "/" + CONTEXT_ID);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "71");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (3 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 9 || i == 19) {
                                    continue;
                                }
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        put(adminTestContainerIds[1], getAdminTestContainerXpathList(1, null));
                        put(adminTestContainerIds[2], getAdminTestContainerXpathList(2, null));
                        put(adminTestContainerIds[8], getAdminTestContainerXpathList(8, "pending"));
                        put(adminTestContainerIds[9], getAdminTestContainerXpathList(9, null));
                        for (int i = 0; i < 42; i += 6) {
                            put(itemIds[i + 1], getItemXpathList(i + 1, null));
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "pending"));
                            put(itemIds[i + 5], getItemXpathList(i + 5, null));
                        }
                        for (int i = 42; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as MdEditor user.
     * 
     * @test.name MdEditor User Search
     * @test.id SB_MdEditorUserSearch
     * @test.input MdEditor user searching all objects.
     *              scope on CONTEXT_ID3.
     * @test.expected 71 hits.
     *              MdEditor has scope on context
     *              - may see items/containers below scope
     *                public-status must be submitted, released, in-revision and withdrawn
     *                version-status must be pending, submitted, released or in-revision
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsMdEditorUser1() throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_MD_EDITOR);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTEXT_BASE_URI
                        + "/" + CONTEXT_ID3);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "71");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (3 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 9 || i == 19) {
                                    continue;
                                }
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        put(adminTestContainerIds[11], getAdminTestContainerXpathList(11, null));
                        put(adminTestContainerIds[12], getAdminTestContainerXpathList(12, null));
                        put(adminTestContainerIds[18], getAdminTestContainerXpathList(18, "pending"));
                        put(adminTestContainerIds[19], getAdminTestContainerXpathList(19, null));
                        for (int i = 42; i < 84; i += 6) {
                            put(itemIds[i + 1], getItemXpathList(i + 1, null));
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "pending"));
                            put(itemIds[i + 5], getItemXpathList(i + 5, null));
                        }
                        for (int i = 0; i < 42; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as MdEditor user.
     * 
     * @test.name MdEditor User Search
     * @test.id SB_MdEditorUserSearch
     * @test.input MdEditor user searching all objects.
     *              scope on CONTEXT_ID and CONTEXT_ID3.
     * @test.expected 88 hits.
     *              MdEditor has scope on context
     *              - may see items/containers below scope
     *                public-status must be submitted, released, in-revision and withdrawn
     *                version-status must be pending, submitted, released or in-revision
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsMdEditorUser2() throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_MD_EDITOR);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTEXT_BASE_URI
                        + "/" + CONTEXT_ID);
                put("role1", GrantHelper
                        .ROLE_HREF_MD_EDITOR);
                put("scope1", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTEXT_BASE_URI
                        + "/" + CONTEXT_ID3);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "88");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (1 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "pending"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        for (int i = 0; i < 84; i += 6) {
                            put(itemIds[i + 1], getItemXpathList(i + 1, null));
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "pending"));
                            put(itemIds[i + 5], getItemXpathList(i + 5, null));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as Moderator user.
     * 
     * @test.name Moderator User Search
     * @test.id SB_ModeratorUserSearch
     * @test.input Moderator user searching all objects.
     *              scope on CONTEXT_ID.
     * @test.expected 71 hits.
     *              Moderator has scope on context
     *              - may see items/containers below scope
     *                public-status must be submitted, released, in-revision and withdrawn
     *                version-status must be pending, submitted, released or in-revision
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsModeratorUser() throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_MODERATOR);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTEXT_BASE_URI
                        + "/" + CONTEXT_ID);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "71");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (3 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 9 || i == 19) {
                                    continue;
                                }
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        put(adminTestContainerIds[1], getAdminTestContainerXpathList(1, null));
                        put(adminTestContainerIds[2], getAdminTestContainerXpathList(2, null));
                        put(adminTestContainerIds[8], getAdminTestContainerXpathList(8, "pending"));
                        put(adminTestContainerIds[9], getAdminTestContainerXpathList(9, null));
                        for (int i = 0; i < 42; i += 6) {
                            put(itemIds[i + 1], getItemXpathList(i + 1, null));
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "pending"));
                            put(itemIds[i + 5], getItemXpathList(i + 5, null));
                        }
                        for (int i = 42; i < 84; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as Moderator user.
     * 
     * @test.name Moderator User Search
     * @test.id SB_ModeratorUserSearch
     * @test.input Moderator user searching all objects.
     *              scope on CONTEXT_ID3.
     * @test.expected 71 hits.
     *              Moderator has scope on context
     *              - may see items/containers below scope
     *                public-status must be submitted, released, in-revision and withdrawn
     *                version-status must be pending, submitted, released or in-revision
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsModeratorUser1() throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_MODERATOR);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTEXT_BASE_URI
                        + "/" + CONTEXT_ID3);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "71");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (3 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 9 || i == 19) {
                                    continue;
                                }
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "released"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        put(adminTestContainerIds[11], getAdminTestContainerXpathList(11, null));
                        put(adminTestContainerIds[12], getAdminTestContainerXpathList(12, null));
                        put(adminTestContainerIds[18], getAdminTestContainerXpathList(18, "pending"));
                        put(adminTestContainerIds[19], getAdminTestContainerXpathList(19, null));
                        for (int i = 42; i < 84; i += 6) {
                            put(itemIds[i + 1], getItemXpathList(i + 1, null));
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "pending"));
                            put(itemIds[i + 5], getItemXpathList(i + 5, null));
                        }
                        for (int i = 0; i < 42; i += 6) {
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "released"));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * Test searching as Moderator user.
     * 
     * @test.name Moderator User Search
     * @test.id SB_ModeratorUserSearch
     * @test.input Moderator user searching all objects.
     *              scope on CONTEXT_ID and CONTEXT_ID3.
     * @test.expected 88 hits.
     *              Moderator has scope on context
     *              - may see items/containers below scope
     *                public-status must be submitted, released, in-revision and withdrawn
     *                version-status must be pending, submitted, released or in-revision
     * @test.status Implemented
     * 
     * @throws Exception
     *             If anything fails.
     */
    @Test
    public void testSearchAsModeratorUser2() throws Exception {
        HashMap<String, Object> role = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("role0", GrantHelper
                        .ROLE_HREF_MODERATOR);
                put("scope0", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTEXT_BASE_URI
                        + "/" + CONTEXT_ID);
                put("role1", GrantHelper
                        .ROLE_HREF_MODERATOR);
                put("scope1", de.escidoc.core.test.common.client
                        .servlet.Constants.CONTEXT_BASE_URI
                        + "/" + CONTEXT_ID3);
                put("handle", PWCallback.TEST_HANDLE1);
                put("user", TEST_USER_ACCOUNT_ID1);
                put("expectedHits", "88");
                put("searchresultIds", new HashMap<String, ArrayList<String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        for (int j = 0; j < 2; j++) {
                            for (int i = (1 + j * 10); i < (10 + j * 10); i++) {
                                if (i == 8 + j * 10) {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, "pending"));
                                } else {
                                    put(adminTestContainerIds[i], getAdminTestContainerXpathList(i, null));
                                }
                                
                            }
                        }
                        for (int i = 0; i < 84; i += 6) {
                            put(itemIds[i + 1], getItemXpathList(i + 1, null));
                            put(itemIds[i + 2], getItemXpathList(i + 2, null));
                            put(itemIds[i + 3], getItemXpathList(i + 3, null));
                            put(itemIds[i + 4], getItemXpathList(i + 4, "pending"));
                            put(itemIds[i + 5], getItemXpathList(i + 5, null));
                        }
                    }
                });
            }
        };
        search(role);
    }

    /**
     * search with roles provided in HashMap.
     * 
     * @param role
     *            parameters
     * @throws Exception
     *             If anything fails.
     */
    private void search(final HashMap<String, Object> role) throws Exception {
        StringBuffer errorTrace = new StringBuffer();
        errorTrace.append("handle: ").append(role.get("handle")).append("\n");
        try {
            for (int i = 0;; i++) {
                if ((String) role.get("role" + i) == null) {
                    break;
                }
                errorTrace.append("role: ")
                        .append(role.get("role" + i)).append("\n");
                errorTrace.append("scope: ")
                        .append(role.get("scope" + i)).append("\n");
                grant.doTestCreateGrant(
                        null,
                        (String) role.get("user"),
                        (String) role.get("scope" + i),
                        (String) role.get("role" + i), null);
            }
            PWCallback.setHandle((String) role.get("handle"));
            HashMap<String, String> parameters = new HashMap<String, String>();
            parameters.put("query", "PID=escidoc*");
            parameters.put("maximumRecords", "150");
            String response = search(parameters, INDEX_NAME);
            assertXmlValidSearchResult(response);
            Document searchResultDoc = getDocument(response, true);
            Node n = selectSingleNode(searchResultDoc,
                    "/searchRetrieveResponse/diagnostics/diagnostic/details");
            String textContent = null;
            if (n != null) {
                textContent = n.getTextContent();
            }
            assertEquals(errorTrace.toString() + "diagnostics: "
                    + textContent, null, n);
            assertEquals(true, checkHighlighting(response));
            assertEquals(errorTrace.toString()
                    + "hits not as expected: expected: "
                    + role.get("expectedHits")
                    + ", but was "
                    + getNumberOfHits(response)
                    + " for ",
                    role.get("expectedHits"), getNumberOfHits(response));
            HashSet<String> foundIds = new HashSet<String>();
            // check if all items in result may be there
            for (String xPath : RESULT_XPATHS) {
                NodeList nodes = selectNodeList(
                        searchResultDoc, xPath);
                for (int i = 0; i < nodes.getLength(); i++) {
                    Node node = nodes.item(i);
                    String objId = getObjidValue(
                            de.escidoc.core.test.common.client
                            .servlet.Constants.TRANSPORT_SOAP, node, null);
                    foundIds.add(objId);
                    assertTrue(errorTrace.toString()
                            + "object " + objId + " may not be in searchResult",
                            ((HashMap<String, String>) role
                                    .get("searchresultIds")).containsKey(objId));
                    ArrayList<String> searchIds = 
                        ((HashMap<String, ArrayList<String>>) role
                            .get("searchresultIds")).get(objId);
                    if (searchIds != null) {
                        for (String searchId : searchIds) {
                            String[] parts = searchId.split("=");
                            assertXmlEquals(errorTrace.toString()
                                    + "not expected value in "
                                    + parts[0] + " for objectId " 
                                    + objId, node, parts[0], parts[1]);
                        }
                    }
                }
            }
            // check if all objects that should be in result are there
            for (String id : ((HashMap<String, String>) role
                    .get("searchresultIds")).keySet()) {
                assertTrue(errorTrace.toString()
                        + id + " was not in searchResult",
                        foundIds.contains(id));

            }
        } finally {
            PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
            if (role.get("role0") != null) {
                grant.revokeAllGrants((String) role.get("user"));
            }
        }
    }

    /**
     * prepare item for tests.
     * 
     * @param creatorHandle
     *            handle of creator
     * @param contextId
     *            id of context to create item in
     * @param containerIds
     *            ids of container to create item in
     * @param templateName
     *            template for item to create
     * @param status
     *            status of item to create
     * @return HashMap id of created item + componentIds
     * @throws Exception
     *             If anything fails.
     */
    private HashMap<String, String> prepareItem(
                final String creatorHandle,
                final String contextId,
                final String[] containerIds,
                final String templateName,
                final String status) throws Exception {
        HashMap<String, String> returnHash = new HashMap<String, String>();
        try {
            if (creatorHandle != null) {
                PWCallback.setHandle(creatorHandle);
            }
            Document xmlData =
                    EscidocRestSoapTestsBase.getTemplateAsDocument(
                            TEMPLATE_ITEM_SEARCH_ADMIN_PATH, templateName);
            if (getTransport() == de.escidoc.core.test.common.client.servlet.Constants.TRANSPORT_REST) {
                String contextHref = de.escidoc.core.test.common
                        .client.servlet.Constants.CONTEXT_BASE_URI + "/"
                        + contextId;
                substitute(xmlData,
                        "/item/properties/context/@href", contextHref);
            } else {
                substitute(xmlData,
                        "/item/properties/context/@objid", contextId);
            }
            String xml = item.create(toString(xmlData, false));
            String objectId = getId(xml);
            xml = xml.replaceAll("Meier", "Meier1");
            xml = item.update(objectId, xml);
            String lastModDate = getLastModificationDate(xml);
            Document itemDoc = EscidocRestSoapTestsBase.getDocument(xml);
            returnHash.put("itemId", objectId);
            for (int i = 1;; i++) {
                try {
                    String componentId = getComponentObjidValue(itemDoc, i);
                    if (componentId == null) {
                        break;
                    }
                    returnHash.put("componentId" + i, componentId);
                } catch (NullPointerException e) {
                    break;
                }
            }

            if (!status.equals(STATUS_PENDING)) {
                // submit item
                item.submit(objectId, "<param last-modification-date=\""
                        + lastModDate + "\" />");
                xml = item.retrieve(objectId);
                xml = xml.replaceAll("Meier", "Meier1");
                xml = item.update(objectId, xml);

                if (!status.equals(STATUS_SUBMITTED)) {
                    if (status.equals(STATUS_IN_REVISION)) {
                        xml = item.retrieve(objectId);
                        lastModDate = getLastModificationDate(xml);
                        PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
                        item.revise(objectId,
                                "<param last-modification-date=\"" + lastModDate
                                        + "\" />");
                    } else {
                        // assignPids
                        xml = item.retrieve(objectId);
                        String componentId = getComponentObjidValue(itemDoc, 1);
                        String pidParam = getItemPidParam(objectId);
                        item.assignContentPid(objectId, componentId, pidParam);
                        pidParam = getItemPidParam(objectId);
                        item.assignObjectPid(objectId, pidParam);
                        Node n = selectSingleNode(getDocument(xml),
                                "/item/properties/version/number");
                        String versionNumber = n.getTextContent();
                        String versionId = objectId + ":" + versionNumber;
                        pidParam = getItemPidParam(versionId);
                        item.assignVersionPid(versionId, pidParam);
                        // }

                        // release item
                        xml = item.retrieve(objectId);
                        lastModDate = getLastModificationDate(xml);
                        item.release(objectId, "<param last-modification-date=\""
                                + lastModDate + "\" />");
                    }
                    if (!status.equals(STATUS_RELEASED)
                            && !status.equals(STATUS_WITHDRAWN)
                            && !status.equals(STATUS_IN_REVISION)) {
                        xml = item.retrieve(objectId);
                        xml = xml.replaceAll("Meier", "Meier1");
                        item.update(objectId, xml);
                    } else if (!status.equals(STATUS_RELEASED)
                        && !status.equals(STATUS_IN_REVISION)) {
                        xml = item.retrieve(objectId);
                        lastModDate = getLastModificationDate(xml);
                        item.withdraw(objectId,
                                "<param last-modification-date=\""
                                        + lastModDate
                                        + "\"><withdraw-comment>"
                                        + "This is a withdraw comment."
                                        + "</withdraw-comment></param>");
                    }
                }
            }
            if (containerIds != null) {
                for (int i = 0; i < containerIds.length; i++) {
                    xml = container.retrieve(containerIds[i]);
                    lastModDate = getLastModificationDate(xml);
                    String taskParam =
                            "<param last-modification-date=\"" + lastModDate
                                    + "\">"
                                    + "<id>" + objectId + "</id></param>";

                    container.addMembers(containerIds[i], taskParam);
                }
            }
            return returnHash;
        } finally {
            PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
        }
    }

    /**
     * prepare container for tests.
     * 
     * @param creatorHandle
     *            handle of creator
     * @param contextId
     *            id of context to create container in
     * @param containerId
     *            id of container if already exists
     * @param parentContainerId
     *            id of container to create container in
     * @param templateName
     *            template for container to create
     * @param status
     *            status of container to create
     * @return String id of created container
     * @throws Exception
     *             If anything fails.
     */
    private String prepareContainer(
                final String creatorHandle,
                final String contextId,
                final String containerId,
                final String parentContainerId,
                final String templateName,
                final String status) throws Exception {
        try {
            PWCallback.setHandle(creatorHandle);
            String xml = null;
            String lastModDate = null;
            String objectId = null;
            String containerStatus = "init";
            if (containerId != null) {
                xml = container.retrieve(containerId);
                lastModDate = getLastModificationDate(xml);
                objectId = containerId;
                Node n = selectSingleNode(getDocument(xml),
                        "/container/properties/version/status");
                containerStatus = n.getTextContent();
            } else {
                Document xmlData =
                        EscidocRestSoapTestsBase.getTemplateAsDocument(
                                TEMPLATE_CONTAINER_SEARCH_PATH, templateName);
                if (getTransport() == de.escidoc.core.test.common.client.servlet.Constants.TRANSPORT_REST) {
                    String contextHref = de.escidoc.core.test.common
                            .client.servlet.Constants.CONTEXT_BASE_URI + "/"
                            + contextId;
                    substitute(xmlData,
                            "/container/properties/context/@href", contextHref);
                } else {
                    substitute(xmlData,
                            "/container/properties/context/@objid", contextId);
                }
                xml = container.create(toString(xmlData, false));
                objectId = getId(xml);
                xml = xml.replaceAll("Hoppe", "Hoppe1");
                xml = container.update(objectId, xml);
                lastModDate = getLastModificationDate(xml);
                containerStatus = STATUS_PENDING;
            }

            if (!status.equals(STATUS_PENDING)) {
                // submit container
                if (containerStatus.equals(STATUS_PENDING)) {
                    container.submit(objectId,
                            "<param last-modification-date=\"" + lastModDate
                                    + "\" />");
                    
                    xml = container.retrieve(objectId);
                    xml = xml.replaceAll("Hoppe", "Hoppe1");
                    xml = container.update(objectId, xml);
                    containerStatus = STATUS_SUBMITTED;
                }
                if (!status.equals(STATUS_SUBMITTED)) {
                    if (containerStatus.equals(STATUS_SUBMITTED)) {
                        if (status.equals(STATUS_IN_REVISION)) {
                            xml = container.retrieve(objectId);
                            lastModDate = getLastModificationDate(xml);
                            PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
                            container.revise(objectId,
                                    "<param last-modification-date=\"" + lastModDate
                                            + "\" />");
                            containerStatus = STATUS_IN_REVISION;
                        } else {
                            // assign pids
                            xml = container.retrieve(objectId);
                            Node n = selectSingleNode(getDocument(xml),
                            "/container/properties/version/number");
                            String versionNumber = n.getTextContent();
                            String pidParam = getContainerPidParam(objectId);
                            container.assignObjectPid(objectId, pidParam);
                            pidParam = getContainerPidParam(objectId);
                            container.assignVersionPid(
                                    objectId + ":" + versionNumber, pidParam);

                            // release container
                            xml = container.retrieve(objectId);
                            lastModDate = getLastModificationDate(xml);
                            container.release(objectId,
                                    "<param last-modification-date=\"" + lastModDate
                                            + "\" />");
                            containerStatus = STATUS_RELEASED;
                        }
                    }
                    if (!status.equals(STATUS_RELEASED)
                            && !status.equals(STATUS_WITHDRAWN)
                            && !status.equals(STATUS_IN_REVISION)) {
                        if (containerStatus.equals(STATUS_RELEASED)) {
                            xml = container.retrieve(objectId);
                            xml = xml.replaceAll("Hoppe", "Hoppe1");
                            container.update(objectId, xml);
                        }
                    } else if (!status.equals(STATUS_RELEASED)
                        && !status.equals(STATUS_IN_REVISION)) {
                        if (containerStatus.equals(STATUS_RELEASED)) {
                            xml = container.retrieve(objectId);
                            lastModDate = getLastModificationDate(xml);
                            container.withdraw(objectId,
                                    "<param last-modification-date=\""
                                    + lastModDate
                                    + "\"><withdraw-comment>"
                                    + "This is a withdraw comment."
                                    + "</withdraw-comment></param>");
                        }
                    }
                }

            }
            if (parentContainerId != null) {
                xml = container.retrieve(parentContainerId);
                lastModDate = getLastModificationDate(xml);
                String taskParam =
                        "<param last-modification-date=\"" + lastModDate
                                + "\">"
                                + "<id>" + objectId + "</id></param>";

                container.addMembers(parentContainerId, taskParam);
            }
            return objectId;
        } finally {
            PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
        }
    }
    
    private ArrayList<String> getItemXpathList(
            final int i, final String postreleasedStatus) {
        ArrayList<String> xpaths = new ArrayList<String>();
        if (postreleasedStatus != null) {
            xpaths.add("properties/version/status=" + postreleasedStatus);
        }
        if (i % 6 == 0) {
            //pending
            xpaths.add("properties/version/status=pending");
            xpaths.add("properties/public-status=pending");
            xpaths.add("properties/version/number=2");
        }
        else if (i % 6 == 1) {
            //submitted
            xpaths.add("properties/version/status=submitted");
            xpaths.add("properties/public-status=submitted");
            xpaths.add("properties/version/number=3");
        }
        else if (i % 6 == 2) {
            //released
            xpaths.add("properties/version/status=released");
            xpaths.add("properties/public-status=released");
            xpaths.add("properties/version/number=3");
        }
        else if (i % 6 == 3) {
            //withdrawn
            xpaths.add("properties/version/status=released");
            xpaths.add("properties/public-status=withdrawn");
            xpaths.add("properties/version/number=3");
        }
        else if (i % 6 == 4) {
            //postreleased
            xpaths.add("properties/public-status=released");
            xpaths.add("properties/latest-version/number=4");
            if (postreleasedStatus != null 
                    && postreleasedStatus.equals("pending")) {
                xpaths.add("properties/version/number=4");
            } else {
                xpaths.add("properties/version/number=3");
            }
        }
        else if (i % 6 == 5) {
            //in-revision
            xpaths.add("properties/version/status=in-revision");
            xpaths.add("properties/public-status=in-revision");
            xpaths.add("properties/version/number=3");
        }
        return xpaths;
    }

    private ArrayList<String> getAdminTestContainerXpathList(
            final int i, final String postreleasedStatus) {
        ArrayList<String> xpaths = new ArrayList<String>();
        if (postreleasedStatus != null) {
            xpaths.add("properties/version/status=" + postreleasedStatus);
        }
        if (i % 10 == 0) {
            //pending
            xpaths.add("properties/version/status=pending");
            xpaths.add("properties/public-status=pending");
            xpaths.add("properties/version/number=4");
        }
        else if (i % 10 == 1 || i % 10 == 2) {
            //submitted
            xpaths.add("properties/version/status=submitted");
            xpaths.add("properties/public-status=submitted");
            xpaths.add("properties/version/number=5");
        }
        else if (i % 10 == 3 || i % 10 == 4 
                || i % 10 == 5 || i % 10 == 6) {
            //released
            xpaths.add("properties/version/status=released");
            xpaths.add("properties/public-status=released");
            xpaths.add("properties/version/number=3");
        }
        else if (i % 10 == 7) {
            //withdrawn
            xpaths.add("properties/version/status=released");
            xpaths.add("properties/public-status=withdrawn");
            xpaths.add("properties/version/number=3");
        }
        else if (i % 10 == 8) {
            //postreleased
            xpaths.add("properties/public-status=released");
            xpaths.add("properties/latest-version/number=4");
            if (postreleasedStatus != null 
                    && postreleasedStatus.equals("pending")) {
                xpaths.add("properties/version/number=4");
            } else {
                xpaths.add("properties/version/number=3");
            }
        }
        else if (i % 10 == 9) {
            //in-revision
            xpaths.add("properties/version/status=in-revision");
            xpaths.add("properties/public-status=in-revision");
            xpaths.add("properties/version/number=3");
        }
        return xpaths;
    }

}
