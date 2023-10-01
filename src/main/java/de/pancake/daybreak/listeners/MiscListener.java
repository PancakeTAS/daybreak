package de.pancake.daybreak.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Misc listener for the daybreak plugin.
 * @author Pancake
 */
public class MiscListener implements Listener {

    /**
     * Handle player join event.
     * @param e Player join event.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        e.joinMessage(Component.text("§6» §6" + e.getPlayer().getName() + "§c joined the game"));
    }

    /**
     * Handle player quit event.
     * @param e Player quit event.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        e.quitMessage(Component.text("§6» §6" + e.getPlayer().getName() + "§c left the game"));
    }

    /**
     * Handle player chat event.
     * @param e Player chat event.
     */
    @EventHandler
    public void onPlayerChat(AsyncChatEvent e) {
        e.setCancelled(true);
        Bukkit.broadcast(Component.text("§6" + e.getPlayer().getName() + " §c» ").append(e.originalMessage().color(NamedTextColor.GRAY)));
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
            e.deathMessage(Component.text("§6» §6" + e.getPlayer().getName() + "§c was slain by §6" + killer.getName()));
    }

}
