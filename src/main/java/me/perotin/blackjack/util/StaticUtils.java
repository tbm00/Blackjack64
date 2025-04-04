package me.perotin.blackjack.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import me.perotin.blackjack.Blackjack;

public class StaticUtils {

    public static final ItemStack dealerHead = new ItemStack(Material.PLAYER_HEAD);
    public static final Random random = new Random();
    public static final int[] decoSpots = {0,1,2,3,4,5,6,7,8,
                                            17,26,35,44,53,
                                            52,51,50,49,48,47,46,45,
                                            36,27,18,9};

    public static void init(Blackjack plugin) {
        SkullMeta meta = (SkullMeta) dealerHead.getItemMeta();
        meta.setOwningPlayer(null);
    }

    public static String translate(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    /**
     *
     * @param cards
     * @return most optimal score under 21 for the cards
     */
    public static int getScoreUnder21(List<String> cards) {
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

    public static int valueOfCard(String card) {
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

    public static ItemStack cardAsItemStack(String card, boolean show) {
        int amount = getCardValue(card);

        if (show) {
            ItemBuilder builder = new ItemBuilder(XMaterial.PAPER.parseItem());
            builder.amount(amount);
            builder.name(ChatColor.YELLOW + convertToFullText(card));
            return builder.build();
        } else {
            ItemBuilder build = new ItemBuilder(XMaterial.MAP.parseItem());
            build.name(Blackjack.getInstance().getString("unknown-card"));
            return build.build();
        }
    }

    public static int getCardValue(String card) {
        String rank = card.substring(0, card.length() - 1);
    
        switch (rank) {
            case "A":
                return 1;
            case "J":
            case "Q":
            case "K":
                return 10;
            default:
                return Integer.parseInt(rank);
        }
    }
    

    public static String convertToFullText(String card) {
        Blackjack plugin = Blackjack.getInstance();
        String identifier = "";
        if (card.length() == 2) {
            identifier = card.substring(0, 1);
        } else if (card.length() == 3) {
            identifier = card.substring(0, 2);
        }

        String suit = card.substring(card.length() - 1);
        switch (suit) {
            case "s":
                suit = plugin.getString("spades");
                break;
            case "d":
                suit = plugin.getString("diamonds");;
                break;
            case "h":
                suit = plugin.getString("hearts");;
                break;
            case "c":
                suit = plugin.getString("clubs");;
        }
        switch (identifier) {
            case "J":
                identifier = plugin.getString("jack");
                break;
            case "Q":
                identifier = plugin.getString("queen");
                break;
            case "K":
                identifier = plugin.getString("king");
                break;
            case "A":
                identifier = plugin.getString("ace");
        }

        return identifier + " of " + suit;
    }
}
