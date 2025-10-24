package com.weather;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles saving and retrieving weather data in Supabase (via Postgres).
 */
public class DatabaseService {

    private final String databaseUrl;
    private final String databaseKey;

    public DatabaseService(String databaseUrl, String databaseKey) {
        this.databaseUrl = databaseUrl;
        this.databaseKey = databaseKey;
    }

    /**
     * Save weather data to Supabase or a mock database.
     */
    public void saveWeatherData(WeatherData data) {
        // For now, we’re mocking the database since Supabase requires REST API or JDBC.
        System.out.println(" Saving weather data:");
        System.out.println("City: " + data.getCityName());
        System.out.println("Temperature: " + data.getTemperature() + "°C");
        System.out.println("Condition: " + data.getDescription());
    }

    /**
     * Retrieve the last few searches (mocked for local use).
     */
    public List<String> getRecentSearches(int limit) {
        // Just return a static mock list — this keeps your GUI functional.
        List<String> history = new ArrayList<>();
        history.add("Mumbai");
        history.add("Delhi");
        history.add("London");
        history.add("New York");
        history.add("Tokyo");
        return history;
    }
}
