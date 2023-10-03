package de.pancake.daybreak.listeners;

import de.pancake.daybreak.DaybreakPlugin;
import net.kyori.adventure.text.Component;
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

/**
 * Survival listener for the daybreak plugin.
 * @author Pancake
 */
public class SurvivalListener implements Listener {

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
            e.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, Component.text("§cThe server is still starting!"));
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
        if (player.getGameMode() == GameMode.ADVENTURE || this.plugin.removeLastSessionSurvivor(player.getUniqueId())) {
            player.sendMessage(Component.text("""
                    §6» §c§lDaybreak
                    §6» §cWelcome to the server! You've been teleported to a random location.
                    §6» §c
                    §6» §cDaybreak is a §6hardcore survival server §cthat resets every day.
                    §6» §cIf you die, you will be §6banned §cfor the rest of the day.
                    §6» §cIf you want to preserve your items to the next map,
                    §6» §cyou have to survive at least 5 minutes.
                    §6» §c
                    §6» §cYour spawn protection towards other players will expire in 5 minutes.
                    """));

            // add spawn protection
            this.spawnProtection.put(player, System.currentTimeMillis());

            // spread player
            var x = (int) (Math.random() * BORDER_RADIUS * 2) - BORDER_RADIUS;
            var z = (int) (Math.random() * BORDER_RADIUS * 2) - BORDER_RADIUS;
            var location = player.getWorld().getHighestBlockAt(x, z).getLocation().add(0, 1, 0);
            player.setGameMode(GameMode.SURVIVAL);
            player.setFallDistance(0f);
            player.teleport(location);
        }

        // add timer for adding player to survivors list
        var login = player.getLastLogin();
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            if (player.getLastLogin() != login)
                return;
            this.plugin.addSurvivor(player.getUniqueId());
            player.sendMessage(Component.text("§6» §cYou are now marked as a survivor"));
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
        this.plugin.kill(e.getPlayer());
    }

}
