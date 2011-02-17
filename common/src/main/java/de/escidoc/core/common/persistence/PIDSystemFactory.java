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
package de.escidoc.core.common.persistence;

import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
import de.escidoc.core.common.exceptions.system.PidSystemException;
import de.escidoc.core.common.util.configuration.EscidocConfiguration;

import java.io.IOException;

/**
 * Factory for PID Generator and Management Systems.
 * 
 * @author SWA
 * 
 */
public abstract class PIDSystemFactory {

    private static PIDSystemFactory pidSystemFactory = null;

    private static String defaultFactory =
        "de.escidoc.core.common.persistence.impl.DummyPIDGeneratorFactory";

    /**
     * Protected constructor as getInstance() should be used to create an
     * instance of an PIDSystemFactory.
     */
    protected PIDSystemFactory() {
    }

    /**
     * Get a new instance using the class name specified in
     * escidoc-core.properties with 'escidoc-core.PidGeneratorFactory'.
     * 
     * @see de.escidoc.core.common.util.configuration.EscidocConfiguration
     * 
     * @return An instance of the PIDGeneratorFactory class specified in
     *         escidoc-core.properties with escidoc-core.PidGeneratorFactory.
     * @throws PidSystemException
     *             If no instance could be returned
     */
    public static synchronized PIDSystemFactory getInstance()
        throws PidSystemException {
        if (pidSystemFactory == null) {
            getImplementationFromConfig();
        }

        return pidSystemFactory;
    }

    /**
     * @see #getInstance()
     * 
     * @throws PidSystemException
     *             If no instance could be returned
     */
    private static void getImplementationFromConfig() throws PidSystemException {
        String factoryClassName;

        try {
            factoryClassName =
                EscidocConfiguration.getInstance().get(
                    EscidocConfiguration.ESCIDOC_CORE_PID_SYSTEM_FACTORY);
            if (factoryClassName == null) {
                factoryClassName = defaultFactory;
            }
        }
        catch (IOException e) {
            factoryClassName = defaultFactory;
        }

        try {
            Class<?> factoryClass = Class.forName(factoryClassName);
            // Class.newInstance can be used only if there is a no-arg
            // constructor ;
            // otherwise, use Class.getConstructor and Constructor.newInstance.
            // java.lang.reflect.Constructor constructor =
            // factoryClass.getConstructor(types);
            // Object[] params = { aConfig };
            // pidGeneratorFactory = (PIDGeneratorFactory)
            // constructor.newInstance( params );
            pidSystemFactory = (PIDSystemFactory) factoryClass.newInstance();
        }
        catch (ClassNotFoundException e) {
            throw new PidSystemException(e);
        }
        catch (InstantiationException e) {
            throw new PidSystemException(e);
        }
        catch (IllegalAccessException e) {
            throw new PidSystemException(e);
        }
    }

    /**
     * Return a PIDSystem using the underlying object model determined when the
     * PIDSystemFactory was instantiated.
     * 
     * @return An instance of a PIDSystem.
     * @throws PidSystemException
     *             If no PIDSystem could be returned
     * @throws MissingMethodParameterException
     *             If necessary parameter are missing.
     */
    public abstract PIDSystem getPIDGenerator() throws PidSystemException,
        MissingMethodParameterException;
}
