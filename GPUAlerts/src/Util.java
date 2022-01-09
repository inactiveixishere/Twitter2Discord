import org.json.JSONObject;

import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Util {

    public static void print(String toPrint) {
        System.out.println(toPrint);
    }

    public static void sendWebhook(String webhookURL, String text[]) {
        DiscordWebhook webhook = new DiscordWebhook(webhookURL);
        webhook.setAvatarUrl(Main.webhookProfilePictureURL);
        webhook.setUsername(Main.webhookProfileUserName);

        DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject();
        embed.setTitle(Main.twitterProfileURL);
        embed.setColor(Color.red);
        embed.setUrl(Main.twitterProfileURL);
        embed.setFooter("developed by ix", "");
        for(String line : text) {
            embed.addField(line, "\u200E", false);
        }

        webhook.addEmbed( embed );

        try {
            webhook.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getJSONData(JSONObject json, String node) {
        return json.getString(node);
    }

    public static String getTime() {
        return DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now());
    }
}
