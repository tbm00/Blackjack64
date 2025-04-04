package dev.tbm00.spigot.blackjack64.command;

import java.text.DecimalFormat;
import java.util.stream.IntStream;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import dev.tbm00.spigot.blackjack64.Blackjack64;
import dev.tbm00.spigot.blackjack64.util.StaticUtils;

public class BlackjackAdminCommand implements CommandExecutor {

    private Blackjack64 plugin;

    public BlackjackAdminCommand(Blackjack64 blackjack) {
        this.plugin = blackjack;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(sender.hasPermission("blackjack64.admin")){
            if(args.length == 0){
                // default
                IntStream.range(0, 3).forEach(x -> sender.sendMessage(" "));
                double impact = plugin.getServerImpact();
                if(impact > 0)
                    sender.sendMessage(ChatColor.YELLOW + "Server impact: " + ChatColor.GREEN + "+"+impact);
                else if(impact < 0)
                    sender.sendMessage(ChatColor.YELLOW + "Server impact: " + ChatColor.RED +impact);
                else
                    sender.sendMessage(ChatColor.YELLOW + "Server impact: " + ChatColor.WHITE + impact);

                double ties = plugin.getGames() - plugin.getServerLosses() - plugin.getServerWins();
                double winRate = 100 * (plugin.getServerWins() / plugin.getGames());

                double tWins, tLosses, tGames, tWinRate, tTies, tLossRate;
                tWins = plugin.getTotalServerWins();
                tLosses = plugin.getTotalServerLosses();
                tGames = plugin.getTotalServerGames();
                tWinRate = (tWins / tGames) * 100;
                tLossRate = (tLosses / tGames) *100;
                tTies = tGames - tWins -tLosses;


                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',  "&7&oSince last restart, the server has recorded these stats..."));
                sender.sendMessage(ChatColor.YELLOW + "Server Wins: " + ChatColor.GREEN+plugin.getServerWins() + ChatColor.GRAY + " ("+ new DecimalFormat("#.##").format(winRate)
                +"% win rate)");
                sender.sendMessage(ChatColor.YELLOW + "Server Losses: " + ChatColor.RED+plugin.getServerLosses());
                sender.sendMessage(ChatColor.YELLOW + "Server Ties: " + ChatColor.WHITE+ties);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',  "&7&oThe server has recorded these stats over "+ (int) tGames+ " games of BlackJack ..."));
                sender.sendMessage(ChatColor.YELLOW + "Total Server Wins: " + ChatColor.GREEN+tWins + ChatColor.GRAY + " ("+ new DecimalFormat("#.##").format(tWinRate)
                +"% win rate)");
                sender.sendMessage(ChatColor.YELLOW + "Total Server Losses: " + ChatColor.RED+tLosses + ChatColor.GRAY + " ("+ new DecimalFormat("#.##").format(tLossRate)
                +"% loss rate)");
                sender.sendMessage(ChatColor.YELLOW + "Total Server Ties: " + ChatColor.WHITE+tTies);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',  "&7&oOn average, the server should win 52% of games."));
                sender.sendMessage(" ");

                sender.sendMessage(ChatColor.RED + "You are running version " + plugin.getDescription().getVersion() + " made by tbm00");
            }

        } else {
            sender.sendMessage(StaticUtils.getString("no-permission"));
            return true;
        }
        return true;
    }
}
