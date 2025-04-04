package dev.tbm00.spigot.blackjack64.gui;

import java.util.Arrays;
import java.util.Collections;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.wesjd.anvilgui.AnvilGUI;

import dev.tbm00.spigot.blackjack64.Blackjack64;
import dev.tbm00.spigot.blackjack64.util.StaticUtils;

public class AnvilGui {
    
    public AnvilGui(Blackjack64 javaPlugin, Player player, String type) {
        ItemStack leftItem = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta leftMeta = leftItem.getItemMeta();
        leftMeta.setDisplayName(" ");
        leftItem.setItemMeta(leftMeta);

        ItemStack rightItem = new ItemStack(Material.GOLD_INGOT);
        ItemMeta rightMeta = rightItem.getItemMeta();
        rightMeta.setDisplayName("enter bet amount ($)");
        rightMeta.setItemName("enter bet amount ($)");
        rightItem.setItemMeta(rightMeta);

        ItemStack outputItem = new ItemStack(Material.STONE_BUTTON);
        ItemMeta outputMeta = outputItem.getItemMeta();
        outputMeta.setDisplayName("enter bet then click to start hand");
        outputMeta.setItemName("enter bet then click to start hand");
        outputItem.setItemMeta(outputMeta);

        new AnvilGUI.Builder()
            .onClick((slot, stateSnapshot) -> {
                if(slot != AnvilGUI.Slot.OUTPUT || stateSnapshot.getText().isBlank()) {
                    return Collections.emptyList();
                }

                String arr[] = {stateSnapshot.getText()}; 
                while (arr[0].startsWith(" ")) {
                    arr[0] = arr[0].substring(1);
                }

                Double betAmount;
                try {
                    betAmount = Double.parseDouble(arr[0]);
                } catch (Exception e) {
                    return Collections.emptyList();
                }

                if (type.equals("change")) {
                    return Arrays.asList(
                        AnvilGUI.ResponseAction.close(),
                        AnvilGUI.ResponseAction.run(() -> StaticUtils.handleChangeBet(player, betAmount))
                    );
                }

                if (type.equals("new")) {
                    return Arrays.asList(
                        AnvilGUI.ResponseAction.close(),
                        AnvilGUI.ResponseAction.run(() -> StaticUtils.handleNewGame(player, betAmount))
                    );
                }

                return Collections.emptyList();
            })
            .text(" ")
            .itemLeft(leftItem)
            .itemRight(rightItem)
            .itemOutput(outputItem)
            .title(StaticUtils.translate("Enter Bet Amount"))
            .plugin(javaPlugin)
            .open(player);
    }
}