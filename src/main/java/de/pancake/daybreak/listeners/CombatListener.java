package de.pancake.daybreak.listeners;

import de.pancake.daybreak.DaybreakPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

/**
 * Combat listener for the daybreak plugin.
 * @author Pancake
 */
public class CombatListener implements Listener {

    /** Combat logging tasks */
    private final Map<Player, Integer> timers = new HashMap<>();

    /**
     * Initialize combat listener.
     * @param plugin Daybreak plugin instance.
     */
    public CombatListener(DaybreakPlugin plugin) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (var entry : this.timers.entrySet()) {
                int val = entry.getValue() - 1;
                this.timers.put(entry.getKey(), val);

                if (val > 0)
                    entry.getKey().sendActionBar(miniMessage().deserialize("<red>You are in combat. Do not log off.</red>"));
                else if (val == 0)
                    entry.getKey().sendActionBar(miniMessage().deserialize("<green>You are no longer in combat.</green>"));

            }
        }, 0, 20);
    }

    /**
     * Handle player damage event.
     * @param e Player damage event.
     */
    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p)
            this.timers.put(p, 10);
    }

    /**
     * Handle player quit event.
     * @param e Player quit event.
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        var p = e.getPlayer();
        if (this.timers.getOrDefault(p, 0) > 0 && !p.isDead()) {
            p.setLastDamageCause(new EntityDamageEvent(p, EntityDamageEvent.DamageCause.CUSTOM, 1000));
            p.setHealth(0);
        }
    }

}
