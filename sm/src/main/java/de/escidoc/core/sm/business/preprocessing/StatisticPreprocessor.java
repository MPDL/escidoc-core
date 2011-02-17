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
package de.escidoc.core.sm.business.preprocessing;

import de.escidoc.core.common.business.queue.errorprocessing.ErrorMessageHandler;
import de.escidoc.core.common.exceptions.application.notfound.ScopeNotFoundException;
import de.escidoc.core.common.exceptions.system.SqlDatabaseSystemException;
import de.escidoc.core.common.exceptions.system.StatisticPreprocessingSystemException;
import de.escidoc.core.common.exceptions.system.XmlParserSystemException;
import de.escidoc.core.common.util.logger.AppLogger;
import de.escidoc.core.sm.business.Constants;
import de.escidoc.core.sm.business.persistence.DirectDatabaseAccessorInterface;
import de.escidoc.core.sm.business.persistence.SmAggregationDefinitionsDaoInterface;
import de.escidoc.core.sm.business.persistence.SmPreprocessingLogsDaoInterface;
import de.escidoc.core.sm.business.persistence.SmScopesDaoInterface;
import de.escidoc.core.sm.business.persistence.SmStatisticDataDaoInterface;
import de.escidoc.core.sm.business.persistence.hibernate.AggregationDefinition;
import de.escidoc.core.sm.business.persistence.hibernate.AggregationStatisticDataSelector;
import de.escidoc.core.sm.business.persistence.hibernate.PreprocessingLog;
import de.escidoc.core.sm.business.persistence.hibernate.Scope;
import de.escidoc.core.sm.business.vo.database.select.AdditionalWhereFieldVo;
import de.escidoc.core.sm.business.vo.database.select.DatabaseSelectVo;
import de.escidoc.core.sm.business.vo.database.select.RootWhereFieldVo;
import de.escidoc.core.sm.business.vo.database.select.RootWhereGroupVo;
import de.escidoc.core.sm.business.vo.database.select.SelectFieldVo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Preprocesses Raw Statistic Data once a night depending on
 * Aggregation-Definitions in the System.
 * Transactional behaviour:
 * -select all aggregation definitions
 * -for each aggregation-definition:
 *  select data from raw statistic-data table
 *  preprocess data
 *  write data to aggregation table
 *  if exception occurs somewhere while preprocessing of this aggregation-definition
 *  do rollback of data-insertion for this aggregation-definition
 *  and process next aggregation-definition.
 * 
 * @spring.bean id="business.StatisticPreprocessor" scope="prototype"
 * @author MIH
 */
public class StatisticPreprocessor {

    private static AppLogger log =
        new AppLogger(StatisticPreprocessor.class.getName());

    private SmAggregationDefinitionsDaoInterface dao;

    private SmScopesDaoInterface scopesDao;

    private SmStatisticDataDaoInterface statisticDataDao;

    private SmPreprocessingLogsDaoInterface preprocessingLogsDao;

    private DirectDatabaseAccessorInterface dbAccessor;

    private AggregationPreprocessor aggregationPreprocessor;

    private ErrorMessageHandler errorMessageHandler;

    /**
     * Retrieves all Aggregation-Definitions from Database and loop
     * through. For each Aggregation-Definition, get raw statistic Data and
     * process the data with Class AggregationPreprocessor.
     * 
     * @param inputDate
     *            date
     * @throws StatisticPreprocessingSystemException e
     * 
     */
    public void execute(final Date inputDate) 
        throws StatisticPreprocessingSystemException {
        if (log.isInfoEnabled()) {
            log.info("Preprocessing Statistics for Date " + inputDate);
        }
        Date date;
        if (inputDate != null) {
            date = inputDate;
        }
        else {
            date = new Date();
        }
        if (log.isInfoEnabled()) {
            log.info("ComputedDate: " + date);
        }
        try {
            // Get all Aggregation Definitions from Database
            Collection<AggregationDefinition> aggregationDefinitions =
                dao.retrieveAggregationDefinitions();
            if (aggregationDefinitions != null) {
                for (AggregationDefinition aggregationDefinition 
                                        : aggregationDefinitions) {
                    try {
                        execute(date, aggregationDefinition);
                    } catch (Exception e) {
                        errorMessageHandler.putErrorMessage(
                                new HashMap<String, String>(), e,
                            de.escidoc.core.common.business.Constants.
                            STATISTIC_PREPROCESSING_ERROR_LOGFILE);
                        log.error(e);
                        
                    }
                }
            }
        }
        catch (Exception e) {
            throw new StatisticPreprocessingSystemException(e);
        }
    }
    
