package de.pancake.daybreak.features.crowns;

import com.destroystokyo.paper.profile.PlayerProfile;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.scoreboard.*;
import org.bukkit.util.Vector;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static de.pancake.daybreak.DaybreakPlugin.BORDER_RADIUS;
import static de.pancake.daybreak.DaybreakPlugin.PREFIX;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

/**
 * Crown manager for the daybreak plugin.
 * @author Cosmic
 */
public class CrownManager {
    /** File storing the crown holders */
    public static final Path CROWNS_FILE = Path.of("crowns.txt");

    /** Crown key */
    public static final NamespacedKey CROWN_KEY = new NamespacedKey("daybreak", "crown");

    /** The UUIDs of the players who currently hold the crowns. */
    public UUID goldenCrownHolder, silverCrownHolder, bronzeCrownHolder;

    /** Entities of the crowns if no player holds them. */
    public Item goldenCrown, silverCrown, bronzeCrown;

    /** Scoreboard displaying crowns information */
    public Scoreboard crownScoreboard;

    /** Scoreboard objective */
    public Objective titleObjective;

    /** Scoreboard scores */
    public Score goldenCrownTitle, goldenCrownPos, silverCrownTitle, silverCrownPos, bronzeCrownTitle, bronzeCrownPos;

    /**
     * Initialise the variables
     */
    @SneakyThrows
    public CrownManager() {
        // load crown holders from file
        if (Files.exists(CROWNS_FILE)) {
            var crownHolders = Files.readAllLines(CROWNS_FILE);
            this.goldenCrownHolder = crownHolders.get(0).equals("null") ? null : UUID.fromString(crownHolders.get(0));
            this.silverCrownHolder = crownHolders.get(1).equals("null") ? null : UUID.fromString(crownHolders.get(1));
            this.bronzeCrownHolder = crownHolders.get(2).equals("null") ? null : UUID.fromString(crownHolders.get(2));
        }

        // create the crown scoreboard
        if (this.crownScoreboard == null) {
            this.crownScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            this.titleObjective = this.crownScoreboard.registerNewObjective("title", Criteria.DUMMY, MiniMessage.miniMessage().deserialize("<red><bold>Daybreak"));
            this.titleObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        // spawn crowns if no player holds them
        if (this.goldenCrownHolder == null)
            createCrown(Material.GOLD_BLOCK);

        if (this.silverCrownHolder == null)
            createCrown(Material.IRON_BLOCK);

        if (this.bronzeCrownHolder == null)
            createCrown(Material.COPPER_BLOCK);
    }

    /**
     * Create a crown.
     * @param type Material of the crown (used to see which crown it is).
     */
    public void createCrown(Material type) {
        switch (type) {
            case GOLD_BLOCK -> {
                var name = "§eGolden Crown";
                this.goldenCrown = this.spawnCrown(type, name);
                var pos = this.goldenCrown.getLocation().getBlock();
                this.goldenCrownTitle = this.titleObjective.getScore("§eGolden Crown");
                this.goldenCrownTitle.setScore(6);
                this.goldenCrownPos = this.titleObjective.getScore("§f » " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
                this.goldenCrownPos.setScore(5);
            }
            case IRON_BLOCK -> {
                var name = "§7Silver Crown";
                this.silverCrown = this.spawnCrown(type, name);
                var pos = this.silverCrown.getLocation().getBlock();
                this.silverCrownTitle = this.titleObjective.getScore("§7Silver Crown");
                this.silverCrownTitle.setScore(4);
                this.silverCrownPos = this.titleObjective.getScore("§f » " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
                this.silverCrownPos.setScore(3);
            }
            default -> {
                var name = "§6Bronze Crown";
                this.bronzeCrown = this.spawnCrown(type, name);
                var pos = this.bronzeCrown.getLocation().getBlock();
                this.bronzeCrownTitle = this.titleObjective.getScore("§6Bronze Crown");
                this.bronzeCrownTitle.setScore(2);
                this.bronzeCrownPos = this.titleObjective.getScore("§f » " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
                this.bronzeCrownPos.setScore(1);
            }
        }
    }

    /**
     * Create a crown at a location.
     * @param type Material of the crown (used to see which crown it is).
     * @param location Location of the crown.
     */
    public void createCrown(Material type, Location location) {
        switch (type) {
            case GOLD_BLOCK -> {
                var name = "§eGolden Crown";
                this.goldenCrown = this.spawnCrown(type, name, location);
                var pos = this.goldenCrown.getLocation().getBlock();
                this.goldenCrownTitle = this.titleObjective.getScore("§eGolden Crown");
                this.goldenCrownTitle.setScore(6);
                this.goldenCrownPos = this.titleObjective.getScore("§f » " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
                this.goldenCrownPos.setScore(5);
            }
            case IRON_BLOCK -> {
                var name = "§7Silver Crown";
                this.silverCrown = this.spawnCrown(type, name, location);
                var pos = this.silverCrown.getLocation().getBlock();
                this.silverCrownTitle = this.titleObjective.getScore("§7Silver Crown");
                this.silverCrownTitle.setScore(4);
                this.silverCrownPos = this.titleObjective.getScore("§f » " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
                this.silverCrownPos.setScore(3);
            }
            default -> {
                var name = "§6Bronze Crown";
                this.bronzeCrown = this.spawnCrown(type, name, location);
                var pos = this.bronzeCrown.getLocation().getBlock();
                this.bronzeCrownTitle = this.titleObjective.getScore("§6Bronze Crown");
                this.bronzeCrownTitle.setScore(2);
                this.bronzeCrownPos = this.titleObjective.getScore("§f » " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
                this.bronzeCrownPos.setScore(1);
            }
        }
    }

    /**
     * Transfer crown to the killer.
     * @param p Player to transfer crown from.
     */
    @SneakyThrows
    public void transferPlayerCrown(Player p) {

        var sourcePdc = p.getPersistentDataContainer();
        int crown = sourcePdc.getOrDefault(CROWN_KEY, PersistentDataType.INTEGER, 0);
        if (crown == 0)
            return;

        var killer = p.getKiller();
        if (killer == null) {
            sourcePdc.set(CROWN_KEY, PersistentDataType.INTEGER, 0);
            if (crown == 3) {
                this.goldenCrownHolder = null;
                this.createCrown(Material.GOLD_BLOCK, p.getLocation());
            }
            if (crown == 2) {
                this.silverCrownHolder = null;
                this.createCrown(Material.IRON_BLOCK, p.getLocation());
            }
            if (crown == 1) {
                this.bronzeCrownHolder = null;
                this.createCrown(Material.COPPER_BLOCK, p.getLocation());
            }
        } else {
            var targetPdc = killer.getPersistentDataContainer();

            // get crown of killer
            int killerCrown = targetPdc.getOrDefault(CROWN_KEY, PersistentDataType.INTEGER, 0);
            if (killerCrown >= crown)
                return;

            // transfer crown
            targetPdc.set(CROWN_KEY, PersistentDataType.INTEGER, crown);
            sourcePdc.set(CROWN_KEY, PersistentDataType.INTEGER, 0);

            if (killerCrown == 2) {
                this.createCrown(Material.IRON_BLOCK);
                this.silverCrownHolder = null;
            }
            if (killerCrown == 1) {
                this.createCrown(Material.COPPER_BLOCK);
                this.bronzeCrownHolder = null;
            }

            // send message to killer
            if (crown == 3) {
                this.goldenCrownHolder = killer.getUniqueId();
                killer.sendMessage(miniMessage().deserialize("<prefix>You stole the <yellow>Golden Crown</yellow> from <gold>" + p.getName() + "</gold>.", PREFIX));
            }
            if (crown == 2) {
                this.silverCrownHolder = killer.getUniqueId();
                killer.sendMessage(miniMessage().deserialize("<prefix>You stole the <gray>Silver Crown</gray> from <gold>" + p.getName() + "</gold>.", PREFIX));
            }
            if (crown == 1) {
                this.bronzeCrownHolder = killer.getUniqueId();
                killer.sendMessage(miniMessage().deserialize("<prefix>You stole the <gold>Bronze Crown</gold> from <gold>" + p.getName() + "</gold>.", PREFIX));
            }
        }

        Files.write(CROWNS_FILE,
                ((this.goldenCrownHolder == null ? "null" : this.goldenCrownHolder.toString()) + "\n" +
                        (this.silverCrownHolder == null ? "null" : this.silverCrownHolder.toString()) + "\n" +
                        (this.bronzeCrownHolder == null ? "null" : this.bronzeCrownHolder.toString())).getBytes()
        );
    }

    /**
     * Spawn a crown entity.
     * @param type Material of the crown.
     * @param name Name of the crown.
     * @return The spawned crown entity.
     */
    public Item spawnCrown(Material type, String name) {
        var world = Bukkit.getWorlds().getFirst();

        // get random position within border
        int x, y, z;
        do {
            x = (int) (Math.random() * (BORDER_RADIUS*2) - BORDER_RADIUS);
            y = (int) (Math.random() * 320) - 32;
            z = (int) (Math.random() * (BORDER_RADIUS*2) - BORDER_RADIUS);
        } while (world.getBlockAt(x, y, z).getType() != Material.AIR);

        // spawn crown
        var crown = (Item) world.spawnEntity(world.getBlockAt(x, y, z).getLocation(), EntityType.DROPPED_ITEM);

        // create itemstack and meta
        var itemStack = new ItemStack(Material.PLAYER_HEAD);
        var skullMeta = (SkullMeta) itemStack.getItemMeta();

        // load texture depending on the material type
        if (type.equals(Material.GOLD_BLOCK)) {
            addSkullTexture(skullMeta, "http://textures.minecraft.net/texture/a645fb0017617de2320bcf9fe2b21c5bc55f5c027060cab7edb929aa1d442327");
        } else if (type.equals(Material.IRON_BLOCK)) {
            addSkullTexture(skullMeta, "http://textures.minecraft.net/texture/fae9e7e3a4e9c656124fd50acb9b685ef5a5c0b61c8abeb98e570252c9a1dd7");
        } else {
            addSkullTexture(skullMeta, "http://textures.minecraft.net/texture/9fa86b629ff2b5839aaa3444f044d4de1dfa13d1333a10c7ceaf93726fbc549d");
        }
        itemStack.setItemMeta(skullMeta);
        crown.setItemStack(itemStack);
        crown.customName(Component.text(name));
        crown.setCustomNameVisible(true);
        crown.setInvulnerable(true);
        crown.setGravity(false);
        crown.setPersistent(true);
        crown.setTicksLived(Integer.MAX_VALUE);

        // stop the crown from floating away
        crown.setVelocity(new Vector(0, 0, 0));

        return crown;
    }

    /**
     * Spawn a crown entity.
     * @param type Material of the crown.
     * @param name Name of the crown.
     * @param location Location of the crown.
     * @return The spawned crown entity.
     */
    public Item spawnCrown(Material type, String name, Location location) {
        var world = Bukkit.getWorlds().getFirst();

        // spawn crown
        var crown = (Item) world.spawnEntity(location, EntityType.DROPPED_ITEM);

        // create itemstack and meta
        var itemStack = new ItemStack(Material.PLAYER_HEAD);
        var skullMeta = (SkullMeta) itemStack.getItemMeta();

        // load texture depending on the material type
        if (type.equals(Material.GOLD_BLOCK)) {
            addSkullTexture(skullMeta, "http://textures.minecraft.net/texture/a645fb0017617de2320bcf9fe2b21c5bc55f5c027060cab7edb929aa1d442327");
        } else if (type.equals(Material.IRON_BLOCK)) {
            addSkullTexture(skullMeta, "http://textures.minecraft.net/texture/fae9e7e3a4e9c656124fd50acb9b685ef5a5c0b61c8abeb98e570252c9a1dd7");
        } else {
            addSkullTexture(skullMeta, "http://textures.minecraft.net/texture/9fa86b629ff2b5839aaa3444f044d4de1dfa13d1333a10c7ceaf93726fbc549d");
        }
        itemStack.setItemMeta(skullMeta);
        crown.setItemStack(itemStack);
        crown.customName(Component.text(name));
        crown.setCustomNameVisible(true);
        crown.setInvulnerable(true);
        crown.setGravity(false);
        crown.setPersistent(true);
        crown.setTicksLived(Integer.MAX_VALUE);

        // stop the crown from floating away
        crown.setVelocity(new Vector(0, 0, 0));

        return crown;
    }

    /**
     * Adds the Texture to the skull
     * @param meta Skull's Meta
     * @param url Minecraft Texture URL
     */
    private void addSkullTexture(SkullMeta meta, String url) {
        PlayerProfile playerProfile = Bukkit.createProfile(UUID.randomUUID());
        PlayerTextures playerTextures = playerProfile.getTextures();

        try {
            playerTextures.setSkin(new URI(url).toURL());
        } catch (MalformedURLException | URISyntaxException ignored) {}

        playerProfile.setTextures(playerTextures);
        meta.setPlayerProfile(playerProfile);
    }
}
