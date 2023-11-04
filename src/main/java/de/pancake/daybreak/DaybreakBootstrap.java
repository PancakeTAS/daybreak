package de.pancake.daybreak;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.codehaus.plexus.util.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Bootstrapper of the plugin
 * @author Pancake
 */
public class DaybreakBootstrap implements PluginBootstrap {

    /** File indicating the server should be reset */
    public static final Path LOCK_FILE = Path.of("reset.lock");
    /** File listing survivors inbetween resets */
    public static final Path SURVIVORS_FILE = Path.of("survivors.txt");
    /** File listing survivors inbetween resets */
    public static final Path LAST_SESSION_FILE = Path.of("last_survivors.txt");
    /** File storing the crown holders */
    public static final Path CROWNS_FILE = Path.of("crowns.txt");
    /** Has server been reset */
    public static boolean RESET = false;
    /** List of deaths from last reset */
    public static List<UUID> LAST_DEATHS;

    /** The logger of the plugin */
    private ComponentLogger logger;

    /**
     * Bootstraps the plugin.
     * @param context The bootstrap context.
     */
    @Override
    public void bootstrap(@NotNull BootstrapContext context) {
        // check if server should reset
        if (!Files.exists(LOCK_FILE))
            return;

        this.logger = context.getLogger();
        this.logger.info("reset.lock found, resetting server...");

        try {
            // read world data of survivors
            var survivors = Files.readAllLines(LOCK_FILE);
            var stats = survivors.stream().distinct().collect(Collectors.toMap(uuid -> uuid, uuid -> tryRead(Path.of("world/stats/" + uuid + ".json"))));
            var playerdata = survivors.stream().distinct().collect(Collectors.toMap(uuid -> uuid, uuid -> tryRead(Path.of("world/playerdata/" + uuid + ".dat"))));
            var advancements = survivors.stream().distinct().collect(Collectors.toMap(uuid -> uuid, uuid -> tryRead(Path.of("world/advancements/" + uuid + ".json"))));

            // read deaths
            try (var stream = Files.list(Path.of("world/playerdata/"))) {
                LAST_DEATHS = stream
                    .filter(path -> !path.getFileName().toString().contains("dat_old") && !survivors.contains(path.getFileName().toString().split("\\.dat")[0]))
                    .map(c -> UUID.fromString(c.getFileName().toString().split("\\.dat")[0]))
                    .collect(Collectors.toList());
            } catch (Exception e) {
                System.err.println("Unable to find list players");
            }

            // recursively delete world
            FileUtils.deleteDirectory(new File("world"));
            Files.deleteIfExists(Path.of("banned-ips.json"));
            Files.deleteIfExists(Path.of("banned-players.json"));

            // recreate folder structures
            Files.createDirectories(Path.of("world/stats"));
            Files.createDirectories(Path.of("world/playerdata"));
            Files.createDirectories(Path.of("world/advancements"));

            // write world data of survivors
            stats.forEach((uuid, data) -> tryWrite(Path.of("world/stats/" + uuid + ".json"), data));
            playerdata.forEach((uuid, data) -> tryWrite(Path.of("world/playerdata/" + uuid + ".dat"), data));
            advancements.forEach((uuid, data) -> tryWrite(Path.of("world/advancements/" + uuid + ".json"), data));

            // delete lock file
            Files.deleteIfExists(SURVIVORS_FILE);
            Files.move(LOCK_FILE, LAST_SESSION_FILE, StandardCopyOption.REPLACE_EXISTING);

            RESET = true;
        } catch (Exception e) {
            this.logger.error("reset.lock found, resetting server...", e);
            System.exit(-1);
        }
    }

    /**
     * Tries to read all bytes from a given file, crashing the jvm on failure.
     * @param path The path to the file.
     */
    private byte[] tryRead(Path path) {
        try {
            if (!Files.exists(path)) {
                this.logger.info("Warning! File " + path + " does not exist!");
                return new byte[0];
            }

            return Files.readAllBytes(path);
        } catch (Exception e) {
            this.logger.error("Failed to read file " + path + "!", e);
            System.exit(-1);
        }
        return null;
    }

    /**
     * Tries to write a file, crashing the jvm on failure.
     * @param path The path to the file.
     * @param data The data to write.
     */
    private void tryWrite(Path path, byte[] data) {
        try {
            Files.write(path, data);
        } catch (Exception e) {
            this.logger.error("Failed to write file " + path + "!", e);
            System.exit(-1);
        }
    }
}
