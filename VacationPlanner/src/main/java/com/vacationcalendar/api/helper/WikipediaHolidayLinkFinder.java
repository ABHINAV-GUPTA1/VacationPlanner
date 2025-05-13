package com.vacationcalendar.api.helper;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.logging.Logger;

@Slf4j
public class WikipediaHolidayLinkFinder {

    private static final String WIKIPEDIA_API_URL = "https://en.wikipedia.org/w/api.php";

    /**
     * Attempts to find a Wikipedia link related to a specific holiday in a given country.
     * <p>
     * This method first constructs the URL of the "Public holidays in [Country]" Wikipedia page using
     * {@link #getWikipediaLink(String, String)}. If the page exists, it searches for a link on that page
     * which matches the given holiday name using {@link #findHolidayLinkInPage(String, String)}.
     * If a relevant link is found, it is returned. Otherwise, the method falls back to a broader Wikipedia
     * search by constructing a URL for the given holiday name within the context of the country.
     * </p>
     *
     * @param countryName the name of the country (e.g., "Canada", "India")
     * @param holidayName the name of the holiday to search for (e.g., "Diwali", "Thanksgiving")
     * @return a Wikipedia URL pointing to a page or section about the specified holiday in the given country,
     *         or a fallback link constructed using the holiday name
     */
    public static String findHolidayLinks(String countryName, String holidayName) {
        String publicHolidaysPageUrl = getWikipediaLink(countryName, "public holidays in"); // corrected

        if (publicHolidaysPageUrl != null && !publicHolidaysPageUrl.equals("NOT_AVAILABLE")) {
            String fuzzyLink = findHolidayLinkInPage(publicHolidaysPageUrl, holidayName);
            if (fuzzyLink != null) {
                return fuzzyLink;
            }
        }
        // If not found in the page or the page doesn't exist, fall back to the API search
        return getWikipediaLink(countryName, holidayName);
    }

    /**
     * Searches for a link related to a specific holiday name within a given Wikipedia page.
     * <p>
     * This method loads the HTML content of the specified Wikipedia page and scans all anchor tags for links
     * whose visible text contains the holiday name (case-insensitive). If one or more direct matches are found,
     * the first matching full Wikipedia URL is returned. If no exact matches are found, it attempts to find the
     * closest match using {@link #findClosestMatch(Elements, String)}.
     * </p>
     *
     * @param pageUrl     the full URL of the Wikipedia page to search within
     * @param holidayName the holiday name to search for within the page's links
     * @return a full Wikipedia URL that most closely matches the holiday name, or {@code null} if none is found
     */
    private static String findHolidayLinkInPage(String pageUrl, String holidayName) {
        try {
            Document doc = Jsoup.connect(pageUrl).get();
            Elements links = doc.select("a[href]");

            List<String> matchingLinks = new ArrayList<>();
            for (Element link : links) {
                String href = link.attr("href");
                String text = link.text().toLowerCase();
                if (text.contains(holidayName.toLowerCase())) {
                    matchingLinks.add("https://en.wikipedia.org" + href);
                }
            }
            if (matchingLinks.isEmpty()) {
                return findClosestMatch(links, holidayName); // Find closest match
            }
            return matchingLinks.get(0); // Return the first matching link

        } catch (IOException e) {
            log.error("Error fetching or parsing the page: " + pageUrl, e);
            return null;
        }
    }

    /**
     * Finds the Wikipedia link whose anchor text is the closest match to a given target name using Levenshtein distance.
     * <p>
     * This method iterates over a collection of anchor elements and computes the Levenshtein distance between each
     * link's text and the target name (case-insensitive). The link with the smallest distance is considered the closest match.
     * The method then returns the full Wikipedia URL for that link.
     * </p>
     *
     * @param links      a collection of {@link Element} objects representing anchor tags from a Wikipedia page
     * @param targetName the name to match against the text of the links
     * @return the full Wikipedia URL of the link whose text most closely resembles the target name,
     *         or {@code null} if no suitable link is found
     */
    private static String findClosestMatch(Elements links, String targetName) {
        String closestMatch = null;
        int minDistance = Integer.MAX_VALUE;

        for (Element link : links) {
            String linkText = link.text().toLowerCase();
            int distance = LevenshteinDistance(linkText, targetName.toLowerCase());
            if (distance < minDistance) {
                minDistance = distance;
                closestMatch = "https://en.wikipedia.org" + link.attr("href");
            }
        }
        return closestMatch;
    }

