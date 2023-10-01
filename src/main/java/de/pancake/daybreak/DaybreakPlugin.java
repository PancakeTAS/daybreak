package de.pancake.daybreak;

import de.pancake.daybreak.commands.DaybreakCommand;
import de.pancake.daybreak.listeners.CombatListener;
import de.pancake.daybreak.listeners.MiscListener;
import de.pancake.daybreak.listeners.SurvivalListener;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import org.bukkit.BanEntry;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.popcraft.chunky.api.ChunkyAPI;

import java.nio.file.Files;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
        Bukkit.getPluginManager().registerEvents(new MiscListener(), this);
        Bukkit.getPluginManager().registerEvents(new CombatListener(this), this);

        // set world border
        Bukkit.getWorld("world").getWorldBorder().setSize(BORDER_RADIUS * 2);

        // preload world
        var chunky = Bukkit.getServer().getServicesManager().load(ChunkyAPI.class);
        chunky.startTask("world", "square", 0, 0, BORDER_RADIUS, BORDER_RADIUS, "concentric");
        chunky.onGenerationComplete(e -> {
            this.getLogger().info("Chunk generation completed for world");
            this.online = true;
        });

        // create automatic reset task
        var executor = Executors.newScheduledThreadPool(4);
        var now = LocalDateTime.now(Clock.systemUTC());
        var secondsUntilMidnight = now.until(now.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0), ChronoUnit.SECONDS);
        executor.schedule(() -> Bukkit.broadcast(Component.text("§6» §cThe server will reset in 5 minutes.")), Math.max(1, secondsUntilMidnight - 60*5), TimeUnit.SECONDS);
        executor.schedule(() -> Bukkit.broadcast(Component.text("§6» §cThe server will reset in 60 seconds.")), Math.max(1, secondsUntilMidnight - 60), TimeUnit.SECONDS);
        executor.schedule(() -> Bukkit.broadcast(Component.text("§6» §cThe server will reset in 30 seconds.")), Math.max(1, secondsUntilMidnight - 30), TimeUnit.SECONDS);
        executor.schedule(() -> Bukkit.broadcast(Component.text("§6» §cThe server will reset in 5 seconds.")), Math.max(1, secondsUntilMidnight - 5), TimeUnit.SECONDS);
        executor.schedule(() -> Bukkit.getScheduler().runTask(this, this::reset), secondsUntilMidnight, TimeUnit.SECONDS);
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
