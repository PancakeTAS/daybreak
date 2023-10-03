package de.pancake.daybreak;

import de.pancake.daybreak.commands.DaybreakCommand;
import de.pancake.daybreak.generators.VanillaGenerator;
import de.pancake.daybreak.listeners.CombatListener;
import de.pancake.daybreak.listeners.MiscListener;
import de.pancake.daybreak.listeners.SurvivalListener;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.popcraft.chunky.api.ChunkyAPI;

import java.nio.file.Files;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static de.pancake.daybreak.DaybreakBootstrap.LAST_SESSION_FILE;
import static de.pancake.daybreak.DaybreakBootstrap.SURVIVORS_FILE;

/**
 * Main class of the plugin.
 * @author Pancake
 */
public class DaybreakPlugin extends JavaPlugin implements Listener {

    /** Size of the border */
    public static final int BORDER_RADIUS = 512;

    /** List of survivors */
    private final List<UUID> survivors = new LinkedList<>();
    /** List of survivors from last session */
    public final List<UUID> lastSession = new LinkedList<>();
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
        Bukkit.getPluginManager().registerEvents(new MiscListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CombatListener(this), this);

        // load survivors
        if (Files.exists(SURVIVORS_FILE))
            this.survivors.addAll(Files.readAllLines(SURVIVORS_FILE).stream().map(UUID::fromString).distinct().toList());
        this.getSLF4JLogger().info("In this session, there are " + this.survivors.size() + " survivors:\n    " + this.survivors.stream().map(Object::toString).collect(Collectors.joining("\n    ")));

        // load last session survivors
        if (Files.exists(LAST_SESSION_FILE))
            this.lastSession.addAll(Files.readAllLines(LAST_SESSION_FILE).stream().map(UUID::fromString).distinct().toList());
        this.getSLF4JLogger().info("From the previous session, there are " + this.lastSession.size() + " survivors that have yet to join:\n    " + this.lastSession.stream().map(Object::toString).collect(Collectors.joining("\n    ")));

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
     * Disable daybreak plugin
     */
    @Override @SneakyThrows
    public void onDisable() {
        // save survivors
        Files.write(SURVIVORS_FILE, this.survivors.stream().map(UUID::toString).distinct().toList());
        Files.write(LAST_SESSION_FILE, this.lastSession.stream().map(UUID::toString).distinct().toList());
    }

    /**
     * Initialize world.
     * @param w World that was initialized
     */
    public void onChunkyInit(World w) {
        // set world border
        w.getWorldBorder().setSize(BORDER_RADIUS * 2);

        // preload world
        var chunky = Bukkit.getServer().getServicesManager().load(ChunkyAPI.class);
        chunky.startTask("world", "square", 0, 0, BORDER_RADIUS + (16*16), BORDER_RADIUS + (16*16), "concentric");
        chunky.onGenerationComplete(e -> {
            this.getLogger().info("Chunk generation completed for world");
            this.online = true;
        });
    }

    /**
     * Replace default world generator with custom one.
     * @param worldName Name of the world that this will be applied to
     * @param id Unique ID, if any, that was specified to indicate which generator was requested
     * @return New vanilla generator
     */
    @Override
    public @Nullable ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, @Nullable String id) {
        return new VanillaGenerator();
    }

    /**
     * Reset the world and preserve survivors.
     */
    @SneakyThrows
    public void reset() {
        Files.write(SURVIVORS_FILE, this.survivors.stream().map(UUID::toString).toList()); // write survivors of this world to file
        Files.write(LAST_SESSION_FILE, this.lastSession.stream().map(UUID::toString).toList()); // write survivors of previous world, that haven't joined yet, to file
        Bukkit.shutdown();
    }

    /**
     * Kill a player.
     * @param uniqueId The unique id of the player.
     */
    public void kill(Player p) {
        if (p.isOp())
            return;

        this.removeSurvivor(p.getUniqueId());
        p.setGameMode(GameMode.SPECTATOR);
        p.setHealth(0.0);
        p.getInventory().clear();
        p.setExp(0.0f);
        p.banPlayer("§cYou died.\n\nYou will be unbanned at 00:00 UTC.");
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
        if (!this.survivors.contains(uniqueId))
            this.survivors.add(uniqueId);
    }

    /**
     * Remove a player from the survivors list.
     * @param uniqueId The unique id of the player.
     */
    public void removeSurvivor(UUID uniqueId) {
        this.survivors.remove(uniqueId);
    }

    /**
     * Remove a player from the survivors list if they joined for the first time.
     * @param uniqueId The unique id of the player.
     * @return True if the player joined for the first time, false otherwise.
     */
    public boolean removeLastSessionSurvivor(UUID uniqueId) {
        return this.lastSession.remove(uniqueId);
    }

}
