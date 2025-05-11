package com.vacationcalendar.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class for setting up Cross-Origin Resource Sharing (CORS) to allow requests from a specific origin.
 * <p>
 * This class defines a Spring configuration that enables CORS for the application's API endpoints.
 * CORS is a mechanism that allows restricted resources on a web page to be requested from another domain
 * outside the domain from which the first resource was served.  This is necessary when the frontend
 * (e.g., an Angular application) is served from a different domain or port than the backend API.
 * </p>
 * @author Abhinav Gupta
 */
@Configuration
public class Config {
    /**
     * Configures CORS settings for the application.
     * <p>
     * This method defines a {@link WebMvcConfigurer} bean that customizes the CORS behavior.  It uses a
     * {@link CorsRegistry} to map specific API patterns to allowed origins, methods, and headers.
     * </p>
     *
     * @return A {@link WebMvcConfigurer} instance with the defined CORS mappings.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {

            /**
             * Adds CORS mappings for the application's API endpoints.
             * <p>
             * This method overrides the {@link WebMvcConfigurer#addCorsMappings(CorsRegistry)} method
             * to define the CORS rules.  It allows requests from "http://localhost:4200" (typically the origin
             * of an Angular development server) for API endpoints under "/api/**".  The allowed HTTP methods
             * are GET, POST, PUT, DELETE, and OPTIONS.  The "Content-Type" and "Authorization" headers are
             * allowed in the requests.  Additionally, credentials (such as cookies) are allowed.
             * </p>
             *
             * @param registry A {@link CorsRegistry} instance to which the CORS mappings are added.
             */
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**") // Or specify your API endpoint(s)
                        .allowedOrigins("http://localhost:4200") // Allow requests from your Angular app's origin
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Specify allowed HTTP methods
                        .allowedHeaders("Content-Type", "Authorization") // Allow specific headers
                        .allowCredentials(true); // If you're using cookies for authentication
            }
        };
    }
}
