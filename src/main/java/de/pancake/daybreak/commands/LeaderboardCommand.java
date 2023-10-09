package de.pancake.daybreak.commands;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import static de.pancake.daybreak.DaybreakPlugin.*;
import static de.pancake.daybreak.features.crowns.CrownManager.CROWNS_FILE;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

/**
 * Daybreak's leaderboard command.
 * @author Cosmic
 */
public class LeaderboardCommand extends Command {

    /**
     * Initialize leaderboard command.
     */
    public LeaderboardCommand() {
        super("leaderboard", "Command that shows the leaderboard of crowns", "/leaderboard", List.of("lb"));
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
        if (sender instanceof Player p) {

            // grab crown holder information
            UUID goldenCrownHolder = null;
            UUID silverCrownHolder = null;
            UUID bronzeCrownHolder = null;

            if (Files.exists(CROWNS_FILE)) {
                var crownHolders = Files.readAllLines(CROWNS_FILE);
                goldenCrownHolder = crownHolders.get(0).equals("null") ? null : UUID.fromString(crownHolders.get(0));
                silverCrownHolder = crownHolders.get(1).equals("null") ? null : UUID.fromString(crownHolders.get(1));
                bronzeCrownHolder = crownHolders.get(2).equals("null") ? null : UUID.fromString(crownHolders.get(2));
            }

            // convert their uuid's to a name or "NO ONE"
            var goldenCrownOwnerName = goldenCrownHolder == null ? "No one" : Bukkit.getOfflinePlayer(goldenCrownHolder).getName();
            var silverCrownOwnerName = silverCrownHolder == null ? "No one" : Bukkit.getOfflinePlayer(silverCrownHolder).getName();
            var bronzeCrownOwnerName = bronzeCrownHolder == null ? "No one" : Bukkit.getOfflinePlayer(bronzeCrownHolder).getName();

            p.sendMessage(miniMessage().deserialize("""
                <prefix><bold>Daybreak</bold>
                <prefix>Current Leaderboard
                <prefix><yellow>1st</yellow>: <gold>%s</gold>.
                <prefix><gray>2nd</gray>: <gold>%s</gold>.
                <prefix><gold>3rd</gold>: <gold>%s</gold>.""".formatted(goldenCrownOwnerName, silverCrownOwnerName, bronzeCrownOwnerName), PREFIX)
            );
        }

        return true;
    }

}
