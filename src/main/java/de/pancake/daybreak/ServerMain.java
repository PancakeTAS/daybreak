package de.pancake.daybreak;

import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

/**
 * Main class for the server. This overrides org.bukkit.craftbukkit.Main!
 * @author Pancake
 */
public class ServerMain {

    /** File indicating the server should be reset */
    public static final Path LOCK_FILE = Path.of("reset.lock");

    /**
     * Main method for the server.
     * @param args Arguments for the server.
     * @throws Exception If something goes wrong.
     */
    public static void main(String[] args) throws Exception {
        // check if server should reset
        if (Files.exists(LOCK_FILE)) {
            System.out.println("reset.lock found, resetting server...");

            // read world data of survivors
            var survivors = Files.readAllLines(LOCK_FILE);
            var stats = survivors.stream().collect(Collectors.toMap(uuid -> uuid, uuid -> tryRead(Path.of("world/stats/" + uuid + ".json"))));
            var playerdata = survivors.stream().collect(Collectors.toMap(uuid -> uuid, uuid -> tryRead(Path.of("world/playerdata/" + uuid + ".dat"))));
            var advancements = survivors.stream().collect(Collectors.toMap(uuid -> uuid, uuid -> tryRead(Path.of("world/advancements/" + uuid + ".json"))));

            // recursively delete world
            FileUtils.deleteDirectory(new File("world"));

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
        }

        // launch server main
        Class.forName("org.bukkit.craftbukkit.Main").getMethod("main", String[].class).invoke(null, (Object) args);
    }

    /**
     * Tries to read all bytes from a given file, crashing the jvm on failure.
     * @param path The path to the file.
     */
    private static byte[] tryRead(Path path) {
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
    private static void tryWrite(Path path, byte[] data) {
        try {
            Files.write(path, data);
        } catch (Exception e) {
            System.err.println("Failed to write file " + path + "!");
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
