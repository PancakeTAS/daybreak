package gay.pancake.daybreak;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import lombok.SneakyThrows;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Loader of the plugin
 * @author Pancake
 */
public class DaybreakLoader implements PluginLoader {

    private static final Path
            SERVER_PROPERTIES = Path.of("server.properties"),
            BUKKIT_YML = Path.of("bukkit.yml"),
            SPIGOT_YML = Path.of("spigot.yml"),
            PAPER_GLOBAL_YML = Path.of("config/paper-global.yml"),
            PAPER_WORLD_YML = Path.of("config/paper-world-defaults.yml");

    /**
     * Loads the plugin
     * @param classpathBuilder The classpath builder.
     */
    @Override @SneakyThrows
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        var logger = classpathBuilder.getContext().getLogger();

        /* check server.properties */
        logger.info("Checking server.properties...");
        var properties = new Properties();
        properties.load(Files.newBufferedReader(SERVER_PROPERTIES));
        // allow-nether: false
        if (!properties.getProperty("allow-nether").equals("false"))
            logger.error("allow-nether: false in server.properties: Nether is enabled!");
        // difficulty: hard
        if (!properties.getProperty("difficulty").equals("hard"))
            logger.warn("difficulty: hard in server.properties: Difficulty is not set to hard, this is not recommended for Daybreak!");
        // gamemode: adventure
        if (!properties.getProperty("gamemode").equals("adventure"))
            logger.error("gamemode: adventure in server.properties: Gamemode is not set to adventure!");
        // hardcore: true
        if (!properties.getProperty("hardcore").equals("true"))
            logger.error("hardcore: true in server.properties: Hardcore is not enabled!");
        // view-distance <= 16
        if (Integer.parseInt(properties.getProperty("view-distance")) > 16)
            logger.warn("view-distance <= 16 in server.properties: View distance is above 16, this is not recommended for Daybreak!");
        // simulation-distance: 10
        if (!properties.getProperty("simulation-distance").equals("16"))
            logger.warn("simulation-distance: 16 in server.properties: Simulation distance is not set to 16, this is not recommended for Daybreak!");
        // hide-online-players: true
        if (!properties.getProperty("hide-online-players").equals("true"))
            logger.warn("hide-online-players: false in server.properties: Online players are not hidden, this is not recommended for Daybreak!");

        // get render distance and world radius
        var renderDistance = Integer.parseInt(properties.getProperty("view-distance"));
        var worldRadius = Integer.parseInt(properties.getProperty("max-world-size")) / 16;
        DaybreakPlugin.BORDER_RADIUS = Integer.parseInt(properties.getProperty("max-world-size"));

        /* check bukkit.yml */
        logger.info("Checking bukkit.yml...");
        var yaml = YamlConfiguration.loadConfiguration(Files.newBufferedReader(BUKKIT_YML));
        // allow-end: false
        if (yaml.getBoolean("settings.allow-end"))
            logger.error("settings.allow-end: false in bukkit.yml: End is enabled!");
        // worlds
        if (!yaml.contains("worlds"))
            logger.error("worlds: world in bukkit.yml: World is not set up to use generator! (worlds: world: generator: Daybreak)");

        /* check spigot.yml */
        logger.info("Checking spigot.yml...");
        yaml = YamlConfiguration.loadConfiguration(Files.newBufferedReader(SPIGOT_YML));
        // entity-tracking-range: players: ((renderDistance-1) * 16)
        if (yaml.getInt("world-settings.default.entity-tracking-range.players") != (renderDistance - 1) * 16)
            logger.warn("world-settings.default.entity-tracking-range.players: " + ((renderDistance - 1) * 16) + " in spigot.yml: Entity tracking range for players is not set to 240, this is not recommended for Daybreak!");

        /* check paper-global.yml */
        logger.info("Checking paper-global.yml...");
        yaml = YamlConfiguration.loadConfiguration(Files.newBufferedReader(PAPER_GLOBAL_YML));
        // chunk-system.io-threads != -1
        if (yaml.getInt("chunk-system.io-threads") == -1)
            logger.warn("chunk-system.io-threads: -1 in paper-global.yml: IO threads are not set, this is not recommended for Daybreak!");
        // chunk-system.worker-threads != -1
        if (yaml.getInt("chunk-system.worker-threads") == -1)
            logger.warn("chunk-system.worker-threads: -1 in paper-global.yml: Worker threads are not set, this is not recommended for Daybreak!");

        /* check paper-world-defaults.yml */
        logger.info("Checking paper-world-defaults.yml...");
        yaml = YamlConfiguration.loadConfiguration(Files.newBufferedReader(PAPER_WORLD_YML));
        // anticheat.anti-xray.enabled = true
        if (!yaml.getBoolean("anticheat.anti-xray.enabled"))
            logger.warn("anticheat.anti-xray.enabled: false in paper-world-defaults.yml: Anti-Xray is not enabled, this is not recommended for Daybreak!");
        // anticheat.anti-xray.engine-mode = 3
        if (yaml.getInt("anticheat.anti-xray.engine-mode") != 3)
            logger.warn("anticheat.anti-xray.engine-mode: 3 in paper-world-defaults.yml: Anti-Xray engine mode is not set to 3, this is not recommended for Daybreak!");
        // anticheat.anti-xray.max-block-height = 96
        if (yaml.getInt("anticheat.anti-xray.max-block-height") != 96)
            logger.warn("anticheat.anti-xray.max-block-height: 96 in paper-world-defaults.yml: Anti-Xray max block height is not set to 96, this is not recommended for Daybreak!");
        // spawn.keep-spawn-loaded-range: renderDistance + worldRadius
        if (yaml.getInt("spawn.keep-spawn-loaded-range") != renderDistance + worldRadius)
            logger.warn("spawn.keep-spawn-loaded-range: " + (renderDistance + worldRadius) + " in paper-world-defaults.yml: Keep spawn loaded range is not set to " + (renderDistance + worldRadius) + ", this is not recommended for Daybreak!");
    }
}
