package com.vacationcalendar.api.client;

import com.vacationcalendar.api.model.Holiday;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * This interface, {@code HolidayClient}, is a Feign client for interacting with the Nager.Date public holiday API.
 * It defines methods for retrieving public holidays for a specific year and country,
 * and for fetching a list of available countries supported by the API.
 * </p>
 *
 * <p>
 * The Feign client is configured with the base URL "https://date.nager.at/api/v3"
 * using the {@link FeignClient} annotation.  All methods in this interface
 * correspond to specific API endpoints provided by Nager.Date.
 * </p>
 * @author Abhinav Gupta
 */
@FeignClient(name = "holidayClient", url = "https://date.nager.at/api/v3")
public interface HolidayClient {
    /**
     * Retrieves a list of public holidays for a given year and country.
     *
     * @param year The year for which to retrieve public holidays (e.g., 2024).
     * @param countryCode The ISO 3166-1 alpha-2 country code (e.g., "US", "DE", "FR").
     * @return A {@link List} of {@link Holiday} objects representing the public holidays.
     * Returns an empty list if no public holidays are found for the given year and country.
     * The {@link Holiday} class is expected to define the structure of each holiday,
     * including properties like date, localName, name, and countryCode.
     * @throws org.springframework.web.client.HttpClientErrorException If the client receives an HTTP 4xx error.
     * @throws org.springframework.web.client.HttpServerErrorException If the client receives an HTTP 5xx error.
     * @see <a href="https://date.nager.at/api/v3">Nager.Date API Documentation</a>
     */
    @GetMapping("/PublicHolidays/{year}/{countryCode}")
    List<Holiday> getPublicHolidays(@PathVariable int year, @PathVariable String countryCode);

    /**
     * Retrieves a list of available countries supported by the Nager.Date API.
     *
     * @return A {@link List} of {@link Map} objects, where each map contains
     * the country code and country name.  The keys in each map are
     * "countryCode" (String) and "name" (String).
     * Returns an empty list if no countries are available.
     * @throws org.springframework.web.client.HttpClientErrorException If the client receives an HTTP 4xx error.
     * @throws org.springframework.web.client.HttpServerErrorException If the client receives an HTTP 5xx error.
     * @see <a href="https://date.nager.at/api/v3/AvailableCountries">Nager.Date Available Countries API</a>
     */
    @GetMapping("/AvailableCountries")
    List<Map<String, String>> getAvailableCountries();
}