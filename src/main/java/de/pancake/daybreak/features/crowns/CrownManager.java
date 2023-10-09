package de.pancake.daybreak.features.crowns;

import com.destroystokyo.paper.profile.PlayerProfile;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
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

/**
 * Crown manager for the daybreak plugin.
 * @author Cosmic
 */
public class CrownManager {
    /** File storing the crown holders */
    public static final Path CROWNS_FILE = Path.of("crowns.txt");

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
        if (this.goldenCrownHolder == null) {
            System.out.println("created golden crown");
            var name = "§eGolden Crown";
            this.goldenCrown = this.spawnCrown(Material.GOLD_BLOCK, name);
            var pos = this.goldenCrown.getLocation().toBlock();
            this.goldenCrownTitle = this.titleObjective.getScore("§eGolden Crown");
            this.goldenCrownTitle.setScore(6);
            this.goldenCrownPos = this.titleObjective.getScore("§f » " + pos.blockX() + ", " + pos.blockY() + ", " + pos.blockZ());
            this.goldenCrownPos.setScore(5);
        }

        if (this.silverCrownHolder == null) {
            System.out.println("created silver crown");
            var name = "§7Silver Crown";
            this.silverCrown = this.spawnCrown(Material.IRON_BLOCK, name);
            var pos = this.silverCrown.getLocation().toBlock();
            this.silverCrownTitle = this.titleObjective.getScore("§7Silver Crown");
            this.silverCrownTitle.setScore(4);
            this.silverCrownPos = this.titleObjective.getScore("§f » " + pos.blockX() + ", " + pos.blockY() + ", " + pos.blockZ());
            this.silverCrownPos.setScore(3);
        }

        if (this.bronzeCrownHolder == null) {
            System.out.println("created bronze crown");
            var name = "§6Bronze Crown";
            this.bronzeCrown = this.spawnCrown(Material.COPPER_BLOCK, name);
            var pos = this.bronzeCrown.getLocation().toBlock();
            this.bronzeCrownTitle = this.titleObjective.getScore("§6Bronze Crown");
            this.bronzeCrownTitle.setScore(2);
            this.bronzeCrownPos = this.titleObjective.getScore("§f » " + pos.blockX() + ", " + pos.blockY() + ", " + pos.blockZ());
            this.bronzeCrownPos.setScore(1);
        }
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
