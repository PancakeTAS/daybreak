package de.pancake.daybreak.features.crowns;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.persistence.PersistentDataType;

import java.nio.file.Files;

import static de.pancake.daybreak.DaybreakPlugin.CROWN_KEY;
import static de.pancake.daybreak.DaybreakPlugin.PREFIX;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

/**
 * Crown listener for the daybreak plugin.
 * @author Cosmic
 */
public class CrownListener implements Listener {

    private CrownManager crownManager;

    /**
     * Handle world init event
     * @param e World init event.
     */
    @EventHandler
    public void onWorldInit(WorldInitEvent e) {
        // initialise CrownManager
        crownManager = new CrownManager();
    }

    /**
     * Handle player pickup item event
     * @param e Player pickup item event.
     */
    @EventHandler @SneakyThrows
    public void onPickup(EntityPickupItemEvent e) {
        // check if the item is a crown
        var item = e.getItem();
        var itemStack = e.getItem().getItemStack();

        var goldenCrownStack = crownManager.goldenCrown == null ? null : crownManager.goldenCrown.getItemStack();
        var silverCrownStack = crownManager.silverCrown == null ? null : crownManager.silverCrown.getItemStack();
        var bronzeCrownStack = crownManager.bronzeCrown == null ? null : crownManager.bronzeCrown.getItemStack();

        if (!itemStack.equals(goldenCrownStack) && !itemStack.equals(silverCrownStack) && !itemStack.equals(bronzeCrownStack)) {
            return;
        }
        e.setCancelled(true);
        if (e.getEntity() instanceof Player p) {
            var uuid = p.getUniqueId();

            var playerPdc = p.getPersistentDataContainer();

            // get crown of player
            int crown = playerPdc.getOrDefault(CROWN_KEY, PersistentDataType.INTEGER, 0);
            if ((crown == 3 || (crown == 2 && itemStack.equals(bronzeCrownStack)))) return;

            // pick up the crown
            if (itemStack.equals(goldenCrownStack)) {
                crownManager.goldenCrown = null;
                crownManager.goldenCrownTitle.resetScore();
                crownManager.goldenCrownPos.resetScore();
                crownManager.goldenCrownHolder = uuid;
                if (crownManager.bronzeCrownHolder == uuid)
                    crownManager.bronzeCrownHolder = null;
                if (crownManager.silverCrownHolder == uuid)
                    crownManager.silverCrownHolder = null;
                playerPdc.set(CROWN_KEY, PersistentDataType.INTEGER, 3);
                Bukkit.broadcast(miniMessage().deserialize("<prefix><yellow>" + p.getName() + " has picked up the golden crown!</yellow>", PREFIX));
            } else if (itemStack.equals(silverCrownStack)) {
                crownManager.silverCrown = null;
                crownManager.silverCrownTitle.resetScore();
                crownManager.silverCrownPos.resetScore();
                crownManager.silverCrownHolder = uuid;
                if (crownManager.bronzeCrownHolder == uuid)
                    crownManager.bronzeCrownHolder = null;
                playerPdc.set(CROWN_KEY, PersistentDataType.INTEGER, 2);
                Bukkit.broadcast(miniMessage().deserialize("<prefix><gray>" + p.getName() + " has picked up the silver crown!</gray>", PREFIX));
            } else if (itemStack.equals(bronzeCrownStack)) {
                crownManager.bronzeCrown = null;
                crownManager.bronzeCrownTitle.resetScore();
                crownManager.bronzeCrownPos.resetScore();
                crownManager.bronzeCrownHolder = uuid;
                playerPdc.set(CROWN_KEY, PersistentDataType.INTEGER, 1);
                Bukkit.broadcast(miniMessage().deserialize("<prefix><gold>" + p.getName() + " has picked up the bronze crown!</gold>", PREFIX));
            }

            // remove item and save crown holders to file
            item.remove();
            Files.write(CrownManager.CROWNS_FILE,
                    ((crownManager.goldenCrownHolder == null ? "null" : crownManager.goldenCrownHolder.toString()) + "\n" +
                            (crownManager.silverCrownHolder == null ? "null" : crownManager.silverCrownHolder.toString()) + "\n" +
                            (crownManager.bronzeCrownHolder == null ? "null" : crownManager.bronzeCrownHolder.toString())).getBytes()
            );
        }
    }

    /**
     * Handle player join event.
     * @param e Player join event.
     */
    @EventHandler
    public void onJoin(PlayerMoveEvent e) {
        // set player scoreboard
        if (!e.getPlayer().getScoreboard().equals(crownManager.crownScoreboard))
            e.getPlayer().setScoreboard(crownManager.crownScoreboard);

    }
}
