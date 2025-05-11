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

}