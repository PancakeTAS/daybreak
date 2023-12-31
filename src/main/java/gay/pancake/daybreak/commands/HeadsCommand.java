package gay.pancake.daybreak.commands;

import gay.pancake.daybreak.DaybreakPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

/**
 * Daybreak's head collection command.
 * @author Pancake
 */
public class HeadsCommand extends Command {

    /**
     * Initialize heads command.
     */
    public HeadsCommand() {
        super("headcollection", "Command that shows the head collection of a player", "/headcollection", List.of("heads"));
    }

    /**
     * Execute heads command.
     * @param sender Source object which is executing this command
     * @param commandLabel The alias of the command used
     * @param args All arguments passed to the command, split via ' '
     * @return Command success
     */
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (sender instanceof Player p) {
            // grab player head collection
            var heads = (Map<UUID, Integer>) p.getPersistentDataContainer().getOrDefault(DaybreakPlugin.HEADS_KEY, DaybreakPlugin.HEADS_TYPE, new HashMap<UUID, Integer>());
            var total = heads.values().stream().mapToInt(i -> i).sum();

            if (total == 0) {
                sender.sendMessage(miniMessage().deserialize("<prefix><red>You don't have any heads yet!", DaybreakPlugin.PREFIX));
                return true;
            }

            // create inventory
            var inv = Bukkit.createInventory(null, (int) Math.ceil(heads.size() / 9.0)*9, Component.text("Your head collection (" + total + " heads)"));
            for (var entry : heads.entrySet()) {
                var skull = new ItemStack(Material.PLAYER_HEAD, entry.getValue());
                var meta = (SkullMeta) skull.getItemMeta();

                meta.displayName(Component.text(""));
                meta.setPlayerProfile(Bukkit.getOfflinePlayer(entry.getKey()).getPlayerProfile());

                meta.displayName(miniMessage().deserialize("<gold><!italic>" + Bukkit.getOfflinePlayer(entry.getKey()).getName(), DaybreakPlugin.PREFIX));
                skull.setItemMeta(meta);
                inv.addItem(skull);
            }

            // open inventory
            p.openInventory(inv);
        }

        return true;
    }

}
