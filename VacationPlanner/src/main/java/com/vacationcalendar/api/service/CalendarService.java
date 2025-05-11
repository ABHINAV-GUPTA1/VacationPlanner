package com.vacationcalendar.api.service;

import com.vacationcalendar.api.dto.HolidayDTO;
import com.vacationcalendar.api.dto.WeekInfo;
import org.apache.coyote.BadRequestException;

import java.util.List;
import java.util.Map;

public interface CalendarService {

    HolidayDTO getWeeklyColorMap(String countryCode, int year, Integer month, Integer quarter) throws BadRequestException;
    List<Map<String, String>> getAvailableCountries();
}
