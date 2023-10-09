package de.pancake.daybreak.webhook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.pancake.daybreak.webhook.data.Embed;
import de.pancake.daybreak.webhook.data.Request;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Utility class for sending webhook messages.
 * @author Pancake
 */
public class WebhookUtil {

    /** The URL of the webhook. */
    private static final URI WEBHOOK_URL = URI.create(System.getenv("DAYBREAK_WEBHOOK")); // definitely didnt leak a test (now deleted) webhook url lol - Cosmic

    /** The GSON instance used to serialize the webhook message. */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** The HTTP client used to send the webhook message. */
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    /** The username and avatar of the webhook. */
    private static final String USERNAME = "Daybreak", AVATAR = "https://cdn.discordapp.com/icons/779452203341447188/1dfa775b9d869bf970a7212a324514e7.webp";

    /**
     * Send an embed message to the webhook.
     * @param message The message to send or null
     * @param embed The embed to send.
     */
    public static void send(String message, Embed... embed) {
        var request = Request.builder().content(message).username(USERNAME).avatar_url(AVATAR).embeds(embed).build();
        HTTP_CLIENT.sendAsync(HttpRequest.newBuilder(WEBHOOK_URL)
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(request))).header("Content-Type", "application/json")
                .build(), HttpResponse.BodyHandlers.discarding());
    }

}
