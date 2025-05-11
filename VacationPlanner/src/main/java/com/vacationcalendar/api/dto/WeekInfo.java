package com.vacationcalendar.api.dto;

import com.vacationcalendar.api.util.WeekColor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * <p>
 * Represents information about a week, including its start and end dates, and an associated color.
 * </p>
 *
 * <p>
 * This class is used to structure the data representing a week in a calendar,
 * particularly for indicating the color-coding of weeks, potentially to highlight
 * holidays or other special periods.
 * </p>
 *
 * @author Abhinav Gupta
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeekInfo {
    /**
     * The date representing the start of the week.
     * <p>
     * This is a {@link LocalDate} object, providing a date without time-of-day.
     * </p>
     */
    private LocalDate startOfWeek;

    /**
     * The date representing the end of the week.
     * <p>
     * This is a {@link LocalDate} object, providing a date without time-of-day.
     * </p>
     */
    private LocalDate endOfWeek;

    /**
     * The color associated with the week.
     * <p>
     * The color is represented by the {@link WeekColor} enum.
     * </p>
     */
    private WeekColor color;
}