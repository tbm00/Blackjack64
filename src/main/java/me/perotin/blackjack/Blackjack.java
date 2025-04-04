package me.perotin.blackjack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;

import me.perotin.blackjack.commands.BlackjackAdminCommand;
import me.perotin.blackjack.commands.BlackjackCommand;
import me.perotin.blackjack.events.BlackjackInventoryClickEvent;
import me.perotin.blackjack.events.BlackjackJoinEvent;
import me.perotin.blackjack.events.BlackjackLeaveMidGameEvent;
import me.perotin.blackjack.events.BlackjackSessionClickEvent;
import me.perotin.blackjack.objects.BlackFile;
import me.perotin.blackjack.objects.BlackjackGame;
import me.perotin.blackjack.objects.BlackjackPlayer;
import me.perotin.blackjack.objects.GameSession;
import me.perotin.blackjack.util.StaticUtils;
import static me.perotin.blackjack.objects.BlackFile.BlackFilesType.STATS;

public class Blackjack extends JavaPlugin {

    private static Blackjack instance;
    private static Economy econ = null;
    private Set<BlackjackPlayer> players;
    private HashSet<GameSession> sessions;
    private boolean overFlow;
    private double taxPercent, betMin, betMax = 0;

    // stats for admins
    private double serverImpact;
    private double serverWins;
    private double games;
    private double serverLosses;
    private boolean surrender, doubleDown, doubleDownOverFlow, secondaryBetOverride;
    private double surrenderPercentage;
    private double blackJackMultiplier;

    public static String[] cards = {
            "As",  "2s", "3s", "4s", "5s", "6s", "7s", "8s", "9s", "10s", "Js", "Qs", "Ks",
            "Ac",  "2c", "3c", "4c", "5c", "6c", "7c", "8c", "9c", "10c", "Jc", "Qc", "Kc",
            "Ah",  "2h", "3h", "4h", "5h", "6h", "7h", "8h", "9h", "10h", "Jh", "Qh", "Kh",
            "Ad",  "2d", "3d", "4d", "5d", "6d", "7d", "8d", "9d", "10d", "Jd", "Qd", "Kd"};
    @Override
    public void onEnable(){
        instance = this;
        this.serverWins = 0;
        this.sessions = new HashSet<>();
        this.games = 0;
        this.serverLosses = 0;
        this.surrender = getConfig().getBoolean("enable-surrender");
        this.doubleDown = getConfig().getBoolean("enable-double-down");
        this.doubleDownOverFlow = getConfig().getBoolean("double-down-overflow");
        this.secondaryBetOverride = getConfig().getBoolean("enable-secondary-bet-override", false);
        this.surrenderPercentage = getConfig().getDouble("surrender-percentage-to-take");
        this.taxPercent = getConfig().getDouble("tax-percent");
        this.betMax = getConfig().getDouble("bet-max");
        this.betMin = getConfig().getDouble("bet-min");
        this.overFlow = getConfig().getBoolean("bet-overflow");
        if (getConfig().getBoolean("enable-multiplier"))
            this.blackJackMultiplier = getConfig().getDouble("multiplier");
        else this.blackJackMultiplier = 0;

        players = new HashSet<>();

        setupEconomy();
        setServerImpact(new BlackFile(STATS).getConfiguration().getDouble("server-impact"));
        saveDefaultConfig();

        getCommand("blackjack").setExecutor(new BlackjackCommand(this));
        getCommand("blackjackadmin").setExecutor(new BlackjackAdminCommand(this));
        Bukkit.getPluginManager().registerEvents(new BlackjackInventoryClickEvent(), this);
        Bukkit.getPluginManager().registerEvents(new BlackjackJoinEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new BlackjackSessionClickEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new BlackjackLeaveMidGameEvent(this), this);

        BlackFile.loadFiles();
        StaticUtils.init(this);

        for(Player player : Bukkit.getOnlinePlayers()){
            players.add(BlackjackPlayer.loadPlayer(player));
        }
    }

