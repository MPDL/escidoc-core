package org.escidoc.core.services.fedora.internal;

import static org.esidoc.core.utils.Preconditions.checkState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import com.googlecode.ehcache.annotations.Cacheable;
import com.googlecode.ehcache.annotations.KeyGenerator;
import com.googlecode.ehcache.annotations.TriggersRemove;
import net.sf.oval.guard.Guarded;

import org.apache.cxf.jaxrs.client.Client;
import org.apache.cxf.jaxrs.client.WebClient;
import org.escidoc.core.services.fedora.AddDatastreamPathParam;
import org.escidoc.core.services.fedora.AddDatastreamQueryParam;
import org.escidoc.core.services.fedora.DatastreamState;
import org.escidoc.core.services.fedora.DeleteDatastreamPathParam;
import org.escidoc.core.services.fedora.DeleteDatastreamQueryParam;
import org.escidoc.core.services.fedora.DeleteObjectPathParam;
import org.escidoc.core.services.fedora.DeleteObjectQueryParam;
import org.escidoc.core.services.fedora.DigitalObjectTO;
import org.escidoc.core.services.fedora.FedoraServiceClient;
import org.escidoc.core.services.fedora.FedoraServiceRESTEndpoint;
import org.escidoc.core.services.fedora.GetBinaryContentPathParam;
import org.escidoc.core.services.fedora.GetBinaryContentQueryParam;
import org.escidoc.core.services.fedora.GetDatastreamHistoryPathParam;
import org.escidoc.core.services.fedora.GetDatastreamHistoryQueryParam;
import org.escidoc.core.services.fedora.GetDatastreamPathParam;
import org.escidoc.core.services.fedora.GetDatastreamProfilePathParam;
import org.escidoc.core.services.fedora.GetDatastreamProfileQueryParam;
import org.escidoc.core.services.fedora.GetDatastreamQueryParam;
import org.escidoc.core.services.fedora.GetDisseminationPathParam;
import org.escidoc.core.services.fedora.GetDisseminationQueryParam;
import org.escidoc.core.services.fedora.GetObjectProfilePathParam;
import org.escidoc.core.services.fedora.GetObjectProfileQueryParam;
import org.escidoc.core.services.fedora.GetObjectXMLPathParam;
import org.escidoc.core.services.fedora.GetObjectXMLQueryParam;
import org.escidoc.core.services.fedora.IngestPathParam;
import org.escidoc.core.services.fedora.IngestQueryParam;
import org.escidoc.core.services.fedora.ListDatastreamsPathParam;
import org.escidoc.core.services.fedora.ListDatastreamsQueryParam;
import org.escidoc.core.services.fedora.ModifiyDatastreamPathParam;
import org.escidoc.core.services.fedora.ModifyDatastreamQueryParam;
import org.escidoc.core.services.fedora.NextPIDPathParam;
import org.escidoc.core.services.fedora.NextPIDQueryParam;
import org.escidoc.core.services.fedora.PidListTO;
import org.escidoc.core.services.fedora.RisearchPathParam;
import org.escidoc.core.services.fedora.RisearchQueryParam;
import org.escidoc.core.services.fedora.UpdateObjectPathParam;
import org.escidoc.core.services.fedora.UpdateObjectQueryParam;
import org.escidoc.core.services.fedora.access.DatastreamTypeTO;
import org.escidoc.core.services.fedora.access.ObjectDatastreamsTO;
import org.escidoc.core.services.fedora.access.ObjectProfileTO;
import org.escidoc.core.services.fedora.management.DatastreamHistoryTO;
import org.escidoc.core.services.fedora.management.DatastreamProfileTO;
import org.esidoc.core.utils.VoidObject;
import org.esidoc.core.utils.io.IOUtils;
import org.esidoc.core.utils.io.Stream;
import org.esidoc.core.utils.io.MimeTypes;
import org.esidoc.core.utils.io.Stream;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Future;

/**
 * Default implementation for {@link FedoraServiceClient}.
 *
 * @author <a href="mailto:mail@eduard-hildebrandt.de">Eduard Hildebrandt</a>
 */
