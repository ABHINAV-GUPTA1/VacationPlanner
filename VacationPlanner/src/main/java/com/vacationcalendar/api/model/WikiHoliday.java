package com.vacationcalendar.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This is a model class which models data from wiki holiday.
 *
 * @author abhig
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WikiHoliday {
    /**
     * The date of the holiday.
     */
    private String date;

    /**
     * The name of the holiday.
     */
    private String name;

    /**
     * The url of the holiday.
     */
    private String url;
}
