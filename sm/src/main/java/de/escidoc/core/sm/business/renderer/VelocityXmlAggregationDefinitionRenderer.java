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
package de.escidoc.core.sm.business.renderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import de.escidoc.core.common.business.Constants;
import de.escidoc.core.common.exceptions.system.SystemException;
import de.escidoc.core.common.exceptions.system.WebserverSystemException;
import de.escidoc.core.common.util.xml.XmlUtility;
import de.escidoc.core.common.util.xml.factory.AggregationDefinitionXmlProvider;
import de.escidoc.core.common.util.xml.factory.XmlTemplateProvider;
import de.escidoc.core.sm.business.persistence.hibernate.AggregationDefinition;
import de.escidoc.core.sm.business.persistence.hibernate.AggregationStatisticDataSelector;
import de.escidoc.core.sm.business.persistence.hibernate.AggregationTable;
import de.escidoc.core.sm.business.persistence.hibernate.AggregationTableField;
import de.escidoc.core.sm.business.persistence.hibernate.AggregationTableIndexField;
import de.escidoc.core.sm.business.persistence.hibernate.AggregationTableIndexe;
import de.escidoc.core.sm.business.renderer.interfaces.AggregationDefinitionRendererInterface;
import de.escidoc.core.sm.business.util.comparator.AggregationStatisticDataSelectorComparator;
import de.escidoc.core.sm.business.util.comparator.AggregationTableComparator;
import de.escidoc.core.sm.business.util.comparator.AggregationTableFieldComparator;
import de.escidoc.core.sm.business.util.comparator.AggregationTableIndexComparator;
import de.escidoc.core.sm.business.util.comparator.AggregationTableIndexFieldComparator;

/**
 * AggregationDefinition renderer implementation using the velocity template engine.
 * 
 * @author MIH
 * @spring.bean 
 *              id="eSciDoc.core.aa.business.renderer.VelocityXmlAggregationDefinitionRenderer"
 * @aa
 */
