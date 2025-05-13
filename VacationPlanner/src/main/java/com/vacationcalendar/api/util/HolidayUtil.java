package com.vacationcalendar.api.util;

import com.vacationcalendar.api.dto.HolidayDetails;
import com.vacationcalendar.api.helper.WikiURLFetchHelper;
import com.vacationcalendar.api.helper.WikipediaHolidayLinkFinder;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Utility class to popuplate the holiday detals in DTO object.
 *
 * @author abhig
 */
@Slf4j
public class HolidayUtil {

    /**
     * Populate the data in holiday object from wiki link object.
     * @param holidayDetailsList the holiday details list object
     * @param countryCode the name of the country.
     */
    public static void populateWikiLinkInHolidayList(List<HolidayDetails> holidayDetailsList, String countryCode) {
        for (HolidayDetails holidayDetails : holidayDetailsList) {
            try {
                String holidayLinks = WikiURLFetchHelper.findHolidayLinks(getCountryNameFromCountryCode(countryCode), holidayDetails.getName());
                if (holidayLinks == null || holidayLinks.isEmpty() || (holidayLinks.contains("Public") && holidayLinks.contains("holidays"))) {
                    holidayLinks =  WikipediaHolidayLinkFinder.findHolidayLinks(getCountryNameFromCountryCode(countryCode), holidayDetails.getName());
                }
                holidayDetails.setUrl(holidayLinks);
            } catch (BadRequestException e) {
                log.error("Exception while fetching url", e);
            } catch (Exception e) {
                log.error("Exception while fetching url", e);
            }
        }
    }

    /**
     * To get the country name from the country code.
     * @param countryCode the country code like AU
     * @return displau country name.
     * @throws BadRequestException
     */
    private static String getCountryNameFromCountryCode(String countryCode) throws BadRequestException {
        Locale locale = new Locale("", countryCode.toUpperCase());
        return locale.getDisplayCountry();
    }

}
