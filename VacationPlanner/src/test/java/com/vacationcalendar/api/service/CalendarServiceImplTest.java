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

    /**
     * Tests the {@link CalendarServiceImpl#getWeeklyColorMapForYear(List, int)} method.
     * This test verifies that the method correctly calculates the WeekInfo list for a given year,
     * taking into account holidays.
     */
    @Test
    void getWeeklyColorMapForYear_shouldReturnCorrectWeekInfoList() {
        // Arrange
        int year = 2024;
        List<Holiday> holidays = new ArrayList<>();
        holidays.add(new Holiday(LocalDate.of(2024, 1, 1), "holiDay", "US")); // Monday
        holidays.add(new Holiday(LocalDate.of(2024, 1, 15), "holiDay", "US")); // Monday
        holidays.add(new Holiday(LocalDate.of(2024, 2, 19), "holiDay", "US")); // Monday
        when(holidayClient.getPublicHolidays(year, "US")).thenReturn(holidays);

        // Act
        List<WeekInfo> weekInfoList = calendarService.getWeeklyColorMap("US", year, null, null); // Adjusted call

        // Assert
        assertEquals(52, weekInfoList.size());

        // Check a few weeks to verify correct calculation
        assertEquals(LocalDate.of(2024, 1, 1), weekInfoList.get(0).getStartOfWeek());
        assertEquals(LocalDate.of(2024, 1, 5), weekInfoList.get(0).getEndOfWeek());
        assertEquals(WeekColor.LIGHT_GREEN, weekInfoList.get(0).getColor());

        assertEquals(LocalDate.of(2024, 1, 15), weekInfoList.get(2).getStartOfWeek());
        assertEquals(LocalDate.of(2024, 1, 19), weekInfoList.get(2).getEndOfWeek());
        assertEquals(WeekColor.LIGHT_GREEN, weekInfoList.get(2).getColor());

        assertEquals(LocalDate.of(2024, 2, 19), weekInfoList.get(7).getStartOfWeek());
        assertEquals(LocalDate.of(2024, 2, 23), weekInfoList.get(7).getEndOfWeek());
        assertEquals(WeekColor.LIGHT_GREEN, weekInfoList.get(7).getColor());

        assertEquals(LocalDate.of(2024, 5, 13), weekInfoList.get(19).getStartOfWeek());
        assertEquals(LocalDate.of(2024, 5, 17), weekInfoList.get(19).getEndOfWeek());
        assertEquals(WeekColor.WHITE, weekInfoList.get(19).getColor());
    }

    /**
     * Tests the {@link CalendarServiceImpl#getWeeklyColorMap(String, int, Integer, Integer)} method
     * for month view.
     */
    @Test
    void getWeeklyColorMap_monthView_shouldReturnCorrectWeekInfoList() {
        // Arrange
        int year = 2024;
        int month = 1; // January
        List<Holiday> holidays = new ArrayList<>();
        holidays.add(new Holiday(LocalDate.of(2024, 1, 1), "holiDay", "US"));
        holidays.add(new Holiday(LocalDate.of(2024, 1, 15), "holiDay", "US"));
        when(holidayClient.getPublicHolidays(year, "US")).thenReturn(holidays);

        // Act
        List<WeekInfo> weekInfoList = calendarService.getWeeklyColorMap("US", year, month, null);

        // Assert
        assertEquals(5, weekInfoList.size()); //jan has 5 weeks

        assertEquals(LocalDate.of(2024, 1, 1), weekInfoList.get(0).getStartOfWeek());
        assertEquals(LocalDate.of(2024, 1, 5), weekInfoList.get(0).getEndOfWeek());
        assertEquals(WeekColor.LIGHT_GREEN, weekInfoList.get(0).getColor());

        assertEquals(LocalDate.of(2024, 1, 15), weekInfoList.get(2).getStartOfWeek());
        assertEquals(LocalDate.of(2024, 1, 19), weekInfoList.get(2).getEndOfWeek());
        assertEquals(WeekColor.LIGHT_GREEN, weekInfoList.get(2).getColor());
    }

    /**
     * Tests the {@link CalendarServiceImpl#getWeeklyColorMap(String, int, Integer, Integer)} method
     * for quarter view.
     */
    @Test
    void getWeeklyColorMap_quarterView_shouldReturnCorrectWeekInfoList() {
        // Arrange
        int year = 2024;
        int quarter = 1; // 1st Quarter (Jan-Mar)
        List<Holiday> holidays = new ArrayList<>();
        holidays.add(new Holiday(LocalDate.of(2024, 1, 1), "holiDay", "US"));
        holidays.add(new Holiday(LocalDate.of(2024, 2, 19), "holiDay", "US"));
        holidays.add(new Holiday(LocalDate.of(2024, 3, 17), "holiDay", "US"));
        when(holidayClient.getPublicHolidays(year, "US")).thenReturn(holidays);

        // Act
        List<WeekInfo> weekInfoList = calendarService.getWeeklyColorMap("US", year, null, quarter);

        // Assert
        // Check that the result is within the 1st quarter
        assertEquals(13, weekInfoList.size());

        assertEquals(LocalDate.of(2024, 1, 1), weekInfoList.get(0).getStartOfWeek());
        assertEquals(LocalDate.of(2024, 1, 5), weekInfoList.get(0).getEndOfWeek());
        assertEquals(WeekColor.LIGHT_GREEN, weekInfoList.get(0).getColor());

        assertEquals(LocalDate.of(2024, 2, 19), weekInfoList.get(7).getStartOfWeek());
        assertEquals(LocalDate.of(2024, 2, 23), weekInfoList.get(7).getEndOfWeek());
        assertEquals(WeekColor.LIGHT_GREEN, weekInfoList.get(7).getColor());

        assertEquals(LocalDate.of(2024, 3, 18), weekInfoList.get(11).getStartOfWeek());
        assertEquals(LocalDate.of(2024, 3, 22), weekInfoList.get(11).getEndOfWeek());
        assertEquals(WeekColor.WHITE, weekInfoList.get(11).getColor());
    }

    /**
     * Tests the {@link CalendarServiceImpl#getWeeklyColorMap(String, int, Integer, Integer)} method
     * for year view.
     */
    @Test
    void getWeeklyColorMap_yearView_shouldReturnCorrectWeekInfoList() {
        // Arrange
        int year = 2024;
        List<Holiday> holidays = new ArrayList<>();
        holidays.add(new Holiday(LocalDate.of(2024, 1, 1), "holiDay", "US"));
        when(holidayClient.getPublicHolidays(year, "US")).thenReturn(holidays);

        // Act
        List<WeekInfo> weekInfoList = calendarService.getWeeklyColorMap("US", year, null, null);

        // Assert
        assertEquals(52, weekInfoList.size());
        assertEquals(LocalDate.of(2024, 1, 1), weekInfoList.get(0).getStartOfWeek());
        assertEquals(LocalDate.of(2024, 12, 23), weekInfoList.get(51).getStartOfWeek());
    }
}