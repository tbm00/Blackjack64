package me.perotin.blackjack.events;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.perotin.blackjack.Blackjack;
import me.perotin.blackjack.objects.BlackjackPlayer;

public class BlackjackJoinEvent implements Listener {

    private Blackjack plugin;
    public static Map<UUID, Double> quitterMidGame = new HashMap<>();

    public BlackjackJoinEvent(Blackjack blackjack) {
        this.plugin = blackjack;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player joiner = event.getPlayer();
        if(plugin.getPlayerFor(joiner) ==null){
            plugin.getPlayers().add(BlackjackPlayer.loadPlayer(joiner));
        }
        // Do a check to see if they are rejoining after being mid-game to notify them
        if (quitterMidGame.containsKey(joiner.getUniqueId())) {
            joiner.sendMessage(plugin.getString("quitter-mid-game")
                    .replace("$amount$", quitterMidGame.get(joiner.getUniqueId())+""));
            quitterMidGame.remove(joiner.getUniqueId());

        }

    }
}
