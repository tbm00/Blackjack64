package dev.tbm00.spigot.blackjack64.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;

import dev.tbm00.spigot.blackjack64.Blackjack64;
import dev.tbm00.spigot.blackjack64.object.BlackjackGame;
import dev.tbm00.spigot.blackjack64.object.BlackjackPlayer;
import dev.tbm00.spigot.blackjack64.object.GameSession;
import dev.tbm00.spigot.blackjack64.util.GuiUtils;
import dev.tbm00.spigot.blackjack64.util.StaticUtils;

public class BlackjackGameGui {
    Blackjack64 javaPlugin;
    public Player player;
    public GameSession session;
    public BlackjackGame game;
    public Gui gui;
    public boolean isHidden, isDoubleDown;
    
    public BlackjackGameGui(Blackjack64 javaPlugin, Player player, GameSession session, BlackjackGame game, boolean isHidden, boolean isDoubleDown) {
        this.javaPlugin = javaPlugin;
        this.player = player;
        this.session = session;
        this.game = game;
        this.isHidden = isHidden;
        this.isDoubleDown = isDoubleDown;

        gui = new Gui(6, StaticUtils.translate(StaticUtils.getString("hand-menu-title").replace("$bet$", "" + StaticUtils.formatInt(game.getBetAmount()))));
        
        fillGui();
        
        gui.disableAllInteractions();
        GuiUtils.disableAll(gui);
        
        gui.open(player);

        checkIfMaxScore();
    }