    public static void withdraw(double amount, Player player){
        Blackjack.getEconomy().withdrawPlayer(player, amount);
    }

    public static void deposit(double amount, Player player){
        Blackjack.getEconomy().depositPlayer(player, amount);
    }

    public double getBlackJackMultiplier() {
        return blackJackMultiplier;
    }

    public HashSet<GameSession> getSessions() {
        return sessions;
    }

    public boolean isSurrenderEnabled() {
        return surrender;
    }

    public boolean isDoubleDownEnabled() {
        return doubleDown;
    }


    public double getSurrenderPercentage() {
        return surrenderPercentage;
    }


    public double getServerLosses() {
        return serverLosses;
    }

    public double getTotalServerLosses() {
        return new BlackFile(STATS).getConfiguration().getInt("server-losses") + getServerLosses();
    }

    public boolean isSecondaryBetOverride() {
        return secondaryBetOverride;
    }

    public void increaseServerLosses() {
        this.serverLosses++;
    }

    public void increaseGamesPlayed(){
        this.games++;
    }

    public double getGames() {
        return games;
    }

    public double getTotalServerGames() {
        return new BlackFile(STATS).getConfiguration().getInt("server-games") + getGames();
    }


    public double getServerWins() {
        return serverWins;
    }
    public double getTotalServerWins() {
        return new BlackFile(STATS).getConfiguration().getInt("server-wins") + getServerWins();
    }

    public void increaseServerWins() {
        this.serverWins++;
    }

    public void setServerImpact(double serverImpact) { this.serverImpact = serverImpact; }

    public double getServerImpact() {
        return this.serverImpact;
    }

    public double getBetMin() {
        return betMin;
    }

    public double getBetMax() {
        return betMax;
    }

    @Override
    public void onDisable(){
        BlackFile file = new BlackFile(STATS);
        sessions.forEach(GameSession::endSession);

        players.stream().forEach(player ->{
            file.set(player.getUuid().toString()+".wins", player.getWins());
            file.set(player.getUuid().toString()+".losses", player.getLosses());
        });
        players.clear();
        sessions.clear();


        file.set("server-impact", serverImpact);
        file.set("server-games", getTotalServerGames());
        file.set("server-wins", getTotalServerWins());
        file.set("server-losses", getTotalServerLosses());
        file.save();

    }

    public double getTaxPercent(){
        return this.taxPercent;
    }


    public BlackjackPlayer getPlayerFor(Player p){
        if(!players.isEmpty()){
            for(BlackjackPlayer player : players){
                if(player.getUuid().equals(p.getUniqueId())) return player;
            }
        }
        return BlackjackPlayer.loadPlayer(p);
    }

    public GameSession getSessionFor(BlackjackGame game){
        for(GameSession s : getSessions()){
            for(BlackjackGame g : s.getGames()){
                if(g.getUuid().equals(game.getUuid())){
                    return s;
                }

            }
        }
        return null;
    }

    public GameSession getSessionFor(UUID uuid){
        for(GameSession game : getSessions()){
            if(game.getUuid().equals(uuid)) return game;
        }
        return null;
    }
    public Set<BlackjackPlayer> getPlayers() {
        return players;
    }

    public boolean isOverflow() {
        return this.overFlow;
    }

    public static Economy getEconomy(){
        return econ;
    }
    public static Blackjack getInstance() {return instance;}


    public boolean isDoubleDownOverFlow() {
        return doubleDownOverFlow;
    }

    public  String getString(String path, String alternative){
        return  getConfig().getString(path) != null ? ChatColor.translateAlternateColorCodes('&', getConfig().getString(path)) : alternative;
    }

    public  String getString(String path){
        if (!getConfig().contains(path)) {
            Bukkit.getLogger().info("[Blackjack] Could not find config path!: " + path);
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString(path));
    }
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public void sendMessage(Player player, String pathTomessage){
        player.sendMessage(getString(pathTomessage));
    }
}
