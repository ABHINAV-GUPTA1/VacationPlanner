package com.vacationcalendar.api.service.impl;

import com.vacationcalendar.api.client.HolidayClient;
import com.vacationcalendar.api.dto.WeekInfo;
import com.vacationcalendar.api.model.Holiday;
import com.vacationcalendar.api.util.WeekColor;
import com.vacationcalendar.api.service.CalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * Implementation of the {@link CalendarService} interface.  This class provides the business logic
 * for retrieving and processing calendar and holiday data.
 * </p>
 *
 * <p>
 * It uses the {@link HolidayClient} to fetch holiday information from an external API
 * and performs calculations to determine the color-coding of weeks based on the number of holidays.
 * </p>
 *
 * @author Abhinav Gupta
 */
@Service
public class CalendarServiceImpl implements CalendarService {

    @Autowired
    private HolidayClient holidayClient;

    /**
     * Retrieves a list of {@link WeekInfo} objects representing the calendar weeks and their
     * corresponding colors for a given country and year.
     * <p>
     * The color of each week is determined by the number of holidays (excluding Saturdays and Sundays)
     * that fall within that week:
     * <ul>
     * <li><b>WHITE</b>: No holidays.</li>
     * <li><b>LIGHT_GREEN</b>: One holiday.</li>
     * <li><b>DARK_GREEN</b>: Two or more holidays.</li>
     * </ul>
     * </p>
     *
     * @param countryCode The ISO 3166-1 alpha-2 country code (e.g., "US", "DE", "FR").
     * @param year        The year for which to retrieve the calendar data (e.g., 2024).
     * @param month       The month for which to retrieve calendar data (1-12). Optional.
     * @param quarter     The quarter for which to retrieve calendar data (1-4). Optional.
     * @return A {@link List} of {@link WeekInfo} objects.  Each {@link WeekInfo} contains the start and end dates
     * of the week, and a {@link WeekColor} indicating the color.
     * Returns an empty list if no holiday data is available or if the country code is invalid.
     */
    @Override
    public List<WeekInfo> getWeeklyColorMap(String countryCode, int year, Integer month, Integer quarter) {
        List<WeekInfo> allWeeks = getWeeklyColorMap(countryCode, year);
        List<WeekInfo> result;

        if (month != null) {
            // Month view
            LocalDate startOfMonth = LocalDate.of(year, month, 1);
            LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());
            result = allWeeks.stream()
                    .filter(week -> !week.getEndOfWeek().isBefore(startOfMonth) && !week.getStartOfWeek().isAfter(endOfMonth))
                    .collect(Collectors.toList());
        } else if (quarter != null) {
            // Quarter view
            int startMonth = (quarter - 1) * 3 + 1;
            int endMonth = quarter * 3;
            LocalDate startOfQuarter = LocalDate.of(year, startMonth, 1);
            LocalDate endOfQuarter = LocalDate.of(year, endMonth, 1).withDayOfMonth(LocalDate.of(year, endMonth, 1).lengthOfMonth());
            result = allWeeks.stream()
                    .filter(week -> !week.getEndOfWeek().isBefore(startOfQuarter) && !week.getStartOfWeek().isAfter(endOfQuarter))
                    .collect(Collectors.toList());
        } else {
            // Year view
            result = allWeeks;
        }
        return result;
    }

    /**
     * Calculates the weekly calendar information for a given year,
     * taking into account holidays.
     *
     * @param holidays A list of {@link Holiday} objects for the year.
     * @param year The year for which to calculate the weekly data.
     * @return A list of {@link WeekInfo} objects, where each object
     * represents a week in the year and its associated color.
     */
    private List<WeekInfo> getWeeklyColorMap(String countryCode, int year) {
        List<Holiday> holidays = holidayClient.getPublicHolidays(year, countryCode);

        Map<Integer, List<LocalDate>> weekMap = new HashMap<>();

        for (int week = 1; week <= 52; week++) {
            LocalDate start = LocalDate.ofYearDay(year, 1).with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week).with(DayOfWeek.MONDAY);
            LocalDate end = start.plusDays(4);

            long holidayCount = holidays.stream()
                    .map(Holiday::getDate)
                    .filter(d -> !d.getDayOfWeek().equals(DayOfWeek.SATURDAY) && !d.getDayOfWeek().equals(DayOfWeek.SUNDAY))
                    .filter(d -> !d.isBefore(start) && !d.isAfter(end))
                    .count();

            WeekColor color = WeekColor.WHITE;
            if (holidayCount == 1) color = WeekColor.LIGHT_GREEN;
            else if (holidayCount >= 2) color = WeekColor.DARK_GREEN;

            weekMap.put(week, List.of(start, end));
        }

        return weekMap.entrySet().stream()
                .map(entry -> {
                    LocalDate s = entry.getValue().get(0);
                    LocalDate e = entry.getValue().get(1);
                    long holidayCount = holidays.stream()
                            .map(Holiday::getDate)
                            .filter(d -> !d.getDayOfWeek().equals(DayOfWeek.SATURDAY) && !d.getDayOfWeek().equals(DayOfWeek.SUNDAY))
                            .filter(d -> !d.isBefore(s) && !d.isAfter(e))
                            .count();

                    WeekColor color = WeekColor.WHITE;
                    if (holidayCount == 1) color = WeekColor.LIGHT_GREEN;
                    else if (holidayCount >= 2) color = WeekColor.DARK_GREEN;

                    return new WeekInfo(s, e, color);
                })
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a list of available countries.
     *
     * @return A {@link List} of {@link Map} objects, where each map contains the country code and name.
     * Returns an empty list if no countries are available.
     */
    @Override
    public List<Map<String, String>> getAvailableCountries() {
        return holidayClient.getAvailableCountries();
    }
}