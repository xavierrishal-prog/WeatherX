package com.weather;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class WeatherService {
    private static final String API_BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private String apiKey;

    public WeatherService(String apiKey) {
        this.apiKey = apiKey;
    }

    public WeatherData getWeather(String city) throws Exception {
        // URL encode the city name to handle spaces and special characters
        String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8.toString());
        String url = API_BASE_URL + "?q=" + encodedCity + "&appid=" + apiKey + "&units=metric";

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());

                if (statusCode != 200) {
                    JSONObject errorJson = new JSONObject(responseBody);
                    throw new Exception("API Error: " + errorJson.optString("message", "Unknown error"));
                }

                return parseWeatherData(responseBody);
            }
        }
    }

    private WeatherData parseWeatherData(String jsonResponse) {
        JSONObject json = new JSONObject(jsonResponse);
        WeatherData data = new WeatherData();

        data.setCityName(json.getString("name"));
        data.setCountry(json.getJSONObject("sys").optString("country", ""));

        JSONObject main = json.getJSONObject("main");
        data.setTemperature(main.getDouble("temp"));
        data.setFeelsLike(main.getDouble("feels_like"));
        data.setTempMin(main.getDouble("temp_min"));
        data.setTempMax(main.getDouble("temp_max"));
        data.setPressure(main.getInt("pressure"));
        data.setHumidity(main.getInt("humidity"));

        JSONArray weatherArray = json.getJSONArray("weather");
        if (weatherArray.length() > 0) {
            JSONObject weather = weatherArray.getJSONObject(0);
            data.setDescription(weather.getString("description"));
            data.setIcon(weather.getString("icon"));
        }

        JSONObject wind = json.getJSONObject("wind");
        data.setWindSpeed(wind.getDouble("speed"));

        return data;
    }
}