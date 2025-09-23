package com.example.restservice.greeting;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Date;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;

@RestController
public class GreetingController {

    private static final Logger logger = LogManager.getLogger(GreetingController.class);

	private static final String template = "Hello, %s!";
	private final AtomicLong counter = new AtomicLong();

    @CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping("/greeting")
	public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
	    logger.info("executing the /greeting request");
        recordSiteActivity(name);

	    logger.info("returning a greeting");
		return new Greeting(counter.incrementAndGet(), String.format(template, name));
	}

	public void recordSiteActivity(String name) {

        logger.info("Recording site activity");
        Connection connection = null;

        String databaseUrl = System.getenv("DATABASE_URL");

	    try {
	        logger.info("Connecting to the database");
            connection = DriverManager.getConnection(databaseUrl);

            PreparedStatement insertStatement = connection
                    .prepareStatement("INSERT INTO SiteActivity (Name, ActivityDate) VALUES (?, ?);");

            long millis = System.currentTimeMillis();
            Date currentDateTime = new Date(millis);

	        logger.info("Add site activity to the database");
            insertStatement.setString(1, name);
            insertStatement.setDate(2, currentDateTime);
            insertStatement.executeUpdate();
        }
        catch (Exception e) {
            logger.error("Error occurred while recording site activity:" + e.toString());
        }
        finally {
            try {
                connection.close();
            }
            catch (Exception e){
                logger.error("Error occurred while closing the connection:" + e.toString());
            }
        }
	}
}