public final class VelocityXmlAggregationDefinitionRenderer
    implements AggregationDefinitionRendererInterface {

    /**
     * Private constructor to prevent initialization.
     */
    private VelocityXmlAggregationDefinitionRenderer() {
    }

    // CHECKSTYLE:JAVADOC-OFF

    /**
     * See Interface for functional description.
     * 
     * @param aggregationDefinition
     * @return
     * @throws SystemException
     * @see de.escidoc.core.sm.business.renderer.interfaces.
     *      AggregationDefinitionRendererInterface#render(Map)
     * @sm
     */
    public String render(
            final AggregationDefinition aggregationDefinition) 
                                        throws SystemException {
        Map<String, Object> values = new HashMap<String, Object>();

        values.put("isRootAggregationDefinition", XmlTemplateProvider.TRUE);
        addAggregationDefinitionNamespaceValues(values);
        addAggregationDefinitionValues(aggregationDefinition, values);

        final String ret =
            getAggregationDefinitionXmlProvider()
                .getAggregationDefinitionXml(values);

        return ret;
    }

    /**
     * Adds the values of the {@link AggregationDefinition} to the provided {@link Map}.
     * 
     * @param aggregationDefinition
     *            The {@link AggregationDefinition}.
     * @param values
     *            The {@link Map} to add the values to.
     * @throws SystemException
     *             Thrown in case of an internal error.
     */
    private void addAggregationDefinitionValues(
        final AggregationDefinition aggregationDefinition, 
        final Map<String, Object> values)
        throws SystemException {
        DateTime createDateTime =
            new DateTime(aggregationDefinition.getCreationDate());
        createDateTime = createDateTime.withZone(DateTimeZone.UTC);
        String create = createDateTime.toString(Constants.TIMESTAMP_FORMAT);
        values.put("aggregationDefinitionCreationDate", create);
        values.put("aggregationDefinitionCreatedById", 
                    aggregationDefinition.getCreatorId());
        values.put("aggregationDefinitionCreatedByTitle", 
            "user " + aggregationDefinition.getCreatorId());
        values.put("aggregationDefinitionCreatedByHref", 
            XmlUtility.getUserAccountHref(aggregationDefinition.getCreatorId()));
        values.put("aggregationDefinitionId", aggregationDefinition.getId());
        values.put("aggregationDefinitionName", aggregationDefinition.getName());
        values.put("aggregationDefinitionHref", 
            XmlUtility.getAggregationDefinitionHref(
                        aggregationDefinition.getId()));
        values.put("aggregationDefinitionScopeId", 
                aggregationDefinition.getScope().getId());
        values.put("aggregationDefinitionScopeTitle", 
                aggregationDefinition.getScope().getName());
        values.put("aggregationDefinitionScopeHref", 
            XmlUtility.getScopeHref(aggregationDefinition.getScope().getId()));
        addAggregationTableValues(
                aggregationDefinition.getAggregationTables(), values);
        addStatisticDataSelectorValues(
                aggregationDefinition
                .getAggregationStatisticDataSelectors(), values);
    }
    
    /**
     * Adds the values of the {@link AggregationTable} to the provided {@link Map}.
     * 
     * @param aggregationTables
     *            set of aggregationTables.
     * @param values
     *            The {@link Map} to add the values to.
     */
    private void addAggregationTableValues(
        final Set<AggregationTable> aggregationTables, 
        final Map<String, Object> values) {
        Vector<HashMap<String, Object>> aggregationTablesVm = 
                            new Vector<HashMap<String, Object>>();
        if (aggregationTables != null) {
            TreeSet<AggregationTable> sortedAggregationTables = 
                new TreeSet<AggregationTable>(
                        new AggregationTableComparator());
            sortedAggregationTables.addAll(aggregationTables);
            for (AggregationTable aggregationTable : sortedAggregationTables) {
                HashMap<String, Object> tableMap = new HashMap<String, Object>();
                tableMap.put("name", aggregationTable.getName());
                
                //fields
                Vector<HashMap<String, String>> aggregationTableFieldsVm = 
                    new Vector<HashMap<String, String>>();
                if (aggregationTable.getAggregationTableFields() != null) {
                    TreeSet<AggregationTableField> sortedAggregationTableFields = 
                        new TreeSet<AggregationTableField>(
                                new AggregationTableFieldComparator());
                    sortedAggregationTableFields.addAll(
                            aggregationTable.getAggregationTableFields());
                    for (AggregationTableField aggregationTableField 
                            : sortedAggregationTableFields) {
                        HashMap<String, String> aggregationTableFieldVm = 
                            new HashMap<String, String>();
                        aggregationTableFieldVm.put(
                                "name", aggregationTableField.getName());
                        aggregationTableFieldVm.put(
                                "fieldTypeId", 
                                Integer.toString(
                                        aggregationTableField.getFieldTypeId()));
                        aggregationTableFieldVm.put(
                                "feed", aggregationTableField.getFeed());
                        aggregationTableFieldVm.put(
                                "xpath", aggregationTableField.getXpath());
                        aggregationTableFieldVm.put(
                                "dataType", aggregationTableField.getDataType());
                        aggregationTableFieldVm.put(
                                "reduceTo", aggregationTableField.getReduceTo());
                        aggregationTableFieldsVm.add(aggregationTableFieldVm);
                    }
                }
                tableMap.put("aggregationTableFields", aggregationTableFieldsVm);
                
                //indexes
                Vector<HashMap<String, Object>> aggregationTableIndexesVm = 
                    new Vector<HashMap<String, Object>>();
                if (aggregationTable.getAggregationTableIndexes() != null) {
                    TreeSet<AggregationTableIndexe> 
                            sortedAggregationTableIndexes = 
                        new TreeSet<AggregationTableIndexe>(
                                new AggregationTableIndexComparator());
                    sortedAggregationTableIndexes.addAll(
                            aggregationTable.getAggregationTableIndexes());
                    for (AggregationTableIndexe aggregationTableIndex 
                            : sortedAggregationTableIndexes) {
                        HashMap<String, Object> aggregationTableIndexVm = 
                            new HashMap<String, Object>();
                        aggregationTableIndexVm.put(
                                "name", aggregationTableIndex.getName());
                        Vector<HashMap<String, String>> indexFields = 
                                    new Vector<HashMap<String, String>>();
                        if (aggregationTableIndex
                                .getAggregationTableIndexFields() != null) {
                            TreeSet<AggregationTableIndexField> 
                            sortedAggregationTableIndexFields = 
                                new TreeSet<AggregationTableIndexField>(
                                new AggregationTableIndexFieldComparator());
                            sortedAggregationTableIndexFields.addAll(
                                    aggregationTableIndex
                                    .getAggregationTableIndexFields());
                            for (AggregationTableIndexField 
                                    aggregationTableIndexField 
                                    : sortedAggregationTableIndexFields) {
                                HashMap<String, String> field = 
                                            new HashMap<String, String>();
                                field.put("field", 
                                        aggregationTableIndexField.getField());
                                indexFields.add(field);
                            }
                        }
                        aggregationTableIndexVm.put(
                                "aggregationTableIndexFields", 
                                indexFields);
                        aggregationTableIndexesVm.add(aggregationTableIndexVm);
                    }
                }
                tableMap.put("aggregationTableIndexes", aggregationTableIndexesVm);
                aggregationTablesVm.add(tableMap);
            }
        }
        values.put("aggregationTables", aggregationTablesVm);
    }

    /**
     * Adds the values of the {@link AggregationStatisticDataSelector} to the provided {@link Map}.
     * 
     * @param aggregationStatisticDataSelectors
     *            set of AggregationStatisticDataSelectors.
     * @param values
     *            The {@link Map} to add the values to.
     */
    private void addStatisticDataSelectorValues(
            final Set<AggregationStatisticDataSelector>
            aggregationStatisticDataSelectors,
            final Map<String, Object> values) {
        Vector<HashMap<String, String>> aggregationDataSelectorsVm =
                new Vector<HashMap<String, String>>();
        if (aggregationStatisticDataSelectors != null) {
            TreeSet<AggregationStatisticDataSelector> 
            sortedAggregationStatisticDataSelectors = 
                new TreeSet<AggregationStatisticDataSelector>(
                new AggregationStatisticDataSelectorComparator());
            sortedAggregationStatisticDataSelectors.addAll(
                    aggregationStatisticDataSelectors);
            for (AggregationStatisticDataSelector 
                    aggregationStatisticDataSelector 
                    : sortedAggregationStatisticDataSelectors) {
                HashMap<String, String> selectorMap =
                        new HashMap<String, String>();
                selectorMap.put("selectorType",
                        aggregationStatisticDataSelector.getSelectorType());
                selectorMap.put("xpath",
                        aggregationStatisticDataSelector.getXpath());
                aggregationDataSelectorsVm.add(selectorMap);
            }
        }
        values.put("aggregationStatisticDataSelectors", 
                                aggregationDataSelectorsVm);
    }

    /**
     * See Interface for functional description.
     * 
     * @param aggregationDefinitions
     * @param asSrw
     * 
     * @return
     * @throws WebserverSystemException
     * @see de.escidoc.core.aa.business.renderer.interfaces.
     *      AggregationDefinitionRendererInterface
     *      #renderAggregationDefinitions(de.escidoc.core.sm.business.aggregationDefinition)
     * @sm
     */
    public String renderAggregationDefinitions(
        final Collection<AggregationDefinition> aggregationDefinitions, 
        final boolean asSrw)
        throws SystemException {

        Map<String, Object> values = new HashMap<String, Object>();

        values.put("isRootAggregationDefinition", XmlTemplateProvider.FALSE);
        values.put("aggregationDefinitionListTitle", 
                        "Aggregation Definition List");
        addAggregationDefinitionNamespaceValues(values);
        addAggregationDefinitionListNamespaceValues(values);

        final List<Map<String, Object>> aggregationDefinitionsValues;
        if(aggregationDefinitions != null) {
            aggregationDefinitionsValues = new ArrayList<Map<String, Object>>(aggregationDefinitions.size());
        } else {
            aggregationDefinitionsValues = new ArrayList(); 
        }
        if (aggregationDefinitions != null) {
            for (AggregationDefinition aggregationDefinition 
                                    : aggregationDefinitions) {
                Map<String, Object> aggregationDefinitionValues =
                        new HashMap<String, Object>();
                addAggregationDefinitionNamespaceValues(
                                aggregationDefinitionValues);
                addAggregationDefinitionValues(
                        aggregationDefinition, aggregationDefinitionValues);
                aggregationDefinitionsValues.add(aggregationDefinitionValues);
            }
        }
        values.put("aggregationDefinitions", aggregationDefinitionsValues);
        if (asSrw) {
            return getAggregationDefinitionXmlProvider()
                .getAggregationDefinitionsSrwXml(values);
        }
        else {
            return getAggregationDefinitionXmlProvider()
                .getAggregationDefinitionsXml(values);
        }
    }

    // CHECKSTYLE:JAVADOC-ON

    /**
     * Adds the aggregation definition name space values.
     * 
     * @param values
     *            The {@link Map} to that the values shall be added.
     * @throws SystemException e
     * @sm
     */
    private void addAggregationDefinitionNamespaceValues(
            final Map<String, Object> values) throws SystemException {
        addEscidocBaseUrl(values);
        values.put("aggregationDefinitionNamespacePrefix",
            Constants.AGGREGATION_DEFINITION_NS_PREFIX);
        values.put("aggregationDefinitionNamespace", 
                Constants.AGGREGATION_DEFINITION_NS_URI);
        values.put(XmlTemplateProvider.ESCIDOC_PROPERTIES_NS_PREFIX,
                Constants.PROPERTIES_NS_PREFIX);
        values.put(XmlTemplateProvider.ESCIDOC_PROPERTIES_NS,
                Constants.PROPERTIES_NS_URI);
        values.put(XmlTemplateProvider.ESCIDOC_SREL_NS_PREFIX,
                Constants.STRUCTURAL_RELATIONS_NS_PREFIX);
        values.put(XmlTemplateProvider.ESCIDOC_SREL_NS,
                Constants.STRUCTURAL_RELATIONS_NS_URI);
    }

    /**
     * Adds the aggregation definition list name space values.
     * 
     * @param values
     *            The {@link Map} to that the values shall be added.
     * @throws SystemException e
     * @sm
     */
    private void addAggregationDefinitionListNamespaceValues(
            final Map<String, Object> values) throws SystemException {
        addEscidocBaseUrl(values);
        values.put("aggregationDefinitionListNamespacePrefix",
            Constants.AGGREGATION_DEFINITION_LIST_NS_PREFIX);
        values.put("aggregationDefinitionListNamespace", 
                Constants.AGGREGATION_DEFINITION_LIST_NS_URI);
    }

    /**
     * Adds the escidoc base URL to the provided map.
     * 
     * @param values
     *            The map to add values to.
     * @throws WebserverSystemException
     *             Thrown in case of an internal error.
     * @sm
     */
    private void addEscidocBaseUrl(final Map<String, Object> values)
        throws WebserverSystemException {

        values.put(XmlTemplateProvider.VAR_ESCIDOC_BASE_URL, XmlUtility
            .getEscidocBaseUrl());
    }

    /**
     * Gets the <code>AggregationDefinitionXmlProvider</code> object.
     * 
     * @return Returns the <code>AggregationDefinitionXmlProvider</code> object.
     * @throws WebserverSystemException
     *             Thrown in case of an internal error.
     * @sm
     */
    private AggregationDefinitionXmlProvider getAggregationDefinitionXmlProvider()
        throws WebserverSystemException {

        return AggregationDefinitionXmlProvider.getInstance();
    }

}
