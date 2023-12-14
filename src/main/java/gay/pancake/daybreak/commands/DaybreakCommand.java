package gay.pancake.daybreak.commands;

import gay.pancake.daybreak.DaybreakPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

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
        super("daybreak", "Daybreak's main command", "/daybreak <reset>", List.of("db"));
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
            sender.sendMessage(miniMessage().deserialize("<prefix>Resetting server...", DaybreakPlugin.PREFIX));
            this.plugin.reset();

        } else
            sender.sendMessage(miniMessage().deserialize("<prefix>Command usage: <gold>/daybreak</gold> <red><</red><gold>reset</gold><red>></red>", DaybreakPlugin.PREFIX));

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
        return List.of("reset");
    }
}
