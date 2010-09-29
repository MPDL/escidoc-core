package de.escicore.statistic.internal;

import de.escicore.statistic.StatisticServiceException;
import de.escidoc.core.sm.business.interfaces.StatisticDataHandlerInterface;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Default implementation of {@link de.escicore.statistic.StatisticService}.
 *
 * @author <a href="mailto:mail@eduard-hildebrandt.de">Eduard Hildebrandt</a>
 */
public class StatisticServiceImpl {

    private static final Log LOG = LogFactory.getLog(StatisticServiceImpl.class);

    private StatisticDataHandlerInterface statisticDataHandler;

    /**
     * Saves the static data string in the repository.
     *
     * @param statisticData the statistic data XML string
     * @throws StatisticServiceException if any error accours.
     */
    public void createStatisticRecord(final String statisticData) throws StatisticServiceException {
        try {
            // TODO: Refactor StatisticDataHandler and move to this module.
            this.statisticDataHandler.insertStatisticData(statisticData);
        } catch (final Exception e) {
            final String errorMessage = "Error on saving statistic data.";
            LOG.error(errorMessage, e);
            throw new StatisticServiceException(e);
        }
    }

    /**
     * Sets the StatisticDataHandler.
     *
     * @param statisticDataHandler the StatisticDataHandlerInterface.
     */
    public void setStatisticDataHandler(final StatisticDataHandlerInterface statisticDataHandler) {
        this.statisticDataHandler = statisticDataHandler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "StatisticServiceImpl{" +
                "statisticDataHandler=" + statisticDataHandler +
                '}';
    }
}
