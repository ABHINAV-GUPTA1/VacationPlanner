package com.vacationcalendar.api.service.impl;

import com.vacationcalendar.api.client.HolidayClient;
import com.vacationcalendar.api.dto.HolidayDTO;
import com.vacationcalendar.api.dto.HolidayDetails;
import com.vacationcalendar.api.dto.WeekInfo;
import com.vacationcalendar.api.helper.WikiURLFetchHelper;
import com.vacationcalendar.api.model.Holiday;
import com.vacationcalendar.api.model.WikiHoliday;
import com.vacationcalendar.api.util.HolidayUtil;
import com.vacationcalendar.api.util.WeekColor;
import com.vacationcalendar.api.service.CalendarService;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.*;
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
@Slf4j
public class CalendarServiceImpl implements CalendarService {

    @Autowired
    private HolidayClient holidayClient;

    @Autowired
    private WikiURLFetchHelper wikiURLFetchHelper;

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
    public HolidayDTO getWeeklyColorMap(String countryCode, int year, Integer month, Integer quarter) throws BadRequestException {

        List<WeekInfo> result;

        if (month != null) {
            // Month view
            LocalDate startOfMonth = LocalDate.of(year, month, 1);
            LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());
            HolidayDTO holidayDTO = getWeeklyColorMap(countryCode, year, startOfMonth, endOfMonth);
            HolidayUtil.populateWikiLinkInHolidayList(holidayDTO.getHolidayDetailsList(), countryCode);
            return holidayDTO;
        } else if (quarter != null) {
            // Quarter view
            int startMonth = (quarter - 1) * 3 + 1;
            int endMonth = quarter * 3;
            LocalDate startOfQuarter = LocalDate.of(year, startMonth, 1);
            LocalDate endOfQuarter = LocalDate.of(year, endMonth, 1).withDayOfMonth(LocalDate.of(year, endMonth, 1).lengthOfMonth());
            HolidayDTO holidayDTO = getWeeklyColorMap(countryCode, year, startOfQuarter, endOfQuarter);
            HolidayUtil.populateWikiLinkInHolidayList(holidayDTO.getHolidayDetailsList(), countryCode);
            return holidayDTO;
        } else {
            throw new BadRequestException("Request Not valid");
        }
    }

    private HolidayDTO getWeeklyColorMap(String countryCode, int year, LocalDate startOfDate, LocalDate endOfDate) {
        // Fetch all holidays once
        List<Holiday> holidays = holidayClient.getPublicHolidays(year, countryCode);

        // Filter out weekend holidays once
        Set<LocalDate> validHolidayDates = holidays.stream()
                .map(Holiday::getDate)
                .filter(d -> !d.getDayOfWeek().equals(DayOfWeek.SATURDAY) && !d.getDayOfWeek().equals(DayOfWeek.SUNDAY))
                .collect(Collectors.toSet());

        List<WeekInfo> weekInfoList = new ArrayList<>();

        for (int week = 1; week <= 52; week++) {
            LocalDate start = LocalDate.ofYearDay(year, 1)
                    .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week)
                    .with(DayOfWeek.MONDAY);
            LocalDate end = start.plusDays(4);

            // Skip if outside date range
            if (end.isBefore(startOfDate) || start.isAfter(endOfDate)) {
                continue;
            }

            long holidayCount = validHolidayDates.stream()
                    .filter(d -> !d.isBefore(start) && !d.isAfter(end))
                    .count();

            WeekColor color = WeekColor.WHITE;
            if (holidayCount == 1) color = WeekColor.LIGHT_GREEN;
            else if (holidayCount >= 2) color = WeekColor.DARK_GREEN;

            weekInfoList.add(new WeekInfo(start, end, color));
        }

        // Filter holidays for the requested date range
        List<HolidayDetails> holidayDetailsList = holidays.stream()
                .filter(h -> {
                    LocalDate d = h.getDate();
                    return !d.getDayOfWeek().equals(DayOfWeek.SATURDAY)
                            && !d.getDayOfWeek().equals(DayOfWeek.SUNDAY)
                            && !d.isBefore(startOfDate)
                            && !d.isAfter(endOfDate);
                })
                .map(h -> new HolidayDetails(h.getName(), h.getDate(), "NOT_AVAILABLE"))
                .collect(Collectors.toList());

        return new HolidayDTO(weekInfoList, holidayDetailsList);
    }

    /**
     * Calculates the weekly calendar information for a given year,
     * taking into account holidays.
     *
     * @param countryCode country code for fetching the holidays.
     * @param year The year for which to calculate the weekly data.
     * @return A {@link HolidayDTO} object, where each object
     * represents a week in the year and its associated color.
     */
    private HolidayDTO getWeeklyColorMap_unoptimized(String countryCode, int year, LocalDate startOfDate, LocalDate endOfDate) {
        List<Holiday> holidays = holidayClient.getPublicHolidays(year, countryCode);

        Map<Integer, List<LocalDate>> weekMap = new HashMap<>();

        for (int week = 1; week <= 52; week++) {
            LocalDate start = LocalDate.ofYearDay(year, 1).with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week).with(DayOfWeek.MONDAY);
            LocalDate end = start.plusDays(4);

            Set<LocalDate> collect = holidays.stream()
                    .map(Holiday::getDate)
                    .filter(d -> !d.getDayOfWeek().equals(DayOfWeek.SATURDAY) && !d.getDayOfWeek().equals(DayOfWeek.SUNDAY))
                    .filter(d -> !d.isBefore(start) && !d.isAfter(end))
                    .collect(Collectors.toSet());
            long holidayCount = collect.size();

            WeekColor color = WeekColor.WHITE;
            if (holidayCount == 1) color = WeekColor.LIGHT_GREEN;
            else if (holidayCount >= 2) color = WeekColor.DARK_GREEN;

            weekMap.put(week, List.of(start, end));
        }

        List<WeekInfo> weekInfoList = weekMap.entrySet().stream()
                .map(entry -> {
                    LocalDate s = entry.getValue().get(0);
                    LocalDate e = entry.getValue().get(1);
                    Set<LocalDate> holidaySet = holidays.stream()
                            .map(Holiday::getDate)
                            .filter(d -> !d.getDayOfWeek().equals(DayOfWeek.SATURDAY)
                                    && !d.getDayOfWeek().equals(DayOfWeek.SUNDAY))
                            .filter(d -> !d.isBefore(s) && !d.isAfter(e)).collect(Collectors.toSet());
                    long holidayCount = holidaySet.size();

                    WeekColor color = WeekColor.WHITE;
                    if (holidayCount == 1) color = WeekColor.LIGHT_GREEN;
                    else if (holidayCount >= 2) color = WeekColor.DARK_GREEN;

                    return new WeekInfo(s, e, color);
                }).filter(week -> !week.getEndOfWeek().isBefore(startOfDate) && !week.getStartOfWeek().isAfter(endOfDate))
                .collect(Collectors.toList());
        List<HolidayDetails> holidayDetailsList = holidays.stream()
                .filter(d -> !d.getDate().getDayOfWeek().equals(DayOfWeek.SATURDAY)
                        && !d.getDate().getDayOfWeek().equals(DayOfWeek.SUNDAY))
                .filter(d -> !d.getDate().isBefore(startOfDate) && !d.getDate().isAfter(endOfDate))
                .map(obj -> new HolidayDetails(obj.getName(), obj.getDate(), "NOT_AVAILABLE"))
                .collect(Collectors.toList());
        return new HolidayDTO(weekInfoList, holidayDetailsList);
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