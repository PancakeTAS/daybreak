package de.pancake.daybreak.listeners;

import de.pancake.daybreak.DaybreakPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import static de.pancake.daybreak.DaybreakPlugin.BORDER_RADIUS;
import static de.pancake.daybreak.DaybreakPlugin.LAST_SESSION;

/**
 * Survival listener for the daybreak plugin.
 * @author Pancake
 */
public class SurvivalListener implements Listener {

    /** Daybreak plugin instance */
    private final DaybreakPlugin plugin;

    /**
     * Initialize survival listener.
     * @param plugin Daybreak plugin instance.
     */
    public SurvivalListener(DaybreakPlugin plugin) {
        this.plugin = plugin;
    }

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
        var player = e.getPlayer();
        e.joinMessage(Component.text("§6» §6" + player.getName() + "§c joined the game"));

        // check if player is already a survivor - return if true
        if (this.plugin.isSurvivor(player.getUniqueId()))
            return;

        // check if player joined for the first time - spread out if true
        if (player.getGameMode() == GameMode.ADVENTURE || LAST_SESSION.contains(player.getUniqueId())) {
            player.sendMessage(Component.text("""
                    §6» §c§lDaybreak
                    §6» §cWelcome to the server! You've been teleported to a random location.
                    §6» §c
                    §6» §cDaybreak is a §6hardcore survival server §cthat resets every day.
                    §6» §cIf you die, you will be §6banned §cfor the rest of the day.
                    §6» §cIf you want to preserve your items to the next map,
                    §6» §cyou have to survive at least 5 minutes.
                    """));

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
     * Handle player join event.
     * @param e Player join event.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        e.quitMessage(Component.text("§6» §6" + e.getPlayer().getName() + "§c left the game"));
    }

    /**
     * Handle player death event.
     * @param e Player death event.
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        var p = e.getPlayer();

        var killer = p.getKiller();
        if (killer == null || killer == p)
            e.deathMessage(Component.text("§6» §6" + e.getPlayer().getName() + "§c died"));
        else
            e.deathMessage(Component.text("§6» §6" + e.getPlayer().getName() + "§c was slain by §b" + killer.getName()));

        this.plugin.kill(p.getUniqueId());
    }

}
