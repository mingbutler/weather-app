import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.lang.invoke.StringConcatException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class WeatherApp {
    public static JSONObject getWeatherData(String locationName) {
        JSONArray locationData = getLocationData(locationName);

        JSONObject location = (JSONObject) locationData.get(0);
        double latitude = (double) location.get("latitude");
        double longitude = (double) location.get("longitude");

        String urlString = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=" + latitude +"&longitude=" + -longitude +
                "&hourly=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&temperature_unit=fahrenheit&wind_speed_unit=mph&timezone=America%2FNew_York";

        try {
            HttpURLConnection conn = fetchApiResponse(urlString);

            //200 = successful
            if (conn.getResponseCode() != 200) {
                System.out.println("Could not connect to API");
                return null;
            } else {
                StringBuilder resultJson = new StringBuilder();
                Scanner sc = new Scanner(conn.getInputStream());
                while(sc.hasNext()) {
                    resultJson.append(sc.nextLine());
                }

                sc.close();

                conn.disconnect();

                JSONParser parser = new JSONParser();
                JSONObject resultsJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

                JSONObject hourly = (JSONObject) resultsJsonObj.get("hourly");

                JSONArray time = (JSONArray) hourly.get("time");
                int index = findIndexOfCurrentTime(time);

                JSONArray tempData = (JSONArray) hourly.get("temperature_2m");
                double temp = (double) tempData.get(index);

                JSONArray weatherCode = (JSONArray) hourly.get("weather_code");
                String weatherCondition = convertWeatherCode((long) weatherCode.get(index));

                JSONArray relativeHumidity = (JSONArray) hourly.get("relative_humidity_2m");
                long humidity = (long) relativeHumidity.get(index);

                JSONArray windSpeedData = (JSONArray) hourly.get("wind_speed_10m");
                double windSpeed = (double) windSpeedData.get(index);

                JSONObject weatherData = new JSONObject();
                weatherData.put("temp", temp);
                weatherData.put("weatherCondition", weatherCondition);
                weatherData.put("humidity", humidity);
                weatherData.put("windSpeed", windSpeed);

                return weatherData;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONArray getLocationData(String locationName) {
        locationName = locationName.replaceAll(" ","+");

        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" +
                locationName + "&count=100&language=en&format=json";

        try {
            HttpURLConnection conn = fetchApiResponse(urlString);

            //200 = successful
            if (conn.getResponseCode() != 200) {
                System.out.println("Could not connect to API");
                return null;
            } else {
                StringBuilder resultJson = new StringBuilder();
                Scanner sc = new Scanner(conn.getInputStream());

                while(sc.hasNext()) {
                    resultJson.append(sc.nextLine());
                }

                sc.close();

                conn.disconnect();

                JSONParser parser = new JSONParser();
                JSONObject resultsJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

                JSONArray locationData = (JSONArray) resultsJsonObj.get("results");
                return locationData;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static HttpURLConnection fetchApiResponse(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection)  url.openConnection();

            conn.setRequestMethod("GET");

            conn.connect();
            return conn;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static int findIndexOfCurrentTime(JSONArray timeList) {
        String currentTime = getCurrentTime();

        for(int i=0; i<timeList.size(); i++) {
            String time = (String) timeList.get(i);
            if (time.equalsIgnoreCase(currentTime)) {
                return i;
            }
        }
        return 0;
    }

    public static String getCurrentTime() {
        LocalDateTime currentDateTime = LocalDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");

        String formattedDateTime = currentDateTime.format(formatter);

        return formattedDateTime;
    }

    private static String convertWeatherCode(long weatherCode) {
        String weatherCondition = "";
        if(weatherCode == 0L) {
            weatherCondition = "Clear";
        }else if(weatherCode >0L && weatherCode <= 3L) {
            weatherCondition = "Cloudy";
        }else if((weatherCode >= 51L && weatherCode <= 67L) || (weatherCode >= 80L && weatherCode <= 99L)) {
            weatherCondition = "Rain";
        }else if (weatherCode >= 71L && weatherCode <= 77L) {
            weatherCondition = "Snow";
        }

        return weatherCondition;
    }
}
