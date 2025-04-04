package me.perotin.blackjack.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import me.perotin.blackjack.Blackjack;
import me.perotin.blackjack.util.StaticUtils;
import me.perotin.blackjack.util.ItemBuilder;
import me.perotin.blackjack.util.XMaterial;

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

    private double getTotalEarnings(){
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

        ItemBuilder stopPlaying = new ItemBuilder(XMaterial.RED_BANNER.parseItem());
        stopPlaying.name(Blackjack.getInstance().getString("stop-playing"));

        if(getTotalEarnings() > 0) {
           getPlayer().sendMessage(Blackjack.getInstance().getString("session-ending")
                   .replace("$amount$", games.size() + "").replace("$outcome$", ChatColor.GREEN + "+"+getTotalEarnings() + ""));
        } else if (getTotalEarnings() < 0) {
           getPlayer().sendMessage(Blackjack.getInstance().getString("session-ending")
                   .replace("$amount$", games.size() + "").replace("$outcome$", ChatColor.RED + ""+getTotalEarnings()));
        } else {
           getPlayer().sendMessage(Blackjack.getInstance().getString("session-ending")
                   .replace("$amount$", games.size() + "").replace("$outcome$", getTotalEarnings()+"" ));
        }

        Blackjack.getInstance().getSessions().remove(this);

        if (getPlayer().getInventory().contains(stopPlaying.build())) {
            // "people can shift click item into their inventory so remove it"
            getPlayer().getInventory().remove(stopPlaying.build());
        }
    }


    public void endGame(BlackjackGame game, BlackjackGame.Ending end) {
        Blackjack plugin = Blackjack.getInstance();
        game.setEnd(end);
        double betAmount = game.getBetAmount();
        Player player = game.getPlayer();
        int playerScore = StaticUtils.getScoreUnder21(game.getPlayerCards());
        double multiplierAmount = (plugin.getBlackJackMultiplier() * betAmount) + betAmount;
        boolean blackJack = false;

        if (end == BlackjackGame.Ending.WIN) {

            boolean taxxed = false;
            if (plugin.getTaxPercent() != 0.0 && plugin.getTaxPercent() <= 100.0) {
                double tax = plugin.getTaxPercent() / 100.0;
                double postTax = betAmount - (tax * betAmount);

                if (playerScore == 21 && plugin.getBlackJackMultiplier() > 1) {
                    blackJack = true;
                    game.setBlackjack(true);

                    Blackjack.deposit( (plugin.getBlackJackMultiplier()*postTax) + betAmount, player);
                } else Blackjack.deposit( postTax + betAmount, player);

                taxxed = true;
            } else {
                if (playerScore == 21 && plugin.getBlackJackMultiplier() > 1) {
                    blackJack = true;
                    game.setBlackjack(true);

                    Blackjack.deposit(multiplierAmount, player);
                } else Blackjack.deposit( betAmount + betAmount, player);
            }

            // player.sendMessage(plugin.getString("earnings").replace("$result$", plugin.getString("won")).replace("$number$", earnings + ""));

            if(blackJack) {
                plugin.setServerImpact(plugin.getServerImpact() - (plugin.getBlackJackMultiplier()* betAmount));

            } else plugin.setServerImpact(plugin.getServerImpact() - betAmount);

            plugin.increaseGamesPlayed();
            plugin.increaseServerLosses();

                if (taxxed) {
                    double tax = plugin.getTaxPercent() / 100.0;
                    double postTax = tax * betAmount;
                    player.sendMessage(plugin.getString("taxxed").replace("$amount$", postTax+""));

                } else {
                    if (blackJack) {
                        player.sendMessage(StaticUtils.translate("&aYou won &2$" + (plugin.getBlackJackMultiplier() * betAmount) + " &afrom that black jack! &7&o(blackjack multipler: x" + plugin.getConfig().getDouble("multiplier")+")"));
                    }
                }

        } else if (end == BlackjackGame.Ending.LOSE) {
            // player.sendMessage(plugin.getString("earnings").replace("$result$", plugin.getString("lost")).replace("$number$", getBetAmount() + ""));
            plugin.setServerImpact(plugin.getServerImpact() + betAmount);
            plugin.increaseGamesPlayed();
            plugin.increaseServerWins();

        } else if (end == BlackjackGame.Ending.TIE) {
            // tie
           Blackjack.deposit(betAmount, player);
            plugin.increaseGamesPlayed();

        } else {
            // they surrender
            double surrender = betAmount - (betAmount * (plugin.getSurrenderPercentage() / 100));
            // player.sendMessage(plugin.getString("surrender-message").replace("$amount$", surrender+"").replace("$bet$", betAmount+""));

            Blackjack.deposit(surrender, player);
            plugin.setServerImpact(plugin.getServerImpact() + surrender);
            plugin.increaseGamesPlayed();
            plugin.increaseServerWins();
        }
        
        showEndMenu(game);
    }

    public void startNewGame(){
        BlackjackPlayer player = Blackjack.getInstance().getPlayerFor(getPlayer());
        if(player.getBalance() >= betAmount) {
            BlackjackGame game = new BlackjackGame(getPlayer(), betAmount);
            getGames().add(game);
             Blackjack.withdraw(betAmount, getPlayer());

            getPlayer().openInventory(game.getInventory(true, false));
        } else {
            getPlayer().closeInventory();
            endSession();
            getPlayer().sendMessage(Blackjack.getInstance().getString("cannot-bet-that-much"));
        }

    }

    public void showEndMenu(BlackjackGame game){
        Blackjack plugin = Blackjack.getInstance();

        String sessiontotal = getTotalEarnings()>=0 ? "&2$"+(int)getTotalEarnings() : "&4$"+(int)getTotalEarnings();
        String handtotal = game.getResult()>=0 ? "&a$"+(int)game.getResult() : "&c$"+(int)game.getResult();
        String name = StaticUtils.translate("&8Session: "+sessiontotal+"&8, Hand: "+handtotal);
        Inventory menu = Bukkit.createInventory(null, 54, name);

        if (StaticUtils.getScoreUnder21(game.getPlayerCards()) == 21 && game.getResult() > 0 && game.isBlackjackEnding()) {
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    for (Integer slot : StaticUtils.decoSpots) {
                        if (StaticUtils.random.nextBoolean()) {
                            menu.setItem(slot, new ItemBuilder(XMaterial.GREEN_STAINED_GLASS_PANE.parseItem())
                                .name(" ")
                                .build());
                        } else {
                            menu.setItem(slot, new ItemBuilder(XMaterial.LIME_STAINED_GLASS_PANE.parseItem())
                            .name(" ")
                            .build());
                        }
                    }
                }
            };
        
            // Run the animation repeatedly with a delay of 12 ticks between iterations
            runnable.runTaskTimer(plugin, 0, 12);
        
            // Cancel the animation after 30 seconds
            new BukkitRunnable() {
                @Override
                public void run() {
                    runnable.cancel();
                }
            }.runTaskLater(plugin, 20 * 30);
        } else if (game.getResult()>0) {
            for (Integer i : StaticUtils.decoSpots) {
                menu.setItem(i, new ItemBuilder(XMaterial.GREEN_STAINED_GLASS_PANE.parseMaterial()).name(" ").build());
            }
        } else if (game.getResult()<0) {
            for (Integer i : StaticUtils.decoSpots) {
                menu.setItem(i, new ItemBuilder(XMaterial.RED_STAINED_GLASS_PANE.parseMaterial()).name(" ").build());
            }
        } else {
            for (Integer i : StaticUtils.decoSpots) {
                menu.setItem(i, new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE.parseMaterial()).name(" ").build());
            }
        }

        ItemBuilder summary = new ItemBuilder(XMaterial.BOOK.parseItem());
        summary.name(StaticUtils.translate("&eSession Summary"));
        summary.lore("");
        summary.lore(StaticUtils.translate("&7Total: " + sessiontotal));
        summary.lore(StaticUtils.translate("&7Prior Hand: " + handtotal));
        menu.setItem(13, summary.build());

        ItemBuilder dealerhead = new ItemBuilder(XMaterial.PLAYER_HEAD.parseMaterial());
        dealerhead.dealerHead();
        dealerhead.name(StaticUtils.translate("&eDealer's Prior Hand"));
        int dealerScore = StaticUtils.getScoreUnder21(game.getHouseCards());
        if (dealerScore==21) {
            dealerhead.lore("");
            dealerhead.lore(StaticUtils.translate("&6BLACKJACK"));
        } else if (dealerScore > 21) {
            dealerhead.lore("");
            dealerhead.lore(StaticUtils.translate("&4BUST"));
        } dealerhead.lore("");
        dealerhead.lore(StaticUtils.translate("&fTotal: &6" + dealerScore));
        dealerhead.amount(dealerScore);
        for (String card : game.getHouseCards()) {
            dealerhead.lore(StaticUtils.translate("&7- " + StaticUtils.convertToFullText(card)));
        } menu.setItem(11, dealerhead.build());

        ItemBuilder playerhead = new ItemBuilder(XMaterial.PLAYER_HEAD.parseMaterial());
        playerhead.playerHead(game.getPlayer());
        playerhead.name(StaticUtils.translate("&eYour Prior Hand"));
        int playerScore = StaticUtils.getScoreUnder21(game.getPlayerCards());
        if (playerScore==21) {
            playerhead.lore("");
            playerhead.lore(StaticUtils.translate("&6BLACKJACK"));
        } else if (playerScore > 21) {
            playerhead.lore("");
            playerhead.lore(StaticUtils.translate("&4BUST"));
        } playerhead.lore("");
        playerhead.lore(StaticUtils.translate("&fTotal: &6" + playerScore));
        playerhead.amount(playerScore);
        for (String card : game.getPlayerCards()) {
            playerhead.lore(StaticUtils.translate("&7- " + StaticUtils.convertToFullText(card)));
        } menu.setItem(15, playerhead.build());

        ItemBuilder keepPlaying = new ItemBuilder(XMaterial.GREEN_BANNER.parseItem());
        keepPlaying.name(plugin.getString("keep-playing"));
        menu.setItem(32, keepPlaying.build());

        ItemBuilder stopPlaying = new ItemBuilder(XMaterial.RED_BANNER.parseItem());
        stopPlaying.name(plugin.getString("stop-playing"));
        menu.setItem(30, stopPlaying.build());

        if(Blackjack.getInstance().getConfig().getBoolean("enable-change-bet")) {
            int betIncrease1 = Blackjack.getInstance().getConfig().getInt("bet-modifiers.increaseValue1");
            int betIncrease2 = Blackjack.getInstance().getConfig().getInt("bet-modifiers.increaseValue2");
            int betIncrease3 = Blackjack.getInstance().getConfig().getInt("bet-modifiers.increaseValue3");
            int betIncrease4 = Blackjack.getInstance().getConfig().getInt("bet-modifiers.increaseValue4");
            int betIncrease5 = Blackjack.getInstance().getConfig().getInt("bet-modifiers.increaseValue5");
            int betDecrease1 = Blackjack.getInstance().getConfig().getInt("bet-modifiers.decreaseValue1");
            int betDecrease2 = Blackjack.getInstance().getConfig().getInt("bet-modifiers.decreaseValue2");
            int betDecrease3 = Blackjack.getInstance().getConfig().getInt("bet-modifiers.decreaseValue3");
            int betDecrease4 = Blackjack.getInstance().getConfig().getInt("bet-modifiers.decreaseValue4");
            int betDecrease5 = Blackjack.getInstance().getConfig().getInt("bet-modifiers.decreaseValue5");

            menu.setItem(28, new ItemBuilder(XMaterial.STONE_BUTTON.parseItem().getType()).name(ChatColor.RED + "- $" + betDecrease1).build());
            menu.setItem(29, new ItemBuilder(XMaterial.STONE_BUTTON.parseItem().getType()).name(ChatColor.RED + "- $" + betDecrease2).build());
            menu.setItem(37, new ItemBuilder(XMaterial.STONE_BUTTON.parseItem().getType()).name(ChatColor.RED + "- $" + betDecrease3).build());
            menu.setItem(38, new ItemBuilder(XMaterial.STONE_BUTTON.parseItem().getType()).name(ChatColor.RED + "- $" + betDecrease4).build());
            menu.setItem(39, new ItemBuilder(XMaterial.STONE_BUTTON.parseItem().getType()).name(ChatColor.RED + "- $" + betDecrease5).build());

            menu.setItem(33, new ItemBuilder(XMaterial.STONE_BUTTON.parseItem().getType()).name(ChatColor.GREEN + "+ $" + betIncrease1).build());
            menu.setItem(34, new ItemBuilder(XMaterial.STONE_BUTTON.parseItem().getType()).name(ChatColor.GREEN + "+ $" + betIncrease2).build());
            menu.setItem(41, new ItemBuilder(XMaterial.STONE_BUTTON.parseItem().getType()).name(ChatColor.GREEN + "+ $" + betIncrease3).build());
            menu.setItem(42, new ItemBuilder(XMaterial.STONE_BUTTON.parseItem().getType()).name(ChatColor.GREEN + "+ $" + betIncrease4).build());
            menu.setItem(43, new ItemBuilder(XMaterial.STONE_BUTTON.parseItem().getType()).name(ChatColor.GREEN + "+ $" + betIncrease5).build());

            menu.setItem(40, new ItemBuilder(XMaterial.GOLD_INGOT.parseMaterial()).name(StaticUtils.translate("&eChange Bet Amount")).lore(StaticUtils.translate("&7Betting &f$" + betAmount)).build());
        } else {
            menu.setItem(40, new ItemBuilder(XMaterial.GOLD_INGOT.parseMaterial()).name(StaticUtils.translate("&eChange Bet Amount")).lore(StaticUtils.translate("&7Betting &f$" + betAmount)).build());
        }

        if (menu != null && game.getPlayer() != null) {
            game.getPlayer().openInventory(menu);
        }
    }
}