@Service("fedoraServiceClient")
@Guarded(applyFieldConstraintsToConstructors = true, applyFieldConstraintsToSetters = true,
        assertParametersNotNull = false, checkInvariants = true, inspectInterfaces = true)
public final class FedoraServiceClientImpl implements FedoraServiceClient {

    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public final static Logger LOG = LoggerFactory.getLogger(FedoraServiceClientImpl.class);

    private FedoraServiceRESTEndpoint fedoraService;

    public void setFedoraService(final FedoraServiceRESTEndpoint fedoraService) {
        this.fedoraService = fedoraService;
    }

    @Override
    public PidListTO getNextPID(final String namespace) {
        return this.getNextPID(namespace, NextPIDQueryParam.DEFAULT_NUMBER_OF_PIDS);
    }

    @Override
    @Async
    public Future<PidListTO> getNextPIDAsync(final String namespace) {
        return new AsyncResult<PidListTO>(this.getNextPID(namespace));
    }

    @Override
    public PidListTO getNextPID(final String namespace, final int numPIDs) {
        checkState(numPIDs > 0, "Number of PIDs must be > 0.");
        final NextPIDPathParam path = new NextPIDPathParam();
        final NextPIDQueryParam query = new NextPIDQueryParam();
        query.setNamespace(namespace);
        query.setNumPIDs(numPIDs);
        return this.fedoraService.getNextPID(path, query);
    }

    @Override
    @Async
    public Future<PidListTO> getNextPIDAsync(final String namespace, final int numPIDs) {
        return new AsyncResult<PidListTO>(this.getNextPID(namespace, numPIDs));
    }

    @Override
    @Cacheable(cacheName = "Fedora.ObjectProfiles", keyGenerator = @KeyGenerator(
            name = "org.escidoc.core.services.fedora.internal.cache.GetObjectProfileKeyGenerator"))
    public ObjectProfileTO getObjectProfile(final String pid) {
        final GetObjectProfilePathParam path = new GetObjectProfilePathParam(pid);
        final GetObjectProfileQueryParam query = new GetObjectProfileQueryParam();
        return this.fedoraService.getObjectProfile(path, query);
    }

    @Override
    @Async
    public Future<ObjectProfileTO> getObjectProfileAsync(final String pid) {
        return new AsyncResult<ObjectProfileTO>(getObjectProfile(pid));
    }

    @Override
    @Cacheable(cacheName = "Fedora.DigitalObjects", keyGenerator = @KeyGenerator(
            name = "org.escidoc.core.services.fedora.internal.cache.GetObjectXMLKeyGenerator"))
    public DigitalObjectTO getObjectXML(final String pid) {
        final GetObjectXMLPathParam path = new GetObjectXMLPathParam(pid);
        final GetObjectXMLQueryParam query = new GetObjectXMLQueryParam();
        return this.fedoraService.getObjectXML(path, query);
    }

    @Override
    @Async
    public Future<DigitalObjectTO> getObjectXMLAsync(final String pid) {
        return new AsyncResult<DigitalObjectTO>(getObjectXML(pid));
    }

    @Override
    @Cacheable(cacheName = "Fedora.DigitalObjectStreams", keyGenerator = @KeyGenerator(
            name = "org.escidoc.core.services.fedora.internal.cache.GetObjectXMLKeyGenerator"))
    public Stream getObjectXMLAsStream(final String pid) {
        final GetObjectXMLPathParam path = new GetObjectXMLPathParam(pid);
        final GetObjectXMLQueryParam query = new GetObjectXMLQueryParam();
        return this.fedoraService.getObjectXMLAsStream(path, query);
    }

    @Override
    @Async
    public Future<Stream> getObjectXMLAsStreamAsync(final String pid) {
        return new AsyncResult<Stream>(getObjectXMLAsStream(pid));
    }

