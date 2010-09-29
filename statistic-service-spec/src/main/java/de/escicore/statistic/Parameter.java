package de.escicore.statistic;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;

/**
 * Represents a parameter in a {@link StatisticRecord}.
 *
 * @author <a href="mailto:mail@eduard-hildebrandt.de">Eduard Hildebrandt</a>
 */
public interface Parameter {

    /**
     * Returns the name of the parameter.
     *
     * @return the name of the parameter
     */
    String getName();

    /**
     * Returns the value of the paramter as {@link String}.
     *
     * @return the value of the paramter as {@link String}.
     */
    String getStringValue();

    /**
     * Returns the value of the paramter as {@link String}.
     *
     * @return the value of the paramter as {@link String}.
     */
    BigDecimal getDecimalValue();

    /**
     * Returns the value of the paramter as {@link XMLGregorianCalendar}.
     *
     * @return the value of the paramter as {@link XMLGregorianCalendar}.
     */
    XMLGregorianCalendar getDateValue();

}
