package dev.tbm00.spigot.blackjack64.object;

public class PlayerStats {
    private int wins;
    private int losses;
    private int ties;
    private double winnings;

    public PlayerStats(int wins, int losses, int ties, double winnings) {
        this.wins = wins;
        this.losses = losses;
        this.ties = ties;
        this.winnings = winnings;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public void addWin(){
        wins +=1;
    }

    public void addLoss(){
        losses +=1;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public int getTies() {
        return ties;
    }

    public void setTies(int ties) {
        this.ties = ties;
    }

    public void addTie(){
        ties +=1;
    }

    public double getWinnings() {
        return winnings;
    }

    public void setWinnings(double winnings) {
        this.winnings = winnings;
    }

    public void adjustWinnings(double difference) {
        winnings = winnings+difference;
    }
}