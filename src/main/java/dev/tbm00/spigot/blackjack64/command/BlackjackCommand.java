package dev.tbm00.spigot.blackjack64.command;

import java.text.DecimalFormat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import dev.tbm00.spigot.blackjack64.Blackjack64;
import dev.tbm00.spigot.blackjack64.gui.BlackjackGameGui;
import dev.tbm00.spigot.blackjack64.gui.BlackjackSessionGui;
import dev.tbm00.spigot.blackjack64.object.BlackjackGame;
import dev.tbm00.spigot.blackjack64.object.BlackjackPlayer;
import dev.tbm00.spigot.blackjack64.object.GameSession;
import dev.tbm00.spigot.blackjack64.gui.AnvilGui;
import dev.tbm00.spigot.blackjack64.util.StaticUtils;

public class BlackjackCommand implements CommandExecutor {

    private Blackjack64 plugin;

    private boolean newGameOverride = false;

    public BlackjackCommand(Blackjack64 plugin){
        this.plugin = plugin;
        this.newGameOverride = plugin.isnewGameOverride();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(commandSender instanceof Player){
            Player player = (Player) commandSender;
            
            if(player.hasPermission("blackjack64.play")) {
                double betMax = plugin.getBetMax();
                double betMin = plugin.getBetMin();
                BlackjackPlayer bjp;

                if (args.length >= 1) {
                    Double betAmount;
                    try {
                        betAmount = Double.parseDouble(args[0]);
                        bjp = plugin.getBlackjackPlayer(player);
                    } catch (NumberFormatException ex) {
                        if (Bukkit.getPlayer(args[0]) != null) {
                            bjp = plugin.getBlackjackPlayer(Bukkit.getPlayer(args[0]));

                            // send player stats
                            // eventually do all player lookups, not just online
                            if(bjp != null){
                                double ratio;
                                if (bjp.getLosses() == 0) ratio = bjp.getWins();
                                else ratio = (double) bjp.getWins() / bjp.getLosses();
                                
                                player.sendMessage(StaticUtils.translate(StaticUtils.getString("name-stat").replace("$name$", ""+args[0])));
                                player.sendMessage(StaticUtils.translate(StaticUtils.getString("wins-stat").replace("$amount$", ""+bjp.getWins())));
                                player.sendMessage(StaticUtils.translate(StaticUtils.getString("losses-stat").replace("$amount$", ""+bjp.getLosses())));
                                player.sendMessage(StaticUtils.translate(StaticUtils.getString("ratio-stat").replace("$amount$",  new DecimalFormat("#.##").format(ratio))));
                                player.sendMessage(StaticUtils.translate(StaticUtils.getString("ties-stat").replace("$amount$", ""+bjp.getTies())));
                                
                                if (bjp.getWinnings()>0)
                                    player.sendMessage(StaticUtils.translate(StaticUtils.getString("winnings-stat").replace("$amount$", "+$"+StaticUtils.formatInt(bjp.getWinnings()))));
                                else if (bjp.getWinnings()<0)
                                    player.sendMessage(StaticUtils.translate(StaticUtils.getString("winnings-stat").replace("$amount$", "-$"+StaticUtils.formatInt(-1*bjp.getWinnings()))));
                                else player.sendMessage(StaticUtils.translate(StaticUtils.getString("winnings-stat").replace("$amount$", "$0")));

                                return true;
                            } else {
                                // should never be the case
                                player.sendMessage(ChatColor.RED+ "Something has gone wrong...");
                                return true;
                            }
                        } else {
                            player.sendMessage(StaticUtils.getString("number-or-player"));
                            return true;
                        }
                    }

                    if (betAmount < 0){
                        betAmount = 0.0;
                    } else if (betMax > 0 && betAmount > betMax){
                        player.sendMessage(StaticUtils.getString("bet-max-message")
                                .replace("$amount$", StaticUtils.formatInt(betMax)));
                        return true;
                    } else if (betMin > 0 && betAmount < betMin){
                        player.sendMessage(StaticUtils.getString("bet-min-message")
                                .replace("$amount$", StaticUtils.formatInt(betMin)));
                        return true;
                    }

                    // search up if they already have an on-going game
                    // However, create a new one if newGameOverride is true
                    GameSession blackjackSession = plugin.getSessionFor(player.getUniqueId());
                    boolean newGameOverrideCheck = blackjackSession != null && newGameOverride &&
                            blackjackSession.hasOngoingGame() != null && betAmount != blackjackSession.getBetOfOngoingGame();

                    boolean noOtherGames = true;
                    if(blackjackSession != null && !newGameOverrideCheck) {
                        for (BlackjackGame game : plugin.getSessionFor(player.getUniqueId()).getGames()) {
                            if (game.getPlayer().getUniqueId().equals(player.getUniqueId()) && game.getResult() == Double.MAX_VALUE) {
                                // they have an ongoing game
                                noOtherGames = false;
                                player.sendMessage(StaticUtils.getString("previous-game", ChatColor.GRAY + "Opening previous blackjack session..."));
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        new BlackjackGameGui(plugin, player, blackjackSession, game, true, false);
                                    }
                                }.runTaskLater(plugin, 20);
                            }
                        }
                    }
                    if(betAmount > bjp.getBalance()){
                        player.sendMessage(StaticUtils.getString("cannot-bet-that-much"));
                        return true;
                    }
                    
                    if(noOtherGames) {
                        if(blackjackSession != null && !newGameOverrideCheck){
                            // they have a session already
                            player.sendMessage(StaticUtils.getString("previous-game", ChatColor.GRAY + "Opening previous blackjack session..."));
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    GameSession session= plugin.getSessionFor(player.getUniqueId());

                                    new BlackjackSessionGui(plugin, player, session, session.getGames().get(session.getGames().size()-1));
                                }
                            }.runTaskLater(plugin, 20);
                            return true;
                        }

                        if (blackjackSession != null && newGameOverrideCheck) {
                            // cancel the old game
                            bjp.adjustWinnings(-1*blackjackSession.getBetOfOngoingGame());
                            blackjackSession.hasOngoingGame().setEnd(BlackjackGame.Ending.LOSE);
                            bjp.addLoss();
                            blackjackSession.endSession();
                        }
                        BlackjackGame game = new BlackjackGame(player, betAmount);
                        GameSession session = new GameSession(player.getUniqueId(), game);
                        StaticUtils.withdraw(betAmount, player);
                        plugin.getSessions().add(session);
                        new BlackjackGameGui(plugin, player, session, game, true, false);
                    }

                } else {
                    plugin.getBlackjackPlayer(player);

                    GameSession session = plugin.getSessionFor(player.getUniqueId());
                    if (session != null) {
                        for (BlackjackGame game : session.getGames()) {
                            if (game.getPlayer().getUniqueId().equals(player.getUniqueId()) && game.getResult() == Double.MAX_VALUE) {
                                // they have an ongoing game
                                player.sendMessage(StaticUtils.getString("previous-game", ChatColor.GRAY + "Opening previous blackjack session..."));
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        new BlackjackGameGui(plugin, player, session, game, true, false);
                                    }
                                }.runTaskLater(plugin, 20);
                                return true;
                            }
                        }

                        player.sendMessage(StaticUtils.getString("previous-game", ChatColor.GRAY + "Opening previous blackjack session..."));
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                new BlackjackSessionGui(plugin, player, session, session.getGames().get(session.getGames().size()-1));
                            }
                        }.runTaskLater(plugin, 20);
                        return true;
                    } else {
                        new AnvilGui(plugin, player, "new");
                        return true;
                    }
                }
            } else {
                StaticUtils.sendConfigMessage(player, "no-permission");
                return true;
            }
        }
        return true;
    }
}