    /**
     * Preprocess one AggregationDefinition for 
     * statistic-raw-data written within a period of time. 
     * 
     * @param startDate startDate
     * @param endDate endDate
     * @param aggregationDefinitionId id of aggregation-definition
     * @throws StatisticPreprocessingSystemException e
     * 
     */
    public void execute(
            final Date startDate, 
            final Date endDate, 
            final String aggregationDefinitionId) 
        throws StatisticPreprocessingSystemException {
        if (log.isInfoEnabled()) {
            log.info("Preprocessing Statistics for AggregationDefinition " 
                                                    + aggregationDefinitionId
                                                    + ", StartDate:"
                                                    + startDate
                                                    + ", EndDate:"
                                                    + endDate);
        }
        Date internalStartDate;
        Date internalEndDate;
        if (aggregationDefinitionId == null) {
            throw new StatisticPreprocessingSystemException(
                    "aggregationDefinitionId may not be null");
        }
        try {
            AggregationDefinition aggregationDefinition = 
                dao.retrieve(aggregationDefinitionId);
            internalStartDate = 
                determineStartDate(
                        startDate, aggregationDefinition.getScope().getId());
            internalEndDate = determineEndDate(endDate);
            if (log.isInfoEnabled()) {
                log.info("ComputedStartDate: " + internalStartDate);
                log.info("ComputedEndDate: " + internalEndDate);
            } 
            if (internalEndDate.before(internalStartDate)) {
                return;
            }
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(internalStartDate.getTime());
            Date executionDate = internalStartDate;
            while (internalEndDate.after(executionDate)) {
                execute(executionDate, aggregationDefinition);
                cal.add(Calendar.DATE, 1);
                executionDate.setTime(cal.getTimeInMillis());
            }
        } catch (Exception e) {
            errorMessageHandler.putErrorMessage(
                    new HashMap<String, String>(), e,
                de.escidoc.core.common.business.Constants.
                STATISTIC_PREPROCESSING_ERROR_LOGFILE);
            log.error(e);
            throw new StatisticPreprocessingSystemException(e);
        }
    }

