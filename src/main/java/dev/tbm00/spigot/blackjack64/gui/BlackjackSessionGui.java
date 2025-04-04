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
import dev.tbm00.spigot.blackjack64.object.GameSession;
import dev.tbm00.spigot.blackjack64.util.GuiUtils;
import dev.tbm00.spigot.blackjack64.util.StaticUtils;

public class BlackjackSessionGui {
    Blackjack64 javaPlugin;
    public Player player;
    public GameSession session;
    public BlackjackGame priorGame;
    public Gui gui;
    public double earnings;
    private String sessiontotal;
    private String handtotal;
    
    public BlackjackSessionGui(Blackjack64 javaPlugin, Player player, GameSession session, BlackjackGame priorGame) {
        this.javaPlugin = javaPlugin;
        this.player = player;
        this.session = session;
        this.priorGame = priorGame;
        this.earnings = session.getTotalEarnings();

        String sessionI = earnings>=0 ? StaticUtils.formatInt(earnings) : StaticUtils.formatInt(-1*earnings);
        String handI = priorGame.getResult()>=0 ? StaticUtils.formatInt(priorGame.getResult()) : StaticUtils.formatInt(-1*priorGame.getResult());

        if (earnings==0) sessiontotal = "&8$0";
        else sessiontotal = earnings>=0 ? "&2+$"+sessionI : "&4-$"+sessionI;

        if (priorGame.getResult()==0) handtotal = "&8$0";
        else handtotal = priorGame.getResult()>=0 ? "&2+$"+handI : "&4-$"+handI;

        gui = new Gui(6, StaticUtils.translate(StaticUtils.getString("session-menu-title").replace("$session$", sessiontotal).replace("$hand$", handtotal)));
        
        handtotal = priorGame.getResult()>0 ? "&a+$"+handI : "&c-$"+handI;
        if (priorGame.getResult()==0) handtotal = "&f$0";
        if (earnings==0) sessiontotal = "&f$0";

        fillGui();
        
        gui.disableAllInteractions();
        GuiUtils.disableAll(gui);
        
        gui.open(player);

        gui.getInventory();
    }

    private void fillGui() {
        ItemStack item = new ItemStack(Material.GLASS);
        ItemMeta meta = item.getItemMeta();
        SkullMeta smeta;
        List<String> lore = new ArrayList<>();

        

        // deco spots
        if (StaticUtils.getScoreUnder21(priorGame.getPlayerCards()) == 21 && priorGame.getResult() > 0 && priorGame.isBlackjackEnding()) {
            playBlackjackAnimation();
        } else {
            for (Integer i : StaticUtils.decoSpots) {
                if (priorGame.getResult()>0) {
                    gui.setItem(i, ItemBuilder.from(Material.GREEN_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> GuiUtils.handleNullClick(event)));
                } else if (priorGame.getResult()<0) {
                    gui.setItem(i, ItemBuilder.from(Material.RED_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> GuiUtils.handleNullClick(event)));
                } else {
                    gui.setItem(i, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> GuiUtils.handleNullClick(event)));
                }
            }
        }

        // session summary
        item.setType(Material.BOOK);
        meta = item.getItemMeta();
        lore.add("");
        lore.add(StaticUtils.translate("&7Total: " + sessiontotal));
        lore.add(StaticUtils.translate("&7Prior Hand: " + handtotal));
        meta.setLore(lore.stream().map(l -> StaticUtils.translate(l)).toList());
        meta.setDisplayName(StaticUtils.translate(StaticUtils.getString("summary-item")));
        item.setItemMeta(meta);
        gui.setItem(13, ItemBuilder.from(item).asGuiItem(event -> GuiUtils.handleNullClick(event)));
        lore.clear();
        meta.setLore(lore);
        item.setItemMeta(meta);

        // player's head
        item.setType(Material.PLAYER_HEAD);
        smeta = (SkullMeta) item.getItemMeta();
        smeta.setOwningPlayer(player);
        ArrayList<String> cards = priorGame.getPlayerCards();
        int score = StaticUtils.getScoreUnder21(cards);
        if (score==21) {
            lore.add("");
            lore.add(StaticUtils.translate("&6BLACKJACK"));
        } else if (score > 21) {
            lore.add("");
            lore.add(StaticUtils.translate("&6BUST"));
        } lore.add("");
        lore.add("&fTotal: &6" + score);
        for (String card : priorGame.getPlayerCards()) {
            lore.add("&7- " + StaticUtils.convertToFullText(card));
        }
        smeta.setLore(lore.stream().map(l -> StaticUtils.translate(l)).toList());
        smeta.setDisplayName(StaticUtils.translate(StaticUtils.getString("your-hand-session-item")));
        item.setItemMeta(smeta);
        item.setAmount(score);
        gui.setItem(15, ItemBuilder.from(item).asGuiItem(event -> GuiUtils.handleNullClick(event)));
        lore.clear();
        meta.setLore(lore);
        item.setItemMeta(meta);
        item.setAmount(1);
        
