package de.pancake.daybreak.commands;

import de.pancake.daybreak.DaybreakPlugin;
import lombok.SneakyThrows;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static de.pancake.daybreak.DaybreakPlugin.PREFIX;
import static de.pancake.daybreak.webhook.WebhookUtil.getPlayerName;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;
import static net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed;

/**
 * Daybreak's leaderboard command.
 * @author Pancake
 */
public class LeaderboardCommand extends Command {

    /** Daybreak plugin instance */
    private final DaybreakPlugin plugin;

    /**
     * Initialize leaderboard command.
     */
    public LeaderboardCommand(DaybreakPlugin plugin) {
        super("leaderboard", "Shows the leaderboard", "/leaderboard", List.of("lb"));
        this.plugin = plugin;
    }

    /**
     * Execute leaderboard command.
     * @param sender Source object which is executing this command
     * @param commandLabel The alias of the command used
     * @param args All arguments passed to the command, split via ' '
     * @return Command success
     */
    @Override @SneakyThrows
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        var crowns = this.plugin.crownListener.crowns;

        sender.sendMessage(miniMessage().deserialize("""
                <prefix><bold>Daybreak Leaderboard</bold>
                <prefix><gold>1st: <first></gold>
                <prefix><gray>2nd: <second></gray>
                <prefix><red>3rd: <third></red>""", PREFIX,
                unparsed("first", crowns[0].getHolder() != null ? getPlayerName(crowns[0].getHolder()) : "Unclaimed"),
                unparsed("second", crowns[1].getHolder() != null ? getPlayerName(crowns[1].getHolder()) : "Unclaimed"),
                unparsed("third", crowns[2].getHolder() != null ? getPlayerName(crowns[2].getHolder()) : "Unclaimed")
        ));

        return true;
    }

}
