package dev.tbm00.spigot.blackjack64.object;

import java.util.UUID;

import org.bukkit.Bukkit;

import dev.tbm00.spigot.blackjack64.Blackjack64;

public class BlackjackPlayer  {

    private final UUID uuid;
    private PlayerStats stats;

    public BlackjackPlayer(UUID uuid, int wins, int losses, int ties, double winnings) {
        this.uuid = uuid;
        stats = new PlayerStats(wins, losses, ties, winnings);
    }

    public UUID getUuid() {
        return uuid;
    }

    public PlayerStats getStats() {
        return stats;
    }

    public void setStats(PlayerStats stats) {
        this.stats = stats;
    }

    public int getWins() {
        return stats.getWins();
    }

    public void addWin() {
        stats.addWin();
    }

    public void setWins(int wins) {
        stats.setWins(wins);
    }

    public int getLosses() {
        return stats.getLosses();
    }

    public void addLoss(){
        stats.addLoss();
    }

    public void setLosses(int losses) {
        stats.setLosses(losses);
    }

    public int getTies() {
        return stats.getTies();
    }

    public void addTie() {
        stats.addTie();
    }

    public void setTies(int ties) {
        stats.setTies(ties);
    }

    public double getWinnings() {
        return stats.getWinnings();
    }

    public void setWinnings(double winnings) {
        stats.setWinnings(winnings);
    }

    public void adjustWinnings(double difference) {
        stats.adjustWinnings(difference);
    }

    public double getBalance(){
        return Blackjack64.getEconomy().getBalance(Bukkit.getOfflinePlayer(uuid));
    }
}