    @Override
    @Cacheable(cacheName = "Fedora.DatastreamLists", keyGenerator = @KeyGenerator(
            name = "org.escidoc.core.services.fedora.internal.cache.ListDatastreamsKeyGenerator"))
    public ObjectDatastreamsTO listDatastreams(final String pid, final DateTime timestamp) {
        final ListDatastreamsPathParam path = new ListDatastreamsPathParam(pid);
        final ListDatastreamsQueryParam query = new ListDatastreamsQueryParam();
        if(timestamp != null) {
            query.setAsOfDateTime(timestamp.toString(TIMESTAMP_FORMAT));
        }
        return this.fedoraService.listDatastreams(path, query);
    }

    @Override
    @Async
    public Future<ObjectDatastreamsTO> listDatastreamsAsync(final String pid, final DateTime timestamp) {
        return new AsyncResult<ObjectDatastreamsTO>(listDatastreams(pid, timestamp));
    }

    @Override
    /*@Cacheable(cacheName = "Fedora.Datastreams", keyGenerator = @KeyGenerator(
            name = "org.escidoc.core.services.fedora.internal.cache.GetDatastreamKeyGenerator"))*/
    public Stream getDatastream(final String pid, final String dsID, final DateTime timestamp) {
        final GetDatastreamPathParam path = new GetDatastreamPathParam(pid, dsID);
        final GetDatastreamQueryParam query = new GetDatastreamQueryParam();
        if(timestamp != null) {
            query.setAsOfDateTime(timestamp.withZone(DateTimeZone.UTC).toString(TIMESTAMP_FORMAT));
        }
        return this.fedoraService.getDatastream(path, query);
    }

    @Override
    @Async
    public Future<Stream> getDatastreamAsync(final String pid, final String dsID, final DateTime timestamp) {
        return new AsyncResult<Stream>(getDatastream(pid, dsID, timestamp));
    }

    @Override
    @Cacheable(cacheName = "Fedora.DatastreamProfiles", keyGenerator = @KeyGenerator(
            name = "org.escidoc.core.services.fedora.internal.cache.GetDatastreamProfileKeyGenerator"))
    public DatastreamProfileTO getDatastreamProfile(final GetDatastreamProfilePathParam path,
                                                    final GetDatastreamProfileQueryParam query) {
        return this.fedoraService.getDatastreamProfile(path, query);
    }

    @Override
    @Async
    public Future<DatastreamProfileTO> getDatastreamProfileAsync(final GetDatastreamProfilePathParam path,
                                                                 final GetDatastreamProfileQueryParam query) {
        return new AsyncResult<DatastreamProfileTO>(getDatastreamProfile(path, query));
    }

    @Override
    @TriggersRemove(
            cacheName = {"Fedora.ObjectProfiles", "Fedora.DigitalObjects", "Fedora.DigitalObjectStreams"},
            keyGenerator = @KeyGenerator(
                    name = "org.escidoc.core.services.fedora.internal.cache.AddDatastreamKeyGenerator"))
    public DatastreamProfileTO addDatastream(final AddDatastreamPathParam path, final AddDatastreamQueryParam query,
                                             final Stream stream) {
        if(stream != null) {
            return this.fedoraService.addDatastream(path, query, stream);
        } else {
            return this.fedoraService.addDatastream(path, query, new Stream());
        }
    }

    @Override
    @Async
    public Future<DatastreamProfileTO> addDatastreamAsync(final AddDatastreamPathParam path,
                                                          final AddDatastreamQueryParam query, final Stream stream) {
        return new AsyncResult<DatastreamProfileTO>(addDatastream(path, query, stream));
    }

    @Override
    @TriggersRemove(cacheName = {"Fedora.ObjectProfiles", "Fedora.DigitalObjects", "Fedora.DigitalObjectStreams"},
            keyGenerator = @KeyGenerator(
                    name = "org.escidoc.core.services.fedora.internal.cache.ModifyDatastreamKeyGenerator"))
    public DatastreamProfileTO modifyDatastream(final ModifiyDatastreamPathParam path,
                                                final ModifyDatastreamQueryParam query, final Stream stream) {
        if(stream != null) {
            return this.fedoraService.modifyDatastream(path, query, stream);
        } else {
            return this.fedoraService.modifyDatastream(path, query, new Stream());
        }
    }

