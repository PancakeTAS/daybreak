package de.pancake.daybreak.listeners;

import de.pancake.daybreak.DaybreakPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static de.pancake.daybreak.DaybreakPlugin.BORDER_SIZE;
import static de.pancake.daybreak.DaybreakPlugin.TODAY;

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

        // check if player died in this session - kick if true
        if (player.getGameMode() == GameMode.SPECTATOR && !player.isOp()) {
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> player.kick(Component.text("§cYou have died in this session already!")), 10L);
            return;
        }

        // check if player joined for the first time - spread out if true
        if (player.getGameMode() == GameMode.ADVENTURE || ChronoUnit.DAYS.between(Instant.ofEpochMilli(0), Instant.ofEpochMilli(player.getLastSeen())) < TODAY) { // TODO: check if this works
            player.sendMessage(Component.text("§6» §cWelcome to the server! You've been spread out to a random location.")); // TODO: send more information to the player

            var x = (int) (Math.random() * BORDER_SIZE) - BORDER_SIZE / 2;
            var z = (int) (Math.random() * BORDER_SIZE) - BORDER_SIZE / 2;
            var location = player.getWorld().getHighestBlockAt(x, z).getLocation().add(0, 1, 0);
            player.setGameMode(GameMode.SURVIVAL);
            player.setFallDistance(0f);
            player.teleport(location);
            return;
        }

        // add timer for adding player to survivors list
        // TODO: add timer
    }

}
