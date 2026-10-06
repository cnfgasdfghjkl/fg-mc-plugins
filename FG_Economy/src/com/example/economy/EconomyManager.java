package com.example.economy;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EconomyManager {

    private final EconomyPlugin plugin;
    private final File dataFile;
    private FileConfiguration data;
    private final Map<UUID, Double> balances = new HashMap<>();
    private static final DecimalFormat df = new DecimalFormat("#.##");

    public EconomyManager(EconomyPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "balances.yml");
        load();
    }

    public void load() {
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
        balances.clear();
        if (data.contains("balances")) {
            for (String key : data.getConfigurationSection("balances").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    double balance = data.getDouble("balances." + key);
                    balances.put(uuid, balance);
                } catch (Exception ignored) {}
            }
        }
        plugin.getLogger().info("已加载 " + balances.size() + " 个玩家的经济数据");
    }

    public void save() {
        try {
            for (UUID uuid : balances.keySet()) {
                data.set("balances." + uuid.toString(), balances.get(uuid));
            }
            data.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double getBalance(Player player) {
        return balances.getOrDefault(player.getUniqueId(), 0.0);
    }

    public void addBalance(Player player, double amount) {
        UUID uuid = player.getUniqueId();
        double current = balances.getOrDefault(uuid, 0.0);
        balances.put(uuid, current + amount);
        save();
    }

    /**
     * 给离线玩家加钱（玩家市场购买时卖家可能不在线）
     */
    public void addBalanceOffline(UUID uuid, double amount) {
        double current = balances.getOrDefault(uuid, 0.0);
        balances.put(uuid, current + amount);
        save();
    }

    public void removeBalance(Player player, double amount) {
        UUID uuid = player.getUniqueId();
        double current = balances.getOrDefault(uuid, 0.0);
        balances.put(uuid, Math.max(0, current - amount));
        save();
    }

    public void setBalance(Player player, double amount) {
        balances.put(player.getUniqueId(), Math.max(0, amount));
        save();
    }

    public String format(double amount) {
        return df.format(amount);
    }
}
