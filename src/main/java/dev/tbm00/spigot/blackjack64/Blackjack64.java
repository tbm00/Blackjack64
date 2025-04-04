package dev.tbm00.spigot.blackjack64;

import static dev.tbm00.spigot.blackjack64.object.BlackFile.BlackFilesType.STATS;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import dev.tbm00.spigot.blackjack64.command.BlackjackAdminCommand;
import dev.tbm00.spigot.blackjack64.command.BlackjackCommand;
import dev.tbm00.spigot.blackjack64.command.BlackjackQuickCommand;
import dev.tbm00.spigot.blackjack64.listener.PlayerConnection;
import dev.tbm00.spigot.blackjack64.object.BlackFile;
import dev.tbm00.spigot.blackjack64.object.BlackjackGame;
import dev.tbm00.spigot.blackjack64.object.BlackjackPlayer;
import dev.tbm00.spigot.blackjack64.object.GameSession;
import dev.tbm00.spigot.blackjack64.util.JSONHandler;
import dev.tbm00.spigot.blackjack64.util.StaticUtils;
import dev.tbm00.spigot.blackjack64.util.GuiUtils;

public class Blackjack64 extends JavaPlugin {

    private static Blackjack64 instance;
    private static Economy econ = null;
    private Set<BlackjackPlayer> players = null;
    private HashSet<GameSession> sessions;
    private double betMin=0, betMax = 0;
    public static String[] cards = {
        "As",  "2s", "3s", "4s", "5s", "6s", "7s", "8s", "9s", "10s", "Js", "Qs", "Ks",
        "Ac",  "2c", "3c", "4c", "5c", "6c", "7c", "8c", "9c", "10c", "Jc", "Qc", "Kc",
        "Ah",  "2h", "3h", "4h", "5h", "6h", "7h", "8h", "9h", "10h", "Jh", "Qh", "Kh",
        "Ad",  "2d", "3d", "4d", "5d", "6d", "7d", "8d", "9d", "10d", "Jd", "Qd", "Kd"};
    private double serverImpact, serverWins, games, serverLosses;
    private boolean surrender, doubleDown, secondaryBetOverride;
    private double surrenderPercentage, blackJackMultiplier;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        if (!getConfig().contains("enabled") || !getConfig().getBoolean("enabled")) {
            StaticUtils.log(ChatColor.RED, "Blackjack disabled in config!");
        }

        BlackFile.loadFiles();
        JSONHandler.init(this);
        StaticUtils.init(this);
        GuiUtils.init(this);

        final PluginDescriptionFile pdf = this.getDescription();

        StaticUtils.log(ChatColor.LIGHT_PURPLE,
            ChatColor.DARK_PURPLE + "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-",
            pdf.getName() + " v" + pdf.getVersion() + " created by tbm00",
            ChatColor.DARK_PURPLE + "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-"
        );

        this.serverWins = 0;
        this.sessions = new HashSet<>();
        this.games = 0;
        this.serverLosses = 0;
        this.surrender = getConfig().getBoolean("enable-surrender");
        this.doubleDown = getConfig().getBoolean("enable-double-down");
        this.secondaryBetOverride = getConfig().getBoolean("enable-secondary-bet-override", false);
        this.surrenderPercentage = getConfig().getDouble("surrender-percentage-to-take");
        this.betMax = getConfig().getDouble("bet-max");
        this.betMin = getConfig().getDouble("bet-min");
        if (getConfig().getBoolean("enable-blackjack-multiplier"))
            this.blackJackMultiplier = getConfig().getDouble("blackjack-multiplier");
        else this.blackJackMultiplier = 0;

        players = new HashSet<>();
        
        setupEconomy();
        getCommand("blackjack").setExecutor(new BlackjackCommand(this));
        getCommand("z64blackjack").setExecutor(new BlackjackQuickCommand(this));
        getCommand("blackjackadmin").setExecutor(new BlackjackAdminCommand(this));
        Bukkit.getPluginManager().registerEvents(new PlayerConnection(this), this);
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

    public void setServerImpact(double serverImpact) { 
        this.serverImpact = serverImpact;
    }

    public double getServerImpact() {
        return serverImpact;
    }

    public double getTotalServerImpact() {
        return new BlackFile(STATS).getConfiguration().getDouble("server-impact") + getServerImpact();
    }

    public double getBetMin() {
        return betMin;
    }

    public double getBetMax() {
        return betMax;
    }

    public Set<BlackjackPlayer> getPlayers() {
        return players;
    }

    public static Economy getEconomy(){
        return econ;
    }

    public static Blackjack64 getInstance() {
        return instance;
    }

    public BlackjackPlayer getBlackjackPlayer(Player p){
        if(!players.isEmpty()){
            for(BlackjackPlayer player : players){
                if(player.getUuid().equals(p.getUniqueId())) return player;
            }
        }
        BlackjackPlayer bjp = JSONHandler.getSavedPlayerStats(p);
        players.add(bjp);
        return bjp;
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
            if (game.getUuid().equals(uuid)) return game;
        }
        return null;
    }

    @Override
    public void onDisable(){
        sessions.forEach(GameSession::endSession);
        JSONHandler.saveStats(players);
        players.clear();
        sessions.clear();

        BlackFile file = new BlackFile(STATS);
        file.set("server-impact", serverImpact);
        file.set("server-games", getTotalServerGames());
        file.set("server-wins", getTotalServerWins());
        file.set("server-losses", getTotalServerLosses());
        file.save();
    }
}
