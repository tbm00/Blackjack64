package me.perotin.blackjack.events;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.perotin.blackjack.Blackjack;
import me.perotin.blackjack.objects.BlackjackGame;
import me.perotin.blackjack.objects.BlackjackPlayer;
import me.perotin.blackjack.objects.GameSession;
import me.perotin.blackjack.util.StaticUtils;
import me.perotin.blackjack.util.XMaterial;
import me.perotin.blackjack.util.InvUtils;

public class BlackjackInventoryClickEvent implements Listener {

    private Blackjack plugin = Blackjack.getInstance();
    private InvUtils util;
    
    public BlackjackInventoryClickEvent( ) {
        util = new InvUtils(Blackjack.getInstance());
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory clicked = event.getInventory();
        //InventoryView view = event.getView();
        if (event.getWhoClicked() instanceof Player) {
            Player clicker = (Player) event.getWhoClicked();

            BlackjackPlayer player = plugin.getPlayerFor(clicker);

            BlackjackGame currentGame = null;
            GameSession session = null;
            UUID clickerUUID = clicker.getUniqueId();
            if(plugin.getSessionFor(clickerUUID) != null) {
                for (BlackjackGame game : plugin.getSessionFor(clickerUUID).getGames()) {
                    if (game.getPlayer().getUniqueId().equals(clickerUUID)) {
                        // same player
                        String name = plugin.getString("menu-title").replace("$number$", "" + (int) game.getBetAmount());
                        if (event.getView().getTitle().equals(name)) {
                            // same inventory, its safe to say its a game click
                            currentGame = game;
                            session = plugin.getSessionFor(clickerUUID);
                        }
                    }
                }
            }

            if (currentGame != null && session != null) {
                event.setCancelled(true);
                ItemStack item = event.getCurrentItem();
                if (item != null && item.getType() != XMaterial.AIR.parseMaterial()) {

                    if (!(item.getType() == XMaterial.DARK_OAK_DOOR.parseMaterial()
                        || item.getType() == XMaterial.ARROW.parseMaterial()
                        || item.getType() == XMaterial.NETHER_STAR.parseMaterial()
                        || item.getType() == XMaterial.BARRIER.parseMaterial())) {
                            event.setCancelled(true);
                            return;
                        } 

                    util.playAnimation(clicked);
												
                    final BlackjackGame gameInstance = currentGame;
                    final GameSession sessionInstance = session;

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (item.getType() == XMaterial.DARK_OAK_DOOR.parseMaterial() && item.getItemMeta().getDisplayName().equals(plugin.getString("surrender-item"))) {
                                // they surrender
                                sessionInstance.endGame(gameInstance,BlackjackGame.Ending.SURRENDER);
                            } 
                            
                            else if (item.getType() == XMaterial.ARROW.parseMaterial() && item.getItemMeta().getDisplayName().equals(plugin.getString("double-down-item"))) {
                                // they double down
                                // Double the bet amount and show them the new inventory where
                                // only option is to stand after they gained a new card
                                // ensure that they only see the nether star if it is the first move
                                Blackjack.withdraw(gameInstance.getBetAmount(), gameInstance.getPlayer());
                                gameInstance.setBetAmount(gameInstance.getBetAmount()*2);
                                // Withdraw bet amount again
                                gameInstance.getNextCard();
                                int score = StaticUtils.getScoreUnder21(gameInstance.getPlayerCards());
                                if (score > 21)  { // they lose
                                    util.playEndAnimation(clicked);

                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            sessionInstance.endGame(gameInstance, BlackjackGame.Ending.LOSE);
                                            player.addLoss();
                                            return;
                                        }
                                    }.runTaskLater(plugin, 15);
                                    return;
                                }
                                clicker.openInventory(gameInstance.getInventory(true, true));
                            } 
                            
                            else if (item.getType() == XMaterial.NETHER_STAR.parseMaterial() && item.getItemMeta().getDisplayName().equals(plugin.getString("hit-item"))) {
                                // they hit
                                gameInstance.getNextCard();
                                int score = StaticUtils.getScoreUnder21(gameInstance.getPlayerCards());
                                if (score > 21) { // they lose
                                    util.playEndAnimation(clicked);

                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            sessionInstance.endGame(gameInstance, BlackjackGame.Ending.LOSE);
                                            player.addLoss();
                                            return;
                                        }
                                    }.runTaskLater(plugin, 15);
                                    return;
                                }
                                clicker.openInventory(gameInstance.getInventory(true, false));
                            } 

                            else if (item.getType() == XMaterial.BARRIER.parseMaterial() && item.getItemMeta().getDisplayName().equals(plugin.getString("stand-item"))) {
                                // player stands, dealers's turn
                                gameInstance.setPlayerTurn(false);

                                Inventory inv = gameInstance.getRevealedInventory(sessionInstance, gameInstance);
                                clicker.openInventory(inv);
                                        
                                util.processDealerTurnWithAnimation(gameInstance, sessionInstance, inv, Blackjack.getInstance().getServer().getPlayer(player.getUuid()), StaticUtils.getScoreUnder21(gameInstance.getPlayerCards()));
                            }
                        }
                    }.runTaskLater(plugin, (StaticUtils.decoSpots.length)+4);
                }
            }
        }
    }
}
