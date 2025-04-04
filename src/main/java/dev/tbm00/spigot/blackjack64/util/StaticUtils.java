package dev.tbm00.spigot.blackjack64.util;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import dev.tbm00.spigot.blackjack64.Blackjack64;
import dev.tbm00.spigot.blackjack64.gui.BlackjackGameGui;
import dev.tbm00.spigot.blackjack64.gui.BlackjackSessionGui;
import dev.tbm00.spigot.blackjack64.object.BlackjackGame;
import dev.tbm00.spigot.blackjack64.object.BlackjackPlayer;
import dev.tbm00.spigot.blackjack64.object.GameSession;

public class StaticUtils {

    public static Blackjack64 plugin;
    public static final ItemStack dealerHead = new ItemStack(Material.PLAYER_HEAD);
    public static final Random random = new Random();
    public static final int[] decoSpots = {0,1,2,3,4,5,6,7,8,
                                            17,26,35,44,53,
                                            52,51,50,49,48,47,46,45,
                                            36,27,18,9};

    public static void init(Blackjack64 jplugin) {
        plugin = jplugin;
        SkullMeta meta = (SkullMeta) dealerHead.getItemMeta();
        meta.setOwningPlayer(null);
    }

    public static void log(ChatColor chatColor, String... strings) {
		for (String s : strings)
            plugin.getServer().getConsoleSender().sendMessage("[BJ] " + chatColor + s);
	}

    public static void sendConfigMessage(Player player, String pathTomessage){
        player.sendMessage(translate(getString(pathTomessage)));
    }

    public static String translate(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static String getString(String path){
        if (!plugin.getConfig().contains(path)) {
            Bukkit.getLogger().info("[Blackjack] Could not find config path!: " + path);
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString(path));
    }

    public static String getString(String path, String alternative){
        return plugin.getConfig().getString(path) != null ? 
            ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString(path)) : alternative;
    }

    /**
     * Formats int to "200,000" style
     */
    public static String formatInt(int amount) {
        return NumberFormat.getNumberInstance(Locale.US).format(amount);
    }

    /**
     * Formats double to "200,000" style
     */
    public static String formatInt(double amount) {
        return formatInt((int) amount);
    }
    
    public static void withdraw(double amount, Player player){
        Blackjack64.getEconomy().withdrawPlayer(player, amount);
    }

    public static void deposit(double amount, Player player){
        Blackjack64.getEconomy().depositPlayer(player, amount);
    }

    /**
     * @return most optimal score under 21 for the cards
     */
    public static int getScoreUnder21(List<String> cards) {
        List<String> cardsCopy = new ArrayList<>(cards);
        List<String> cardsToRemoveAndAdd = new ArrayList<>();

        for (String s : cardsCopy) {
            if (s.startsWith("A")) {
                cardsToRemoveAndAdd.add(s);
            }
        }
        cardsCopy.removeAll(cardsToRemoveAndAdd);
        cardsCopy.addAll(cardsToRemoveAndAdd);

        int score = 0;
        for (String s : cardsCopy) {
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

    public static ItemStack cardAsItemStack(ItemStack item, String card, boolean show) {
        int amount = getCardValue(card);

        if (show) {
            item.setType(Material.PAPER);
            item.setAmount(amount);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(StaticUtils.translate(StaticUtils.getString("prefix-card-item")+convertToFullText(card)));
            item.setItemMeta(meta);
            return item;
        } else {
            item.setType(Material.MAP);
            item.setAmount(1);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(StaticUtils.translate(StaticUtils.getString("prefix-card-item")+StaticUtils.getString("hidden-card-item")));
            item.setItemMeta(meta);
            return item;
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
        String identifier = "";
        if (card.length() == 2) {
            identifier = card.substring(0, 1);
        } else if (card.length() == 3) {
            identifier = card.substring(0, 2);
        }

        String suit = card.substring(card.length() - 1);
        switch (suit) {
            case "s":
                suit = StaticUtils.getString("spades");
                break;
            case "d":
                suit = StaticUtils.getString("diamonds");;
                break;
            case "h":
                suit = StaticUtils.getString("hearts");;
                break;
            case "c":
                suit = StaticUtils.getString("clubs");;
        }
        switch (identifier) {
            case "J":
                identifier = StaticUtils.getString("jack");
                break;
            case "Q":
                identifier = StaticUtils.getString("queen");
                break;
            case "K":
                identifier = StaticUtils.getString("king");
                break;
            case "A":
                identifier = StaticUtils.getString("ace");
        }

        return identifier + " of " + suit;
    }

    public static boolean handleNewGame(Player player, Double betAmount) {
        BlackjackPlayer bjp = Blackjack64.getInstance().getBlackjackPlayer(player);
        double betMax = plugin.getBetMax();
        double betMin = plugin.getBetMin();

        if(betAmount < 0){
            betAmount = 0.0;
        }
        if(betMax > 0 && betAmount > betMax){
            player.sendMessage(StaticUtils.getString("bet-max-message")
            .replace("$amount$", betMax+""));
            return true;
        }
        if(betMin > 0 && betAmount < betMin){
            player.sendMessage(StaticUtils.getString("bet-min-message")
                    .replace("$amount$", betMin+""));
            return true;
        }
        if(betAmount > bjp.getBalance()) {
            // can't
            player.sendMessage(StaticUtils.getString("cannot-bet-that-much"));
            return true;
        }
        

        BlackjackGame game = new BlackjackGame(player, betAmount);
        GameSession session = new GameSession(player.getUniqueId(), game);
        StaticUtils.withdraw(betAmount, player);
        plugin.getSessions().add(session);
        new BlackjackGameGui(plugin, player, session, game, true, false);
        return true;
    }

    public static boolean handleChangeBet(Player player, Double betAmount) {
        BlackjackPlayer bjp = Blackjack64.getInstance().getBlackjackPlayer(player);
        double betMax = plugin.getBetMax();
        double betMin = plugin.getBetMin();

        if(betAmount < 0){
            betAmount = 0.0;
        }
        if(betMax > 0 && betAmount > betMax){
            player.sendMessage(StaticUtils.getString("bet-max-message")
            .replace("$amount$", betMax+""));
            return true;
        }
        if(betMin > 0 && betAmount < betMin){
            player.sendMessage(StaticUtils.getString("bet-min-message")
                    .replace("$amount$", betMin+""));
            return true;
        }
        if(betAmount > bjp.getBalance()){
            // can't
            player.sendMessage(StaticUtils.getString("cannot-bet-that-much"));
            return true;
        }
    


        GameSession session = plugin.getSessionFor(player.getUniqueId());
        if (session != null) {
            for (BlackjackGame game : session.getGames()) {
                if (game.getPlayer().getUniqueId().equals(player.getUniqueId()) && game.getResult() == Double.MAX_VALUE) {
                    // they have an ongoing game
                    // cant change bet amount during hand
                    new BlackjackGameGui(plugin, player, session, game, true, false);
                    return true;
                }
            }

            // they have a session already
            // change bet amount
            session.setBetAmount(betAmount);
            new BlackjackSessionGui(plugin, player, session, session.getGames().get(session.getGames().size()-1));
            return true;
        } else {
            BlackjackGame game = new BlackjackGame(player, betAmount);
            session = new GameSession(player.getUniqueId(), game);
            StaticUtils.withdraw(betAmount, player);
            plugin.getSessions().add(session);
            new BlackjackGameGui(plugin, player, session, game, true, false);
            return true;
        }
    }
}
