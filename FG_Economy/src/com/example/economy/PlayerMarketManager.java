package com.example.economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerMarketManager {

    private final EconomyPlugin plugin;
    private final File dataFile;
    private FileConfiguration data;
    private final Map<Integer, MarketListing> listings = new HashMap<>();
    private int nextId = 1;

    public static class MarketListing {
        public final int id;
        public final UUID sellerUuid;
        public final String sellerName;
        public final ItemStack item;
        public final double price;
        public final long timestamp;

        public MarketListing(int id, UUID sellerUuid, String sellerName, ItemStack item, double price, long timestamp) {
            this.id = id;
            this.sellerUuid = sellerUuid;
            this.sellerName = sellerName;
            this.item = item;
            this.price = price;
            this.timestamp = timestamp;
        }
    }

    public PlayerMarketManager(EconomyPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "market.yml");
        load();
    }

    @SuppressWarnings("unchecked")
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
        listings.clear();
        nextId = data.getInt("nextId", 1);

        if (data.contains("listings")) {
            for (String key : data.getConfigurationSection("listings").getKeys(false)) {
                try {
                    int id = Integer.parseInt(key);
                    UUID sellerUuid = UUID.fromString(data.getString("listings." + key + ".sellerUuid"));
                    String sellerName = data.getString("listings." + key + ".sellerName");
                    double price = data.getDouble("listings." + key + ".price");
                    long timestamp = data.getLong("listings." + key + ".timestamp");
                    String itemBase64 = data.getString("listings." + key + ".item");
                    ItemStack item = itemFromBase64(itemBase64);
                    if (item != null) {
                        listings.put(id, new MarketListing(id, sellerUuid, sellerName, item, price, timestamp));
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("加载市场条目失败: " + key);
                }
            }
        }
        plugin.getLogger().info("已加载 " + listings.size() + " 个玩家市场上架物品");
    }

    public void save() {
        try {
            data.set("nextId", nextId);
            if (data.contains("listings")) {
                for (String key : data.getConfigurationSection("listings").getKeys(false)) {
                    data.set("listings." + key, null);
                }
            }
            for (MarketListing listing : listings.values()) {
                String path = "listings." + listing.id;
                data.set(path + ".sellerUuid", listing.sellerUuid.toString());
                data.set(path + ".sellerName", listing.sellerName);
                data.set(path + ".price", listing.price);
                data.set(path + ".timestamp", listing.timestamp);
                data.set(path + ".item", itemToBase64(listing.item));
            }
            data.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String listItem(Player player, double price) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() == Material.AIR) {
            return "手里没有物品";
        }
        if (price <= 0) {
            return "价格必须大于0";
        }
        if (price > 1000000) {
            return "价格不能超过100万";
        }

        int id = nextId++;
        ItemStack item = hand.clone();
        listings.put(id, new MarketListing(id, player.getUniqueId(), player.getName(), item, price, System.currentTimeMillis()));

        if (hand.getAmount() > 1) {
            hand.setAmount(hand.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }

        save();
        return "上架成功！物品ID: " + id + "，价格: " + plugin.getEconomyManager().format(price) + " 金币";
    }

    /**
     * 直接上架指定物品（不从玩家背包移除，用于快捷上架GUI）
     */
    public String listItemDirect(Player player, ItemStack item, double price) {
        if (item == null || item.getType() == Material.AIR) {
            return "物品为空";
        }
        if (price <= 0) {
            return "价格必须大于0";
        }
        if (price > 1000000) {
            return "价格不能超过100万";
        }

        int id = nextId++;
        listings.put(id, new MarketListing(id, player.getUniqueId(), player.getName(), item.clone(), price, System.currentTimeMillis()));
        save();
        return "上架成功！物品ID: " + id;
    }

    public String buyItem(Player buyer, int listingId) {
        MarketListing listing = listings.get(listingId);
        if (listing == null) {
            return "该物品不存在或已被购买";
        }
        if (listing.sellerUuid.equals(buyer.getUniqueId())) {
            return "不能购买自己上架的物品";
        }

        EconomyManager eco = plugin.getEconomyManager();
        double buyerBalance = eco.getBalance(buyer);
        if (buyerBalance < listing.price) {
            return "余额不足，需要 " + eco.format(listing.price) + " 金币";
        }

        eco.removeBalance(buyer, listing.price);
        Player sellerOnline = Bukkit.getPlayer(listing.sellerUuid);
        if (sellerOnline != null) {
            eco.addBalance(sellerOnline, listing.price);
        } else {
            eco.addBalanceOffline(listing.sellerUuid, listing.price);
        }

        HashMap<Integer, ItemStack> leftover = buyer.getInventory().addItem(listing.item.clone());
        if (!leftover.isEmpty()) {
            for (ItemStack item : leftover.values()) {
                buyer.getWorld().dropItemNaturally(buyer.getLocation(), item);
            }
        }

        listings.remove(listingId);
        save();

        Player seller = Bukkit.getPlayer(listing.sellerUuid);
        if (seller != null) {
            seller.sendMessage(ChatColor.GREEN + "你的物品 " + listing.item.getType().name() +
                " 已被 " + buyer.getName() + " 购买，获得 " + eco.format(listing.price) + " 金币");
        }

        return "购买成功！花费 " + eco.format(listing.price) + " 金币";
    }

    public String removeListing(Player player, int listingId) {
        MarketListing listing = listings.get(listingId);
        if (listing == null) {
            return "该物品不存在";
        }
        if (!listing.sellerUuid.equals(player.getUniqueId()) && !player.hasPermission("economy.admin")) {
            return "403 无权限：你不能下架别人的物品";
        }

        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(listing.item.clone());
        if (!leftover.isEmpty()) {
            for (ItemStack item : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
        }

        listings.remove(listingId);
        save();
        return "下架成功，物品已返还";
    }

    public List<MarketListing> getAllListings() {
        return new ArrayList<>(listings.values());
    }

    public List<MarketListing> getPlayerListings(UUID uuid) {
        List<MarketListing> result = new ArrayList<>();
        for (MarketListing listing : listings.values()) {
            if (listing.sellerUuid.equals(uuid)) {
                result.add(listing);
            }
        }
        return result;
    }

    public MarketListing getListing(int id) {
        return listings.get(id);
    }

    private String itemToBase64(ItemStack item) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(item);
            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            return "";
        }
    }

    private ItemStack itemFromBase64(String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();
            return item;
        } catch (Exception e) {
            return null;
        }
    }
}