    /**
     * Retrieves one Aggregation-Definition from Database. 
     * Get raw statistic Data and
     * process the data with Class AggregationPreprocessor.
     * If aggregationDefinitionId is null, 
     * throw Exception
     * 
     * @param inputDate date
     * @param aggregationDefinition aggregation-definition
     * @throws StatisticPreprocessingSystemException e
     * 
     */
    private void execute(
            final Date inputDate, 
            final AggregationDefinition aggregationDefinition) 
        throws StatisticPreprocessingSystemException {
        if (aggregationDefinition == null) {
            throw new StatisticPreprocessingSystemException(
                        "aggregationDefinition may not be null");
        }
        Date date;
        if (inputDate != null) {
            date = inputDate;
        }
        else {
            date = new Date();
        }
        try {
            // dont process statistic-data for this date and
            // aggregation-definition
            // if statistic-data was processed successfully before.
            Collection<PreprocessingLog> preprocessingLogs =
                    preprocessingLogsDao.retrievePreprocessingLogs(
                    aggregationDefinition.getId(), date, false);
            if (preprocessingLogs != null && preprocessingLogs.size() > 0) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                log.error("aggregation-definition "
                        + aggregationDefinition.getId()
                        + " already preprocessed successfully for date "
                        + dateFormat.format(date));
                return;
            }

            if (log.isInfoEnabled()) {
                log.info("preprocessing aggregation-definition " 
                        + aggregationDefinition.getName() + " for Date " + date);
            }

            // Do we have to access the statistic-data-table?
            if (aggregationDefinition
                .getAggregationStatisticDataSelectors() != null) {
                for (AggregationStatisticDataSelector 
                        aggregationStatisticDataSelector 
                        : (Set<AggregationStatisticDataSelector>)
                        aggregationDefinition
                .getAggregationStatisticDataSelectors()) {
                    if (aggregationStatisticDataSelector
                            .getSelectorType().equals("statistic-table")) {
                        // Extract Data from raw-statistics-table
                        List resultList =
                            dbAccessor
                                .executeSql(generateStatisticTableSelectVo(
                                        aggregationStatisticDataSelector,
                                        aggregationDefinition.getScope().getId(),
                                        date));
                        if (log.isInfoEnabled() && resultList != null) {
                            log.info("found " 
                                    + resultList.size() + " records");
                        }
                        // preprocess Data
                        AggregationPreprocessorVo 
                            aggregationPreprocessorVo = 
                                    aggregationPreprocessor
                                            .processAggregation(
                            aggregationDefinition, resultList);
                        // write dataHash into Database 
                        //(either insert or update)
                        synchronized (AggregationIdMapper
                                .getInstance().getAggregationIdEntry(
                                      aggregationDefinition.getId())) {
                            aggregationPreprocessor.persistAggregation(
                                    aggregationPreprocessorVo, 
                                    aggregationDefinition.getId(), date);
                        }

                    }
                }
            }
        } catch (RuntimeException e) {
           handleException(date, aggregationDefinition, e);
        } catch (Exception e) {
           handleException(date, aggregationDefinition, e);
        }
    }

    private void handleException(final Date date,
                                 final AggregationDefinition aggregationDefinition,
                                 Throwable e) throws StatisticPreprocessingSystemException {
        PreprocessingLog preprocessingLog = new PreprocessingLog();
            preprocessingLog.setAggregationDefinition(aggregationDefinition);
            preprocessingLog.setHasError(true);
            preprocessingLog.setLogEntry(e.toString());
            preprocessingLog.setProcessingDate(new java.sql.Date(date.getTime()));
            try {
                preprocessingLogsDao.savePreprocessingLog(preprocessingLog);
            } catch (SqlDatabaseSystemException e1) {}
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            throw new StatisticPreprocessingSystemException(
                    "error while preprocessing aggregationDefinition "
                    +  aggregationDefinition.getId()
                    + " for date " + dateFormat.format(date) + ": " + e);
    }

    /**
     * Generates DatabaseSelectVo for DirectDatabaseAccessor. Selects
     * statistic-records from statistic-table, matching scopeId, date and xpath.
     * 
     * @param aggregationStatisticDataSelector
     *            aggregationStatisticDataSelector Hibernate object
     * @param scopeId
     *            scopeId
     * @param date
     *            date
     * @return DatabaseSelectVo Returns DatabaseSelectVo with recordInfo.
     * @throws SqlDatabaseSystemException
     *             e
     * @throws XmlParserSystemException
     *             e
     * @throws StatisticPreprocessingSystemException
     *             e
     * @throws ScopeNotFoundException
     *             e
     * 
     * 
     * @sm
     */
    private DatabaseSelectVo generateStatisticTableSelectVo(
        final AggregationStatisticDataSelector aggregationStatisticDataSelector, 
        final String scopeId, final Date date)
                throws StatisticPreprocessingSystemException,
                XmlParserSystemException, 
                ScopeNotFoundException, SqlDatabaseSystemException {
        String xpath = null;
        if (aggregationStatisticDataSelector.getSelectorType() != null
                && aggregationStatisticDataSelector.getSelectorType()
                .equals("statistic-table")
            && aggregationStatisticDataSelector.getXpath() != null) {
            xpath = 
                aggregationStatisticDataSelector.getXpath();
        }
        DatabaseSelectVo databaseSelectVo = new DatabaseSelectVo();
        databaseSelectVo.setSelectType(Constants.DATABASE_SELECT_TYPE_SELECT);
        Collection<String> tablenames = new ArrayList<String>();
        tablenames.add(Constants.STATISTIC_DATA_TABLE_NAME);
        databaseSelectVo.setTableNames(tablenames);

        Collection<SelectFieldVo> selectFieldVos = new ArrayList<SelectFieldVo>();
        SelectFieldVo selectFieldVo = new SelectFieldVo();
        selectFieldVo.setFieldName(Constants.STATISTIC_DATA_XML_FIELD_NAME);
        selectFieldVos.add(selectFieldVo);
        SelectFieldVo selectFieldVo1 = new SelectFieldVo();
        selectFieldVo1.setFieldName(
                Constants.STATISTIC_DATA_TIMESTAMP_FIELD_NAME);
        selectFieldVos.add(selectFieldVo1);
        databaseSelectVo.setSelectFieldVos(selectFieldVos);

        RootWhereGroupVo rootWhereGroupVo = new RootWhereGroupVo();
        RootWhereFieldVo rootWhereFieldVo = new RootWhereFieldVo();
        Collection<AdditionalWhereFieldVo> additionalWhereFieldVos =
            new ArrayList<AdditionalWhereFieldVo>();

        rootWhereFieldVo.setFieldName(
                Constants.STATISTIC_DATA_TIMESTAMP_FIELD_NAME);
        rootWhereFieldVo
            .setFieldType(Constants.DATABASE_FIELD_TYPE_DAYDATE);
        rootWhereFieldVo.setOperator(Constants.DATABASE_OPERATOR_EQUALS);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        rootWhereFieldVo.setFieldValue(dateFormat.format(date));
        rootWhereGroupVo.setRootWhereFieldVo(rootWhereFieldVo);


        //get Scope to decide if scope-type is admin
        Scope scope = scopesDao.retrieve(scopeId);

        //only restrict to scope_id if Scope is no admin-scope
        if (!scope.getScopeType().equals(ScopeTypes.ADMIN.name())) {
            AdditionalWhereFieldVo additionalWhereFieldVo =
                new AdditionalWhereFieldVo();
            additionalWhereFieldVo.setAlliance(Constants.DATABASE_ALLIANCE_AND);
            additionalWhereFieldVo.setFieldName("scope_id");
            additionalWhereFieldVo.setFieldType(
                    Constants.DATABASE_FIELD_TYPE_TEXT);
            additionalWhereFieldVo.setOperator(Constants.DATABASE_OPERATOR_EQUALS);
            additionalWhereFieldVo.setFieldValue(scopeId);
            additionalWhereFieldVos.add(additionalWhereFieldVo);
        }

        if (xpath != null && !xpath.equals("")) {
            AdditionalWhereFieldVo xpathWhereFieldVo =
                new AdditionalWhereFieldVo();
            xpathWhereFieldVo.setAlliance(Constants.DATABASE_ALLIANCE_AND);
            xpathWhereFieldVo
                .setFieldType(Constants.DATABASE_FIELD_TYPE_FREE_SQL);
            xpathWhereFieldVo.setFieldValue(
                handleXpathQuery(xpath.replaceAll("\\s+", " "), 
                    Constants.STATISTIC_DATA_XML_FIELD_NAME));
            additionalWhereFieldVos.add(xpathWhereFieldVo);
        }

        rootWhereGroupVo.setAdditionalWhereFieldVos(additionalWhereFieldVos);

        databaseSelectVo.setRootWhereGroupVo(rootWhereGroupVo);
        return databaseSelectVo;
    }

    /**
     * Generates String (for db-query) out of xpath-element for extraction of
     * raw data.
     * 
     * @param inputXpathQuery
     *            The xpathQuery.
     * @param field
     *            The field.
     * @return String with xpathQuery
     * @throws StatisticPreprocessingSystemException
     *             e
     * 
     * @sm
     */
    private String handleXpathQuery(
        final String inputXpathQuery, final String field)
        throws StatisticPreprocessingSystemException {
        try {
            StringBuffer dbXpathQuery = new StringBuffer(" (");
            String xpathQuery = inputXpathQuery.replaceAll("\\s+", " ");
            // Split at and/ors and save and/ors in array
            String operatorHelper =
                xpathQuery.replaceAll(
                    ".*?(( and )|( AND )|( And )|( or )|( OR )|( Or )).*?",
                    "$1\\|");
            String[] operators = operatorHelper.split("\\|");
            String[] xpathQueryParts =
                xpathQuery
                    .split("( and )|( AND )|( And )|( or )|( OR )|( Or )");

            // iterate over xpath-query-parts
            // (eg //parameter[@name>\"type\"]/* > \u2018page-request\u2019)
            for (int i = 0; i < xpathQueryParts.length; i++) {
                if (i > 0) {
                    dbXpathQuery.append(" ").append(operators[i - 1]).append(
                        " ");
                }

                // save opening and closing brackets
                String xpathQueryPart = xpathQueryParts[i].trim();
                StringBuffer openingBracketSaver = new StringBuffer("");
                StringBuffer closingBracketSaver = new StringBuffer("");
                while (xpathQueryPart.indexOf('(') == 0) {
                    xpathQueryPart = xpathQueryPart.substring(1);
                    openingBracketSaver.append("(");
                }
                while (xpathQueryPart.lastIndexOf(')') == xpathQueryPart
                    .length() - 1) {
                    xpathQueryPart =
                        xpathQueryPart
                            .substring(0, xpathQueryPart.length() - 2);
                    closingBracketSaver.append(")");
                }

                // split xpath-query part at operator (<,> or =)
                String xpathExpression =
                    xpathQueryPart.replaceAll("(\\[.*?\\].*?)(=|>|<)",
                        "$1\\|$2\\|");
                String[] xpathExpressionParts =
                    xpathExpression.split("\\|.*?\\|");
                dbXpathQuery.append(openingBracketSaver);
                if (xpathExpressionParts.length > 1) {
                    String operator =
                        xpathExpression.replaceAll(".*?\\|(.*?)\\|.*", "$1");
                    String left =
                        dbAccessor.getXpathString(xpathExpressionParts[0],
                            field);
                    String right =
                        xpathExpressionParts[1].replaceAll("['\"]", "").trim();
                    dbXpathQuery
                        .append(left).append(" ").append(operator).append(" '")
                        .append(right).append("'");
                }
                else {
                    String left =
                        dbAccessor.getXpathBoolean(xpathExpressionParts[0],
                            field);
                    dbXpathQuery.append(left);
                }
                dbXpathQuery.append(closingBracketSaver);
            }
            dbXpathQuery.append(") ");
            return dbXpathQuery.toString();
        }
        catch (Exception e) {
            throw new StatisticPreprocessingSystemException(
                "Cannot handle xpath-query for statistic-table");
        }
    }
    
    /**
     * Determines the earliest startDate.
     * 
     * @param startDate
     *            requested startDate.
     * @param scopeId
     *            scopeId of requested aggregationDefinition.
     * @return Date
     * @throws SqlDatabaseSystemException e
     * 
     */
    private Date determineStartDate(
            final Date startDate, final String scopeId) 
                        throws SqlDatabaseSystemException {
        Date internalStartDate = 
            statisticDataDao.retrieveMinTimestamp(scopeId);
        if (internalStartDate == null) {
            internalStartDate = new Date();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(internalStartDate.getTime());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 1);
        internalStartDate.setTime(cal.getTimeInMillis());
        if (startDate != null 
                && startDate.after(internalStartDate)) {
            internalStartDate.setTime(startDate.getTime());
        }
        return internalStartDate;
    }

    /**
     * Determines the latest endDate.
     * 
     * @param endDate
     *            requested endDate.
     * @return Date
     * 
     * @sm
     */
    private Date determineEndDate(
            final Date endDate) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date internalEndDate = new Date(cal.getTimeInMillis());
        if (endDate != null 
                && endDate.before(internalEndDate)) {
            cal.setTimeInMillis(endDate.getTime());
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            internalEndDate.setTime(cal.getTimeInMillis());
        }
        return internalEndDate;
    }

    /**
     * Setter for the dao.
     * 
     * @spring.property ref="persistence.SmAggregationDefinitionsDao"
     * @param dao
     *            The data access object.
     * 
     * @sm
     */
    public void setDao(final SmAggregationDefinitionsDaoInterface dao) {
        this.dao = dao;
    }

    /**
     * Setter for the scopesDao.
     * 
     * @spring.property ref="persistence.SmScopesDao"
     * @param scopesDao
     *            The data access object.
     * 
     * @sm
     */
    public void setScopesDao(final SmScopesDaoInterface scopesDao) {
        this.scopesDao = scopesDao;
    }

    /**
     * Setter for the statisticDataDao.
     * 
     * @spring.property ref="persistence.SmStatisticDataDao"
     * @param statisticDataDao
     *            The data access object.
     * 
     * @sm
     */
    public void setStatisticDataDao(
            final SmStatisticDataDaoInterface statisticDataDao) {
        this.statisticDataDao = statisticDataDao;
    }

    /**
     * Setter for the preprocessingLogsDao.
     * 
     * @spring.property ref="persistence.SmPreprocessingLogsDao"
     * @param preprocessingLogsDao
     *            The data access object.
     * 
     * @sm
     */
    public void setPreprocessingLogsDao(
            final SmPreprocessingLogsDaoInterface preprocessingLogsDao) {
        this.preprocessingLogsDao = preprocessingLogsDao;
    }

    /**
     * Setting the directDatabaseAccessor.
     * 
     * @param dbAccessorIn
     *            The directDatabaseAccessor to set.
     * @spring.property ref="sm.persistence.DirectDatabaseAccessor"
     */
    public final void setDirectDatabaseAccessor(
        final DirectDatabaseAccessorInterface dbAccessorIn) {
        this.dbAccessor = dbAccessorIn;
    }

    /**
     * Setting the AggregationPreprocessor.
     * 
     * @param aggregationPreprocessor
     *            The AggregationPreprocessor to set.
     * @spring.property ref="business.AggregationPreprocessor"
     */
    public final void setAggregationPreprocessor(
        final AggregationPreprocessor aggregationPreprocessor) {
        this.aggregationPreprocessor = aggregationPreprocessor;
    }

    /**
     * Setting the errorMessageHandler.
     * 
     * @param errorMessageHandler
     *            The ErrorMessageHandler to set.
     * @spring.property ref="common.ErrorMessageHandler"
     */
    public final void setErrorMessageHandler(
        final ErrorMessageHandler errorMessageHandler) {
        this.errorMessageHandler = errorMessageHandler;
    }
}