    // Levenshtein Distance Algorithm
    private static int LevenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            for (int j = 0; j <= b.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j], Math.min(dp[i][j - 1], dp[i - 1][j - 1]));
                }
            }
        }
        return dp[a.length()][b.length()];
    }

    /**
     * Retrieves a Wikipedia link for a holiday in a given country by querying the Wikipedia Search API.
     * <p>
     * This method constructs a search query using the holiday name and (optionally) the country name to improve relevance.
     * It encodes the query, makes an HTTP GET request to the Wikipedia API, and parses the JSON response to find
     * the most relevant Wikipedia article related to the holiday. If a valid response is received and a matching article
     * is found, the corresponding Wikipedia URL is returned. If the holiday name is missing or an error occurs during
     * the API request or response parsing, the method returns {@code "NOT_AVAILABLE"}.
     * </p>
     *
     * @param countryName the name of the country to include in the search for better disambiguation (can be {@code null} or empty)
     * @param holidayName the name of the holiday to search for (must not be {@code null} or empty)
     * @return the URL of the most relevant Wikipedia page for the holiday, or {@code "NOT_AVAILABLE"} if not found or on error
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
                log.error("Error fetching data from Wikipedia API. Response Code: " + responseCode);
                connection.disconnect();
                return "NOT_AVAILABLE";
            }

        } catch (Exception e) {
            log.error("An error occurred: " + e.getMessage());
            return "NOT_AVAILABLE";
        }
    }

    /**
     * Parses the JSON response from the Wikipedia API to find the most relevant link.
     *
     * @param jsonResponse The JSON string from the API.
     * @param holidayName  The original holiday name (for better matching).
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
                        return getURLFromSearchByIndex(searchResults, holidayName, 0); // Start from 0
                    } else {
                        log.info("No Wikipedia results found for: {}", holidayName);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error parsing Wikipedia JSON response: ", e);
        }
        return null;
    }

    /**
     * Recursively searches through Wikipedia API search results to find a page title that contains the specified holiday name.
     * <p>
     * Starting from the given index, this method checks each result's title (case-insensitive) to see if it contains the holiday name.
     * If a match is found, it returns the properly encoded Wikipedia URL for that result. If no match is found by the time
     * the end of the results is reached, the method returns {@code null}.
     * </p>
     *
     * @param searchResults a {@link JSONArray} containing search results from a Wikipedia API response;
     *                      each element is expected to be a JSON object with a "title" field
     * @param holidayName   the holiday name to match against the result titles
     * @param idx           the current index to check within the search results array
     * @return the full Wikipedia URL of the first matching result, or {@code null} if no match is found
     * @throws UnsupportedEncodingException if UTF-8 encoding is not supported (should not occur in standard environments)
     */
    private static String getURLFromSearchByIndex(JSONArray searchResults, String holidayName, int idx) throws UnsupportedEncodingException {

        if (idx >= searchResults.length()) {
            return null; // Base case: index out of bounds, no suitable result found.
        }
        JSONObject result = searchResults.getJSONObject(idx);
        String title = result.getString("title");
        if (title.toLowerCase().contains(holidayName.toLowerCase())) {
            String encodedTitle = URLEncoder.encode(title.replace(" ", "_"), StandardCharsets.UTF_8.toString());
            return "https://en.wikipedia.org/wiki/" + encodedTitle;
        } else {
            return getURLFromSearchByIndex(searchResults, holidayName, idx + 1);
        }
    }
}