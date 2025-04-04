package dev.tbm00.spigot.blackjack64.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.tbm00.spigot.blackjack64.Blackjack64;
import dev.tbm00.spigot.blackjack64.gui.BlackjackGameGui;
import dev.tbm00.spigot.blackjack64.gui.BlackjackSessionGui;
import dev.tbm00.spigot.blackjack64.object.BlackjackGame;
import dev.tbm00.spigot.blackjack64.object.GameSession;
import dev.tbm00.spigot.blackjack64.gui.AnvilGui;

public class BlackjackQuickCommand implements CommandExecutor {

    private Blackjack64 plugin;

    public BlackjackQuickCommand(Blackjack64 plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(commandSender instanceof Player) {
            Player player = (Player) commandSender;

            if (player.hasPermission("blackjack64.play")) {
                plugin.getBlackjackPlayer(player);


                GameSession session = plugin.getSessionFor(player.getUniqueId());
                if (session != null) {
                    for (BlackjackGame game : session.getGames()) {
                        if (game.getPlayer().getUniqueId().equals(player.getUniqueId()) && game.getResult() == Double.MAX_VALUE) {
                            // they have an ongoing hand
                            new BlackjackGameGui(plugin, player, session, game, true, false);
                            return true;
                        }
                    }

                    // they have a session already
                    new BlackjackSessionGui(plugin, player, session, session.getGames().get(session.getGames().size()-1));
                    return true;
                } else {
                    new AnvilGui(plugin, player, "new");
                }
            }
        }
        return true;
    }
}