    @Override
    @Async
    public Future<DatastreamProfileTO> modifyDatastreamAsync(final ModifiyDatastreamPathParam path,
                                                             final ModifyDatastreamQueryParam query,
                                                             final Stream stream) {
        return new AsyncResult<DatastreamProfileTO>(modifyDatastream(path, query, stream));
    }

    @Override
    @TriggersRemove(cacheName = {"Fedora.ObjectProfiles", "Fedora.DigitalObjects", "Fedora.DigitalObjectStreams"},
            keyGenerator = @KeyGenerator(
                    name = "org.escidoc.core.services.fedora.internal.cache.DeleteDatastreamKeyGenerator"))
    public void deleteDatastream(final DeleteDatastreamPathParam path, final DeleteDatastreamQueryParam query) {
        final Client client = WebClient.client(this.fedoraService);
        final WebClient webClient = WebClient.fromClient(client);
        webClient.accept(MimeTypes.APPLICATION_JSON)
                .path("/objects/" + path.getPid() + "/datastreams/" + path.getDsID()).delete();
    }

    @Override
    @Async
    public Future<VoidObject> deleteDatastreamAsync(final DeleteDatastreamPathParam path,
                                                    final DeleteDatastreamQueryParam query) {
        deleteDatastream(path, query);
        return new AsyncResult<VoidObject>(VoidObject.getInstance());
    }

    @Override
    @TriggersRemove(cacheName = {"Fedora.ObjectProfiles", "Fedora.DigitalObjects", "Fedora.DigitalObjectStreams"},
            keyGenerator = @KeyGenerator(
                    name = "org.escidoc.core.services.fedora.internal.cache.SetDatastreamStateKeyGenerator"))
    public DatastreamProfileTO setDatastreamState(final String pid, final String dsID, final DatastreamState state) {
        final ModifiyDatastreamPathParam path = new ModifiyDatastreamPathParam(pid, dsID);
        final ModifyDatastreamQueryParam query = new ModifyDatastreamQueryParam();
        query.setDsState(state);
        return this.fedoraService.modifyDatastream(path, query, new Stream());
    }

    @Override
    @Async
    public Future<DatastreamProfileTO> setDatastreamStateAsync(final String pid, final String dsID,
                                                               final DatastreamState state) {
        return new AsyncResult<DatastreamProfileTO>(setDatastreamState(pid, dsID, state));
    }

    @Override
    @TriggersRemove(cacheName = {"Fedora.ObjectProfiles", "Fedora.DigitalObjects", "Fedora.DigitalObjectStreams"},
            keyGenerator = @KeyGenerator(
                    name = "org.escidoc.core.services.fedora.internal.cache.UpdateObjectKeyGenerator"))
    public void updateObject(final UpdateObjectPathParam path, final UpdateObjectQueryParam query) {
        this.fedoraService.updateObject(path, query);
    }

    @Override
    @Async
    public Future<VoidObject> updateObjectAsync(final UpdateObjectPathParam path, final UpdateObjectQueryParam query) {
        updateObject(path, query);
        return new AsyncResult<VoidObject>(VoidObject.getInstance());
    }

    @Override
    @TriggersRemove(cacheName = {"Fedora.ObjectProfiles", "Fedora.DigitalObjects", "Fedora.DigitalObjectStreams"},
            keyGenerator = @KeyGenerator(
                    name = "org.escidoc.core.services.fedora.internal.cache.DeleteObjectKeyGenerator"))
    public void deleteObject(final String pid) {
        final DeleteObjectPathParam path = new DeleteObjectPathParam(pid);
        final DeleteObjectQueryParam query = new DeleteObjectQueryParam();
        this.fedoraService.deleteObject(path, query);
    }

    @Override
    @Async
    public Future<VoidObject> deleteObjectAsync(final String pid) {
        deleteObject(pid);
        return new AsyncResult<VoidObject>(VoidObject.getInstance());
    }

