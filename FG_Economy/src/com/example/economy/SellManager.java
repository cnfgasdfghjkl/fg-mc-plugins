package com.example.economy;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class SellManager {

    private final EconomyPlugin plugin;
    private final Map<Material, Double> sellPrices = new HashMap<>();

    public SellManager(EconomyPlugin plugin) {
        this.plugin = plugin;
        registerPrices();
    }

    private void registerPrices() {
        // 煤炭
        sellPrices.put(Material.COAL, 5.0);
        sellPrices.put(Material.CHARCOAL, 3.0);
        sellPrices.put(Material.COAL_BLOCK, 45.0);
        // 铁
        sellPrices.put(Material.RAW_IRON, 8.0);
        sellPrices.put(Material.IRON_INGOT, 12.0);
        sellPrices.put(Material.IRON_BLOCK, 108.0);
        // 铜
        sellPrices.put(Material.RAW_COPPER, 4.0);
        sellPrices.put(Material.COPPER_INGOT, 6.0);
        sellPrices.put(Material.COPPER_BLOCK, 54.0);
        // 金
        sellPrices.put(Material.RAW_GOLD, 15.0);
        sellPrices.put(Material.GOLD_INGOT, 25.0);
        sellPrices.put(Material.GOLD_BLOCK, 225.0);
        // 钻石
        sellPrices.put(Material.DIAMOND, 100.0);
        sellPrices.put(Material.DIAMOND_BLOCK, 900.0);
        // 绿宝石
        sellPrices.put(Material.EMERALD, 80.0);
        sellPrices.put(Material.EMERALD_BLOCK, 720.0);
        // 青金石
        sellPrices.put(Material.LAPIS_LAZULI, 3.0);
        sellPrices.put(Material.LAPIS_BLOCK, 27.0);
        // 红石
        sellPrices.put(Material.REDSTONE, 2.0);
        sellPrices.put(Material.REDSTONE_BLOCK, 18.0);
        // 石英
        sellPrices.put(Material.QUARTZ, 4.0);
        sellPrices.put(Material.QUARTZ_BLOCK, 36.0);
        // 下界合金
        sellPrices.put(Material.NETHERITE_SCRAP, 500.0);
        sellPrices.put(Material.NETHERITE_INGOT, 1500.0);
        sellPrices.put(Material.NETHERITE_BLOCK, 13500.0);
        // 紫水晶
        sellPrices.put(Material.AMETHYST_SHARD, 10.0);
    }

    public boolean isSellable(Material material) {
        return sellPrices.containsKey(material);
    }

    public double getPrice(Material material) {
        return sellPrices.getOrDefault(material, 0.0);
    }

    public Map<Material, Double> getAllPrices() {
        return new HashMap<>(sellPrices);
    }

    /**
     * 出售手持物品
     */
    public SellResult sellHand(Player player, int amount) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() == Material.AIR) {
            return new SellResult(false, 0, 0, "手里没有物品");
        }

        Material type = hand.getType();
        if (!isSellable(type)) {
            return new SellResult(false, 0, 0, "该物品不可出售（仅支持矿物和矿物成品）");
        }

        int handAmount = hand.getAmount();
        int sellCount = Math.min(amount, handAmount);
        double totalPrice = getPrice(type) * sellCount;

        if (sellCount >= handAmount) {
            player.getInventory().setItemInMainHand(null);
        } else {
            hand.setAmount(handAmount - sellCount);
        }

        plugin.getEconomyManager().addBalance(player, totalPrice);
        return new SellResult(true, sellCount, totalPrice, "出售成功");
    }

    /**
     * 出售背包中所有可出售物品
     */
    public SellAllResult sellAll(Player player) {
        int totalAmount = 0;
        double totalPrice = 0;
        ItemStack[] contents = player.getInventory().getContents();

        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType() == Material.AIR) continue;

            Material type = item.getType();
            if (!isSellable(type)) continue;

            int amount = item.getAmount();
            totalPrice += getPrice(type) * amount;
            totalAmount += amount;
            player.getInventory().setItem(i, null);
        }

        if (totalAmount == 0) {
            return new SellAllResult(false, 0, 0, "背包中没有可出售的物品");
        }

        plugin.getEconomyManager().addBalance(player, totalPrice);
        return new SellAllResult(true, totalAmount, totalPrice, "出售成功");
    }

    /**
     * 出售背包中指定Material的物品
     * @param amount 要出售的数量，<=0表示全部
     */
    public SellResult sellMaterial(Player player, Material material, int amount) {
        if (!isSellable(material)) {
            return new SellResult(false, 0, 0, "该物品不可出售（不是矿物或矿物成品）");
        }

        int totalSold = 0;
        double totalPrice = 0;
        ItemStack[] contents = player.getInventory().getContents();

        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType() == Material.AIR) continue;
            if (item.getType() != material) continue;

            int itemAmount = item.getAmount();
            int sellCount;
            if (amount <= 0) {
                sellCount = itemAmount;
            } else {
                sellCount = Math.min(amount - totalSold, itemAmount);
            }

            if (sellCount <= 0) break;

            totalPrice += getPrice(material) * sellCount;
            totalSold += sellCount;

            if (sellCount >= itemAmount) {
                player.getInventory().setItem(i, null);
            } else {
                item.setAmount(itemAmount - sellCount);
            }

            if (amount > 0 && totalSold >= amount) break;
        }

        if (totalSold == 0) {
            return new SellResult(false, 0, 0, "背包中没有该物品");
        }

        plugin.getEconomyManager().addBalance(player, totalPrice);
        return new SellResult(true, totalSold, totalPrice, "出售成功");
    }

    public static class SellResult {
        public final boolean success;
        public final int amount;
        public final double totalPrice;
        public final String message;

        public SellResult(boolean success, int amount, double totalPrice, String message) {
            this.success = success;
            this.amount = amount;
            this.totalPrice = totalPrice;
            this.message = message;
        }
    }

    public static class SellAllResult {
        public final boolean success;
        public final int totalAmount;
        public final double totalPrice;
        public final String message;

        public SellAllResult(boolean success, int totalAmount, double totalPrice, String message) {
            this.success = success;
            this.totalAmount = totalAmount;
            this.totalPrice = totalPrice;
            this.message = message;
        }
    }
}
