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
package de.escidoc.core.common.util.aop;

import de.escicore.statistic.StatisticRecord;
import de.escicore.statistic.StatisticRecordBuilder;
import de.escicore.statistic.StatisticService;
import de.escidoc.core.common.exceptions.EscidocException;
import de.escidoc.core.common.exceptions.system.SystemException;
import de.escidoc.core.common.util.logger.AppLogger;
import de.escidoc.core.common.util.service.UserContext;
import de.escidoc.core.common.util.string.StringUtility;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;

import java.util.regex.Pattern;

/**
 * Interceptor used to create statistic data for the eSciDoc base services.
 * <p/>
 * 
 * This Interceptor is invoked every time an EJB calls one of its service
 * classes.<br/>
 * It must be the first interceptor in the chain.<br/>
 * This interceptor performs user authentication, too, as the user data is
 * needed for the statistics.<br/>
 * This interceptor stores in the thread local <code>StatisticDataVo</code>
 * object the following information before calling the handler method:
 * <ul>
 * <li><code>PARAM_HANDLER</code>, the name of the called handler.</li>
 * <li><code>PARAM_REQUEST</code>, the name of the called handler method.</li>
 * <li><code>PARAM_INTERFACE</code>, the interface that has been called, i.e.
 * REST or SOAP.</li>
 * <li><code>PARAM_INTERNAL</code>, the flag indicating if this is an internal
 * call from one infrastructure service (EJB) to another (
 * <code>VALUE_INTERNAL_TRUE</code>), or if it is an external call from a
 * non-infrastructure service or application (<code>VALUE_INTERNAL_FALSE</code>
 * ).</li>
 * <li><code>PARAM_USER_ID</code>, the id of the user performing the current
 * request.</li>
 * <li><code>PARAM_OBJECT_ID</code>, the id of the accessed resource, if this is
 * available in the first parameter. To check this, it is asserted that the
 * first parameter does not seem to contain XML data, i.e. does not contain a
 * &lt;</li>
 * </ul>
 * After the handler method call, the interceptor adds the following information
 * and sends the statistics data to the <code>StatisticQueueHandler</code>.
 * <ul>
 * <li><code>PARAM_SUCCESS</code>, flag indicating if the method call has
 * successfully returned (<code>VALUE_SUCCESS_TRUE</code>) or if an exception
 * has occurred (<code>VALUE_SUCCESS_FALSE</code>).</li>
 * <li><code>PARAM_ELAPSED_TIME</code>, the elapsed time from start of this
 * interceptor to the returning from the called method.</li>
 * <li><code>PARAM_EXCEPTION_NAME</code>, in case of an error, the name of the
 * exception.</li>
 * <li><code>PARAM_EXCEPTION_MESSAGE</code>, in case of an error, the message of
 * the exception.</li>
 * <li><code>PARAM_EXCEPTION_SOURCE</code>, in case of an error, the source of
 * the top level exception. This source information consist of the full class
 * name, the method name and the line from that the exception has been thrown.</li>
 * </ul>
 * <br/>
 * The called business methods may add further information to the statistic data
 * record. For information about how to access the thread local statistic data
 * record and adding information to it.
 * 
 * @author TTE
 * @common
 */
@Aspect
public class StatisticInterceptor implements Ordered {

    private static final AppLogger LOG = new AppLogger(StatisticInterceptor.class.getName());

    /**
     * Pattern used to determine that a method parameter is XML parameter and
     * not an id parameter.
     */
    private static final Pattern PATTERN_DETERMINE_XML_PARAMETER = Pattern.compile("<");

    private static final String MSG_CLASS_CAST_EXCEPTION =
        "This ClassCastException should occur for binary content, only.";

    private static final String PARAM_ELAPSED_TIME = "elapsed_time";

    private static final String PARAM_EXCEPTION_NAME = "exception_name";

    private static final String PARAM_EXCEPTION_SOURCE = "exception_source";

    private static final String PARAM_HANDLER = "handler";

    private static final String PARAM_INTERNAL = "internal";

    private static final String PARAM_INTERFACE = "interface";

    private static final String PARAM_OBJID = "object_id";

    private static final String PARAM_PARENT_OBJID = "parent_" + PARAM_OBJID;

    private static final String PARAM_REQUEST = "request";

    private static final String PARAM_SUCCESSFUL = "successful";

    private static final String PARAM_USER_ID = "user_id";

    private static final String VALUE_INTERFACE_SOAP = "SOAP";

    private static final String VALUE_INTERFACE_REST = "REST";

    private StatisticService statisticService;

    // CHECKSTYLE:JAVADOC-OFF

    /**
     * See Interface for functional description.
     * 
     * @return
     * @see org.springframework.core.Ordered#getOrder()
     * @common
     */
    public int getOrder() {
        return AopUtil.PRECEDENCE_STATISTIC_INTERCEPTOR;
    }

    // CHECKSTYLE:JAVADOC-ON

