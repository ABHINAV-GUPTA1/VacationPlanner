package com.vacationcalendar.api.controller;

import com.vacationcalendar.api.dto.WeekInfo;
import com.vacationcalendar.api.service.CalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * This class, {@code CalendarController}, is a REST controller that handles requests related to calendar and holiday data.
 * It exposes API endpoints for retrieving weekly calendar information and a list of supported countries.
 * </p>
 *
 * <p>
 * The controller is mapped to the base URL "/api/v1/holidays" using the {@link RequestMapping} annotation.
 * It uses the {@link CalendarService} to perform the business logic of fetching the data.
 * </p>
 * @author Abhinav Gupta
 */
@RestController
@RequestMapping("/api/v1/holidays")
public class CalendarController {

    @Autowired
    private CalendarService calendarService;

    /**
     * Retrieves weekly calendar information, including color-coded weeks, for a specified country and year.
     *
     * @param countryCode The ISO 3166-1 alpha-2 country code (e.g., "US", "DE", "FR").  This is a required parameter.
     * @param year The year for which to retrieve the calendar data (e.g., 2024). This is a required parameter.
     * @return A {@link List} of {@link WeekInfo} objects, where each object represents a week and its associated color.
     * The {@link WeekInfo} class is expected to contain properties like startOfWeek, endOfWeek, and color.
     * Returns an empty list if no data is found for the given country and year.
     * @throws org.springframework.web.bind.MissingServletRequestParameterException If the {@code countryCode} or {@code year}
     * parameters are not provided in the request.
     * @see CalendarService#getWeeklyColorMap(String, int)
     */
    @GetMapping("/weeks")
    public List<WeekInfo> getCalendarWeeks(@RequestParam String countryCode, @RequestParam int year) {
        return calendarService.getWeeklyColorMap(countryCode, year);
    }

    /**
     * Retrieves a list of available countries supported by the application.
     *
     * @return A {@link List} of {@link Map} objects, where each map contains the country code and country name.
     * The keys in each map are "countryCode" (String) and "name" (String).
     * Returns an empty list if no countries are available.
     * @see CalendarService#getAvailableCountries()
     */
    @GetMapping("/countries")
    public List<Map<String, String>> getCountries() {
        return calendarService.getAvailableCountries();
    }
}