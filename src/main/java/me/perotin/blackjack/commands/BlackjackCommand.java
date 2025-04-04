package me.perotin.blackjack.commands;

import java.text.DecimalFormat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.perotin.blackjack.Blackjack;
import me.perotin.blackjack.objects.BlackjackGame;
import me.perotin.blackjack.objects.BlackjackPlayer;
import me.perotin.blackjack.objects.GameSession;

public class BlackjackCommand implements CommandExecutor {

    private Blackjack plugin;

    private boolean secondaryBetOverride = false;

    public BlackjackCommand(Blackjack plugin){
        this.plugin = plugin;
        this.secondaryBetOverride = plugin.isSecondaryBetOverride();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(commandSender instanceof Player){
            Player player = (Player) commandSender;
            BlackjackPlayer bj = Blackjack.getInstance().getPlayerFor(player);

            if(player.hasPermission("blackjack.play")) {
                double betMax = plugin.getBetMax();
                double betMin = plugin.getBetMin();
                if (args.length >= 1) {
                    Double betAmount;

                    try {
                        betAmount = Double.parseDouble(args[0]);
                    } catch (NumberFormatException ex) {
                        if(Bukkit.getPlayer(args[0]) != null){
                            BlackjackPlayer blackjackPlayer = plugin.getPlayerFor(Bukkit.getPlayer(args[0]));

                            // send player stats
                            // eventually do all player lookups, not just online
                            if(blackjackPlayer != null){
                                player.sendMessage(ChatColor.BLACK + "------------- " + ChatColor.RED + args[0] + ChatColor.BLACK + " -------------");
                                player.sendMessage(plugin.getString("wins-stat").replace("$amount$", ""+blackjackPlayer.getWins()));
                                player.sendMessage(plugin.getString("loss-stat").replace("$amount$", ""+blackjackPlayer.getLosses()));
                                double ratio;
                                if(blackjackPlayer.getLosses() == 0) {
                                     ratio = blackjackPlayer.getWins();
                                } else {
                                    ratio = (double) blackjackPlayer.getWins() / blackjackPlayer.getLosses();
                                }

                                player.sendMessage(plugin.getString("ratio-stat").replace("$amount$",  new DecimalFormat("#.##").format(ratio)));

                                return true;
                            } else {
                                // should never be the case
                                player.sendMessage(ChatColor.RED+ "Something has gone wrong...");
                                return true;
                            }


                        } else{
                            player.sendMessage(plugin.getString("number-or-player"));
                            return true;
                        }
                    }

                    if(betAmount < 0){
                        betAmount = 0.0;
                    }
                    if(betMax > 0 && betAmount > betMax){
                        player.sendMessage(plugin.getString("bet-max-message")
                        .replace("$amount$", betMax+""));
                        return true;
                    }
                    if(betMin > 0 && betAmount < betMin){
                        player.sendMessage(plugin.getString("bet-min-message")
                                .replace("$amount$", betMin+""));
                        return true;
                    }

                    // search up if they already have an on-going game
                    // However, create a new one if secondary enable override is true
                    GameSession blackjackSession = plugin.getSessionFor(player.getUniqueId());
                    // Check if session exists, config option is set to true, current game exists and bet amounts are
                    // different.
                    boolean secondaryOverrideCheckCurrentGame = blackjackSession != null && secondaryBetOverride &&
                            blackjackSession.hasOngoingGame() != null && betAmount != blackjackSession.getBetOfOngoingGame();
                        // Specific case for NPC shops where you want to create different bets.

                    // do this for other scenario where no game but session exists

                    boolean noOtherGames = true;
                    if(plugin.getSessionFor(player.getUniqueId()) != null && !secondaryOverrideCheckCurrentGame) {
                        for (BlackjackGame game : plugin.getSessionFor(player.getUniqueId()).getGames()) {
                            if (game.getPlayer().getUniqueId().equals(player.getUniqueId()) && game.getResult() == Double.MAX_VALUE) {
                                // they have an ongoing game
                                noOtherGames = false;
                                player.sendMessage(plugin.getString("previous-game", ChatColor.GRAY + "Opening previous blackjack session..."));
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        player.openInventory(game.getInventory(true, false));
                                    }
                                }.runTaskLater(plugin, 20);
                            }
                        }
                    }
                    if(plugin.isOverflow()) {
                        int overflowAmount = plugin.getConfig().getInt("bet-overflow-max");
                        if(betAmount > bj.getBalance() + overflowAmount){
                            // too much
                            player.sendMessage(plugin.getString("can-only-bet")
                            .replace("$amount$", overflowAmount+""));
                            return true;
                        }

                    } else {
                        if(betAmount > bj.getBalance()){
                            // can't
                            player.sendMessage(plugin.getString("cannot-bet-that-much"));
                            return true;
                        }
                    }
                    if(noOtherGames) {
                        if(plugin.getSessionFor(player.getUniqueId()) != null && !secondaryOverrideCheckCurrentGame){
                            // they have a session already
                            player.sendMessage(plugin.getString("previous-game", ChatColor.GRAY + "Opening previous blackjack session..."));
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    GameSession session= plugin.getSessionFor(player.getUniqueId());
                                    session.showEndMenu(session.getGames().get(session.getGames().size()-1));
                                }
                            }.runTaskLater(plugin, 20);
                            return true;
                        }

                        if (secondaryOverrideCheckCurrentGame) {
                            // cancel the old game
                            blackjackSession.hasOngoingGame().setEnd(BlackjackGame.Ending.LOSE);
                            bj.addLoss();
                            blackjackSession.endSession();
                        }
                        BlackjackGame game = new BlackjackGame(player, betAmount);
                        GameSession session = new GameSession(player.getUniqueId(), game);
                        Blackjack.withdraw(betAmount, player);
                        plugin.getSessions().add(session);
                        player.openInventory(game.getInventory(true, false));
                        if(plugin.getConfig().getBoolean("custom-command")){
                            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), plugin.getConfig().getString("command").replace("$amount$", betAmount+""));
                        }
                    }

                } else {
                    boolean noOtherGames = true;
                    if(plugin.getSessionFor(player.getUniqueId()) != null) {
                        for (BlackjackGame game : plugin.getSessionFor(player.getUniqueId()).getGames()) {
                            if (game.getPlayer().getUniqueId().equals(player.getUniqueId()) && game.getResult() == Double.MAX_VALUE) {
                                // they have an ongoing game
                                noOtherGames = false;
                                player.sendMessage(plugin.getString("previous-game", ChatColor.GRAY + "Opening previous blackjack session..."));
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        player.openInventory(game.getInventory(true, false));
                                    }
                                }.runTaskLater(plugin, 20);
                            }
                        }
                    }
                    if(noOtherGames) {
                        if(plugin.getSessionFor(player.getUniqueId()) != null){
                            // they have a session already
                            player.sendMessage(plugin.getString("previous-game", ChatColor.GRAY + "Opening previous blackjack session..."));
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    GameSession session= plugin.getSessionFor(player.getUniqueId());
                                    session.showEndMenu(session.getGames().get(session.getGames().size()-1));
                                }
                            }.runTaskLater(plugin, 20);
                            return true;
                        }
                        player.sendMessage(plugin.getString("incorrect-args"));
                    }
                    return true;
                }
            } else {
                plugin.sendMessage(player, "no-permission");
                return true;
            }
        }
        return true;
    }
}
