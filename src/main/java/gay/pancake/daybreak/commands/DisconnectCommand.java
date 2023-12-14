package gay.pancake.daybreak.commands;

import gay.pancake.daybreak.DaybreakPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

/**
 * Daybreak's disconnect command.
 * @author Pancake
 */
public class DisconnectCommand extends Command {

    /** Daybreak plugin instance */
    private final DaybreakPlugin plugin;

    /**
     * Initialize disconnect command.
     * @param plugin Daybreak plugin instance.
     */
    public DisconnectCommand(DaybreakPlugin plugin) {
        super("disconnect", "Disconnect the player safely", "/disconnect", List.of("dc"));
        this.plugin = plugin;
    }

    /**
     * Execute disconnect command.
     * @param sender Source object which is executing this command
     * @param commandLabel The alias of the command used
     * @param args All arguments passed to the command, split via ' '
     * @return Command success
     */
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (sender instanceof Player p) {
            var combat = this.plugin.combatListener;
            if (combat.timers.containsKey(p)) {
                p.sendMessage(miniMessage().deserialize("<prefix><red>You are currently in combat.</red>", DaybreakPlugin.PREFIX));
                return true;
            }

            if (combat.disconnects.containsKey(p)) {
                p.sendMessage(miniMessage().deserialize("<prefix><red>You are already disconnecting.</red>", DaybreakPlugin.PREFIX));
                return true;
            }

            combat.disconnects.put(p, 5);
            p.sendMessage(miniMessage().deserialize("<prefix><green>You are now disconnecting. Do not log off. Do not move. You will be kicked in 5 seconds</green>", DaybreakPlugin.PREFIX));
        }
        return true;
    }

}
