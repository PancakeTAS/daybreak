package de.pancake.daybreak.listeners;

import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.inventory.ItemStack;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static de.pancake.daybreak.DaybreakPlugin.BORDER_RADIUS;
import static de.pancake.daybreak.DaybreakPlugin.PREFIX;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

/**
 * Crown listener for the daybreak plugin.
 * @author Pancake
 */
public class CrownListener implements Listener {

    /** File storing the crown holders */
    private static final Path CROWNS_FILE = Path.of("crowns.txt");

    /** The UUIDs of the players who currently hold the crowns. */
    public UUID goldenCrownHolder, silverCrownHolder, bronzeCrownHolder;

    /** Entities of the crowns if no player holds them. */
    public Entity goldenCrown, silverCrown, bronzeCrown;

    /** Boss bars displaying the crowns' positions. */
    public BossBar goldenCrownBar, silverCrownBar, bronzeCrownBar;

    /**
     * Handle world init event
     * @param e World init event.
     */
    @EventHandler @SneakyThrows
    public void onWorldInit(WorldInitEvent e) {
        // load crown holders from file
        if (Files.exists(CROWNS_FILE)) {
            var crownHolders = Files.readAllLines(CROWNS_FILE);
            this.goldenCrownHolder = crownHolders.get(0).isEmpty() ? null : UUID.fromString(crownHolders.get(0));
            this.silverCrownHolder = crownHolders.get(1).isEmpty() ? null : UUID.fromString(crownHolders.get(1));
            this.bronzeCrownHolder = crownHolders.get(2).isEmpty() ? null : UUID.fromString(crownHolders.get(2));
        }

        // spawn crowns if no player holds them
        if (this.goldenCrownHolder == null) {
            var name = "§eGolden Crown";
            this.goldenCrown = this.spawnCrown(Material.GOLD_BLOCK, name);
            var pos = this.goldenCrown.getLocation().toBlock();
            this.goldenCrownBar = Bukkit.createBossBar(name + " is at " + pos.blockX()  + ", " + pos.blockY() + ", " + pos.blockZ(), BarColor.YELLOW, BarStyle.SOLID);
            this.goldenCrownBar.setProgress(1.0);
            this.goldenCrownBar.setVisible(true);
        }

        if (this.silverCrownHolder == null) {
            var name = "§7Silver Crown";
            this.silverCrown = this.spawnCrown(Material.IRON_BLOCK, name);
            var pos = this.silverCrown.getLocation().toBlock();
            this.silverCrownBar = Bukkit.createBossBar(name + " is at " + pos.blockX()  + ", " + pos.blockY() + ", " + pos.blockZ(), BarColor.WHITE, BarStyle.SOLID);
            this.silverCrownBar.setProgress(1.0);
            this.silverCrownBar.setVisible(true);
        }

        if (this.bronzeCrownHolder == null) {
            var name = "§6Bronze Crown";
            this.bronzeCrown = this.spawnCrown(Material.COPPER_BLOCK, name);
            var pos = this.bronzeCrown.getLocation().toBlock();
            this.bronzeCrownBar = Bukkit.createBossBar(name + " is at " + pos.blockX()  + ", " + pos.blockY() + ", " + pos.blockZ(), BarColor.PINK, BarStyle.SOLID);
            this.bronzeCrownBar.setProgress(1.0);
            this.bronzeCrownBar.setVisible(true);
        }
    }

    /**
     * Handle player pickup item event
     * @param e Player pickup item event.
     */
    @EventHandler @SneakyThrows
    public void onPickup(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player p) {
            // check if item is crown
            var item = e.getItem();
            if (!item.equals(this.goldenCrown) && !item.equals(this.silverCrown) && !item.equals(this.bronzeCrown))
                return;

            // check if player already holds crown
            var uuid = p.getUniqueId();
            if (uuid.equals(this.goldenCrownHolder) || uuid.equals(this.silverCrownHolder) || uuid.equals(this.bronzeCrownHolder)) {
                e.setCancelled(true);
                return;
            }

            // pick up the crown
            e.setCancelled(true);
            if (item.equals(this.goldenCrown)) {
                this.goldenCrown = null;
                this.goldenCrownBar.removeAll();
                this.goldenCrownBar = null;
                this.goldenCrownHolder = uuid;
                Bukkit.broadcast(miniMessage().deserialize("<prefix><yellow>" + p.getName() + " has picked up the golden crown!</yellow>", PREFIX));
            } else if (item.equals(this.silverCrown)) {
                this.silverCrown = null;
                this.silverCrownBar.removeAll();
                this.silverCrownBar = null;
                this.silverCrownHolder = uuid;
                Bukkit.broadcast(miniMessage().deserialize("<prefix><gray>" + p.getName() + " has picked up the silver crown!</gray>", PREFIX));
            } else if (item.equals(this.bronzeCrown)) {
                this.bronzeCrown = null;
                this.bronzeCrownBar.removeAll();
                this.bronzeCrownBar = null;
                this.bronzeCrownHolder = uuid;
                Bukkit.broadcast(miniMessage().deserialize("<prefix><gold>" + p.getName() + " has picked up the bronze crown!</gold>", PREFIX));
            }

            // remove item and save crown holders to file
            item.remove();
            Files.write(CROWNS_FILE,
                    ((this.goldenCrownHolder == null ? "" : this.goldenCrownHolder.toString()) + "\n" +
                    (this.silverCrownHolder == null ? "" : this.silverCrownHolder.toString()) + "\n" +
                    (this.bronzeCrownHolder == null ? "" : this.bronzeCrownHolder.toString())).getBytes()
            );
        }
    }

    /**
     * Handle player join event.
     * @param e Player join event.
     */
    @EventHandler
    public void onJoin(PlayerMoveEvent e) {
        // add player to crown boss bars
        if (this.goldenCrownBar != null)
            this.goldenCrownBar.addPlayer(e.getPlayer());

        if (this.silverCrownBar != null)
            this.silverCrownBar.addPlayer(e.getPlayer());

        if (this.bronzeCrownBar != null)
            this.bronzeCrownBar.addPlayer(e.getPlayer());
    }

    /**
     * Spawn a crown entity.
     * @param type Material of the crown.
     * @param name Name of the crown.
     * @return The spawned crown entity.
     */
    private Entity spawnCrown(Material type, String name) {
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
        crown.setItemStack(new ItemStack(type));
        crown.customName(Component.text(name));
        crown.setCustomNameVisible(true);
        crown.setInvulnerable(true);
        crown.setGravity(false);
        crown.setPersistent(true);

        return crown;
    }


}
