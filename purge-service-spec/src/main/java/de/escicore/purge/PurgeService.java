package de.escicore.purge;

import org.apache.camel.InOnly;

/**
 * Service to purge resources from repository.
 *
 * @author <a href="mailto:mail@eduard-hildebrandt.de">Eduard Hildebrandt</a>
 */
public interface PurgeService {

    /**
     * Purge a resource from repository.
     *
     * @param purgeRequest the request to purge data from repository
     */
    @InOnly
    void purge(PurgeRequest purgeRequest);

}
