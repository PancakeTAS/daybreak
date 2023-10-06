package de.pancake.daybreak.listeners;

import de.pancake.daybreak.DaybreakPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

import static de.pancake.daybreak.listeners.SurvivalListener.PREFIX;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

/**
 * Combat listener for the daybreak plugin.
 * @author Pancake
 */
public class CombatListener implements Listener {

    /** Players currently in combat */
    private final Map<Player, Integer> timers = new HashMap<>();

    /** Players that will be kicked and are allowed to disconnect without punishment */
    private final List<Player> safe = new ArrayList<>();

    /** Players that will get punished on next login */
    private final List<UUID> unsafe = new ArrayList<>();

    /**
     * Initialize combat listener.
     * @param plugin Daybreak plugin instance.
     */
    public CombatListener(DaybreakPlugin plugin) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (var entry : new HashMap<>(this.timers).entrySet()) {
                int val = entry.getValue() - 1;
                this.timers.put(entry.getKey(), val);

                if (val > 0) {
                    entry.getKey().sendActionBar(miniMessage().deserialize("<red>You are in combat. Do not log off.</red>"));
                } else if (val <= 0) {
                    entry.getKey().sendActionBar(miniMessage().deserialize("<green>You are no longer in combat.</green>"));
                    this.timers.remove(entry.getKey());
                }

            }
        }, 0, 20);
    }

    /**
     * Handle player join event.
     * @param e Player join event.
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        var p = e.getPlayer();
        if (this.unsafe.remove(p.getUniqueId())) {
            p.sendMessage(miniMessage().deserialize("<prefix><red>You logged off without /dc. Your health has been lowered 3 hearts and you will not be given invincibility frames</red>", PREFIX));
            p.setNoDamageTicks(5);
        }
    }

    /**
     * Handle player damage event.
     * @param e Player damage event.
     */
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player p && e.getDamager() instanceof Player)
            this.timers.put(p, 30);
    }

    /**
     * Handle player quit event.
     * @param e Player quit event.
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        var p = e.getPlayer();
        if (this.timers.containsKey(p) && !p.isDead()) {
            p.setLastDamageCause(new EntityDamageEvent(p, EntityDamageEvent.DamageCause.CUSTOM, 1000));
            p.setHealth(0);
        } else if (!this.safe.remove(p)) {
            if (p.getHealth() > 6.0)
                p.setHealth(6.0);

            this.unsafe.add(p.getUniqueId());
        }
    }

}
