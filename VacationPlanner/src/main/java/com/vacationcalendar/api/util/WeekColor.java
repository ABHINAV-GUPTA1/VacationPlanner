package com.vacationcalendar.api.util;

/**
 * <p>
 * Represents the color associated with a week in a calendar.
 * </p>
 *
 * <p>
 * This enum defines the possible colors that can be assigned to a week
 * to indicate its status, such as the number of holidays within that week.
 * </p>
 *
 * @author Abhinav Gupta
 */
public enum WeekColor {

    /**
     * Represents a week with no holidays.
     */
    WHITE,

    /**
     * Represents a week with one holiday.
     */
    LIGHT_GREEN,

    /**
     * Represents a week with two or more holidays.
     */
    DARK_GREEN
}