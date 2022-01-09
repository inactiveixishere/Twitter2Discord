import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import java.awt.Color;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;

public class Main {

    static String apiKey, apiKeySecret, apiBearerToken,
            twitterProfileURL, twitterProfileID, webhookProfilePictureURL, webhookProfileUserName;

    static String webhookURLS[];

    static int updateIntervalMS = 0;

    static HashMap<String, String> twitterPosts = new HashMap<String, String>();

    public static void main(String[] args) throws IOException, ParseException, InterruptedException {
        if (!initConfig()) {
            Util.print("Error In Config");
            System.exit(1);
        }
        Util.print("Config Loaded Successfully");

        JSONArray lastPosts = getLastTwitterPostsJSON();
        for(Object lastPost : lastPosts) {
            if (lastPost != null) {
                String ID = Util.getJSONData((JSONObject) lastPost, "id");
                String text = Util.getJSONData((JSONObject) lastPost, "text").replace("#", "");

                if (!twitterPosts.containsKey(ID)) {
                    twitterPosts.put(ID, text);
                }
            }
        }
        Util.print("Input existing posts into hashmap");

        for(;;){
            lastPosts = getLastTwitterPostsJSON();
            if(lastPosts != null) {
                for (Object lastPost : lastPosts) {
                    String ID = Util.getJSONData((JSONObject) lastPost, "id");
                    String text = Util.getJSONData((JSONObject) lastPost, "text").replace("#", "");

                    if (!twitterPosts.containsKey(ID)) {
                        for(String webhookURL : webhookURLS) {
                            Util.sendWebhook(webhookURL, text.split("\\n"));
                            Util.print("Sent Webhook to URL -> " + webhookURL);
                            Thread.sleep(300); // Delay for webhook rate limit
                        }
                        twitterPosts.put(ID, text);
                        Util.print(Util.getTime() + " -> Put: " + ID + " into hashmap");
                    }
                }
            }
            Thread.sleep(updateIntervalMS);
        }
    }

    public static JSONArray getLastTwitterPostsJSON() throws IOException, ParseException {
        URL url = new URL("https://api.twitter.com/2/users/" + twitterProfileID + "/tweets");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + apiBearerToken);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String output;

        StringBuffer response = new StringBuffer();
        while ((output = in.readLine()) != null) {
            response.append(output);
        }
        in.close();

        JSONObject obj = new JSONObject(response.toString());

        if(response.toString().contains("data")) {
            JSONArray arr = obj.getJSONArray("data");
            return arr;
        }

        return null;
    }

    public static boolean initConfig() {
        try {
            File myObj = new File("config.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String line = myReader.nextLine();

                if(line.contains("=")) {
                    String[] split = line.split("=");
                    if(split.length == 1) {
                        Util.print("Error in config file.");
                        Util.print("Delete config file to regenerate or fix the error.");
                        System.exit(1);
                    }
                    switch (split[0]) {
                        case "ApiKey" -> apiKey = split[1];
                        case "ApiKeySecret" -> apiKeySecret = split[1];
                        case "ApiBearerToken" -> apiBearerToken = split[1];
                        case "TwitterProfileURL" -> twitterProfileURL = split[1];
                        case "TwitterProfileID" -> twitterProfileID = split[1];
                        case "WebhookURLS" -> webhookURLS = split[1].replace("[", "").replace("]", "").split(",");
                        case "WebhookProfilePictureURL" -> webhookProfilePictureURL = split[1];
                        case "WebhookProfileUserName" -> webhookProfileUserName = split[1];
                        case "UpdateIntervalMS" -> updateIntervalMS = Integer.parseInt(split[1]);
                        default -> { }
                    }
                }
            }
            myReader.close();

            return true;
        } catch (FileNotFoundException e) {
            try {
                File myObj = new File("config.txt");
                if (myObj.createNewFile()) {
                    FileWriter myWriter = new FileWriter("config.txt");
                    myWriter.write(ConfigFile.configText);
                    myWriter.close();
                    Util.print("Created Config File: " + myObj.getName());
                    Util.print("To continue, modify config.txt");
                    System.exit(1);
                }
                return false;
            } catch (IOException ee) {
                Util.print("Error occurred in config.");
                return false;
            }
        }
    }
}
