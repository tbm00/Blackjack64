package dev.tbm00.spigot.blackjack64.object;

import java.util.*;

import org.bukkit.entity.Player;

import dev.tbm00.spigot.blackjack64.Blackjack64;
import dev.tbm00.spigot.blackjack64.util.StaticUtils;

public class BlackjackGame {

    private final Player player;
    private final UUID uuid;
    private double betAmount;
    private ArrayList<String> playerCards;
    private ArrayList<String> houseCards;
    private ArrayList<String> cardsAvailable;
    private boolean playerTurn, blackjack;
    private int dealerCardIndex = 1;
    private Ending end = null;

    public enum Ending {
        WIN, LOSE, TIE, SURRENDER
    }

    public BlackjackGame(Player player, double betAmount) {
        this.player = player;
        this.uuid = UUID.randomUUID();
        this.betAmount = betAmount;
        this.playerTurn = true;
        this.cardsAvailable = new ArrayList<>(Arrays.asList(Blackjack64.cards));
        this.playerCards = new ArrayList<>();
        this.houseCards = new ArrayList<>();

        getNextCard();
        getNextCard();
        setPlayerTurn(false);
        getNextCard();
        getNextCard();
        setPlayerTurn(true);
    }

    /**
     * Gets the next available card
     */
    public void getNextCard() {
        int random = new Random().nextInt(cardsAvailable.size());
        String card = cardsAvailable.get(random);

        if (isPlayerTurn()) playerCards.add(card);
        else houseCards.add(card);
    
        cardsAvailable.remove(random);
    }

    public double getResult(){
        Blackjack64 plugin = Blackjack64.getInstance();

        if(end != null){
            switch (end){
                case TIE: return 0;
                case WIN:
                    int playerScore = StaticUtils.getScoreUnder21(getPlayerCards());

                    if (playerScore == 21 && plugin.getBlackJackMultiplier() > 1) {
                        return plugin.getBlackJackMultiplier() * betAmount;
                    } else return betAmount;
                    
                case LOSE:
                    return -betAmount;
                case SURRENDER:
                    double surrender = betAmount - (betAmount * (plugin.getSurrenderPercentage() / 100));

                    return -surrender;
            }
        }
        return Double.MAX_VALUE;
    }


    public void incrementDealerCardIndex(int totalCards) {
        dealerCardIndex++;
        if (dealerCardIndex >= totalCards - 1) {
            dealerCardIndex = 1;
        }
    }

    public int getDealerCardIndex() {
        return dealerCardIndex;
    }

    public boolean equals(BlackjackGame game){
        return game.getUuid().equals(getUuid());
    }

    public Ending getEnd() {
        return end;
    }

    public void setEnd(Ending end) {
        this.end = end;
    }

    public UUID getUuid() {
        return uuid;
    }

    public ArrayList<String> getPlayerCards() {
        return playerCards;
    }

    public void setBlackjack(boolean blackjack) {
        this.blackjack = blackjack;
    }

    public boolean isBlackjackEnding() {
        return blackjack;
    }

    public ArrayList<String> getHouseCards() {
        return houseCards;
    }

    public Player getPlayer() {
        return player;
    }

    public double getBetAmount() {
        return betAmount;
    }

    public boolean isPlayerTurn() {
        return playerTurn;
    }

    public void setPlayerTurn(boolean playerTurn) {
        this.playerTurn = playerTurn;
    }

    public ArrayList<String> getCardsAvailable() {
        return cardsAvailable;
    }

    public void setBetAmount(double betAmount) {
        this.betAmount = betAmount;
    }

    public void setCardsAvailable(ArrayList<String> cardsAvailable) {
        this.cardsAvailable = cardsAvailable;
    }
}