    @Override
    @TriggersRemove(cacheName = {"Fedora.ObjectProfiles", "Fedora.DigitalObjects", "Fedora.DigitalObjectStreams"},
            keyGenerator = @KeyGenerator(
                    name = "org.escidoc.core.services.fedora.internal.cache.IngestKeyGenerator"))
    public String ingest(final IngestPathParam path, final IngestQueryParam query,
                         final DigitalObjectTO digitalObjectTO) {
        return this.fedoraService.ingest(path, query, digitalObjectTO);
    }

    @Override
    @Async
    public Future<String> ingestAsync(final IngestPathParam path, final IngestQueryParam query,
                                      final DigitalObjectTO digitalObjectTO) {
        return new AsyncResult<String>(ingest(path, query, digitalObjectTO));
    }


    /**
     * FIXME Fix {@link FedoraServiceClientImpl#ingest(IngestPathParam, IngestQueryParam, DigitalObjectTO)} and remove
     * this method.
     */
    @Override
    @TriggersRemove(cacheName = {"Fedora.ObjectProfiles", "Fedora.DigitalObjects", "Fedora.DigitalObjectStreams"},
            keyGenerator = @KeyGenerator(
                    name = "org.escidoc.core.services.fedora.internal.cache.IngestKeyGenerator"))
    public String ingest(final IngestPathParam path, final IngestQueryParam query, final String foxml) {
        final Stream stream = new Stream();
        try {
            stream.write(foxml.getBytes("UTF-8"));
            final Stream result = this.fedoraService.ingest(path, query, stream);
            return new String(result.getBytes(), "UTF-8");
        } catch(final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    /*@Cacheable(cacheName = "Fedora.DatastreamHistories", keyGenerator = @KeyGenerator(
            name = "org.escidoc.core.services.fedora.internal.cache.GetDatastreamHistoryKeyGenerator"))*/
    public DatastreamHistoryTO getDatastreamHistory(final GetDatastreamHistoryPathParam path,
                                                    final GetDatastreamHistoryQueryParam query) {
        return this.fedoraService.getDatastreamHistory(path, query);
    }

    @Override
    @Async
    public Future<DatastreamHistoryTO> getDatastreamHistoryAsync(final GetDatastreamHistoryPathParam path,
                                                                 final GetDatastreamHistoryQueryParam query) {
        return new AsyncResult<DatastreamHistoryTO>(getDatastreamHistory(path, query));
    }

    @Override
    public void sync() {
        final RisearchPathParam path = new RisearchPathParam();
        final RisearchQueryParam query = new RisearchQueryParam();
        query.setFlush(Boolean.TRUE.toString());
        this.fedoraService.risearch(path, query);
    }

    @Override
    public Collection<String> queryResourceIdsByType(final String resourceType) {
        final RisearchPathParam path = new RisearchPathParam();
        final RisearchQueryParam query = new RisearchQueryParam();
        query.setType("triples");
        query.setLang("spo");
        query.setFormat("N-Triples");
        query.setQuery("* <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" + resourceType + '>');
        return parseIdsFromStream(this.fedoraService.risearch(path, query));
    }

    private Collection<String> parseIdsFromStream(final Stream stream) {
        final Collection<String> result = new ArrayList<String>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(stream.getInputStream()));
            String line;
            while((line = reader.readLine()) != null) {
                final String subject = extractSubject(line);
                if(subject != null) {
                    final String id = subject.substring(subject.indexOf('/') + 1);
                    result.add(id);
                }
            }
        } catch(IOException e) {
            throw new RuntimeException("Error on parsing IDs.", e);
        } finally {
            IOUtils.closeStream(reader);
        }
        return result;
    }

    /**
     * Extract the subject from the given triple.
     *
     * @param triple the triple from which the subject has to be extracted
     * @return the subject of the given triple
     */
    private static String extractSubject(final String triple) {
        String result = null;
        if(triple != null) {
            final int index = triple.indexOf(' ');
            if(index > 0) {
                result = triple.substring(triple.indexOf('/') + 1, index - 1);
            }
        }
        return result;
    }

