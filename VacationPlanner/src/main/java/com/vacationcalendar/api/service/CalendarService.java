package com.vacationcalendar.api.service;

import com.vacationcalendar.api.dto.WeekInfo;

import java.util.List;
import java.util.Map;

public interface CalendarService {
    List<WeekInfo> getWeeklyColorMap(String countryCode, int year);

    List<Map<String, String>> getAvailableCountries();
}
