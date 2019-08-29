package com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.*;
import org.json.JSONArray;
import org.json.JSONObject;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.lang.String;
import static com.amazon.ask.request.Predicates.intentName;


public class GiveAllMovieDetailsIntentHandler implements RequestHandler {
    private static String year = "";
    private static String director= "";
    private static String awards= "";
    private static String rating= "";
    private static String plot= "";


    public boolean canHandle(HandlerInput handlerInput) {
        return handlerInput.matches(intentName("GiveMovieDetailsIntent"));
    }

    Map<String, Slot> getSlots(HandlerInput input) {
        // this chunk of code gets the slots
        Request request = input.getRequestEnvelope().getRequest();
        IntentRequest intentRequest = (IntentRequest) request;
        Intent intent = intentRequest.getIntent();
        return Collections.unmodifiableMap(intent.getSlots());
    }
    void logSlots(HandlerInput input) {
        Map<String, Slot> slots = getSlots(input);
        // log slot values including request id and time for debugging
        for (String key : slots.keySet()) {
            log(input, String.format("Slot value key=%s, value = %s", key, slots.get(key).toString()));
        }
    }
    void log(HandlerInput input, String message) {
        System.out.printf("[%s] [%s] : %s]\n",
                input.getRequestEnvelope().getRequest().getRequestId().toString(),
                new Date(),
                message);
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        String initialEndPoint = "http://www.omdbapi.com/?apikey=2d2e9fc7&type=movie&t=";
        log(input, "Starting request");
        logSlots(input);

        Request request = input.getRequestEnvelope().getRequest();
        IntentRequest intentRequest = (IntentRequest) request;
        Intent intent = intentRequest.getIntent();
        Map<String, Slot> slots = intent.getSlots();

        String speechText = null;
        //Get the movieName slot


        // if we're given a movie name
        if(slots.containsKey("movieName") && null != slots.get("movieName").getValue())
        {
            String slotWord = slots.get("movieName").getValue();
            slotWord = slotWord.replace(" ", "+");
            try {
                String jsonString = getAsString(initialEndPoint, slotWord);
                simplify(jsonString);

                slotWord = slotWord.replace("+", " ");
                speechText = slotWord + " was released in " + year + " with a metascore rating of " + rating + ", " +
                       "and was directed by " + director + ". " +
                        "Here's a summary: " + plot;
                if (awards != null)
                {
                    speechText += " " + awards.substring(0, awards.indexOf('.') + 1);
                }
                speechText = speechText.replace("&", "and");
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        }
        else
        {
            speechText = "No Moves have %s in them";
        }
        log(input, "Speech text response is " + speechText);
        return input.getResponseBuilder()
                .withSpeech(speechText) // alexa says this
                .build();
    }

    public void simplify(String jsonString) {
        JSONObject object = new JSONObject(jsonString);
        //List<Object> arrayObjects = objectArray.toList();
        System.out.println(object);
            year = object.getString("Year");
            director = object.getString("Director");
            awards = object.getString("Awards");
            rating = object.getString("Metascore");
            plot = object.getString("Plot");

    }

    public static String getAsString(String apiEndpoint, String slot) throws Exception {
        // from http://www.omdbapi.com/?apikey=2d2e9fc7&t=

        String query = apiEndpoint + slot;

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
        return result;
    }

}

