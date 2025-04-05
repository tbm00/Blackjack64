package dev.tbm00.spigot.blackjack64.object;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import dev.tbm00.spigot.blackjack64.Blackjack64;
import dev.tbm00.spigot.blackjack64.gui.BlackjackGameGui;
import dev.tbm00.spigot.blackjack64.gui.BlackjackSessionGui;
import dev.tbm00.spigot.blackjack64.util.StaticUtils;

public class GameSession {

    private UUID uuid;
    private List<BlackjackGame> games;
    private double betAmount;

    public GameSession(UUID uuid, BlackjackGame game) {
        this.uuid = uuid;
        this.games = new ArrayList<>();
        games.add(game);
        this.betAmount = game.getBetAmount();
    }

    public double getBetAmount() {
        return betAmount;
    }

    public void setBetAmount(double betAmount) {
        this.betAmount = betAmount;
    }

    public double getTotalEarnings(){
        double earnings = 0;
        for(BlackjackGame game : games){
            earnings += game.getResult();
        }
        return earnings;
    }

    public BlackjackGame hasOngoingGame(){
        for (BlackjackGame game : games) {
            if (game.getResult() == Double.MAX_VALUE) return game;
        }
        return null;
    }
    public double getBetOfOngoingGame(){
        BlackjackGame game = hasOngoingGame();
        if (game == null) return -1;
        return game.getBetAmount();
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public List<BlackjackGame> getGames() {
        return games;
    }

    public void addGame(List<BlackjackGame> games) {
        this.games.addAll(games);
    }

    private Player getPlayer(){
        return Bukkit.getPlayer(uuid);
    }

    public void endSession(){
        getPlayer().closeInventory();

        if(getTotalEarnings() > 0) {
           getPlayer().sendMessage(StaticUtils.translate(StaticUtils.getString("session-ending")
                   .replace("$amount$", games.size() + "").replace("$outcome$", "&a+$"+StaticUtils.formatInt(getTotalEarnings()))));
        } else if (getTotalEarnings() < 0) {
           getPlayer().sendMessage(StaticUtils.translate(StaticUtils.getString("session-ending")
                   .replace("$amount$", games.size() + "").replace("$outcome$", "&c-$"+StaticUtils.formatInt(-1*getTotalEarnings()))));
        } else {
           getPlayer().sendMessage(StaticUtils.translate(StaticUtils.getString("session-ending")
                   .replace("$amount$", games.size() + "").replace("$outcome$", "&e$0" )));
        }

        Blackjack64.getInstance().getSessions().remove(this);
    }

    public void endGame(BlackjackGame game, BlackjackGame.Ending end) {
        Blackjack64 plugin = Blackjack64.getInstance();
        game.setEnd(end);
        double betAmount = game.getBetAmount();
        Player player = game.getPlayer();
        int playerScore = StaticUtils.getScoreUnder21(game.getPlayerCards());
        double multiplierAmount = (plugin.getBlackJackMultiplier() * betAmount) + betAmount;
        boolean blackJack = false;

        if (end == BlackjackGame.Ending.WIN) {
            if (playerScore == 21 && plugin.getBlackJackMultiplier() > 1) {
                blackJack = true;
                game.setBlackjack(true);

                StaticUtils.deposit(multiplierAmount, player);
            } else StaticUtils.deposit( betAmount + betAmount, player);

            if(blackJack) {
                player.sendMessage(StaticUtils.translate(StaticUtils.getString("blackjack-reward").replace("$amount$", StaticUtils.formatInt((plugin.getBlackJackMultiplier() * betAmount))).replace("$multiplier$", ""+plugin.getConfig().getDouble("blackjack-multiplier"))));
                plugin.setServerImpact(plugin.getServerImpact() - (plugin.getBlackJackMultiplier()* betAmount));
            } else plugin.setServerImpact(plugin.getServerImpact() - betAmount);
            plugin.increaseGamesPlayed();
            plugin.increaseServerLosses();
                
        } else if (end == BlackjackGame.Ending.LOSE) {
            plugin.setServerImpact(plugin.getServerImpact() + betAmount);
            plugin.increaseGamesPlayed();
            plugin.increaseServerWins();
        } else if (end == BlackjackGame.Ending.TIE) {
            // tie
            StaticUtils.deposit(betAmount, player);
            plugin.increaseGamesPlayed();
        } else {
            // they surrender
            double surrender = betAmount - (betAmount * (plugin.getSurrenderPercentage() / 100));

            StaticUtils.deposit(surrender, player);
            plugin.setServerImpact(plugin.getServerImpact() + surrender);
            plugin.increaseGamesPlayed();
            plugin.increaseServerWins();
        }
        
        new BlackjackSessionGui(plugin, player, this, game);
    }

    public void startNewGame(){
        BlackjackPlayer player = Blackjack64.getInstance().getBlackjackPlayer(getPlayer());
        if(player.getBalance() >= betAmount) {
            BlackjackGame game = new BlackjackGame(getPlayer(), betAmount);
            getGames().add(game);
            StaticUtils.withdraw(betAmount, getPlayer());

            new BlackjackGameGui(Blackjack64.getInstance(), getPlayer(), Blackjack64.getInstance().getSessionFor(game), game, true, false);
        } else {
            getPlayer().closeInventory();
            endSession();
            getPlayer().sendMessage(StaticUtils.getString("cannot-bet-that-much"));
        }
    }
}
