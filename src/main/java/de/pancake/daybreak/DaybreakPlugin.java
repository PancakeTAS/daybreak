package de.pancake.daybreak;

import de.pancake.daybreak.commands.DaybreakCommand;
import de.pancake.daybreak.listeners.SurvivalListener;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.popcraft.chunky.api.ChunkyAPI;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Main class of the plugin.
 * @author Pancake
 */
public class DaybreakPlugin extends JavaPlugin implements Listener {

    /** Size of the border */
    public static final int BORDER_RADIUS = 512;
    /** Today's date */
    public static final long TODAY = ChronoUnit.DAYS.between(LocalDate.ofEpochDay(0), LocalDateTime.now(Clock.systemUTC()));

    /** List of survivors */
    private final List<UUID> survivors = new ArrayList<>();
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

    public void reset() {

    }

    public void revive(UUID uniqueId) {

    }

    public void kill(UUID uniqueId) {

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
