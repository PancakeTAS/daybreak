package de.pancake.daybreak.crowns;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.Vector;

import java.net.URI;
import java.util.UUID;

import static de.pancake.daybreak.DaybreakPlugin.BORDER_RADIUS;
import static de.pancake.daybreak.DaybreakPlugin.PREFIX;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;
import static net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.component;
import static net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed;

/**
 * Crown instance
 * @author Pancake
 */
@Getter
public class Crown {

    /**
     * Crown types
     */
    @Getter @ToString @RequiredArgsConstructor
    public enum CrownType {
        GOLDEN("Golden Crown", NamedTextColor.GOLD, BarColor.YELLOW, "https://textures.minecraft.net/texture/a645fb0017617de2320bcf9fe2b21c5bc55f5c027060cab7edb929aa1d442327"),
        SILVER("Silver Crown", NamedTextColor.GRAY, BarColor.WHITE, "https://textures.minecraft.net/texture/fae9e7e3a4e9c656124fd50acb9b685ef5a5c0b61c8abeb98e570252c9a1dd7"),
        BRONZE("Bronze Crown", NamedTextColor.RED, BarColor.PINK, "https://textures.minecraft.net/texture/9fa86b629ff2b5839aaa3444f044d4de1dfa13d1333a10c7ceaf93726fbc549d");

        private final String name;
        private final TextColor color;
        private final BarColor bar;
        private final String texture;
    }

    /** The crown type */
    private final CrownType type;

    /** The boss bar of the crown */
    private final BossBar bossBar;

    /** The holder of the crown or null not held */
    private UUID holder;

    /** The crown item entity or null if held */
    private Item entity;

    /**
     * Creates a new crown
     * @param type The crown type
     * @param holder The holder of the crown or null if not held
     */
    public Crown(CrownType type, UUID holder) {
        this.type = type;
        this.holder = holder;
        this.bossBar = Bukkit.createBossBar("null", this.type.getBar(), BarStyle.SOLID);
        this.bossBar.setVisible(false);
        this.bossBar.setProgress(1.0);

        if (this.holder == null)
            spawnCrownEntity(null);
    }

    /**
     * Pickup crown
     * @param player The player that picked up the crown
     */
    public void pickupCrown(Player player) {
        this.entity.remove();
        this.entity = null;
        this.bossBar.setVisible(false);
        this.transferCrown(player);
    }

    /**
     * Transfer crown from one player to another
     * @param target The player to transfer the crown to
     */
    public void transferCrown(Player player) {
        this.holder = player.getUniqueId();
        Bukkit.broadcast(miniMessage().deserialize("<prefix><gold><player></gold> <red>stole the</red> <crown><red>!</red>", PREFIX, unparsed("player", player.getName()), component("crown", Component.text(this.type.getName()).color(this.type.getColor()))));
    }

    /**
     * Drop crown from a player
     * @param player The player to drop the crown from
     */
    public void dropCrown(Player player) {
        this.holder = null;
        this.spawnCrownEntity(player.getLocation());
        Bukkit.broadcast(miniMessage().deserialize("<prefix><gold><player></gold> <red>dropped the</red> <crown><red>!</red>", PREFIX, unparsed("player", player.getName()), component("crown", Component.text(this.type.getName()).color(this.type.getColor()))));
    }

    /**
     * Spawn the crown item entity
     * @param pos The position to spawn the crown at or null to spawn at random position within border
     */
    @SneakyThrows
    private void spawnCrownEntity(Location pos) {
        var world = Bukkit.getWorlds().getFirst();

        // get random position within border if not specified
        if (pos == null) {
            int x, y, z;
            do {
                x = (int) (Math.random() * (BORDER_RADIUS*2) - BORDER_RADIUS);
                y = (int) (Math.random() * 320) - 32;
                z = (int) (Math.random() * (BORDER_RADIUS*2) - BORDER_RADIUS);
            } while (world.getBlockAt(x, y, z).getType() != Material.AIR);

            pos = new Location(world, x, y, z);
        }

        // spawn crown
        this.entity = (Item) world.spawnEntity(pos, EntityType.DROPPED_ITEM);
        var itemStack = new ItemStack(Material.PLAYER_HEAD);
        var skullMeta = (SkullMeta) itemStack.getItemMeta();

        // set skull texture
        var profile = Bukkit.createProfile(UUID.randomUUID());
        var textures = profile.getTextures();
        textures.setSkin(URI.create(this.type.getTexture()).toURL());
        profile.setTextures(textures);
        skullMeta.setPlayerProfile(profile);
        itemStack.setItemMeta(skullMeta);

        // update crown properties
        this.entity.setItemStack(itemStack);
        this.entity.customName(Component.text(this.type.getName()).color(this.type.getColor()));
        this.entity.setCustomNameVisible(true);
        this.entity.setInvulnerable(true);
        this.entity.setGravity(false);
        this.entity.setPersistent(true);
        this.entity.setTicksLived(Integer.MAX_VALUE);
        this.entity.setVelocity(new Vector(0, 0, 0));

        // create bossbar
        this.bossBar.setTitle(pos.getBlockX() + " " + pos.getBlockY() + " " + pos.getBlockZ());
        this.bossBar.setVisible(true);
    }

}
