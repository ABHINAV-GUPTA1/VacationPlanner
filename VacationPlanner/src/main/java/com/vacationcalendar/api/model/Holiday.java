package com.vacationcalendar.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * <p>
 * Represents a holiday, including its date, local name, and the country code.
 * </p>
 *
 * <p>
 * This class is used to structure holiday data retrieved from an external API,
 * such as the Nager.Date API.  It encapsulates the essential information
 * about a single holiday.  The {@link Data} annotation from the Lombok library
 * automatically generates boilerplate code such as getters, setters, equals,
 * hashCode, and toString methods.
 * </p>
 *
 * @author Abhinav Gupta
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Holiday {
    /**
     * The date of the holiday.
     * <p>
     * This is a {@link LocalDate} object, representing a date without time-of-day.
     * </p>
     */
    private LocalDate date;

    /**
     * The local name of the holiday.
     * <p>
     * This is a {@link String} representing the name of the holiday in the local language.
     * </p>
     */
    private String localName;

    /**
     * The ISO 3166-1 alpha-2 country code for the holiday.
     * <p>
     * This is a {@link String} representing the two-letter country code (e.g., "US", "DE", "FR").
     * </p>
     */
    private String countryCode;
}
