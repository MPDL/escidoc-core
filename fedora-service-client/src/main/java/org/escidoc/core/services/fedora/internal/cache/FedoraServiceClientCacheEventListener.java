/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the Common Development and Distribution License, Version 1.0
 * only (the "License"). You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the license at license/ESCIDOC.LICENSE or http://www.escidoc.de/license. See the License
 * for the specific language governing permissions and limitations under the License.
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

package org.escidoc.core.services.fedora.internal.cache;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;
import net.sf.ehcache.event.CacheEventListenerFactory;

/**
 *
 */
public class FedoraServiceClientCacheEventListener implements CacheEventListener {

    @Override
    public void notifyElementRemoved(final Ehcache cache, final Element element) throws CacheException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void notifyElementPut(final Ehcache cache, final Element element) throws CacheException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void notifyElementUpdated(final Ehcache cache, final Element element) throws CacheException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void notifyElementExpired(final Ehcache cache, final Element element) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void notifyElementEvicted(final Ehcache cache, final Element element) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void notifyRemoveAll(final Ehcache cache) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void dispose() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return this;
    }
}
