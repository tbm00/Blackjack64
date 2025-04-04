package me.perotin.blackjack.objects;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.perotin.blackjack.Blackjack;

public class BlackFile {

    private final File file;
    private final FileConfiguration configuration;
    private final BlackFilesType type;

    public BlackFile(BlackFilesType type) {
        this.type = type;
        switch (type) {
            case STATS:
                file = new File(Blackjack.getInstance().getDataFolder(), "stats.yml");
                configuration = YamlConfiguration.loadConfiguration(file);
                break;
            default:
                throw new IllegalArgumentException("Unsupported file type: " + type);
        }
    }

    public void save() {
        try {
            configuration.save(file);
        } catch (IOException ex) {
            Bukkit.getLogger().severe("[Blackjack] Error saving " + type.toString().toLowerCase() + " file.");
            ex.printStackTrace();
        }
    }

    public FileConfiguration getConfiguration() {
        return configuration;
    }

    public Object get(String path) {
        return configuration.get(path);
    }

    public void set(String path, Object value) {
        configuration.set(path, value);
    }

    public String getString(String path) {
        String value = configuration.getString(path);
        return value != null ? ChatColor.translateAlternateColorCodes('&', value) : null;
    }

    public void load() {
        File dataFile;
        InputStream defaultStream;
        switch (type) {
            case STATS:
                dataFile = new File(Blackjack.getInstance().getDataFolder(), "stats.yml");
                defaultStream = Blackjack.getInstance().getResource("stats.yml");
                break;
            default:
                throw new IllegalArgumentException("Unsupported file type: " + type);
        }

        if (!dataFile.exists()) {
            try {
                File dataFolder = Blackjack.getInstance().getDataFolder();
                if (!dataFolder.exists()) {
                    dataFolder.mkdirs();
                }
                dataFile.createNewFile();
                if (defaultStream != null) {
                    try (OutputStream out = new FileOutputStream(dataFile)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = defaultStream.read(buffer)) != -1) {
                            out.write(buffer, 0, length);
                        }
                    }
                }
            } catch (IOException e) {
                Bukkit.getLogger().severe("[Blackjack] Couldn't create " + type.toString().toLowerCase() + " file.");
                Bukkit.getLogger().severe("[Blackjack] This is a fatal error. Now disabling plugin.");
                e.printStackTrace();
                Bukkit.getPluginManager().disablePlugin(Blackjack.getInstance());
            } finally {
                if (defaultStream != null) {
                    try {
                        defaultStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public File getFile() {
        return file;
    }

    public static void loadFiles() {
        for (BlackFilesType type : BlackFilesType.values()) {
            new BlackFile(type).load();
        }
    }

    public enum BlackFilesType {
        STATS
    }
}
