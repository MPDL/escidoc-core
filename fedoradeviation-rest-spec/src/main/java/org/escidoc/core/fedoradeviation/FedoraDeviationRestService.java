/**
 *
 */
package org.escidoc.core.fedoradeviation;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.escidoc.core.common.exceptions.system.SystemException;
import net.sf.oval.constraint.NotNull;

/**
 * @author Michael Hoppe
 * @author Marko Voss (marko.voss@fiz-karlsruhe.de)
 */
@Path("/fedoradeviation")
public interface FedoraDeviationRestService {

    /**
     * Overwrites the Fedora Method-Call export. Variable pid contains uri to resource. Calls Method-mapper with given
     * uri to retrieve object as xml. return xml-string as byte[].
     * <p/>
     * FIXME: Use types instead of String!
     *
     * @param id uri to the resource.
     * @return String with the fedora-object as escidoc-xml
     * @throws SystemException ex
     */
    @GET
    @Path("/objects/{id}/export")
    @Produces(MediaType.TEXT_XML)
    String export(@NotNull @PathParam("id") String id)
        throws SystemException;


    /**
     * Overwrites the Fedora Method-Call getDatastreamDissemination. Variable dsID contains uri to component-content .
     * Calls Method-mapper with given uri to retrieve content as byte[]. Fill EscidocBinaryContent with byte[] and
     * mime-type.
     *
     * @param id   unused.
     * @param dsId uri to component-content
     * @return EscidocBinaryContent escidocBinaryContent
     * @throws SystemException ex
     */
    @GET
    @Path("/objects/{id}/datastreams/{ds-id}/content")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    Response getDatastreamDissemination(@NotNull @PathParam("id") String id, @NotNull @PathParam("ds-id") String dsId)
        throws SystemException;

    /**
     * Overwrites the Fedora http-Call /describe. Executes http-request to /describe and returns String.
     * <p/>
     * FIXME: Use types instead of String and XML! FIXME: Use eSciDoc-exception(s) instead of Exception!
     *
     * @param xml request parameters.
     * @return String response
     * @throws Exception ex
     */
    @GET
    @Path("/describe")
    @Produces(MediaType.TEXT_XML)
    String getFedoraDescription(@QueryParam("xml") String xml)
        throws Exception;
}