package de.pancake.daybreak;

import de.pancake.daybreak.commands.DaybreakCommand;
import de.pancake.daybreak.listeners.SurvivalListener;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.BanEntry;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.popcraft.chunky.api.ChunkyAPI;

import java.nio.file.Files;
import java.util.*;

import static de.pancake.daybreak.DaybreakBootstrap.LOCK_FILE;

/**
 * Main class of the plugin.
 * @author Pancake
 */
public class DaybreakPlugin extends JavaPlugin implements Listener {

    /** Size of the border */
    public static final int BORDER_RADIUS = 512;
    /** Survivors of last session */
    public static final List<UUID> LAST_SESSION = new ArrayList<>();

    /** List of survivors */
    private final List<UUID> survivors = new ArrayList<>();
    /** List of bans in this session */
    private final Map<UUID, BanEntry<?>> bans = new HashMap<>();
    /** Whether the server is online */
    @Getter private boolean online = false;

    /**
     * Enable daybreak plugin
     */
    @Override @SneakyThrows
    public void onEnable() {
        // register commands and listeners
        Bukkit.getCommandMap().register("daybreak", "db", new DaybreakCommand(this));
        Bukkit.getPluginManager().registerEvents(new SurvivalListener(this), this);

        // set world border
        Bukkit.getWorld("world").getWorldBorder().setSize(BORDER_RADIUS * 2);

        // preload world
        var chunky = Bukkit.getServer().getServicesManager().load(ChunkyAPI.class);
        chunky.startTask("world", "square", 0, 0, BORDER_RADIUS, BORDER_RADIUS, "concentric");
        chunky.onGenerationComplete(e -> {
            this.getLogger().info("Chunk generation completed for world");
            this.online = true;
        });
    }

    /**
     * Reset the world and preserve survivors.
     */
    @SneakyThrows
    public void reset() {
        Files.write(LOCK_FILE, this.survivors.stream().map(UUID::toString).toList());
        Bukkit.shutdown();
    }

    /**
     * Revive a player.
     * @param uniqueId The unique id of the player.
     */
    public void revive(UUID uniqueId) {
        var ban = this.bans.remove(uniqueId);
        if (ban != null)
            ban.remove();

        this.addSurvivor(uniqueId);
    }

    /**
     * Kill a player.
     * @param uniqueId The unique id of the player.
     */
    public void kill(UUID uniqueId) {
        this.removeSurvivor(uniqueId);

        var player = Bukkit.getOfflinePlayer(uniqueId);
        if (player.isOp())
            return;

        this.bans.put(uniqueId, player.banPlayer("§cYou died.\n\nYou will be unbanned at 00:00 UTC."));
    }

    // Query survivors list

    /**
     * Check survivors list for a given player.
     * @param uniqueId The unique id of the player.
     * @return True if the player is a survivor, false otherwise.
     */
    public boolean isSurvivor(UUID uniqueId) {
        return this.survivors.contains(uniqueId);
    }

    /**
     * Add a player to the survivors list.
     * @param uniqueId The unique id of the player.
     */
    public void addSurvivor(UUID uniqueId) {
        this.survivors.add(uniqueId);
    }

    /**
     * Remove a player from the survivors list.
     * @param uniqueId The unique id of the player.
     */
    public void removeSurvivor(UUID uniqueId) {
        this.survivors.remove(uniqueId);
    }

}
