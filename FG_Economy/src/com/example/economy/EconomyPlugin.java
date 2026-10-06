package com.example.economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class EconomyPlugin extends JavaPlugin implements CommandExecutor {

    private EconomyManager economyManager;
    private SellManager sellManager;
    private PlayerMarketManager playerMarketManager;
    private ShopGUI shopGUI;

    @Override
    public void onEnable() {
        economyManager = new EconomyManager(this);
        sellManager = new SellManager(this);
        playerMarketManager = new PlayerMarketManager(this);
        shopGUI = new ShopGUI(this);

        getServer().getPluginManager().registerEvents(shopGUI, this);

        getCommand("money").setExecutor(this);
        getCommand("sell").setExecutor(this);
        getCommand("sellall").setExecutor(this);
        getCommand("shop").setExecutor(this);
        getCommand("pay").setExecutor(this);
        getCommand("market").setExecutor(this);
        getCommand("menu").setExecutor(this);
        getCommand("ecoadmin").setExecutor(this);
        getCommand("mall").setExecutor(this);

        getLogger().info("FG_Economy 经济系统已启用");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以使用此命令");
            return true;
        }
        Player player = (Player) sender;

        switch (command.getName().toLowerCase()) {
            case "money":
                player.sendMessage(ChatColor.GOLD + "========== 余额 ==========");
                player.sendMessage(ChatColor.YELLOW + "  当前余额: " + ChatColor.GREEN +
                    economyManager.format(economyManager.getBalance(player)) + " 金币");
                player.sendMessage(ChatColor.GOLD + "==========================");
                return true;
            case "sell":
                return handleSell(player, args);
            case "sellall":
                SellManager.SellAllResult allResult = sellManager.sellAll(player);
                if (allResult.success) {
                    player.sendMessage(ChatColor.GOLD + "========== 全部出售 ==========");
                    player.sendMessage(ChatColor.YELLOW + "  出售物品: " + ChatColor.WHITE + allResult.totalAmount + " 个");
                    player.sendMessage(ChatColor.YELLOW + "  获得金币: " + ChatColor.GREEN +
                        economyManager.format(allResult.totalPrice) + " 金币");
                    player.sendMessage(ChatColor.YELLOW + "  当前余额: " + ChatColor.GREEN +
                        economyManager.format(economyManager.getBalance(player)) + " 金币");
                    player.sendMessage(ChatColor.GOLD + "============================");
                } else {
                    player.sendMessage(ChatColor.RED + allResult.message);
                }
                return true;
            case "shop":
                shopGUI.openMainMenu(player);
                return true;
            case "pay":
                return handlePay(player, args);
            case "market":
                return handleMarket(player, args);
            case "menu":
                shopGUI.giveMenuCompass(player);
                return true;
            case "ecoadmin":
                return handleEcoAdmin(player, args);
            case "mall":
                return handleMall(player, args);
            default:
                return false;
        }
    }

    private boolean handleSell(Player player, String[] args) {
        int amount = 1;
        if (args.length > 0) {
            try {
                amount = Integer.parseInt(args[0]);
                if (amount < 1) amount = 1;
                if (amount > 64) amount = 64;
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "数量必须是数字");
                return true;
            }
        }
        SellManager.SellResult result = sellManager.sellHand(player, amount);
        if (result.success) {
            player.sendMessage(ChatColor.GREEN + "出售成功！卖出 " + result.amount + " 个，获得 " +
                ChatColor.GOLD + economyManager.format(result.totalPrice) + ChatColor.GREEN + " 金币");
        } else {
            player.sendMessage(ChatColor.RED + result.message);
        }
        return true;
    }

    private boolean handlePay(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "用法: /pay <玩家> <金额>");
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "玩家不在线");
            return true;
        }
        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "金额必须是数字");
            return true;
        }
        if (amount <= 0) {
            player.sendMessage(ChatColor.RED + "金额必须大于0");
            return true;
        }
        if (economyManager.getBalance(player) < amount) {
            player.sendMessage(ChatColor.RED + "余额不足");
            return true;
        }
        economyManager.removeBalance(player, amount);
        economyManager.addBalance(target, amount);
        player.sendMessage(ChatColor.GREEN + "已转账 " + ChatColor.GOLD +
            economyManager.format(amount) + ChatColor.GREEN + " 金币给 " + target.getName());
        target.sendMessage(ChatColor.GREEN + "收到 " + player.getName() + " 转账 " +
            ChatColor.GOLD + economyManager.format(amount) + ChatColor.GREEN + " 金币");
        return true;
    }

    private boolean handleMarket(Player player, String[] args) {
        if (args.length == 0) {
            shopGUI.openPlayerMarket(player, 0);
            return true;
        }
        if (args[0].equalsIgnoreCase("my")) {
            shopGUI.openMyListings(player);
            return true;
        }
        try {
            double price = Double.parseDouble(args[0]);
            String result = playerMarketManager.listItem(player, price);
            if (result.contains("成功")) {
                player.sendMessage(ChatColor.GREEN + result);
            } else {
                player.sendMessage(ChatColor.RED + result);
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "用法: /market <价格> 或 /market my");
        }
        return true;
    }

    private boolean handleEcoAdmin(Player player, String[] args) {
        if (!player.hasPermission("economy.admin")) {
            player.sendMessage(ChatColor.RED + "403 无权限：此命令需要OP权限");
            return true;
        }
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "用法: /ecoadmin <set|add|remove> <玩家> <金额>");
            return true;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "玩家不在线");
            return true;
        }
        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "金额必须是数字");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "set":
                economyManager.setBalance(target, amount);
                player.sendMessage(ChatColor.GREEN + "已设置 " + target.getName() + " 的余额为 " + amount);
                break;
            case "add":
                economyManager.addBalance(target, amount);
                player.sendMessage(ChatColor.GREEN + "已给 " + target.getName() + " 增加 " + amount + " 金币");
                break;
            case "remove":
                economyManager.removeBalance(target, amount);
                player.sendMessage(ChatColor.GREEN + "已从 " + target.getName() + " 扣除 " + amount + " 金币");
                break;
            default:
                player.sendMessage(ChatColor.RED + "未知操作: " + args[0]);
        }
        return true;
    }

    private boolean handleMall(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "用法:");
            player.sendMessage(ChatColor.GRAY + "  /mall <物品ID> [数量] - 出售背包中指定物品");
            player.sendMessage(ChatColor.GRAY + "  /mall yes - 出售背包中所有可出售物品");
            player.sendMessage(ChatColor.GRAY + "示例: /mall minecraft:tnt 或 /mall minecraft:tnt 10");
            return true;
        }

        if (args[0].equalsIgnoreCase("yes")) {
            SellManager.SellAllResult result = sellManager.sellAll(player);
            if (result.success) {
                player.sendMessage(ChatColor.GOLD + "========== 全部出售 ==========");
                player.sendMessage(ChatColor.YELLOW + "  出售物品: " + ChatColor.WHITE + result.totalAmount + " 个");
                player.sendMessage(ChatColor.YELLOW + "  获得金币: " + ChatColor.GREEN +
                    economyManager.format(result.totalPrice) + " 金币");
                player.sendMessage(ChatColor.YELLOW + "  当前余额: " + ChatColor.GREEN +
                    economyManager.format(economyManager.getBalance(player)) + " 金币");
                player.sendMessage(ChatColor.GOLD + "============================");
            } else {
                player.sendMessage(ChatColor.RED + result.message);
            }
            return true;
        }

        String materialName = args[0].toUpperCase();
        if (materialName.contains(":")) {
            materialName = materialName.substring(materialName.indexOf(":") + 1);
        }

        Material material = Material.getMaterial(materialName);
        if (material == null) {
            player.sendMessage(ChatColor.RED + "未知物品ID: " + args[0]);
            player.sendMessage(ChatColor.GRAY + "示例: /mall minecraft:tnt, /mall minecraft:diamond");
            return true;
        }

        int amount = 0;
        if (args.length > 1) {
            try {
                amount = Integer.parseInt(args[1]);
                if (amount < 1) {
                    player.sendMessage(ChatColor.RED + "数量必须大于0");
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "数量必须是数字");
                return true;
            }
        }

        SellManager.SellResult result = sellManager.sellMaterial(player, material, amount);
        if (result.success) {
            String amountText = (amount <= 0) ? "全部" : String.valueOf(result.amount);
            player.sendMessage(ChatColor.GREEN + "出售成功！卖出 " + amountText + " 个 " +
                ChatColor.WHITE + material.name().toLowerCase() + ChatColor.GREEN + "，获得 " +
                ChatColor.GOLD + economyManager.format(result.totalPrice) + ChatColor.GREEN + " 金币");
            player.sendMessage(ChatColor.GRAY + "当前余额: " + ChatColor.GREEN +
                economyManager.format(economyManager.getBalance(player)) + " 金币");
        } else {
            player.sendMessage(ChatColor.RED + result.message);
        }
        return true;
    }

    public EconomyManager getEconomyManager() { return economyManager; }
    public SellManager getSellManager() { return sellManager; }
    public PlayerMarketManager getPlayerMarketManager() { return playerMarketManager; }
    public ShopGUI getShopGUI() { return shopGUI; }
}
