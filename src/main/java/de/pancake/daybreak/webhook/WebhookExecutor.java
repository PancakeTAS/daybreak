package de.pancake.daybreak.webhook;

import com.google.gson.Gson;
import de.pancake.daybreak.DaybreakPlugin;
import de.pancake.daybreak.webhook.data.Embed;
import de.pancake.daybreak.webhook.data.Field;
import de.pancake.daybreak.webhook.data.Footer;
import de.pancake.daybreak.webhook.data.Image;
import lombok.SneakyThrows;
import org.bukkit.entity.Player;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static de.pancake.daybreak.DaybreakBootstrap.LAST_DEATHS;

/**
 * This class is used to execute the webhooks.
 * @author Pancake
 */
public class WebhookExecutor {

    /** Gson instance */
    private static final Gson GSON = new Gson();

    /** Http client instance */
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    /**
     * Send death message to webhook.
     * @param p Player that died
     * @param k Killer or null
     * @param msg Death message
     */
    public void sendDeathMessage(Player p, Player k, String msg) {
        var embed = Embed.builder();

        // create base embed
        embed.title(msg.replaceAll("§.", ""))
            .description("""
            %P% will be unbanned %T%."""
                    .replace("%P%", p.getName())
                    .replace("%T%", "<t:"  + LocalDateTime.now(Clock.systemUTC()).plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0).toEpochSecond(ZoneOffset.UTC) + ":R>"))
            .color(0x9F23EB)
            .thumbnail(Image.builder().url("https://crafatar.com/avatars/" + p.getUniqueId() + "?overlay").build());

        // add killer footer
        if (k != null)
            embed.footer(Footer.builder()
                .text("Murdered by " + k.getName())
                .icon_url("https://crafatar.com/avatars/" + k.getUniqueId() + "?overlay").build());

        // send webhook
        WebhookUtil.send(null, embed.build());
    }

    /**
     * Send reset message to webhook.
     * @param plugin Daybreak plugin instance
     */
    public void sendResetMessage(DaybreakPlugin plugin) {
        var embed = Embed.builder();

        // create embed
        embed.title("Daybreak has reset!")
            .description("It is now 0:00 UTC and the server has finished resetting the world.")
            .thumbnail(Image.builder().url("https://static.planetminecraft.com/files/image/minecraft/texture-pack/2023/003/16537288-pack_l.webp").build()) // FIXME: fix icon
            .fields(new Field[] {
                    Field.builder()
                            .name("Survivors")
                            .value(plugin.lastSession.stream().map(WebhookExecutor::getPlayerName).collect(Collectors.joining(", ")))
                            .inline(true)
                            .build(),
                    Field.builder()
                            .name("Deaths")
                            .value(LAST_DEATHS.stream().map(WebhookExecutor::getPlayerName).collect(Collectors.joining(", ")))
                            .inline(true)
                            .build()
            })
            .footer(Footer.builder().text("Daybreak resets every day at midnight utc").build())
            .color(0xEBDD23)
        ;

        // send webhook
        WebhookUtil.send(null, embed.build());
    }

    /**
     * Get player name from UUID
     * @param uuid UUID of player
     * @return Player name
     */
    @SneakyThrows
    private static String getPlayerName(UUID uuid) {
        var list = GSON.fromJson(HTTP_CLIENT.send(HttpRequest.newBuilder().GET().uri(URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString())).build(), HttpResponse.BodyHandlers.ofString()).body(), Map.class);
        return (String) list.get("name");
    }

}