    @Override
    @Async
    public Future<Collection<String>> queryResourceIdsByTypeAsync(final String resourceType) {
        return new AsyncResult<Collection<String>>(queryResourceIdsByType(resourceType));
    }

    @Override
    /*@Cacheable(cacheName = "Fedora.DatastreamBinaryContent", keyGenerator = @KeyGenerator(
            name = "org.escidoc.core.services.fedora.internal.cache.GetBinaryContentKeyGenerator"))*/
    public Stream getBinaryContent(final String pid, final String dsId, final DateTime versionDate) {
        final GetBinaryContentPathParam path =
                new GetBinaryContentPathParam(pid, dsId, versionDate.withZone(DateTimeZone.UTC).toString());
        final GetBinaryContentQueryParam query = new GetBinaryContentQueryParam();
        return this.fedoraService.getBinaryContent(path, query);
    }

    @Override
    @Async
    public Future<Stream> getBinaryContentAsync(final String pid, final String dsId, final DateTime versionDate) {
        return new AsyncResult<Stream>(getBinaryContent(pid, dsId, versionDate));
    }

    @Override
    public Stream getDissemination(final String pid, final String contentModelPid, final String methodName) {
        final String cmp = "sdef:" + contentModelPid.replace(':', '_') + '-' + methodName;
        final GetDisseminationPathParam path = new GetDisseminationPathParam(pid, cmp, methodName);
        final GetDisseminationQueryParam query = new GetDisseminationQueryParam();
        return this.fedoraService.getDissemination(path, query);
    }

    @Override
    @Async
    public Future<Stream> getDisseminationAsync(final String pid, final String contentModelPid,
                                                final String methodName) {
        return new AsyncResult<Stream>(getDissemination(pid, contentModelPid, methodName));
    }

    @Override
    public Stream risearch(final RisearchPathParam path, final RisearchQueryParam query) {
        return this.fedoraService.risearch(path, query);
    }

    @Override
    public Future<Stream> risearchAsync(final RisearchPathParam path, final RisearchQueryParam query) {
        return new AsyncResult<Stream>(risearch(path, query));
    }

    @Override
    public List<DatastreamProfileTO> getDatastreamProfiles(final String pid, final DateTime timestamp) {
        return getDatastreamProfilesByAltId(pid, null, timestamp);
    }

    @Override
    @Async
    public Future<List<DatastreamProfileTO>> getDatastreamProfilesAsync(final String pid, final DateTime timestamp) {
        return new AsyncResult<List<DatastreamProfileTO>>(getDatastreamProfiles(pid, timestamp));
    }

    @Override
    public List<DatastreamProfileTO> getDatastreamProfilesByAltId(final String pid, final String altId,
                                                                  final DateTime timestamp) {
        final List<DatastreamProfileTO> result = new ArrayList<DatastreamProfileTO>();
        final ObjectDatastreamsTO datastreams = listDatastreams(pid, timestamp);
        for(final DatastreamTypeTO datastream : datastreams.getDatastream()) {
            final GetDatastreamProfilePathParam path = new GetDatastreamProfilePathParam(pid, datastream.getDsid());
            final GetDatastreamProfileQueryParam query = new GetDatastreamProfileQueryParam();
            final DatastreamProfileTO profile = getDatastreamProfile(path, query);
            if(altId == null) {
                result.add(profile);
            } else {
                if(! profile.getDsAltID().isEmpty()) {
                    for(final String dsAltID : profile.getDsAltID()) {
                        if(altId.equals(dsAltID)) {
                            result.add(profile);
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    @Async
    public Future<List<DatastreamProfileTO>> getDatastreamProfilesByAltIdAsync(final String pid, final String altId,
                                                                               final DateTime timestamp) {
        return new AsyncResult<List<DatastreamProfileTO>>(getDatastreamProfilesByAltId(pid, altId, timestamp));
    }
}