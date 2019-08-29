package com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.handlers;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MovieFinderTest {
    private static String year;
    private static String director;
    private static String awards;
    private static String rating;
    private static String plot;


    private MovieFinderIntentHandler handler;

    /**
     * This is called before each test.  Do shared setup work here.
     * @throws Exception
     */
    @org.junit.Before // this annotation makes this method run before each test
    public void setUp() throws Exception {
        handler = new MovieFinderIntentHandler();
    }

    @org.junit.Test // same, this is a test
    public void handle() throws Exception {
        // from https://www.mbta.com/developers/v3-api
        //String apiEndpoint = "http://www.omdbapi.com/?apikey=2d2e9fc7&t=";
        String apiEndpoint =  "http://www.omdbapi.com/?apikey=2d2e9fc7&type=movie&t=";
        String batman = "batman begins";
        batman = batman.replace(" ", "+");
        System.out.println(batman);
        String query = apiEndpoint + "batman+begins";


        // lets get some data from it
        HttpURLConnection urlc = (HttpURLConnection) new URL(query).openConnection();

        urlc.setRequestMethod("GET");
        urlc.setRequestProperty("Accept", "application/json");

        BufferedReader br = new BufferedReader(new InputStreamReader(
                (urlc.getInputStream())));

        StringBuilder sb = new StringBuilder();
        String output;
        System.out.println("Output from Server .... \n");
        while ((output = br.readLine()) != null) {
            sb.append(output);
        }

        String result = sb.toString();
        urlc.disconnect();

        //System.out.println(result);
        simplify(result);

    }

    public void simplify(String jsonString) {
        JSONObject object = new JSONObject(jsonString);
        System.out.println(object);
        year = object.getString("Year");
        director = object.getString("Director");
        awards = object.getString("Awards");
        rating = object.getString("Metascore");
        plot = object.getString("Plot");

       System.out.println(year);
    }
}