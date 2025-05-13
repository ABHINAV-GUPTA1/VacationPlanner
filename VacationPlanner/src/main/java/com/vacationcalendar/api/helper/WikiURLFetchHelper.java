package com.vacationcalendar.api.helper;

import com.vacationcalendar.api.model.WikiHoliday;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * This class is used to fetch holiday url from wiki.
 *
 * @author Abhinav Gupta
 */
@Component
@Slf4j
public class WikiURLFetchHelper {


    // Base URL for the English Wikipedia API
    private static final String WIKIPEDIA_API_URL = "https://en.wikipedia.org/w/api.php";

    /**
     * Fetches a Wikipedia link for a given holiday and country.
     *
     * @param countryName The name of the country.
     * @param holidayName The name of the holiday.
     * @return The Wikipedia URL as a String, or null if not found or an error occurs.
     */
    public static String getWikipediaLink(String countryName, String holidayName) {
        if (holidayName == null || holidayName.trim().isEmpty()) {
            log.error("Holiday name cannot be empty.");
            return "NOT_AVAILABLE";
        }

        // Construct a search query. Adding the country can help disambiguate.
        String searchQuery = holidayName + (countryName != null && !countryName.trim().isEmpty() ? " " + countryName : "");

        try {
            // 1. Construct the API request URL
            String encodedSearchQuery = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8.toString());
            String params = String.format("action=query&list=search&srsearch=%s&format=json&srlimit=2", encodedSearchQuery);
            URL url = new URL(WIKIPEDIA_API_URL + "?" + params);

            // 2. Make the HTTP GET request
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            // User-Agent is good practice for API requests
            connection.setRequestProperty("User-Agent", "MyHolidayApp/1.0 (myemail@example.com)");


            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                connection.disconnect();

                // 3. Parse the JSON response
                return parseWikipediaResponse(content.toString(), holidayName);
            } else {
                System.err.println("Error fetching data from Wikipedia API. Response Code: " + responseCode);
                connection.disconnect();
                return "NOT_AVAILABLE";
            }

        } catch (Exception e) {
            log.error("An error occurred: ", e);
            return "NOT_AVAILABLE";
        }
    }

    /**
     * Parses the JSON response from the Wikipedia API to find the most relevant link.
     *
     * @param jsonResponse The JSON string from the API.
     * @param holidayName The original holiday name (for better matching).
     * @return The Wikipedia URL or null.
     */
    private static String parseWikipediaResponse(String jsonResponse, String holidayName) {
        try {
            JSONObject responseObject = new JSONObject(new JSONTokener(jsonResponse));
            if (responseObject.has("query")) {
                JSONObject query = responseObject.getJSONObject("query");
                if (query.has("search")) {
                    JSONArray searchResults = query.getJSONArray("search");
                    if (searchResults.length() > 0) {
                        // Take the first result, assuming it's the most relevant.
                        // More sophisticated logic could be added here to choose the best match.
                        return getURLFromSearchByIndex(searchResults, holidayName, 1);
                    } else {
                        log.error("No Wikipedia results found for: " + holidayName);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing Wikipedia JSON response: " + e.getMessage());
        }
        return null;
    }

    /**
     * Recursively searches through the given Wikipedia search results to find a URL whose title contains the specified holiday name.
     * <p>
     * Starts at the given index and checks if the title of the result at that index contains the holiday name.
     * If not, the method recursively advances the index. If the index exceeds 1 without a match,
     * it falls back to returning the URL generated from the first result via {@link #getURLFromSearch(JSONArray)}.
     * The title is URL-encoded to safely form a valid Wikipedia URL.
     * </p>
     *
     * @param searchResults a {@link JSONArray} containing search results from a Wikipedia search API response;
     *                      each element is expected to be a JSON object with a "title" field
     * @param holidayName   the holiday name to look for within the titles of the search results
     * @param idx           the index to start or continue the search from (typically 0)
     * @return a Wikipedia URL corresponding to the first matching search result title that contains the holiday name,
     *         or a fallback URL if no match is found within the first two results
     * @throws UnsupportedEncodingException if UTF-8 encoding is not supported (should not occur in standard environments)
     */
    private static String getURLFromSearchByIndex(JSONArray searchResults, String holidayName, int idx) throws UnsupportedEncodingException {
        if (idx > 1) {
            return getURLFromSearch(searchResults);
        }
        JSONObject firstResult = searchResults.getJSONObject(idx);
        String title = firstResult.getString("title");
        if (!title.contains(holidayName)) {
            return getURLFromSearchByIndex(searchResults, holidayName, idx + 1);
        }
        String encodedTitle = URLEncoder.encode(title.replace(" ", "_"), StandardCharsets.UTF_8.toString());
        return "https://en.wikipedia.org/wiki/" + encodedTitle;
    }

    /**
     * Constructs a Wikipedia URL from the first result in a given JSON search result array.
     * <p>
     * This method extracts the title of the first search result, replaces spaces with underscores,
     * URL-encodes the title to ensure it is safe for inclusion in a URL, and appends it to the base Wikipedia URL.
     * </p>
     *
     * @param searchResults a {@link JSONArray} containing search results from a Wikipedia search API response;
     *                      the first element must be a JSON object with a "title" field
     * @return a fully formed Wikipedia URL pointing to the article corresponding to the first search result
     * @throws UnsupportedEncodingException if UTF-8 encoding is not supported (should not occur in standard environments)
     */
    private static String getURLFromSearch(JSONArray searchResults) throws UnsupportedEncodingException {
        JSONObject firstResult = searchResults.getJSONObject(0);
        String title = firstResult.getString("title");
        String encodedTitle = URLEncoder.encode(title.replace(" ", "_"), StandardCharsets.UTF_8.toString());
        return "https://en.wikipedia.org/wiki/" + encodedTitle;
    }

    /**
     * Searches for a link related to a specific holiday within a Wikipedia page about public holidays in a given country.
     * <p>
     * This method constructs a Wikipedia URL based on the provided country name and retrieves the page content using Jsoup.
     * It then scans all anchor tags (<code>&lt;a href="..."&gt;</code>) for links that contain the specified holiday name
     * (case-insensitive), either in the hyperlink reference (href) or the link text. If a match is found, it returns the full URL
     * to that section or page. If no such link is found or an error occurs during the process, it falls back to returning
     * a default Wikipedia link based on the given parameters.
     * </p>
     *
     * @param countryName the name of the country (e.g., "United States")
     * @param holidayName the name of the holiday to search for (e.g., "Independence Day")
     * @return a Wikipedia URL pointing to a page or section that mentions the holiday, or a fallback link if none is found
     */
    public static String findHolidayLinks(String countryName, String holidayName) {
        String url = getWikipediaLink(countryName, "public holiday");
        try {
            Document doc = Jsoup.connect(url).get();
            Elements links = doc.select("a[href]");

            log.info("Links on " + url + " containing '" + holidayName + "':");
            boolean found = false;
            for (Element link : links) {
                String href = link.attr("href");
                String text = link.text().toLowerCase(); // Convert link text to lowercase for case-insensitive search
                if (href.contains(holidayName.toLowerCase()) || text.contains(holidayName.toLowerCase())) {
                    if (href.startsWith("#")) {
                        return "https://en.wikipedia.org/wiki/"+href.substring(1);
                    }
                    return "https://en.wikipedia.org"+href;
                }
            }

        } catch (IOException e) {
            log.error("Error fetching or parsing the page for " + countryName + ": " + e.getMessage());
        }
        return getWikipediaLink(countryName, holidayName);
    }
}
