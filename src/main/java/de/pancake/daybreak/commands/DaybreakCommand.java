package de.pancake.daybreak.commands;

import de.pancake.daybreak.DaybreakPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Daybreak's main command.
 * @author Pancake
 */
public class DaybreakCommand extends Command {

    /** Daybreak plugin instance */
    private final DaybreakPlugin plugin;

    /**
     * Initialize daybreak command.
     * @param plugin Daybreak plugin instance.
     */
    public DaybreakCommand(DaybreakPlugin plugin) {
        super("daybreak", "Daybreak's main command", "/daybreak <reset/revive/kill>", List.of("db"));
        this.plugin = plugin;
    }

    /**
     * Execute daybreak command.
     * @param sender Source object which is executing this command
     * @param commandLabel The alias of the command used
     * @param args All arguments passed to the command, split via ' '
     * @return Command success
     */
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        // check for permission
        if (!sender.isOp()) {
            sender.sendMessage(Bukkit.permissionMessage());
            return true;
        }

        // execute command
        if (args.length == 1 && "reset".equals(args[0])) {

            // reset server
            sender.sendMessage("§6» §cResetting server... (not automatically restarting!)");
            this.plugin.reset();

        } else if (args.length == 2 && "revive".equals(args[0])) {

            // revive player
            var player = Bukkit.getPlayer(args[1]);
            if (player != null && !this.plugin.isSurvivor(player.getUniqueId())) {
                sender.sendMessage("§6» §cReviving §6" + args[1] + "§c...");
                this.plugin.revive(player.getUniqueId());
            } else
                sender.sendMessage("§6» §cPlayer §6" + args[1] + "§c is not dead!");

        } else if (args.length == 2 && "kill".equals(args[0])) {

            // kill player
            var player = Bukkit.getPlayer(args[1]);
            if (player != null && this.plugin.isSurvivor(player.getUniqueId())) {
                sender.sendMessage("§6» §cKilling §6" + args[1] + "§c...");
                this.plugin.kill(player.getUniqueId());
            } else
                sender.sendMessage("§6» §cPlayer §6" + args[1] + "§c is already dead!");

        } else
            sender.sendMessage("§6» §cCommand usage: §6/daybreak §c<§6reset§c|§6revive§c|§6kill§c>");

        return true;
    }

    /**
     * Tab complete daybreak command.
     * @param sender Source object which is executing this command
     * @param alias the alias being used
     * @param args All arguments passed to the command, split via ' '
     * @return A list of possible completions for the final argument, or null to default to the command executor
     * @throws IllegalArgumentException Thrown when sender, alias, or args is null
     */
    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        if (args.length < 2)
            return List.of("reset", "revive", "kill");
        else if (args.length == 2)
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(name -> name.startsWith(args[1])).toList();
        return null;
    }
}
