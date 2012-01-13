/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the Common Development and Distribution License, Version 1.0
 * only (the "License"). You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the license at license/ESCIDOC.LICENSE or http://www.escidoc.de/license. See the License for
 * the specific language governing permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each file and include the License file at
 * license/ESCIDOC.LICENSE. If applicable, add the following below this CDDL HEADER, with the fields enclosed by
 * brackets "[]" replaced with your own identifying information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 * Copyright 2006-2011 Fachinformationszentrum Karlsruhe Gesellschaft fuer wissenschaftlich-technische Information mbH
 * and Max-Planck-Gesellschaft zur Foerderung der Wissenschaft e.V. All rights reserved. Use is subject to license
 * terms.
 */

package de.escidoc.core.common.exceptions.application.invalid;

/**
 * The XmlSchemaValidationException is used to indicate that the XML cannot get validated by the given Schema. returned
 * httpStatusCode is 412. Status code (412) indicating that the precondition given in one or more of the request-header
 * fields evaluated to false when it was tested on the server.
 *
 * @author Michael Hoppe (FIZ Karlsruhe)
 */
public class XmlSchemaValidationException extends InvalidXmlException {

    /**
     * The serial version uid.
     */
    private static final long serialVersionUID = -4152264003695863784L;

    public static final int HTTP_STATUS_CODE = ESCIDOC_HTTP_SC_INVALID;

    public static final String HTTP_STATUS_MESSAGE = "XML schema validation failed.";

    /**
     * Default constructor.
     */
    public XmlSchemaValidationException() {
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message - the detail message.
     */
    public XmlSchemaValidationException(final String message) {
        super(message);
    }

    /**
     * Constructor used to map an initial exception.
     *
     * @param error Throwable
     */
    public XmlSchemaValidationException(final Throwable error) {
        super(error);
    }

    /**
     * Constructor used to create a new Exception with the specified detail message and a mapping to an initial
     * exception.
     *
     * @param message - the detail message.
     * @param error   Throwable
     */
    public XmlSchemaValidationException(final String message, final Throwable error) {
        super(message, error);
    }
}