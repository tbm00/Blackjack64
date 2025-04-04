package me.perotin.blackjack.objects;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import me.perotin.blackjack.Blackjack;
import me.perotin.blackjack.util.StaticUtils;
import me.perotin.blackjack.util.ItemBuilder;
import me.perotin.blackjack.util.XMaterial;

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
        this.cardsAvailable = new ArrayList<>(Arrays.asList(Blackjack.cards));
        this.playerCards = new ArrayList<>();
        this.houseCards = new ArrayList<>();

        getNextCard();
        getNextCard();
        setPlayerTurn(false);
        getNextCard();
        getNextCard();
        setPlayerTurn(true);
    }

    public int getDealerCardIndex() {
        return dealerCardIndex;
    }

    public void incrementDealerCardIndex(int totalCards) {
        dealerCardIndex++;
        if (dealerCardIndex >= totalCards - 1) {
            dealerCardIndex = 1;
        }
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
        Blackjack plugin = Blackjack.getInstance();

        if(end != null){
            switch (end){
                case TIE: return 0;
                case WIN:
                    int playerScore = StaticUtils.getScoreUnder21(getPlayerCards());

                    if(plugin.getTaxPercent() != 0.0 && plugin.getTaxPercent() <= 100.0) {
                        double tax = plugin.getTaxPercent() / 100.0;
                        double postTax = betAmount - (tax * betAmount);
                        if(playerScore == 21 && plugin.getBlackJackMultiplier() > 1) {
                            return plugin.getBlackJackMultiplier() * postTax;
                        }
                            return postTax;
                    } else {
                        if(playerScore == 21 && plugin.getBlackJackMultiplier() > 1) {
                            return plugin.getBlackJackMultiplier() * betAmount;

                        }
                            return betAmount;
                    }
                case LOSE:
                    return -betAmount;
                case SURRENDER:
                    double surrender = betAmount - (betAmount * (plugin.getSurrenderPercentage() / 100));

                    return -surrender;
            }
        }
        return Double.MAX_VALUE;
    }

    /**
     * @param cards
     * @return most optimal score under 21 for the cards
     */
    public int getScoreUnder21(List<String> cards) {
        List<String> cardsToRemoveAndAdd = new ArrayList<>();

        for (String s : cards) {
            if (s.startsWith("A")) {
                cardsToRemoveAndAdd.add(s);
            }
        }
        cards.removeAll(cardsToRemoveAndAdd);
        cards.addAll(cardsToRemoveAndAdd);

        int score = 0;
        for (String s : cards) {
            if (!s.startsWith("A")) {
                score += valueOfCard(s);
            } else {
                if (score + 11 <= 21) {
                    score += 11;
                } else  {
                    score += 1;
                }
            }
        }
        return score;
    }

    private int valueOfCard(String card) {
        // make sure this is right and its not 1 because starting at 0
        if (card.length() == 2) {
            card = card.substring(0, 1);
            switch (card) {
                case "K":
                    return 10;
                case "J":
                    return 10;
                case "Q":
                    return 10;
                case "A":
                    return 0;
                default:
                    return Integer.parseInt(card);
            }
        } else if (card.length() == 3) {
            return 10;
        } else {
            return 0;
        }
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

    public Inventory getInventory(boolean hideHouse, boolean isDoubleDown) {
        Blackjack plugin = Blackjack.getInstance();
        Inventory inventory = Bukkit.createInventory(null, 54, Blackjack.getInstance().getString("menu-title").replace("$number$", "" + (int) betAmount));

        for (Integer i : StaticUtils.decoSpots) {
            inventory.setItem(i, new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE.parseMaterial()).name(" ").build());
        }

        ItemBuilder dealerhead = new ItemBuilder(XMaterial.PLAYER_HEAD.parseMaterial());
        dealerhead.dealerHead();
        dealerhead.name(StaticUtils.translate("&eDealer's Hand"));
        dealerhead.lore("");
        String showHouseCard = houseCards.get(0);
        if (hideHouse) {
            dealerhead.lore(StaticUtils.translate("&fTotal: &6???"));
            dealerhead.lore(StaticUtils.translate("&7- " + StaticUtils.convertToFullText(showHouseCard)));
        } else {
            dealerhead.lore(StaticUtils.translate("&fTotal: &6" + StaticUtils.getScoreUnder21(houseCards)));
            for (String card : houseCards) {
                dealerhead.lore(StaticUtils.translate("&7- " + StaticUtils.convertToFullText(card)));
            }
        } inventory.setItem(10, dealerhead.build());

        ItemBuilder playerhead = new ItemBuilder(XMaterial.PLAYER_HEAD.parseMaterial());
        playerhead.playerHead(player);
        playerhead.name(StaticUtils.translate("&eYour Hand"));
        playerhead.lore("");
        playerhead.lore(StaticUtils.translate("&fTotal: &6" + StaticUtils.getScoreUnder21(playerCards)));
        for (String card : playerCards) {
            playerhead.lore(StaticUtils.translate("&7- " + StaticUtils.convertToFullText(card)));
        } inventory.setItem(28, playerhead.build());

        ItemBuilder stand = new ItemBuilder(XMaterial.BARRIER.parseItem());
        stand.name(Blackjack.getInstance().getString("stand-item"));
        stand.lore(StaticUtils.translate("&7Stay with current hand"));
        inventory.setItem(16, stand.build());

        if(!isDoubleDown) {
            ItemBuilder hit = new ItemBuilder(XMaterial.NETHER_STAR.parseItem());
            hit.name(Blackjack.getInstance().getString("hit-item"));
            hit.lore(StaticUtils.translate("&7Draw 1 more card"));
            inventory.setItem(25, hit.build());
        }

        if(plugin.isDoubleDownEnabled() && playerCards.size() == 2) {
            if (Blackjack.getEconomy().getBalance(player) >= betAmount  || plugin.isDoubleDownOverFlow()){
                ItemBuilder doubleDown = new ItemBuilder(XMaterial.ARROW.parseItem());
                doubleDown.name(Blackjack.getInstance().getString("double-down-item"));
                doubleDown.lore(StaticUtils.translate("&7Double bet amount and stand after 1 more card"));
                inventory.setItem(34, doubleDown.build());
            }
        }

        if(plugin.isSurrenderEnabled() && !isDoubleDown) {
            ItemBuilder surrender = new ItemBuilder(XMaterial.DARK_OAK_DOOR.parseItem());
            surrender.name(Blackjack.getInstance().getString("surrender-item"));
            surrender.lore(StaticUtils.translate("&7End hand and keep half your bet"));
            inventory.setItem(43, surrender.build());
        }

        int invSlot = 29;
        for (String card : playerCards) {
            inventory.setItem(invSlot, StaticUtils.cardAsItemStack(card, true));
            invSlot++;
        }

        int dealersSlot = 11;
        if (hideHouse) {
            for (String card : houseCards) {
                if (dealersSlot == 11) {
                    inventory.setItem(dealersSlot, StaticUtils.cardAsItemStack(card, true));
                    dealersSlot++;
                } else {
                    inventory.setItem(dealersSlot, StaticUtils.cardAsItemStack(card, false));
                    dealersSlot++;
                }
            }
        } else {
            for (String card : houseCards) {
                inventory.setItem(dealersSlot, StaticUtils.cardAsItemStack(card, true));
                dealersSlot++;
            }
        }

        return inventory;

    }

    public Inventory getHiddenInventory(GameSession session, BlackjackGame game) {
        Inventory inventory = Bukkit.createInventory(null, 54, Blackjack.getInstance().getString("menu-title").replace("$number$", "" + (int) betAmount));

        for (Integer i : StaticUtils.decoSpots) {
            inventory.setItem(i, new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE.parseMaterial()).name(" ").build());
        }

        ItemBuilder dealerhead = new ItemBuilder(XMaterial.PLAYER_HEAD.parseMaterial());
        dealerhead.dealerHead();
        dealerhead.name(StaticUtils.translate("&eDealer's Hand"));
        dealerhead.lore("");
        dealerhead.lore(StaticUtils.translate("&fTotal: &6" + StaticUtils.getScoreUnder21(houseCards)));
        for (String card : houseCards) {
            dealerhead.lore(StaticUtils.translate("&7- " + StaticUtils.convertToFullText(card)));
        }
        inventory.setItem(10, dealerhead.build());

        ItemBuilder playerhead = new ItemBuilder(XMaterial.PLAYER_HEAD.parseMaterial());
        playerhead.playerHead(player);
        playerhead.name(StaticUtils.translate("&eYour Hand"));
        playerhead.lore("");
        playerhead.lore(StaticUtils.translate("&fTotal: &6" + StaticUtils.getScoreUnder21(playerCards)));
        for (String card : playerCards) {
            playerhead.lore(StaticUtils.translate("&7- " + StaticUtils.convertToFullText(card)));
        } inventory.setItem(28, playerhead.build());

        int invSlot = 29;
        for (String card : playerCards) {
            inventory.setItem(invSlot, StaticUtils.cardAsItemStack(card, true));
            invSlot++;
        }

        int dealersSlot = 11;
        int lastIndex = houseCards.size()-1;
        for (String card : houseCards) {
            if ((dealersSlot-11)==lastIndex && lastIndex!=0) inventory.setItem(dealersSlot, StaticUtils.cardAsItemStack(card, false));
            else inventory.setItem(dealersSlot, StaticUtils.cardAsItemStack(card, true));
            dealersSlot++;
        }
        return inventory;
    }

    public Inventory getRevealedInventory(GameSession session, BlackjackGame game) {
        Inventory inventory = Bukkit.createInventory(null, 54, Blackjack.getInstance().getString("menu-title").replace("$number$", "" + (int) betAmount));

        for (Integer i : StaticUtils.decoSpots) {
            inventory.setItem(i, new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE.parseMaterial()).name(" ").build());
        }

        ItemBuilder dealerhead = new ItemBuilder(XMaterial.PLAYER_HEAD.parseMaterial());
        dealerhead.dealerHead();
        dealerhead.name(StaticUtils.translate("&eDealer's Hand"));
        dealerhead.lore("");
        dealerhead.lore(StaticUtils.translate("&fTotal: &6" + StaticUtils.getScoreUnder21(houseCards)));
        for (String card : houseCards) {
            dealerhead.lore(StaticUtils.translate("&7- " + StaticUtils.convertToFullText(card)));
        }
        inventory.setItem(10, dealerhead.build());

        ItemBuilder playerhead = new ItemBuilder(XMaterial.PLAYER_HEAD.parseMaterial());
        playerhead.playerHead(player);
        playerhead.name(StaticUtils.translate("&eYour Hand"));
        playerhead.lore("");
        playerhead.lore(StaticUtils.translate("&fTotal: &6" + StaticUtils.getScoreUnder21(playerCards)));
        for (String card : playerCards) {
            playerhead.lore(StaticUtils.translate("&7- " + StaticUtils.convertToFullText(card)));
        } inventory.setItem(28, playerhead.build());

        int invSlot = 29;
        for (String card : playerCards) {
            inventory.setItem(invSlot, StaticUtils.cardAsItemStack(card, true));
            invSlot++;
        }

        int dealersSlot = 11;
        for (String card : houseCards) {
            inventory.setItem(dealersSlot, StaticUtils.cardAsItemStack(card, true));
            dealersSlot++;
        }
        return inventory;
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
