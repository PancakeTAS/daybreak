package de.pancake.daybreak.listeners;

import de.pancake.daybreak.crowns.Crown;
import lombok.SneakyThrows;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.WorldInitEvent;

import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import static de.pancake.daybreak.DaybreakBootstrap.CROWNS_FILE;

/**
 * Crown listener for the daybreak plugin.
 * @author Pancake
 */
public class CrownListener implements Listener {

    /** Crown instances */
    public final Crown[] crowns = new Crown[3];

    /**
     * Handle world init event.
     * @param e World init event.
     */
    @EventHandler @SneakyThrows
    public void onWorldInit(WorldInitEvent e) {
        // load crown holders from file
        var crownHolders = List.of("", "", "");
        if (Files.exists(CROWNS_FILE))
            crownHolders = Files.readAllLines(CROWNS_FILE);

        this.crowns[0] = new Crown(Crown.CrownType.GOLDEN, crownHolders.get(0).isBlank() ? null : UUID.fromString(crownHolders.get(0)));
        this.crowns[1] = new Crown(Crown.CrownType.SILVER, crownHolders.get(1).isBlank() ? null : UUID.fromString(crownHolders.get(1)));
        this.crowns[2] = new Crown(Crown.CrownType.BRONZE, crownHolders.get(2).isBlank() ? null : UUID.fromString(crownHolders.get(2)));
    }

    /**
     * Handle player pickup item event
     * @param e Player pickup item event.
     */
    @EventHandler @SneakyThrows
    public void onPickup(EntityPickupItemEvent e) {
        // check if item is crown
        var crown = this.getCrown(e.getItem());
        if (crown == null)
            return;

        // check if entity is player
        if (!(e.getEntity() instanceof Player p)) {
            e.setCancelled(true);
            return;
        }

        // check if player already holds crown
        var heldCrown = this.getCrown(p.getUniqueId());
        if (heldCrown != null) {
            var index = heldCrown.getType().ordinal();
            var crownIndex = crown.getType().ordinal();

            // check if player holds higher crown
            if (index < crownIndex) {
                e.setCancelled(true);
                return;
            }

            // remove crown from player
            heldCrown.dropCrown(p);
        }

        // pick up the crown
        e.setCancelled(true);
        crown.pickupCrown(p);

        // save crown holders to file
        Files.write(CROWNS_FILE,
                ((this.crowns[0].getHolder() == null ? "": this.crowns[0].getHolder().toString()) + "\n" +
                (this.crowns[1].getHolder() == null ? "": this.crowns[1].getHolder().toString()) + "\n" +
                (this.crowns[2].getHolder() == null ? "": this.crowns[2].getHolder().toString()) + "\n").getBytes()
        );
    }

    /**
     * Handle player join event.
     * @param e Player join event.
     */
    @EventHandler
    public void onJoin(PlayerMoveEvent e) {
        for (var crown : this.crowns)
            crown.getBossBar().addPlayer(e.getPlayer());
    }

    /**
     * Get crown from entity
     * @param item The item entity.
     * @return The crown instance or null if not found.
     */
    public Crown getCrown(UUID uuid) {
        for (var crown : this.crowns)
            if (uuid.equals(crown.getHolder()))
                return crown;

        return null;
    }

    /**
     * Get crown from entity
     * @param item The item entity.
     * @return The crown instance or null if not found.
     */
    public Crown getCrown(Item item) {
        for (var crown : this.crowns)
            if (item.equals(crown.getEntity()))
                return crown;

        return null;
    }

}
