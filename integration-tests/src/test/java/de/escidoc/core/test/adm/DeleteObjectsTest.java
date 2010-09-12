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
package de.escidoc.core.test.adm;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import de.escidoc.core.common.exceptions.remote.application.notfound.ItemNotFoundException;
import de.escidoc.core.test.EscidocRestSoapTestsBase;

/**
 * Test suite for the DeleteObjects method of the admin tool.
 * 
 * @author SCHE
 * 
 */
@RunWith(value = Parameterized.class)
public class DeleteObjectsTest extends AdminToolTestBase {

    /**
     * The constructor.
     * 
     * @param transport
     *            The transport identifier.
     * 
     * @throws Exception
     *             If anything fails.
     */
    public DeleteObjectsTest(final int transport) throws Exception {
        super(transport);
    }

    /**
     * Delete a list of objects from Fedora, resource cache and search index.
     *
     * @throws Exception If anything fails.
     */
    @Test
    public void testDeleteObjects() throws Exception {
        // create item
        String xml =
            EscidocRestSoapTestsBase.getTemplateAsString(TEMPLATE_ITEM_PATH
                + "/" + getTransport(false), "escidoc_item_198_for_create.xml");
        String itemId = getObjidValue(createItem(xml));

        // delete item
        xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<param><id>" + itemId + "</id></param>";
        deleteObjects(xml);

        // wait until process has finished
        final int waitTime = 5000;

        while (true) {
            if (getPurgeStatus().indexOf("finished") > 0) {
                break;
            }
            Thread.sleep(waitTime);
        }

        // check if item still exists
        try {
            retrieveItem(itemId);
        }
        catch (Exception e) {
            EscidocRestSoapTestsBase.assertExceptionType(
                ItemNotFoundException.class, e);
        }
    }
}
