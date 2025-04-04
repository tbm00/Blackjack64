package me.perotin.blackjack.util;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import me.perotin.blackjack.Blackjack;
import me.perotin.blackjack.objects.BlackjackGame;
import me.perotin.blackjack.objects.BlackjackPlayer;
import me.perotin.blackjack.objects.GameSession;

public class InvUtils {

    public Blackjack plugin;

    public InvUtils(Blackjack javaPlugin) {
        plugin = javaPlugin;
    }

    public void playAnimation(Inventory menu) {
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
                            menu.setItem(StaticUtils.decoSpots[tempIndex], new ItemBuilder(XMaterial.WHITE_STAINED_GLASS_PANE.parseMaterial()).name(" ").build());
                            if (tempIndex >= 1) {
                                menu.setItem(StaticUtils.decoSpots[tempIndex-1], new ItemBuilder(XMaterial.LIGHT_GRAY_STAINED_GLASS_PANE.parseMaterial()).name(" ").build());
                                if (tempIndex >= 2) {
                                    menu.setItem(StaticUtils.decoSpots[tempIndex-2], new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE.parseMaterial()).name(" ").build());
                                    if (tempIndex >= 3) {
                                        menu.setItem(StaticUtils.decoSpots[tempIndex-3], new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE.parseMaterial()).name(" ").build());
                                    }
                                }
                            }
                        }
                    }.runTaskLater(plugin, frameIndex);
                    frameIndex++;
                }

                // End animation (last 4 frames)
                while (frameIndex<(decoSize+4)) {
                    final int tempIndex = frameIndex;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (tempIndex==decoSize) {
                                menu.setItem(StaticUtils.decoSpots[0], new ItemBuilder(XMaterial.WHITE_STAINED_GLASS_PANE.parseMaterial()).name(" ").build());
                                menu.setItem(StaticUtils.decoSpots[decoSize-1], new ItemBuilder(XMaterial.LIGHT_GRAY_STAINED_GLASS_PANE.parseMaterial()).name(" ").build());
                                menu.setItem(StaticUtils.decoSpots[decoSize-2], new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE.parseMaterial()).name(" ").build());
                                menu.setItem(StaticUtils.decoSpots[decoSize-3], new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE.parseMaterial()).name(" ").build());
                            } else if (tempIndex==decoSize+1) {
                                menu.setItem(StaticUtils.decoSpots[0], new ItemBuilder(XMaterial.LIGHT_GRAY_STAINED_GLASS_PANE.parseMaterial()).name(" ").build());
                                menu.setItem(StaticUtils.decoSpots[decoSize-1], new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE.parseMaterial()).name(" ").build());
                                menu.setItem(StaticUtils.decoSpots[decoSize-2], new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE.parseMaterial()).name(" ").build());
                            } else if (tempIndex==decoSize+2) {
                                menu.setItem(StaticUtils.decoSpots[0], new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE.parseMaterial()).name(" ").build());
                                menu.setItem(StaticUtils.decoSpots[decoSize-1], new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE.parseMaterial()).name(" ").build());
                            } else if (tempIndex==decoSize+3) {
                                menu.setItem(StaticUtils.decoSpots[0], new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE.parseMaterial()).name(" ").build());
                            }
                        }
                    }.runTaskLater(plugin, frameIndex);
                    frameIndex++;
                }
            }
        }.runTask(plugin);
    }

    public void playEndAnimation(Inventory menu) {
        int decoSize = StaticUtils.decoSpots.length;

        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i=0; i<decoSize; i++) {
                    menu.setItem(StaticUtils.decoSpots[i], new ItemBuilder(XMaterial.WHITE_STAINED_GLASS_PANE.parseMaterial()).name(" ").build());
                }
            }
        }.runTaskLater(plugin, 3);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i=0; i<decoSize; i++) {
                    menu.setItem(StaticUtils.decoSpots[i], new ItemBuilder(XMaterial.LIGHT_GRAY_STAINED_GLASS_PANE.parseMaterial()).name(" ").build());
                }
            }
        }.runTaskLater(plugin, 6);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i=0; i<decoSize; i++) {
                    menu.setItem(StaticUtils.decoSpots[i], new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE.parseMaterial()).name(" ").build());
                }
            }
        }.runTaskLater(plugin, 9);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i=0; i<decoSize; i++) {
                    menu.setItem(StaticUtils.decoSpots[i], new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE.parseMaterial()).name(" ").build());
                }
            }
        }.runTaskLater(plugin, 12);
    }

    public void processDealerTurnWithAnimation(final BlackjackGame gameInstance, final GameSession sessionInstance, final Inventory inventory, final Player clicker, final int playerScore) {
        final BlackjackPlayer player = BlackjackPlayer.loadPlayer(clicker);
        final ArrayList<String> houseCards = gameInstance.getHouseCards();
        int dealerScore = StaticUtils.getScoreUnder21(houseCards);
        
        if (dealerScore>=17) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    playEndAnimation(inventory);
    
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            // dealer stands, end game
                            if (dealerScore > 21) {
                                sessionInstance.endGame(gameInstance,BlackjackGame.Ending.WIN);
                                player.addWin();
                            } else if (dealerScore > playerScore) {
                                // house wins
                                player.addLoss();
                                sessionInstance.endGame(gameInstance,BlackjackGame.Ending.LOSE);
                            } else if (playerScore > dealerScore) {
                                // player wins
                                sessionInstance.endGame(gameInstance,BlackjackGame.Ending.WIN);
                                player.addWin();
                            } else if (playerScore == dealerScore) {
                                // tie
                                sessionInstance.endGame(gameInstance,BlackjackGame.Ending.TIE);
                            }
                        }
                    }.runTaskLater(plugin, 15);
                }
            }.runTaskLater(plugin, 16*1);

        } else { // dealer hits, draw another card and update inv
            new BukkitRunnable() {
                @Override
                public void run() {
                    playAnimation(inventory);
    
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            gameInstance.getNextCard();
                            Inventory inv = gameInstance.getRevealedInventory(sessionInstance, gameInstance);
                            clicker.openInventory(inv);
                    
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    processDealerTurnWithAnimation(gameInstance, sessionInstance, inv, Blackjack.getInstance().getServer().getPlayer(player.getUuid()), StaticUtils.getScoreUnder21(gameInstance.getPlayerCards()));
                                }
                            }.runTaskLater(plugin, 16*1);
                        }
                    }.runTaskLater(plugin, (StaticUtils.decoSpots.length+4));
                }
            }.runTaskLater(plugin, 16*1);
        }
    }
}
