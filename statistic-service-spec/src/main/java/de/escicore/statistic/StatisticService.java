package de.escicore.statistic;

import org.apache.camel.InOnly;

/**
 * Service for managing statistic data.
 *
 * @author <a href="mailto:mail@eduard-hildebrandt.de">Eduard Hildebrandt</a>
 */
public interface StatisticService {

    /**
     * Creates a new record in statistic repository.
     *
     * @param statisticRecord a statistic data record
     */
    @InOnly
    void createStatisticRecord(StatisticRecord statisticRecord);

}
