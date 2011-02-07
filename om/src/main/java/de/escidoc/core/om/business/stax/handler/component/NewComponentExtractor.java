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
package de.escidoc.core.om.business.stax.handler.component;

import de.escidoc.core.common.business.Constants;
import de.escidoc.core.common.business.fedora.Utility;
import de.escidoc.core.common.util.stax.StaxParser;
import de.escidoc.core.common.util.xml.XmlUtility;
import de.escidoc.core.common.util.xml.stax.events.Attribute;
import de.escidoc.core.common.util.xml.stax.events.EndElement;
import de.escidoc.core.common.util.xml.stax.events.StartElement;
import de.escidoc.core.common.util.xml.stax.handler.DefaultHandler;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

public class NewComponentExtractor extends DefaultHandler {

    protected XMLStreamWriter writer = null;

    protected Vector outputStreams = new Vector();

    private final StaxParser parser;

    private boolean inside = false;

    private final HashMap nsuris = new HashMap();

    private int deepLevel = 0;

    public NewComponentExtractor(StaxParser parser) {
        // TODO Auto-generated constructor stub
        this.parser = parser;
    }

    @Override
    public String characters(String data, StartElement element)
        throws XMLStreamException {

        String curPath = parser.getCurPath();

        if (inside) {
            writer.writeCharacters(data);
        }
        return data;
    }

    @Override
    public EndElement endElement(EndElement element) throws Exception {
        String curPath = parser.getCurPath();

        if (inside) {
            writer.writeEndElement();
            deepLevel--;

            String ns = element.getNamespace();
            Vector nsTrace = (Vector) nsuris.get(ns);

            if (nsTrace != null
                && (nsTrace.get(2) == null || nsTrace.get(2).equals(
                    element.getPrefix()))
                && nsTrace.get(1).equals(element.getLocalName())
                && ((Integer) nsTrace.get(0)).intValue() == (deepLevel + 1)) {

                nsuris.remove(ns);

            }

            // attribute namespaces
            // TODO iteration is a hack, use
            // javax.xml.namespace.NamespaceContext
            Iterator it = nsuris.keySet().iterator();
            Vector toRemove = new Vector();
            while (it.hasNext()) {
                try {
                    String key = (String) it.next();
                    nsTrace = (Vector) nsuris.get(key);
                    if (((Integer) nsTrace.get(0)).intValue() == (deepLevel + 1)) {
                        toRemove.add(key);
                    }
                }
                catch (Exception e) {
                    throw e;
                }
            }
            it = toRemove.iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                nsuris.remove(key);
            }
            if (curPath.endsWith("components/component")) {
                inside = false;
                writer.flush();
                writer.close();
            }
        }
        return element;
    }

    @Override
    public StartElement startElement(StartElement element)
        throws XMLStreamException {
        String curPath = parser.getCurPath();
        if (inside) {
            deepLevel++;
            writeElement(element);

            int attCount = element.getAttributeCount();
            for (int i = 0; i < attCount; i++) {
                Attribute curAtt = element.getAttribute(i);
                writeAttribute(curAtt.getNamespace(), element.getLocalName(),
                    curAtt.getLocalName(), curAtt.getValue(), curAtt
                        .getPrefix());
            }
        }
        else {
            if (curPath.endsWith("components/component")) {
                int indexObjid = element.indexOfAttribute(null, "objid");
                int indexHref =
                    element.indexOfAttribute(Constants.XLINK_NS_URI, "href");
                if (!(indexObjid > -1 && element
                    .getAttribute(indexObjid).getValue().length() > 0)
                    || (indexHref > -1 && Utility.getId(
                        element.getAttribute(indexHref).getValue()).length() > 0)) {
                    // start new component
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    writer = XmlUtility.createXmlStreamWriter(out);
                    outputStreams.add(out);
                    inside = true;
                    deepLevel++;
                    writeElement(element);
                    int attCount = element.getAttributeCount();
                    for (int i = 0; i < attCount; i++) {
                        Attribute curAtt = element.getAttribute(i);
                        writeAttribute(curAtt.getNamespace(), element
                            .getLocalName(), curAtt.getLocalName(), curAtt
                            .getValue(), curAtt.getPrefix());
                    }
                }
            }
        }
        return element;
    }

    private void writeElement(StartElement element) throws XMLStreamException {
        String name = element.getLocalName();
        String uri = element.getNamespace();
        String prefix = element.getPrefix();
        if ((uri) != null) {
            if (!nsuris.containsKey(uri)) {
                Vector namespaceTrace = new Vector();
                namespaceTrace.add(Integer.valueOf(deepLevel));
                namespaceTrace.add(name);
                namespaceTrace.add(prefix);
                nsuris.put(uri, namespaceTrace);
                writer.writeStartElement(prefix, name, uri);
                writer.writeNamespace(prefix, uri);
            } else {
                Vector namespaceTrace = (Vector) nsuris.get(uri);
                Integer deepLevelInMAp = (Integer) namespaceTrace.get(0);
                String prefixTrace = (String) namespaceTrace.get(2);
                if (prefixTrace == null || !prefixTrace.equals(prefix)) {
                    prefix = prefixTrace;
                }
                if (deepLevelInMAp.intValue() >= deepLevel) {
                    writer.writeStartElement(prefix, name, uri);
                    writer.writeNamespace(prefix, uri);
                } else {
                    writer.writeStartElement(prefix, name, uri);
                }
            }
        }
        else {
            writer.writeStartElement(name);
        }

    }

    private void writeAttribute(
        String uri, String elementName, String attributeName,
        String attributeValue, String prefix) throws XMLStreamException {

        if (uri != null) {
            if (!nsuris.containsKey(uri)) {
                Vector namespaceTrace = new Vector();
                namespaceTrace.add(Integer.valueOf(deepLevel));
                namespaceTrace.add(elementName);
                namespaceTrace.add(prefix);
                nsuris.put(uri, namespaceTrace);

                writer.writeNamespace(prefix, uri);
            } else {
                Vector namespaceTrace = (Vector) nsuris.get(uri);
                String prefixTrace = (String) namespaceTrace.get(2);
                if (!prefixTrace.equals(prefix)) {
                    prefix = prefixTrace;
                }
            }
        }
        writer.writeAttribute(prefix, uri, attributeName, attributeValue);
    }

    public Vector getOutputStreams() {
        return outputStreams;
    }
}
