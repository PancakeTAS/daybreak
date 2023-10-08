package de.pancake.daybreak.listeners;

import de.pancake.daybreak.DaybreakPlugin;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.HashMap;
import java.util.Map;

import static de.pancake.daybreak.DaybreakPlugin.BORDER_RADIUS;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;
import static net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.parsed;

/**
 * Survival listener for the daybreak plugin.
 * @author Pancake
 */
public class SurvivalListener implements Listener {

    /** Prefix for messages */
    public final static TagResolver.Single PREFIX = parsed("prefix", "<gold>»</gold> <red>");

    /** Daybreak plugin instance */
    private final DaybreakPlugin plugin;

    /** Players with spawn protection */
    private final Map<Player, Long> spawnProtection = new HashMap<>();

    /**
     * Initialize survival listener.
     * @param plugin Daybreak plugin instance.
     */
    public SurvivalListener(DaybreakPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Handle player login event.
     * @param e Player login event.
     */
    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent e) {
        if (!this.plugin.isOnline())
            e.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, miniMessage().deserialize("<red>The server is still starting!</red>"));
    }

    /**
     * Handle player join event.
     * @param e Player join event.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        // check if player is already a survivor - return if true
        var player = e.getPlayer();
        if (this.plugin.isSurvivor(player.getUniqueId()))
            return;

        // check if player joined for the first time - spread out if true
        if (player.getGameMode() != GameMode.SURVIVAL || this.plugin.removeLastSessionSurvivor(player.getUniqueId())) {
            player.sendMessage(miniMessage().deserialize("""
                    <prefix><bold>Daybreak</bold>
                    <prefix>Welcome to the server! You've been teleported to a random location.
                    <prefix>
                    <prefix>Daybreak is a <gold>hardcore survival server</gold> <red>that resets every day.</red>
                    <prefix>If you die, you will be <gold>banned</gold> <red>for the rest of the day.</red>
                    <prefix>If you want to preserve your items to the next map,
                    <prefix>you have to survive at least 5 minutes.
                    <prefix>
                    <prefix>Your spawn protection towards other players will expire in 5 minutes.
                    """, PREFIX));

            // add spawn protection
            this.spawnProtection.put(player, System.currentTimeMillis());

            // spread player
            var x = (int) (Math.random() * BORDER_RADIUS * 2) - BORDER_RADIUS;
            var z = (int) (Math.random() * BORDER_RADIUS * 2) - BORDER_RADIUS;
            var location = player.getWorld().getHighestBlockAt(x, z).getLocation().add(0, 1, 0);
            player.setGameMode(GameMode.SURVIVAL);
            player.setHealth(20.0f);
            player.setFallDistance(0f);
            player.teleport(location);
        }

        // add timer for adding player to survivors list
        var login = player.getLastLogin();
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            if (player.getLastLogin() != login)
                return;
            this.plugin.addSurvivor(player.getUniqueId());
            player.sendMessage(miniMessage().deserialize("<prefix>You are now marked as a survivor", PREFIX));
        }, 20L*60*5);
    }

    /**
     * Handle player damage event.
     * @param e Player damage event.
     */
    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player p && e.getDamager() instanceof Player && (System.currentTimeMillis() - this.spawnProtection.getOrDefault(p, 0L)) < 1000*60*5)
            e.setCancelled(true);
    }

    /**
     * Handle player death event.
     * @param e Player death event.
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        this.plugin.kill(e.getPlayer(), e.deathMessage());
    }

}
