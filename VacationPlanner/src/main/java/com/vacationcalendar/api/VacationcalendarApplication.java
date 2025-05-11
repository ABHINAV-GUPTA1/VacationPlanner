package com.vacationcalendar.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * <p>
 * This is the main application class for the Vacation Calendar application.
 * </p>
 *
 * <p>
 * The {@link SpringBootApplication} annotation indicates that this is a Spring Boot application,
 * and it enables features like auto-configuration, component scanning, and Spring Boot's
 * web support.  The {@link EnableFeignClients} annotation enables the use of Feign clients,
 * which are used for declarative REST client definitions (in this application, to communicate
 * with the Nager.Date API).
 * </p>
 *
 * @author Abhinav Gupta
 */
@SpringBootApplication
@EnableFeignClients
public class VacationcalendarApplication {

	/**
	 * The main method, which is the entry point for the application.
	 * <p>
	 * This method uses {@link SpringApplication#run(Class, String...)} to start the Spring Boot application.
	 * </p>
	 *
	 * @param args Command line arguments passed to the application.
	 */
	public static void main(String[] args) {
		SpringApplication.run(VacationcalendarApplication.class, args);
	}

}