    private void checkIfMaxScore() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (StaticUtils.getScoreUnder21(game.getPlayerCards()) > 21) {
                    playEndAnimation();
                
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            javaPlugin.getBlackjackPlayer(player).addLoss();
                            javaPlugin.getBlackjackPlayer(player).adjustWinnings(-1*game.getBetAmount());
                            session.endGame(game, BlackjackGame.Ending.LOSE);
                            return;
                        }
                    }.runTaskLater(javaPlugin, 8);
                }
            }
        }.runTaskLater(javaPlugin, 12*1);
    }

    private void fillGui() {
        ItemStack item = new ItemStack(Material.GLASS);
        ItemMeta meta = item.getItemMeta();
        SkullMeta smeta;
        List<String> lore = new ArrayList<>();

        // deco spots
        for (Integer i : StaticUtils.decoSpots) {
            gui.setItem(i, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> GuiUtils.handleNullClick(event)));
        }

        // player's head
        item.setType(Material.PLAYER_HEAD);
        smeta = (SkullMeta) item.getItemMeta();
        smeta.setOwningPlayer(player);
        lore.add("");
        lore.add("&fTotal: &6" + StaticUtils.getScoreUnder21(game.getPlayerCards()));
        for (String card : game.getPlayerCards()) {
            lore.add("&7- " + StaticUtils.convertToFullText(card));
        } 
        smeta.setLore(lore.stream().map(l -> StaticUtils.translate(l)).toList());
        smeta.setDisplayName(StaticUtils.translate(StaticUtils.getString("your-hand-game-item")));
        item.setItemMeta(smeta);
        gui.setItem(28, ItemBuilder.from(item).asGuiItem(event -> GuiUtils.handleNullClick(event)));
        lore.clear();
        meta.setLore(lore);
        item.setItemMeta(meta);

        // player's cards
        int playerSlot = 29;
        for (String card : game.getPlayerCards()) {
            gui.setItem(playerSlot, ItemBuilder.from(StaticUtils.cardAsItemStack(item, card, true)).asGuiItem(event -> GuiUtils.handleNullClick(event)));
            playerSlot++;
        } item.setAmount(1);

        
        if (isHidden) {
            // dealer's head
            item.setType(Material.PLAYER_HEAD);
            smeta = (SkullMeta) item.getItemMeta();
            lore.add("");
            lore.add("&fTotal: &6???");
            lore.add("&7- " + StaticUtils.convertToFullText(game.getHouseCards().get(0)));
            smeta.setLore(lore.stream().map(l -> StaticUtils.translate(l)).toList());
            smeta.setDisplayName(StaticUtils.translate(StaticUtils.getString("dealers-hand-game-item")));
            item.setItemMeta(smeta);
            gui.setItem(10, ItemBuilder.from(item).asGuiItem(event -> GuiUtils.handleNullClick(event)));
            lore.clear();
            meta.setLore(lore);
            item.setItemMeta(meta);

            // dealers's cards
            int dealerSlot = 11;
            for (String card : game.getHouseCards()) {
                if (dealerSlot == 11) {
                    gui.setItem(dealerSlot, ItemBuilder.from(StaticUtils.cardAsItemStack(item, card, true)).asGuiItem(event -> GuiUtils.handleNullClick(event)));
                    dealerSlot++;
                } else {
                    gui.setItem(dealerSlot, ItemBuilder.from(StaticUtils.cardAsItemStack(item, card, false)).asGuiItem(event -> GuiUtils.handleNullClick(event)));
                    dealerSlot++;
                }
            }

            if ((StaticUtils.getScoreUnder21(game.getPlayerCards()) <= 21)) {
                if (!isDoubleDown) {
                    // stand item
                    item.setType(Material.BARRIER);
                    lore.add("&7Stay with current hand");
                    meta.setLore(lore.stream().map(l -> StaticUtils.translate(l)).toList());
                    meta.setDisplayName(StaticUtils.translate(StaticUtils.getString("stand-item")));
                    item.setItemMeta(meta);
                    gui.setItem(16, ItemBuilder.from(item).asGuiItem(event -> GuiUtils.handleStandClick(event, this)));
                    lore.clear();
                    meta.setLore(lore);
                    item.setItemMeta(meta);

                    // hit item
                    item.setType(Material.NETHER_STAR);
                    lore.add("&7Draw 1 more card");
                    meta.setLore(lore.stream().map(l -> StaticUtils.translate(l)).toList());
                    meta.setDisplayName(StaticUtils.translate(StaticUtils.getString("hit-item")));
                    item.setItemMeta(meta);
                    gui.setItem(25, ItemBuilder.from(item).asGuiItem(event -> GuiUtils.handleHitClick(event, this)));
                    lore.clear();
                    meta.setLore(lore);
                    item.setItemMeta(meta);

                    // surrender item
                    if (javaPlugin.isSurrenderEnabled()) {
                        item.setType(Material.DARK_OAK_DOOR);
                        lore.add("&7End hand and keep half your bet");
                        meta.setLore(lore.stream().map(l -> StaticUtils.translate(l)).toList());
                        meta.setDisplayName(StaticUtils.translate(StaticUtils.getString("surrender-item")));
                        item.setItemMeta(meta);
                        gui.setItem(43, ItemBuilder.from(item).asGuiItem(event -> GuiUtils.handleSurrenderClick(event, this)));
                        lore.clear();
                        meta.setLore(lore);
                        item.setItemMeta(meta);
                    }

                    // double down item
                    if (game.getPlayerCards().size()==2 && javaPlugin.isDoubleDownEnabled()) {
                        if (Blackjack64.getEconomy().getBalance(player) >= game.getBetAmount()) {
                            item.setType(Material.ARROW);
                            lore.add("&7Double bet amount and stand after 1 more card");
                            meta.setLore(lore.stream().map(l -> StaticUtils.translate(l)).toList());
                            meta.setDisplayName(StaticUtils.translate(StaticUtils.getString("double-down-item")));
                            item.setItemMeta(meta);
                            gui.setItem(34, ItemBuilder.from(item).asGuiItem(event -> GuiUtils.handleDoubleClick(event, this)));
                            lore.clear();
                            meta.setLore(lore);
                            item.setItemMeta(meta);
                        }
                    }
                }
            }
        } else {
            // dealer's head
            item.setType(Material.PLAYER_HEAD);
            smeta = (SkullMeta) item.getItemMeta();
            lore.add("");
            lore.add("&fTotal: &6" + StaticUtils.getScoreUnder21(game.getHouseCards()));
            for (String card : game.getHouseCards()) {
                lore.add("&7- " + StaticUtils.convertToFullText(card));
            } 
            smeta.setLore(lore.stream().map(l -> StaticUtils.translate(l)).toList());
            smeta.setDisplayName(StaticUtils.translate(StaticUtils.getString("dealers-hand-game-item")));
            item.setItemMeta(smeta);
            gui.setItem(10, ItemBuilder.from(item).asGuiItem(event -> GuiUtils.handleNullClick(event)));
            lore.clear();
            meta.setLore(lore);
            item.setItemMeta(meta);

            // dealers's cards
            int dealerSlot = 11;
            for (String card : game.getHouseCards()) {
                gui.setItem(dealerSlot, ItemBuilder.from(StaticUtils.cardAsItemStack(item, card, true)).asGuiItem(event -> GuiUtils.handleNullClick(event)));
                dealerSlot++;
            }
        }
    }

    public void processDealerTurnWithAnimation(Player player) {
        final BlackjackPlayer bjp = javaPlugin.getBlackjackPlayer(player);
        final ArrayList<String> houseCards = game.getHouseCards();
        int dealerScore = StaticUtils.getScoreUnder21(houseCards);
        int playerScore = StaticUtils.getScoreUnder21(game.getPlayerCards());

        if (dealerScore>=17) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    playEndAnimation();

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            // dealer stands, end game
                            if (dealerScore > 21) {
                                session.endGame(game,BlackjackGame.Ending.WIN);
                                bjp.addWin();
                                bjp.adjustWinnings(game.getBetAmount());
                            } else if (dealerScore > playerScore) {
                                // house wins
                                bjp.addLoss();
                                bjp.adjustWinnings(-1*game.getBetAmount());
                                session.endGame(game,BlackjackGame.Ending.LOSE);
                            } else if (playerScore > dealerScore) {
                                // player wins
                                session.endGame(game,BlackjackGame.Ending.WIN);
                                bjp.addWin();
                                bjp.adjustWinnings(game.getBetAmount());
                            } else if (playerScore == dealerScore) {
                                // tie
                                session.endGame(game,BlackjackGame.Ending.TIE);
                                bjp.addTie();
                            }
                        }
                    }.runTaskLater(javaPlugin, 8);
                }
            }.runTaskLater(javaPlugin, 12*1);

        } else { // dealer hits, draw another card and update inv
            new BukkitRunnable() {
                @Override
                public void run() {
                    playCircleAnimation();
    
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            game.getNextCard();
                            
                            BlackjackGameGui newGui = new BlackjackGameGui(javaPlugin, Blackjack64.getInstance().getServer().getPlayer(bjp.getUuid()), session, game, false, false);
                    
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    newGui.processDealerTurnWithAnimation(player);
                                }
                            }.runTaskLater(javaPlugin, 12*1);
                        }
                    }.runTaskLater(javaPlugin, (StaticUtils.decoSpots.length+4));
                }
            }.runTaskLater(javaPlugin, 12*1);
        }
    }

    public void playCircleAnimation() {
        gui.removeItem(16);
        gui.removeItem(25);
        gui.removeItem(34);
        gui.removeItem(43);
        gui.update();

        int decoSize = StaticUtils.decoSpots.length;

        new BukkitRunnable() {
            @Override
            public void run() {
                int frameIndex = 0;

                // Start animation (first X frames)
                while (frameIndex<decoSize) {
                    final int tempIndex = frameIndex;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            gui.setItem(StaticUtils.decoSpots[tempIndex], ItemBuilder.from(Material.WHITE_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> GuiUtils.handleNullClick(event)));
                            if (tempIndex >= 1) {
                                gui.setItem(StaticUtils.decoSpots[tempIndex-1], ItemBuilder.from(Material.LIGHT_GRAY_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> GuiUtils.handleNullClick(event)));
                                if (tempIndex >= 2) {
                                    gui.setItem(StaticUtils.decoSpots[tempIndex-2], ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> GuiUtils.handleNullClick(event)));
                                    if (tempIndex >= 3) {
                                        gui.setItem(StaticUtils.decoSpots[tempIndex-3], ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> GuiUtils.handleNullClick(event)));
                                    }
                                }
                            }
                            gui.update();
                        }
                    }.runTaskLater(javaPlugin, frameIndex);
                    frameIndex++;
                }

                // End animation (last 4 frames)
                while (frameIndex<(decoSize+4)) {
                    final int tempIndex = frameIndex;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (tempIndex==decoSize) {
                                gui.setItem(StaticUtils.decoSpots[0], ItemBuilder.from(Material.WHITE_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> GuiUtils.handleNullClick(event)));
                                gui.setItem(StaticUtils.decoSpots[decoSize-1], ItemBuilder.from(Material.LIGHT_GRAY_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> GuiUtils.handleNullClick(event)));
                                gui.setItem(StaticUtils.decoSpots[decoSize-2], ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> GuiUtils.handleNullClick(event)));
                                gui.setItem(StaticUtils.decoSpots[decoSize-3], ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> GuiUtils.handleNullClick(event)));
                            } else if (tempIndex==decoSize+1) {
                                gui.setItem(StaticUtils.decoSpots[0], ItemBuilder.from(Material.LIGHT_GRAY_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> GuiUtils.handleNullClick(event)));
                                gui.setItem(StaticUtils.decoSpots[decoSize-1], ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> GuiUtils.handleNullClick(event)));
                                gui.setItem(StaticUtils.decoSpots[decoSize-2], ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> GuiUtils.handleNullClick(event)));
                            } else if (tempIndex==decoSize+2) {
                                gui.setItem(StaticUtils.decoSpots[0], ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> GuiUtils.handleNullClick(event)));
                                gui.setItem(StaticUtils.decoSpots[decoSize-1], ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> GuiUtils.handleNullClick(event)));
                            } else if (tempIndex==decoSize+3) {
                                gui.setItem(StaticUtils.decoSpots[0], ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> GuiUtils.handleNullClick(event)));
                            }
                            gui.update();
                        }
                    }.runTaskLater(javaPlugin, frameIndex);
                    frameIndex++;
                }
            }
        }.runTask(javaPlugin);
    }

    public void playEndAnimation() {
        gui.removeItem(16);
        gui.removeItem(25);
        gui.removeItem(34);
        gui.removeItem(43);
        gui.update();

        int decoSize = StaticUtils.decoSpots.length;

        for (int i=0; i<decoSize; i++) {
            gui.setItem(StaticUtils.decoSpots[i], ItemBuilder.from(Material.WHITE_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> GuiUtils.handleNullClick(event)));
        }
        gui.update();

        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i=0; i<decoSize; i++) {
                    gui.setItem(StaticUtils.decoSpots[i], ItemBuilder.from(Material.LIGHT_GRAY_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> GuiUtils.handleNullClick(event)));
                }
                gui.update();
            }
        }.runTaskLater(javaPlugin, 2);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i=0; i<decoSize; i++) {
                    gui.setItem(StaticUtils.decoSpots[i], ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> GuiUtils.handleNullClick(event)));
                }
                gui.update();
            }
        }.runTaskLater(javaPlugin, 4);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i=0; i<decoSize; i++) {
                    gui.setItem(StaticUtils.decoSpots[i], ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> GuiUtils.handleNullClick(event)));
                }
                gui.update();
            }
        }.runTaskLater(javaPlugin, 6);
    }
}