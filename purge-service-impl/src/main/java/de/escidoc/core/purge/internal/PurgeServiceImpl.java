package de.escidoc.core.purge.internal;

import de.escidoc.core.adm.business.admin.PurgeStatus;
import de.escidoc.core.common.business.fedora.FedoraUtility;
import de.escidoc.core.common.business.fedora.TripleStoreUtility;
import de.escidoc.core.common.exceptions.system.WebserverSystemException;
import de.escidoc.core.common.util.service.UserContext;
import de.escidoc.core.purge.PurgeRequest;
import de.escidoc.core.purge.PurgeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Default implementation of {@link PurgeService}.
 */
@Service("de.escidoc.core.purge.internal.PurgeServiceImpl")
public class PurgeServiceImpl implements PurgeService {

    private static final Log LOGGER = LogFactory.getLog(PurgeServiceImpl.class);

    @Autowired
    private FedoraUtility fedoraUtility;

    @Autowired
    private TripleStoreUtility tripleStoreUtility;

    public void purge(final PurgeRequest purgeRequest) {
        // TODO: Refector this old code.
        try {
            try {
                final boolean isInternalUser = UserContext.isInternalUser();

                if (!isInternalUser) {
                    UserContext.setUserContext("");
                    UserContext.runAsInternalUser();
                }
            }
            catch (final Exception e) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Error on setting user context.");
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Error on setting user context.", e);
                }
                UserContext.setUserContext("");
                UserContext.runAsInternalUser();
            }
            for (final String componentId : this.tripleStoreUtility.getComponents(purgeRequest.getResourceId())) {
                this.fedoraUtility.deleteObject(componentId, false);
            }
            this.fedoraUtility.deleteObject(purgeRequest.getResourceId(), false);
            // synchronize triple store
            this.fedoraUtility.sync();

        }
        catch (final Exception e) {
            LOGGER.error("could not dequeue message", e);
        }
        finally {
            PurgeStatus.getInstance().dec();
        }
    }
}
