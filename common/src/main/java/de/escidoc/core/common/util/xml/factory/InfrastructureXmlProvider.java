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
 * Copyright 2007-2008 Fachinformationszentrum Karlsruhe Gesellschaft
 * fuer wissenschaftlich-technische Information mbH and Max-Planck-
 * Gesellschaft zur Foerderung der Wissenschaft e.V.  
 * All rights reserved.  Use is subject to license terms.
 */
package de.escidoc.core.common.util.xml.factory;

import de.escidoc.core.common.exceptions.system.WebserverSystemException;
import de.escidoc.core.common.util.service.UserContext;

/**
 * XmlTemplateProvider implementation using the velocity template engine.<br>
 * This implementation uses the velocity singleton pattern.
 * 
 * @author TTE
 * @common
 */
public abstract class InfrastructureXmlProvider extends VelocityXmlProvider {

    private static final String REST_PATH = "rest";

    private static final String SOAP_PATH = "soap";

    /**
     * Protected constructor to prevent initialization.
     * 
     * @common
     */
    protected InfrastructureXmlProvider() {

    }

    @Override
    protected String completePath() throws WebserverSystemException {
        if (UserContext.isRestAccess()) {
            return REST_PATH;
        }
        else {
            return SOAP_PATH;
        }
    }

}
