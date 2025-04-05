package dev.tbm00.spigot.blackjack64.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import dev.tbm00.spigot.blackjack64.Blackjack64;
import dev.tbm00.spigot.blackjack64.object.BlackjackGame;
import dev.tbm00.spigot.blackjack64.object.GameSession;
import dev.tbm00.spigot.blackjack64.util.StaticUtils;

public class PlayerConnection implements Listener {

    private Blackjack64 plugin;
    public static Map<UUID, Double> quitterMidGame = new HashMap<>();

    public PlayerConnection(Blackjack64 blackjack) {
        this.plugin = blackjack;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player joiner = event.getPlayer();

        // Do a check to see if they are rejoining after being mid-game to notify them
        if (quitterMidGame.containsKey(joiner.getUniqueId())) {
            joiner.sendMessage(StaticUtils.getString("quitter-mid-game")
                    .replace("$amount$", StaticUtils.formatInt(quitterMidGame.get(joiner.getUniqueId()))));
            quitterMidGame.remove(joiner.getUniqueId());
        }
    }

    @EventHandler
    public void leaveMidGame(PlayerQuitEvent event){
        exitMidSession(event.getPlayer());
    }

    @EventHandler
    public void leaveMidGame(PlayerKickEvent event){
        exitMidSession(event.getPlayer());
    }

    private void exitMidSession(Player player) {
        Player quitter = player;
        
        BlackjackGame currentGame = null;
        GameSession session = null;
        if (plugin.getSessionFor(quitter.getUniqueId()) != null) {

            Bukkit.getConsoleSender().sendMessage("Deleting instance");

            for (BlackjackGame game : plugin.getSessionFor(quitter.getUniqueId()).getGames()) {
                if (game.getPlayer().getUniqueId().equals(quitter.getUniqueId())) {
                    currentGame = game;
                    session = plugin.getSessionFor(quitter.getUniqueId());
                }
            }
        }
        if(currentGame != null && session != null){
            session.endGame(currentGame, BlackjackGame.Ending.LOSE);
            plugin.getBlackjackPlayer(quitter).addLoss();
            plugin.getBlackjackPlayer(quitter).adjustWinnings(-1*currentGame.getBetAmount());
            quitterMidGame.put(quitter.getUniqueId(), currentGame.getBetAmount());
            session.endSession();
        }
    }
}
