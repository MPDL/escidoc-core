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
 * Copyright 2006-2012 Fachinformationszentrum Karlsruhe Gesellschaft
 * fuer wissenschaftlich-technische Information mbH and Max-Planck-
 * Gesellschaft zur Foerderung der Wissenschaft e.V.
 * All rights reserved.  Use is subject to license terms.
 */
/**
 * 
 */
package de.escidoc.core.aa;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import net.sf.oval.constraint.NotNull;
import org.escidoc.core.domain.aa.pdp.request.RequestsTO;
import org.escidoc.core.domain.aa.pdp.result.ResultsTO;

import de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException;
import de.escidoc.core.common.exceptions.application.invalid.XmlSchemaValidationException;
import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
import de.escidoc.core.common.exceptions.application.notfound.ResourceNotFoundException;
import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
import de.escidoc.core.common.exceptions.system.SystemException;

/**
 * @author Michael Hoppe
 * 
 */
@Path("/aa/pdp")
public interface PolicyDecisionPointRestService {

    /**
     * The method evaluates the provided authorization requests. <br/> Information about user(s), action(s) and
     * resource(s) are contained in the provided XML data string which represents the content of a
     * {@link com.sun.xacml.ctx.RequestCtx} objects.<br/>
     * <p/>
     * <b>Prerequisites:</b><br/>
     * <p/>
     * The provided XML data in the body is only accepted if the size is less than ESCIDOC_MAX_XML_SIZE.<br/>
     * <p/>
     * At least one authorization request must be specified.<br/>
     * <p/>
     * Each authorization request must contain the specification of the resource-id attribute. In case of a "create"
     * action this attribute should be the empty string.<br/>
     * <p/>
     * If the addressed object is an object under version control, e.g. container or item, the resource id may consist
     * of object id and the version number. In this case, the privileges are evaluated for the specified version.
     * Otherwise, the privileges are evaluated for the latest version, as this is the version that may be modified by
     * accesses to the base services.<br/>
     * <p/>
     * <b>Tasks:</b><br/> <ul> <li>The XML data is validated against the XML-Schema of authorization requests.</li>
     * <li>It is checked that within each request either the resource-id (urn:oasis:names:tc:xacml:1.0:resource:resource-id)
     * or the new object-type (info:escidoc/names:aa:1.0:resource:object-type-new) attribute has been provided.</li>
     * <li>It is checked that no values are provided for eScidoc PDP attributes that are (partly) resolved by the policy
     * decision point.</li> <li>The values of subject attributes and resource attributes that are needed for evaluating
     * the authorization requests are determined.</li> <li>The authorization requests are evaluated using the determined
     * attribute values and the provided attribute values that could not be determined, automatically.</li> <li>The XML
     * representation of the evaluation results corresponding to XML-schema is returned as output. This result list has
     * the same cardinality and the same order as the input data.</li> </ul>
     * <p/>
     * <b>Parameter for Request:</b> (example)<br/>
     * <p/>
     * <xi:include href="requests-example.xml" parse="text"></xi:include>
     * <p/>
     * <pre>
     * &lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;
     * </pre>
     * <p/>
     * <pre>
     * &lt;requests:requests
     * </pre>
     * <p/>
     * <pre>
     *   xmlns:requests=&quot;http://www.escidoc.de/schemas/pdp/0.2/requests&quot;
     * </pre>
     * <p/>
     * <pre>
     *   xmlns:xacml-context=&quot;urn:oasis:names:tc:xacml:1.0:context&quot;&gt;
     * </pre>
     * <p/>
     * <pre>
     *   &lt;xacml-context:Request&gt;
     * </pre>
     * <p/>
     * <pre>
     *     &lt;xacml-context:Subject
     * </pre>
     * <p/>
     * <pre>
     *       SubjectCategory=
     * </pre>
     * <p/>
     * <pre>
     *         &quot;urn:oasis:names:tc:xacml:1.0:subject-category:access-subject&quot;&gt;
     * </pre>
     * <p/>
     * <pre>
     *       &lt;xacml-context:Attribute
     * </pre>
     * <p/>
     * <pre>
     * AttributeId = &quot;urn:oasis:names:tc:xacml:1.0:subject:subject-id&quot;
     * </pre>
     * <p/>
     * <pre>
     *         DataType=&quot;http://www.w3.org/2001/XMLSchema#string&quot;&gt;
     * </pre>
     * <p/>
     * <pre>
     *         &lt;xacml-context:AttributeValue&gt;
     * </pre>
     * <p/>
     * <pre>
     *           escidoc:user1
     * </pre>
     * <p/>
     * <pre>
     *         &lt;/xacml-context:AttributeValue&gt;
     * </pre>
     * <p/>
     * <pre>
     *       &lt;/xacml-context:Attribute&gt;
     * </pre>
     * <p/>
     * <pre>
     *     &lt;/xacml-context:Subject&gt;
     * </pre>
     * <p/>
     * <pre>
     *     &lt;xacml-context:Resource&gt;
     * </pre>
     * <p/>
     * <pre>
     *       &lt;xacml-context:Attribute
     * </pre>
     * <p/>
     * <pre>
     * AttributeId = &quot;urn:oasis:names:tc:xacml:1.0:resource:resource-id&quot;
     * </pre>
     * <p/>
     * <pre>
     *         DataType=&quot;http://www.w3.org/2001/XMLSchema#string&quot;&gt;
     * </pre>
     * <p/>
     * <pre>
     *         &lt;xacml-context:AttributeValue&gt;
     * </pre>
     * <p/>
     * <pre>
     *           escidoc:1
     * </pre>
     * <p/>
     * <pre>
     *         &lt;/xacml-context:AttributeValue&gt;
     * </pre>
     * <p/>
     * <pre>
     *       &lt;/xacml-context:Attribute&gt;
     * </pre>
     * <p/>
     * <pre>
     *     &lt;/xacml-context:Resource&gt;
     * </pre>
     * <p/>
     * <pre>
     *     &lt;xacml-context:Action&gt;
     * </pre>
     * <p/>
     * <pre>
     *       &lt;xacml-context:Attribute
     * </pre>
     * <p/>
     * <pre>
     * AttributeId = &quot;urn:oasis:names:tc:xacml:1.0:action:action-id&quot;
     * </pre>
     * <p/>
     * <pre>
     *         DataType=&quot;http://www.w3.org/2001/XMLSchema#string&quot;&gt;
     * </pre>
     * <p/>
     * <pre>
     *         &lt;xacml-context:AttributeValue&gt;
     * </pre>
     * <p/>
     * <pre>
     *           info:escidoc/names:aa:1.0:action:retrieve-item
     * </pre>
     * <p/>
     * <pre>
     *         &lt;/xacml-context:AttributeValue&gt;
     * </pre>
     * <p/>
     * <pre>
     *       &lt;/xacml-context:Attribute&gt;
     * </pre>
     * <p/>
     * <pre>
     *     &lt;/xacml-context:Action&gt;
     * </pre>
     * <p/>
     * <pre>
     *   &lt;/xacml-context:Request&gt;
     * </pre>
     * <p/>
     * <pre>
     *   &lt;xacml-context:Request&gt;
     * </pre>
     * <p/>
     * <pre>
     *     &lt;xacml-context:Subject
     * </pre>
     * <p/>
     * <pre>
     *       SubjectCategory=
     * </pre>
     * <p/>
     * <pre>
     *         &quot;urn:oasis:names:tc:xacml:1.0:subject-category:access-subject&quot;&gt;
     * </pre>
     * <p/>
     * <pre>
     *       &lt;xacml-context:Attribute
     * </pre>
     * <p/>
     * <pre>
     * AttributeId = &quot;urn:oasis:names:tc:xacml:1.0:subject:subject-id&quot;
     * </pre>
     * <p/>
     * <pre>
     *         DataType=&quot;http://www.w3.org/2001/XMLSchema#string&quot;&gt;
     * </pre>
     * <p/>
     * <pre>
     *         &lt;xacml-context:AttributeValue&gt;
     * </pre>
     * <p/>
     * <pre>
     *           escidoc:user42
     * </pre>
     * <p/>
     * <pre>
     *         &lt;/xacml-context:AttributeValue&gt;
     * </pre>
     * <p/>
     * <pre>
     *       &lt;/xacml-context:Attribute&gt;
     * </pre>
     * <p/>
     * <pre>
     *     &lt;/xacml-context:Subject&gt;
     * </pre>
     * <p/>
     * <pre>
     *     &lt;xacml-context:Resource&gt;
     * </pre>
     * <p/>
     * <pre>
     *       &lt;xacml-context:Attribute
     * </pre>
     * <p/>
     * <pre>
     * AttributeId = &quot;info:escidoc/names:aa:1.0:resource:object-type&quot;
     * </pre>
     * <p/>
     * <pre>
     *         DataType=&quot;http://www.w3.org/2001/XMLSchema#string&quot;&gt;
     * </pre>
     * <p/>
     * <pre>
     *         &lt;xacml-context:AttributeValue&gt;
     * </pre>
     * <p/>
     * <pre>
     * item
     * </pre>
     * <p/>
     * <pre>
     *         &lt;/xacml-context:AttributeValue&gt;
     * </pre>
     * <p/>
     * <pre>
     *       &lt;/xacml-context:Attribute&gt;
     * </pre>
     * <p/>
     * <pre>
     *       &lt;xacml-context:Attribute
     * </pre>
     * <p/>
     * <pre>
     * AttributeId = &quot;urn:oasis:names:tc:xacml:1.0:resource:resource-id&quot;
     * </pre>
     * <p/>
     * <pre>
     *         DataType=&quot;http://www.w3.org/2001/XMLSchema#string&quot;&gt;
     * </pre>
     * <p/>
     * <pre>
     *         &lt;xacml-context:AttributeValue&gt;&lt;/xacml-context:AttributeValue&gt;
     * </pre>
     * <p/>
     * <pre>
     *       &lt;/xacml-context:Attribute&gt;
     * </pre>
     * <p/>
     * <pre>
     *       &lt;xacml-context:Attribute
     * </pre>
     * <p/>
     * <pre>
     * AttributeId = &quot;info:escidoc/names:aa:1.0:resource:item:context&quot;
     * </pre>
     * <p/>
     * <pre>
     *         DataType=&quot;http://www.w3.org/2001/XMLSchema#string&quot;&gt;
     * </pre>
     * <p/>
     * <pre>
     *         &lt;xacml-context:AttributeValue&gt;
     * </pre>
     * <p/>
     * <pre>
     *           escidoc:persistent3
     * </pre>
     * <p/>
     * <pre>
     *         &lt;/xacml-context:AttributeValue&gt;
     * </pre>
     * <p/>
     * <pre>
     *       &lt;/xacml-context:Attribute&gt;
     * </pre>
     * <p/>
     * <pre>
     *     &lt;/xacml-context:Resource&gt;
     * </pre>
     * <p/>
     * <pre>
     *     &lt;xacml-context:Action&gt;
     * </pre>
     * <p/>
     * <pre>
     *       &lt;xacml-context:Attribute
     * </pre>
     * <p/>
     * <pre>
     * AttributeId = &quot;urn:oasis:names:tc:xacml:1.0:action:action-id&quot;
     * </pre>
     * <p/>
     * <pre>
     *         DataType=&quot;http://www.w3.org/2001/XMLSchema#string&quot;&gt;
     * </pre>
     * <p/>
     * <pre>
     *         &lt;xacml-context:AttributeValue&gt;
     * </pre>
     * <p/>
     * <pre>
     *           info:escidoc/names:aa:1.0:action:create-item
     * </pre>
     * <p/>
     * <pre>
     *         &lt;/xacml-context:AttributeValue&gt;
     * </pre>
     * <p/>
     * <pre>
     *       &lt;/xacml-context:Attribute&gt;
     * </pre>
     * <p/>
     * <pre>
     *     &lt;/xacml-context:Action&gt;
     * </pre>
     * <p/>
     * <pre>
     *   &lt;/xacml-context:Request&gt;
     * </pre>
     * <p/>
     * <pre>
     * &lt;/requests:requests&gt;
     * </pre>
     * <p/>
     * <br/> <br/> <b>Result:</b> (example)<br/>
     * <p/>
     * <xi:include href="results-example.xml" parse="text"></xi:include>
     * <p/>
     * <pre>
     * &lt;results:results
     * </pre>
     * <p/>
     * <pre>
     *   xmlns:results=&quot;http://www.escidoc.de/schemas/pdp/0.2/results&quot;
     * </pre>
     * <p/>
     * <pre>
     *   xmlns:xacml-context=&quot;urn:oasis:names:tc:xacml:1.0:context&quot;&gt;
     * </pre>
     * <p/>
     * <pre>
     *   &lt;results:result decision=&quot;permit&quot;&gt;
     * </pre>
     * <p/>
     * <pre>
     *     &lt;xacml-context:Response&gt;
     * </pre>
     * <p/>
     * <pre>
     *       &lt;xacml-context:Result&gt;
     * </pre>
     * <p/>
     * <pre>
     *         &lt;xacml-context:Decision&gt;Permit&lt;/xacml-context:Decision&gt;
     * </pre>
     * <p/>
     * <pre>
     *         &lt;xacml-context:Status&gt;
     * </pre>
     * <p/>
     * <pre>
     *           &lt;xacml-context:StatusCode
     * </pre>
     * <p/>
     * <pre>
     *             Value=&quot;urn:oasis:names:tc:xacml:1.0:status:ok&quot;/&gt;
     * </pre>
     * <p/>
     * <pre>
     *         &lt;/xacml-context:Status&gt;
     * </pre>
     * <p/>
     * <pre>
     *       &lt;/xacml-context:Result&gt;
     * </pre>
     * <p/>
     * <pre>
     *     &lt;/xacml-context:Response&gt;
     * </pre>
     * <p/>
     * <pre>
     *   &lt;/results:result&gt;
     * </pre>
     * <p/>
     * <pre>
     *   &lt;results:result decision=&quot;deny&quot;&gt;
     * </pre>
     * <p/>
     * <pre>
     *     &lt;xacml-context:Response&gt;
     * </pre>
     * <p/>
     * <pre>
     *       &lt;xacml-context:Result&gt;
     * </pre>
     * <p/>
     * <pre>
     *         &lt;xacml-context:Decision&gt;NotApplicable&lt;/xacml-context:Decision&gt;
     * </pre>
     * <p/>
     * <pre>
     *         &lt;xacml-context:Status&gt;
     * </pre>
     * <p/>
     * <pre>
     *           &lt;xacml-context:StatusCode
     * </pre>
     * <p/>
     * <pre>
     *             Value=&quot;urn:oasis:names:tc:xacml:1.0:status:ok&quot;/&gt;
     * </pre>
     * <p/>
     * <pre>
     *         &lt;/xacml-context:Status&gt;
     * </pre>
     * <p/>
     * <pre>
     *       &lt;/xacml-context:Result&gt;
     * </pre>
     * <p/>
     * <pre>
     *     &lt;/xacml-context:Response&gt;
     * </pre>
     * <p/>
     * <pre>
     *   &lt;/results:result&gt;
     * </pre>
     * <p/>
     * <pre>
     * &lt;/results:results&gt;
     * </pre>
     *
     * @param pdpRequestsTO The XML representation of the authorization requests corresponding to "requests.xsd" as TO.
     * @return Returns the XML representation of the evaluation results corresponding to XML-schema "results.xsd" as TO.
     * @throws ResourceNotFoundException    Thrown if a resource that needs to be handled during authorization can not
     *                                      be found.
     * @throws XmlCorruptedException        Thrown if the provided xml data is invalid.
     * @throws XmlSchemaValidationException Thrown if the provided xml data is not schma conform.
     * @throws MissingMethodParameterException
     *                                      Thrown if no role data has been provided.
     * @throws AuthenticationException      Thrown if the authentication fails due to an invalid provided
     *                                      eSciDocUserHandle.
     * @throws AuthorizationException       Thrown if the authorization fails.
     * @throws SystemException              Thrown in case of an internal error
     */
    @PUT
    @Produces(MediaType.TEXT_XML)
    @Consumes(MediaType.TEXT_XML)
    ResultsTO evaluate(@NotNull RequestsTO pdpRequestsTO) throws ResourceNotFoundException, XmlCorruptedException,
            XmlSchemaValidationException, MissingMethodParameterException, AuthenticationException,
            AuthorizationException, SystemException;

}
