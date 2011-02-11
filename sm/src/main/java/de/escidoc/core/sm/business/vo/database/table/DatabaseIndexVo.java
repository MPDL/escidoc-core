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
package de.escidoc.core.sm.business.vo.database.table;

import java.util.Collection;

import de.escidoc.core.common.exceptions.system.SqlDatabaseSystemException;

/**
 * Holds information about an index in the database.
 * 
 * @author MIH
 * @sm
 */
public class DatabaseIndexVo {
    private String indexName;

    private Collection<String> fields;

    /**
     * @return the fields
     */
    public Collection<String> getFields() {
        return fields;
    }

    /**
     * @param fields
     *            the fields to set
     * @throws SqlDatabaseSystemException databaseException
     */
    public void setFields(final Collection<String> fields) 
                            throws SqlDatabaseSystemException {
        for (String field : fields) {
            if (field != null && (field.matches("(?s).*?\\s.*") 
                || field.matches("(?s).*?'.*"))) {
                throw new SqlDatabaseSystemException(
                    "index-field-name may not contain whitespaces or quotes");
            }
        }
        this.fields = fields;
    }

    /**
     * @return the indexName
     */
    public String getIndexName() {
        return indexName;
    }

    /**
     * @param indexName
     *            the indexName to set
     * @throws SqlDatabaseSystemException databaseException
     */
    public void setIndexName(final String indexName) 
                    throws SqlDatabaseSystemException {
        if (indexName != null && (indexName.matches("(?s).*?\\s.*") 
            || indexName.matches("(?s).*?'.*"))) {
            throw new SqlDatabaseSystemException(
                "index-name may not contain whitespaces or quotes");
        }
        this.indexName = indexName;
    }
}
