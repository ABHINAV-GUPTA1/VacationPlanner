package com.vacationcalendar.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Details of the holidayDTO.
 *
 * @author Abhinav Gupta
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HolidayDetails {
    /**
     * The name of the holiday.
     */
    private String name;

    /**
     * The date of the holiday.
     */
    private LocalDate date;

    /**
     * The url of the holiday
     */
    private String url;
}
