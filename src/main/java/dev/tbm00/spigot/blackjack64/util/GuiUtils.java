package dev.tbm00.spigot.blackjack64.util;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scheduler.BukkitRunnable;

import dev.triumphteam.gui.guis.BaseGui;

import dev.tbm00.spigot.blackjack64.Blackjack64;
import dev.tbm00.spigot.blackjack64.gui.BlackjackGameGui;
import dev.tbm00.spigot.blackjack64.gui.BlackjackSessionGui;
import dev.tbm00.spigot.blackjack64.object.BlackjackGame;
import dev.tbm00.spigot.blackjack64.gui.AnvilGui;

public class GuiUtils {

    private static Blackjack64 javaPlugin;

    public static void init(Blackjack64 javaPlugin) {
        GuiUtils.javaPlugin = javaPlugin;
    }

    public static void handleNullClick(InventoryClickEvent event) {
        event.setCancelled(true);
    }

    public static void handleSurrenderClick(InventoryClickEvent event, BlackjackGameGui gui) {
        event.setCancelled(true);

        gui.playEndAnimation();

        new BukkitRunnable() {
            @Override
            public void run() {
                gui.session.endGame(gui.game, BlackjackGame.Ending.SURRENDER);
            }
        }.runTaskLater(javaPlugin, 8);
        return;
    }

    public static void handleDoubleClick(InventoryClickEvent event, BlackjackGameGui gui) {
        event.setCancelled(true);

        gui.playCircleAnimation();

        new BukkitRunnable() {
            @Override
            public void run() {
                StaticUtils.withdraw(gui.game.getBetAmount(), gui.game.getPlayer());
                gui.game.setBetAmount(gui.game.getBetAmount()*2);
                gui.game.getNextCard();
                gui.game.setPlayerTurn(false);

                BlackjackGameGui newGui = new BlackjackGameGui(javaPlugin, (Player) event.getWhoClicked(), gui.session, gui.game, true, true);
            
                if (StaticUtils.getScoreUnder21(gui.game.getPlayerCards())<=21) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            handleStandClick(event, newGui);
                        }
                    }.runTaskLater(javaPlugin, 12*1);
                }
            }
        }.runTaskLater(javaPlugin, (StaticUtils.decoSpots.length)+4);
        return;
    }

    public static void handleHitClick(InventoryClickEvent event, BlackjackGameGui gui) {
        event.setCancelled(true);

        gui.playCircleAnimation();

        new BukkitRunnable() {
            @Override
            public void run() {
                gui.game.getNextCard();
                
                new BlackjackGameGui(javaPlugin, (Player) event.getWhoClicked(), gui.session, gui.game, true, false);
            }
        }.runTaskLater(javaPlugin, (StaticUtils.decoSpots.length)+4);
        return;
    }

    public static void handleStandClick(InventoryClickEvent event, BlackjackGameGui gui) {
        event.setCancelled(true);

        gui.playCircleAnimation();

        new BukkitRunnable() {
            @Override
            public void run() {
                gui.game.setPlayerTurn(false);
                BlackjackGameGui newGui = new BlackjackGameGui(javaPlugin, (Player) event.getWhoClicked(), gui.session, gui.game, false, false);
            
                newGui.processDealerTurnWithAnimation((Player) event.getWhoClicked());
            }
        }.runTaskLater(javaPlugin, (StaticUtils.decoSpots.length)+4);
    }

    public static void handleEndClick(InventoryClickEvent event, BlackjackSessionGui gui) {
        event.setCancelled(true);

        gui.session.endSession();
    }

    public static void handleNextClick(InventoryClickEvent event, BlackjackSessionGui gui) {
        event.setCancelled(true);

        gui.session.startNewGame();
    }

    public static void handleChangeClick(InventoryClickEvent event, BlackjackSessionGui gui) {
        event.setCancelled(true);

        new AnvilGui(javaPlugin, (Player) event.getWhoClicked(), "change");
    }

    public static void handleChangeExactClick(InventoryClickEvent event, BlackjackSessionGui gui, String type, double amount) {
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();

        if (type.equals("decrease")) amount = amount * -1;

        if(gui.session.getBetAmount() + amount > Blackjack64.getInstance().getBlackjackPlayer(player).getBalance()) return;
        if(gui.session.getBetAmount() + amount < 1) return;
        if(javaPlugin.getBetMax() > 0 && gui.session.getBetAmount() + amount > javaPlugin.getBetMax()) return;
        if(javaPlugin.getBetMin() > 0 && gui.session.getBetAmount() + amount < javaPlugin.getBetMin()) return;

        gui.session.setBetAmount(gui.session.getBetAmount() + amount);

        new BlackjackSessionGui(javaPlugin, player, gui.session, gui.session.getGames().get(gui.session.getGames().size()-1)).gui.update();
    }

    public static void disableAll(BaseGui gui) {
        gui.disableItemDrop();
        gui.disableItemPlace();
        gui.disableItemSwap();
        gui.disableItemTake();
        gui.disableOtherActions();
    }
}