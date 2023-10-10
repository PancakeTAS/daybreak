package de.pancake.daybreak.webhook;

import com.google.gson.Gson;
import de.pancake.daybreak.DaybreakPlugin;
import de.pancake.daybreak.crowns.Crown;
import de.pancake.daybreak.webhook.data.Embed;
import de.pancake.daybreak.webhook.data.Field;
import de.pancake.daybreak.webhook.data.Footer;
import de.pancake.daybreak.webhook.data.Image;
import org.bukkit.entity.Player;

import java.net.http.HttpClient;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static de.pancake.daybreak.DaybreakBootstrap.LAST_DEATHS;
import static de.pancake.daybreak.DaybreakPlugin.HEADS_KEY;
import static de.pancake.daybreak.DaybreakPlugin.HEADS_TYPE;
import static de.pancake.daybreak.webhook.WebhookUtil.getPlayerName;

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
     * @param c Crown or null
     * @param msg Death message
     */
    public void sendDeathMessage(Player p, Player k, Crown c, String msg) {
        var embed = Embed.builder();

        // grab player head information
        var headCollection = (Map<UUID, Integer>) p.getPersistentDataContainer().getOrDefault(HEADS_KEY, HEADS_TYPE, new HashMap<UUID, Integer>());
        var total = headCollection.entrySet().stream().mapToInt(Map.Entry::getValue).sum();
        var heads = headCollection.entrySet().stream().map(e -> "- " + e.getValue() + "x " + WebhookUtil.getPlayerName(e.getKey())).collect(Collectors.joining("\n"));

        // create base embed
        embed.title(msg.replaceAll("§.", ""))
            .description("""
            %P% will be unbanned %T%.
            
            They've lost %H% player head%HS%:
            %HEADS%"""
                    .replace("%P%", p.getName())
                    .replace("%T%", "<t:"  + LocalDateTime.now(Clock.systemUTC()).plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0).toEpochSecond(ZoneOffset.UTC) + ":R>")
                    .replace("%H%", total + "")
                    .replace("%HS%", total == 1 ? "" : "s")
                    .replace("%HEADS%", heads))
            .color(0x9F23EB)
            .thumbnail(Image.builder().url("https://crafatar.com/avatars/" + p.getUniqueId() + "?overlay").build());


        // add killer footer
        if (k != null)
            if (c == null)
                embed.footer(Footer.builder()
                    .text("Murdered by " + k.getName())
                    .icon_url("https://crafatar.com/avatars/" + k.getUniqueId() + "?overlay").build());
            else
                embed.footer(Footer.builder()
                    .text(k.getName() + " stole the " + c.getType().getName() + " and is now rank " + (c.getType().ordinal()+1) + ".")
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

        // get top 3 players
        var crowns = plugin.crownListener.crowns;
        var first = crowns[0].getHolder() != null ? getPlayerName(crowns[0].getHolder()) : "Unclaimed";
        var second = crowns[1].getHolder() != null ? getPlayerName(crowns[1].getHolder()) : "Unclaimed";
        var third = crowns[2].getHolder() != null ? getPlayerName(crowns[2].getHolder()) : "Unclaimed";

        // create embed
        embed.title("Daybreak has reset!")
            .description("It is now 0:00 UTC and the server has finished resetting the world.")
            .thumbnail(Image.builder().url("https://static.planetminecraft.com/files/image/minecraft/texture-pack/2023/003/16537288-pack_l.webp").build()) // FIXME: fix icon
            .fields(new Field[] {
                    Field.builder()
                            .name("Leaderboard")
                            .value("1. " + first + "\n"
                                    + "2. " + second + "\n"
                                    + "3. " + third)
                            .build(),
                    Field.builder()
                            .name("Survivors")
                            .value(plugin.lastSession.stream().map(WebhookUtil::getPlayerName).collect(Collectors.joining(", ")))
                            .inline(true)
                            .build(),
                    Field.builder()
                            .name("Deaths")
                            .value(LAST_DEATHS.stream().map(WebhookUtil::getPlayerName).collect(Collectors.joining(", ")))
                            .inline(true)
                            .build()
            })
            .footer(Footer.builder().text("Daybreak resets every day at midnight utc").build())
            .color(0xEBDD23)
        ;

        // send webhook
        WebhookUtil.send(null, embed.build());
    }

}
