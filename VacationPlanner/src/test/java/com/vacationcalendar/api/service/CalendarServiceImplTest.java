package com.vacationcalendar.api.service;

import com.vacationcalendar.api.client.HolidayClient;
import com.vacationcalendar.api.dto.WeekInfo;
import com.vacationcalendar.api.model.Holiday;
import com.vacationcalendar.api.service.impl.CalendarServiceImpl;
import com.vacationcalendar.api.util.WeekColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CalendarServiceImpl}.
 *
 * This class contains unit tests to verify the functionality of the {@code CalendarServiceImpl} class.
 * It uses Mockito to mock the {@link HolidayClient} dependency and test the service's logic
 * in isolation.
 */
public class CalendarServiceImplTest {

    @Mock
    private HolidayClient holidayClient;

    @InjectMocks
    private CalendarServiceImpl calendarService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Tests the {@link CalendarServiceImpl#getWeeklyColorMap(String, int)} method.
     *
     * This test verifies that the method correctly calculates the week colors based on the holidays
     * returned by the mocked {@link HolidayClient}. It checks the number of weeks returned and the
     * start/end dates and colors of specific weeks.
     */
    @Test
    void getWeeklyColorMap_shouldReturnCorrectWeekInfoList() {
        // Arrange
        String countryCode = "US";
        int year = 2024;

        // Mock HolidayClient response
        List<Holiday> holidays = new ArrayList<>();
        //holidays.add(new Holiday(LocalDate.of(2024, 1, 1), "New Year's Day", countryCode)); // Monday
        holidays.add(new Holiday(LocalDate.of(2024, 1, 3), "Holiday 1", countryCode)); // Wednesday
        holidays.add(new Holiday(LocalDate.of(2024, 1, 5), "Holiday 2", countryCode)); // Friday
        holidays.add(new Holiday(LocalDate.of(2024, 1, 8), "Holiday 3", countryCode)); // Monday
        when(holidayClient.getPublicHolidays(year, countryCode)).thenReturn(holidays);

        // Act
        List<WeekInfo> weekInfoList = calendarService.getWeeklyColorMap(countryCode, year);

        // Assert
        assertEquals(52, weekInfoList.size()); // Check that we get 52 weeks

        // Check the first week (2024-01-01 to 2024-01-05)
        assertEquals(LocalDate.of(2024, 1, 1), weekInfoList.get(0).getStartOfWeek());
        assertEquals(LocalDate.of(2024, 1, 5), weekInfoList.get(0).getEndOfWeek());
        assertEquals(WeekColor.DARK_GREEN, weekInfoList.get(0).getColor()); // 2 holiday

        // Check the second week (2024-01-08 to 2024-01-12)
        assertEquals(LocalDate.of(2024, 1, 8), weekInfoList.get(1).getStartOfWeek());
        assertEquals(LocalDate.of(2024, 1, 12), weekInfoList.get(1).getEndOfWeek());
        assertEquals(WeekColor.LIGHT_GREEN, weekInfoList.get(1).getColor()); // 1 holiday

        //check third week(2024-01-15 to 2024-01-19)
        assertEquals(LocalDate.of(2024, 1, 15), weekInfoList.get(2).getStartOfWeek());
        assertEquals(LocalDate.of(2024, 1, 19), weekInfoList.get(2).getEndOfWeek());
        assertEquals(WeekColor.WHITE, weekInfoList.get(2).getColor()); // 0 holiday
    }

    /**
     * Tests the {@link CalendarServiceImpl#getAvailableCountries()} method.
     *
     * This test verifies that the method correctly retrieves and returns the list of countries
     * from the mocked {@link HolidayClient}.  It checks the number of countries returned and the
     * content of the list.
     */
    @Test
    void getAvailableCountries_shouldReturnListOfCountries() {
        // Arrange
        List<Map<String, String>> countries = new ArrayList<>();
        Map<String, String> country1 = Map.of("countryCode", "US", "name", "United States");
        Map<String, String> country2 = Map.of("countryCode", "CA", "name", "Canada");
        countries.add(country1);
        countries.add(country2);
        when(holidayClient.getAvailableCountries()).thenReturn(countries);

        // Act
        List<Map<String, String>> result = calendarService.getAvailableCountries();

        // Assert
        assertEquals(2, result.size());
        assertEquals("US", result.get(0).get("countryCode"));
        assertEquals("United States", result.get(0).get("name"));
        assertEquals("CA", result.get(1).get("countryCode"));
        assertEquals("Canada", result.get(1).get("name"));
    }
}