package de.pancake.daybreak;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import org.codehaus.plexus.util.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Collectors;

import static de.pancake.daybreak.DaybreakPlugin.LAST_SESSION;

/**
 * Bootstrapper of the plugin
 * @author Pancake
 */
public class DaybreakBootstrap implements PluginBootstrap {

    /** File indicating the server should be reset */
    public static final Path LOCK_FILE = Path.of("reset.lock");

    /**
     * Main method for the server.
     * @param args Arguments for the server.
     * @throws Exception If something goes wrong.
     */
    @Override
    public void bootstrap(@NotNull BootstrapContext context) {
        // check if server should reset
        if (!Files.exists(LOCK_FILE))
            return;

        System.out.println("reset.lock found, resetting server...");

        try {
            // read world data of survivors
            var survivors = Files.readAllLines(LOCK_FILE);
            var stats = survivors.stream().collect(Collectors.toMap(uuid -> uuid, uuid -> tryRead(Path.of("world/stats/" + uuid + ".json"))));
            var playerdata = survivors.stream().collect(Collectors.toMap(uuid -> uuid, uuid -> tryRead(Path.of("world/playerdata/" + uuid + ".dat"))));
            var advancements = survivors.stream().collect(Collectors.toMap(uuid -> uuid, uuid -> tryRead(Path.of("world/advancements/" + uuid + ".json"))));

            // save last session survivors
            LAST_SESSION.addAll(survivors.stream().map(UUID::fromString).toList());

            // recursively delete world
            FileUtils.deleteDirectory(new File("world"));
            Files.delete(Path.of("banned-ips.json"));
            Files.delete(Path.of("banned-players.json"));

            // recreate folder structures
            Files.createDirectories(Path.of("world/stats"));
            Files.createDirectories(Path.of("world/playerdata"));
            Files.createDirectories(Path.of("world/advancements"));

            // write world data of survivors
            stats.forEach((uuid, data) -> tryWrite(Path.of("world/stats/" + uuid + ".json"), data));
            playerdata.forEach((uuid, data) -> tryWrite(Path.of("world/playerdata/" + uuid + ".dat"), data));
            advancements.forEach((uuid, data) -> tryWrite(Path.of("world/advancements/" + uuid + ".json"), data));

            // delete lock file
            Files.delete(LOCK_FILE);
        } catch (Exception e) {
            System.err.println("Failed to reset server!");
            e.printStackTrace();
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
                System.out.println("Warning! File " + path + " does not exist!");
                return new byte[0];
            }

            return Files.readAllBytes(path);
        } catch (Exception e) {
            System.err.println("Failed to read file " + path + "!");
            e.printStackTrace();
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
            System.err.println("Failed to write file " + path + "!");
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
