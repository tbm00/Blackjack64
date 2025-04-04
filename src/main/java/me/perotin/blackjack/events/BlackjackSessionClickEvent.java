package me.perotin.blackjack.events;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import me.perotin.blackjack.Blackjack;
import me.perotin.blackjack.objects.GameSession;
import me.perotin.blackjack.util.XMaterial;

public class BlackjackSessionClickEvent implements Listener {

    private Blackjack plugin;

    public BlackjackSessionClickEvent(Blackjack blackjack) {
        this.plugin = blackjack;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent event) {
        //Inventory menu = event.getInventory();
        if (event.getWhoClicked() instanceof Player) {
            Player clicker = (Player) event.getWhoClicked();
            if (plugin.getSessionFor(clicker.getUniqueId()) != null) {
                GameSession session = plugin.getSessionFor(clicker.getUniqueId());
                if (event.getView().getTitle().contains("Session:")) {
                    event.setCancelled(true);
                    double betMax = plugin.getBetMax();
                    double betMin = plugin.getBetMin();
                    ItemStack clicked = event.getCurrentItem();
                    if(clicked != null && clicked.hasItemMeta()) {
                        if (clicked.getType() == XMaterial.RED_BANNER.parseItem().getType()) {
                            event.setCancelled(true);
                            session.endSession();

                        } else if (clicked.getType() == XMaterial.GREEN_BANNER.parseItem().getType()) {
                            session.startNewGame();

                        } else if (clicked.getType() == XMaterial.STONE_BUTTON.parseMaterial()) {
                            String changeAmount = event.getCurrentItem().getItemMeta().getDisplayName();
                            changeAmount = ChatColor.stripColor(changeAmount);
                            changeAmount = changeAmount.charAt(0) == '+' ? ChatColor.stripColor(changeAmount.substring(3)) : "-"+ChatColor.stripColor(changeAmount.substring(3));
                            double change = Double.parseDouble(changeAmount);
                            if(session.getBetAmount() + change > Blackjack.getInstance().getPlayerFor(clicker).getBalance()) return;
                                if(session.getBetAmount() + change < 1) return;
                            if(betMax > 0 && session.getBetAmount() + change > betMax) return;

                            if(betMin > 0 &&session.getBetAmount()+change < betMin) return;

                            session.setBetAmount(session.getBetAmount() + change);

                            session.showEndMenu(session.getGames().get(session.getGames().size()-1));
                        }

                    }
                }
            }
        }
    }


}