    /**
     * Around advice to create a statistic record for the current method call.<br>
     * 
     * @param joinPoint
     *            The current {@link ProceedingJoinPoint}.
     * @return Returns the changed result.
     * @throws Throwable
     *             Thrown in case of an error.
     * @common
     */
    @Around("call(public !static * de.escidoc.core.*.service.interfaces.*.*(..))"
        + " && within(de.escidoc.core.*.ejb.*Bean)"
        + " && !call(* de.escidoc.core..*.SemanticStoreHandler*.*(..))"
        + " && !call(* de.escidoc.core..*.StatisticService*.*(..))"
        + " && !call(* de.escidoc.core.common..*.*(..))")
    public Object createStatisticRecord(final ProceedingJoinPoint joinPoint) throws Throwable {
        if (LOG.isDebugEnabled()) {
            LOG.debug(StringUtility.concatenateWithBracketsToString("createStatisticRecord", this));
        }
        final long invocationStartTime = System.currentTimeMillis();
        boolean successful = true;
        String exceptionName = null;
        String exceptionSource = null;
        try {
            return proceed(joinPoint);
        } catch (final Exception e) {
            successful = false;
            exceptionName = e.getClass().getName();
            final StackTraceElement[] elements = e.getStackTrace();
            if (elements != null && elements.length > 0) {
                StackTraceElement element = elements[0];
                exceptionSource = StringUtility.concatenateWithBracketsToString(element.getClassName(),
                            element.getMethodName(), element.getLineNumber());
            } else {
                exceptionSource = "unknown";
            }
            if (e instanceof EscidocException) {
                throw e;
            } else {
                // this should not occur. To report this failure, the exception is wrapped by a SystemException
                throw new SystemException("Service throws unexpected exception. ", e);
            }
        } finally {
            // get callee and method info
            final MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            // get interface info
            String interfaceInfo = VALUE_INTERFACE_SOAP;
            if (UserContext.isRestAccess()) {
                interfaceInfo = VALUE_INTERFACE_REST;
            }
            // create a new statistic data record stored in thread local for this scope
            final StatisticRecordBuilder statisticRecordBuilder = StatisticRecordBuilder.createStatisticRecord();
            handleObjectIds(statisticRecordBuilder, methodSignature.getMethod().getName(), joinPoint.getArgs());
            final StatisticRecord statisticRecord = statisticRecordBuilder
                    .withParameter(PARAM_HANDLER, methodSignature.getDeclaringTypeName())
                    .withParameter(PARAM_REQUEST, methodSignature.getMethod().getName())
                    .withParameter(PARAM_INTERFACE, interfaceInfo)
                    .withParameter(PARAM_INTERNAL, !UserContext.isExternalUser())
                    .withParameter(PARAM_SUCCESSFUL , successful)
                    .withParameter(PARAM_EXCEPTION_NAME, exceptionName)
                    .withParameter(PARAM_EXCEPTION_SOURCE, exceptionSource)
                    .withParameter(PARAM_USER_ID, UserContext.getId())
                    .withParameter(PARAM_ELAPSED_TIME, "" + (System.currentTimeMillis() - invocationStartTime))
                    .build();
            this.statisticService.createStatisticRecord(statisticRecord);
        }
    }

    /**
     * Continue the invocation.
     * 
     * @param joinPoint
     *            The current {@link ProceedingJoinPoint}.
     * @return Returns the result of the continued invocation.
     * @throws Throwable
     *             Thrown in case of an error during proceeding the method call.
     */
    private Object proceed(final ProceedingJoinPoint joinPoint)
        throws Throwable {

        if (LOG.isDebugEnabled()) {
            LOG.debug(StringUtility.concatenateWithBracketsToString("proceed",
                this));
        }

        return joinPoint.proceed();
    }

/**
     * Inserts the method parameter that hold object ids into the provided
     * {@link StatisticRecord} object.<br>
     * The objids are taken from the method parameters, that are string
     * parameters, contain a :, but do not seem to be XML data, i.e. does not
     * contain '<'. <br>
     * The last found objid is logged as PARAM_OBJID, because this addresses the
     * accessed (sub- )resource, e.g. item or component of an item. The other
     * object ids (if any) are logged as PARAM_PARENT_OBJID + index.
     * 
     * @param statisticRecordBuilder
     *            The {@link StatisticRecordBuilder} object to put the object ids into.
     * @param calledMethodName
     *            The name of the called method.
     * @param arguments
     *            The arguments of the method call.
     * @common
     */
    private void handleObjectIds(
        final StatisticRecordBuilder statisticRecordBuilder, final String calledMethodName,
        final Object[] arguments) {

        if (arguments != null && arguments.length > 0) {
            int indexLastObjid = -1;
            for (int i = 0; i < arguments.length; i++) {
                try {
                    final String argument = (String) arguments[i];
                    if (argument != null && !PATTERN_DETERMINE_XML_PARAMETER .matcher(argument).find()) {
                        indexLastObjid = i;
                    }
                    else {
                        // First string parameter found that holds xml. Suspend
                        // loop;
                        break;
                    }
                }
                catch (ClassCastException e) {
                    // e.g., this is the case for binary content
                    // (createStagingFile), ignore exception
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(MSG_CLASS_CAST_EXCEPTION + calledMethodName + e.getMessage());
                    }
                    // Parameter found that is not a string. In this case, the
                    // loop is stopped and no objids are logged, as it seems to
                    // be a special method, e.g. evaluateRetrieve of the PDP.
                    indexLastObjid = -1;
                    break;
                }
            }
            if (indexLastObjid >= 0) {
                statisticRecordBuilder.withParameter(PARAM_OBJID, (String) arguments[indexLastObjid]);
                for (int i = indexLastObjid - 1, parent = 1; i >= 0; i--) {
                    statisticRecordBuilder.withParameter(PARAM_PARENT_OBJID + parent++, (String) arguments[i]);
                }
            }
        }
    }

    public void setStatisticService(final StatisticService statisticService) {
        this.statisticService = statisticService;
    }
}
