package de.pancake.daybreak.listeners;

import de.pancake.daybreak.DaybreakPlugin;
import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.SneakyThrows;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginEnableEvent;

import java.nio.file.Files;
import java.util.UUID;

import static de.pancake.daybreak.DaybreakPlugin.PREFIX;
import static de.pancake.daybreak.features.crowns.CrownManager.CROWNS_FILE;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;
import static net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.*;
import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection;

/**
 * Misc listener for the daybreak plugin.
 * @author Pancake
 */
public class MiscListener implements Listener {

    /** Daybreak plugin instance */
    private final DaybreakPlugin plugin;

    /**
     * Initialize misc listener.
     * @param plugin Daybreak plugin instance.
     */
    public MiscListener(DaybreakPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Handle player join event.
     * @param e Player join event.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        e.joinMessage(miniMessage().deserialize("<prefix><gold><player></gold> <red>joined the game</red>", PREFIX, unparsed("player", e.getPlayer().getName())));
        e.getPlayer().sendPlayerListHeaderAndFooter(miniMessage().deserialize("<red><bold>Daybreak</bold></red>"), miniMessage().deserialize("<gray>The hardcore Minecraft server, which resets every 24 hours</gray>"));
    }

    /**
     * Handle player quit event.
     * @param e Player quit event.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        e.quitMessage(null);
        var p = e.getPlayer();
        if (p.getGameMode() == GameMode.SPECTATOR || p.isDead() || p.getHealth() < 0.01)
            return;

        e.quitMessage(miniMessage().deserialize("<prefix><gold><player></gold> <red>left the game</red>", PREFIX, unparsed("player", p.getName())));
    }

    /**
     * Handle player chat event.
     * @param e Player chat event.
     */
    @EventHandler
    public void onPlayerChat(AsyncChatEvent e) {
        e.setCancelled(true);
        Bukkit.broadcast(miniMessage().deserialize("<gold><player></gold> <red>»</red> <gray><message></gray>", unparsed("player", e.getPlayer().getName()), component("message", e.originalMessage().color(NamedTextColor.GRAY))));
    }

    /**
     * Handle plugin enable event.
     * @param e Plugin enable event.
     */
    @EventHandler
    public void onPluginLoad(PluginEnableEvent e) {
        if (e.getPlugin().getName().equals("Chunky"))
            this.plugin.onChunkyInit(Bukkit.getWorld("world"));
    }

    /**
     * Handle inventory click event.
     * @param e Inventory click event.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        var item = e.getInventory().getItem(0);
        if (!e.getWhoClicked().isOp() && item != null && item.getType() == Material.PLAYER_HEAD)
            e.setCancelled(true);
    }

    /**
     * Handle player death event.
     * @param e Player death event.
     */
    @EventHandler(priority = EventPriority.HIGH) @SneakyThrows // run after other event handlers
    public void onPlayerDeath(PlayerDeathEvent e) {
        var p = e.getPlayer();
        var killer = p.getKiller();

        var omsg = legacySection().serialize(e.deathMessage());
        var msg = omsg.replace(p.getName(), "<gold>" + p.getName() + "</gold><red>");

        if (killer != null && killer != p)
            msg = msg.replace(killer.getName(), "<gold>" + killer.getName() + "</gold><red>");

        if (p.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.CUSTOM)
            msg = "<gold>" + p.getName() + "</gold> <red>logged out during combat!";

        e.deathMessage(miniMessage().deserialize("<prefix><msg>", PREFIX, parsed("msg", msg)));

        // grab crown holders
        UUID goldenCrownHolder = null;
        UUID silverCrownHolder = null;
        UUID bronzeCrownHolder = null;

        if (Files.exists(CROWNS_FILE)) {
            var crownHolders = Files.readAllLines(CROWNS_FILE);
            goldenCrownHolder = crownHolders.get(0).equals("null") ? null : UUID.fromString(crownHolders.get(0));
            silverCrownHolder = crownHolders.get(1).equals("null") ? null : UUID.fromString(crownHolders.get(1));
            bronzeCrownHolder = crownHolders.get(2).equals("null") ? null : UUID.fromString(crownHolders.get(2));
        }

        this.plugin.webhookExecutor.sendDeathMessage(p, killer, omsg, goldenCrownHolder, silverCrownHolder, bronzeCrownHolder);
    }

}
