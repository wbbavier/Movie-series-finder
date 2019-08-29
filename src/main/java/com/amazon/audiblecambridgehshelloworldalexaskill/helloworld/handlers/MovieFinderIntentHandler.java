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
import java.util.HashMap;
import java.lang.String;

/*import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;
*/

import static com.amazon.ask.request.Predicates.intentName;

public class MovieFinderIntentHandler implements RequestHandler {
    private final String speechTextNoMovieName = "No movies have %s in them.";
    private int numMovies = 0;
    private static Map<String, String> movieList = new HashMap<>();
    private static ArrayList<String> arrList = new ArrayList<>();

    @Override
    public boolean canHandle(HandlerInput handlerInput) {
        return handlerInput.matches(intentName("MovieFinderIntent"));
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
        String initialEndPoint = "http://www.omdbapi.com/?apikey=2d2e9fc7&type=movie&s=";

        String query = "";
        log(input, "Starting request");
        logSlots(input);
        Map<String, Slot> slots = getSlots(input);
        String speechText = "";

        // if we're given a movie name
        if(slots.containsKey("word") && null != slots.get("word").getValue())
        {
            String slotWord = slots.get("word").getValue();
            slotWord = slotWord.replace(" ", "+");
            try {
                String jsonString = getAsString(initialEndPoint, slotWord);
                movieList = simplify(jsonString);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            String movies = showMovies(arrList);
            int limit = previewMovies(numMovies);
            String limitString = Integer.toString(limit);
            if (numMovies > 0 )
            {
                speechText = "I found " + numMovies + " movies with " + slots.get("word").getValue()+ " in them. " +
                        "The first " + limitString + " movies are: " + movies + ". Which movie would you like to hear more about?";
            }
            else
            {
                speechText = "There are no movies with " + slots.get("word").getValue() + " in them";
            }

        }
        else {

        }

        log(input, "Speech text response is " + speechText);
        numMovies = 0;
        movieList.clear();
        arrList.clear();
//        Intent movieDetailsIntent = Intent.builder()
//                .withName("GiveMovieDetailsIntent")
//                .withConfirmationStatus(IntentConfirmationStatus.fromValue("NONE"))
//                .build();`

        // response object with a card (shown on devices with a screen) and speech (what alexa says)
        return input.getResponseBuilder()
                .withSpeech(speechText) // alexa says this
                .withSimpleCard("HelloWorld", speechText)
                .withShouldEndSession(false)
                //.addDelegateDirective(movieDetailsIntent)// alexa will show this on a screen
                .build();

    }

    public static String getAsString(String apiEndpoint, String slot) throws Exception {
        // from " http://www.omdbapi.com/?apikey=2d2e9fc7&t=

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

    /**
     * give me {movieName}
     * let's do {movieName}
     * how about {movieName}
     * {movieName}
     * @param movies
     * @return
     */
    private int previewMovies(int movies) {
        if (movies > 3)
        {
            return 3;
        }
        else
        {
            return movies;
        }
    }


    public String showMovies(ArrayList<String> arrList) {
        String answer = "";
        for (int i = 0; i < previewMovies(numMovies); i ++)
        {
            if (i == previewMovies(numMovies) - 1)
            {
                answer = answer + "and " + arrList.get(i);
            }
            else{
                answer = answer + arrList.get(i) + ", ";
            }
        }
        return answer;
    }

    //want to take JSON string and simplify for easy input into map, as well as update the numMovies to be accurate
    public Map<String, String> simplify(String jsonString) {
        Map<String, String> temp = new HashMap<>();
        JSONObject object = new JSONObject(jsonString);
        JSONArray objectArray = object.getJSONArray("Search");
        //List<Object> arrayObjects = objectArray.toList();
        for (int i = 0; i < objectArray.length(); i++) {
            JSONObject j = objectArray.getJSONObject(i);
            String title = j.getString("Title");
            String imdbID = j.getString("imdbID");
            temp.put(imdbID, title);
            arrList.add(title);
            numMovies++;
        }
        return temp;
    }

    public static String sayMovies(Map<String, String> input)
    {
        String sayThis = "";
        for(Map.Entry<String, String> entry : input.entrySet())
        {
            sayThis = sayThis + ", " + entry.getValue();
        }
        return sayThis;
    }



    public static Map<String, String> getMovieList()
    {
        return movieList;
    }


}