        // dealer's head
        item.setType(Material.PLAYER_HEAD);
        smeta = (SkullMeta) item.getItemMeta();
        cards = priorGame.getHouseCards();
        score = StaticUtils.getScoreUnder21(cards);
        if (score==21) {
            lore.add("");
            lore.add(StaticUtils.translate("&6BLACKJACK"));
        } else if (score > 21) {
            lore.add("");
            lore.add(StaticUtils.translate("&6BUST"));
        } lore.add("");
        lore.add("&fTotal: &6" + score);
        for (String card : priorGame.getHouseCards()) {
            lore.add("&7- " + StaticUtils.convertToFullText(card));
        }
        smeta.setLore(lore.stream().map(l -> StaticUtils.translate(l)).toList());
        smeta.setDisplayName(StaticUtils.translate(StaticUtils.getString("dealers-hand-session-item")));
        item.setItemMeta(smeta);
        item.setAmount(score);
        gui.setItem(11, ItemBuilder.from(item).asGuiItem(event -> GuiUtils.handleNullClick(event)));
        lore.clear();
        meta.setLore(lore);
        item.setItemMeta(meta);
        item.setAmount(1);

        // next hand
        item.setType(Material.GREEN_BANNER);
        meta = item.getItemMeta();
        meta.setLore(lore.stream().map(l -> StaticUtils.translate(l)).toList());
        meta.setDisplayName(StaticUtils.translate(StaticUtils.getString("keep-playing-item")));
        item.setItemMeta(meta);
        gui.setItem(32, ItemBuilder.from(item).asGuiItem(event -> GuiUtils.handleNextClick(event, this)));
        lore.clear();
        meta.setLore(lore);
        item.setItemMeta(meta);

        // end session
        item.setType(Material.RED_BANNER);
        meta = item.getItemMeta();
        meta.setLore(lore.stream().map(l -> StaticUtils.translate(l)).toList());
        meta.setDisplayName(StaticUtils.translate(StaticUtils.getString("stop-playing-item")));
        item.setItemMeta(meta);
        gui.setItem(30, ItemBuilder.from(item).asGuiItem(event -> GuiUtils.handleEndClick(event, this)));
        lore.clear();
        meta.setLore(lore);
        item.setItemMeta(meta);

        // change bet amount
        item.setType(Material.GOLD_INGOT);
        meta = item.getItemMeta();
        lore.add("&7Click to enter amount");
        meta.setLore(lore.stream().map(l -> StaticUtils.translate(l)).toList());
        meta.setDisplayName(StaticUtils.translate(StaticUtils.getString("enter-bet-item").replace("$number$", "" + StaticUtils.formatInt(session.getBetAmount()))));
        item.setItemMeta(meta);
        gui.setItem(40, ItemBuilder.from(item).asGuiItem(event -> GuiUtils.handleChangeClick(event, this)));
        lore.clear();
        meta.setLore(lore);
        item.setItemMeta(meta);

        addModifier("increase", 1, 33);
        addModifier("increase", 2, 34);
        addModifier("increase", 3, 41);
        addModifier("increase", 4, 42);
        addModifier("increase", 5, 43);

        addModifier("decrease", 1, 28);
        addModifier("decrease", 2, 29);
        addModifier("decrease", 3, 37);
        addModifier("decrease", 4, 38);
        addModifier("decrease", 5, 39);
    }

    private void addModifier(String type, int index, int slot) {
        double betDiff = Blackjack64.getInstance().getConfig().getInt("bet-modifiers." + type + "Value" + index);

        ItemStack item = new ItemStack(Material.STONE_BUTTON);
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();
        meta.setLore(lore.stream().map(l -> StaticUtils.translate(l)).toList());
        if (type.equals("increase")) 
            meta.setDisplayName(StaticUtils.translate(StaticUtils.getString("prefix-increase-bet-item")+ StaticUtils.formatInt(betDiff)));
        else meta.setDisplayName(StaticUtils.translate(StaticUtils.getString("prefix-decrease-bet-item")+ StaticUtils.formatInt(betDiff)));
        item.setItemMeta(meta);
        gui.setItem(slot, ItemBuilder.from(item).asGuiItem(event -> GuiUtils.handleChangeExactClick(event, this, type, betDiff)));
        lore.clear();
    }

    public void playBlackjackAnimation() {
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                for (Integer i : StaticUtils.decoSpots) {
                    if (StaticUtils.random.nextBoolean()) {
                        gui.setItem(i, ItemBuilder.from(Material.GREEN_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> GuiUtils.handleNullClick(event)));
                    } else {
                        gui.setItem(i, ItemBuilder.from(Material.LIME_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> GuiUtils.handleNullClick(event)));
                    }
                }
                gui.update();
            }
        };
        runnable.runTaskTimer(javaPlugin, 0, 12);
    
        new BukkitRunnable() {
            @Override
            public void run() {
                runnable.cancel();
            }
        }.runTaskLater(javaPlugin, 20 * 30);
    }
}