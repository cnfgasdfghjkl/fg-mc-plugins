package com.example.economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopGUI implements Listener {

    private final EconomyPlugin plugin;
    private final Map<Material, Double> buyPrices = new HashMap<>();

    public ShopGUI(EconomyPlugin plugin) {
        this.plugin = plugin;
        registerBuyPrices();
    }

    private void registerBuyPrices() {
        buyPrices.put(Material.BREAD, 5.0);
        buyPrices.put(Material.COOKED_BEEF, 10.0);
        buyPrices.put(Material.COOKED_PORKCHOP, 8.0);
        buyPrices.put(Material.COOKED_CHICKEN, 7.0);
        buyPrices.put(Material.COOKED_MUTTON, 7.0);
        buyPrices.put(Material.COOKED_COD, 6.0);
        buyPrices.put(Material.COOKED_SALMON, 8.0);
        buyPrices.put(Material.GOLDEN_CARROT, 15.0);
        buyPrices.put(Material.APPLE, 4.0);
        buyPrices.put(Material.GOLDEN_APPLE, 100.0);
        buyPrices.put(Material.WOODEN_PICKAXE, 10.0);
        buyPrices.put(Material.STONE_PICKAXE, 25.0);
        buyPrices.put(Material.IRON_PICKAXE, 80.0);
        buyPrices.put(Material.DIAMOND_PICKAXE, 500.0);
        buyPrices.put(Material.WOODEN_AXE, 10.0);
        buyPrices.put(Material.STONE_AXE, 25.0);
        buyPrices.put(Material.IRON_AXE, 80.0);
        buyPrices.put(Material.DIAMOND_AXE, 500.0);
        buyPrices.put(Material.WOODEN_SWORD, 10.0);
        buyPrices.put(Material.STONE_SWORD, 25.0);
        buyPrices.put(Material.IRON_SWORD, 80.0);
        buyPrices.put(Material.DIAMOND_SWORD, 500.0);
        buyPrices.put(Material.IRON_HELMET, 100.0);
        buyPrices.put(Material.IRON_CHESTPLATE, 150.0);
        buyPrices.put(Material.IRON_LEGGINGS, 130.0);
        buyPrices.put(Material.IRON_BOOTS, 90.0);
        buyPrices.put(Material.DIAMOND_HELMET, 600.0);
        buyPrices.put(Material.DIAMOND_CHESTPLATE, 900.0);
        buyPrices.put(Material.DIAMOND_LEGGINGS, 800.0);
        buyPrices.put(Material.DIAMOND_BOOTS, 550.0);
        buyPrices.put(Material.SHIELD, 60.0);
        buyPrices.put(Material.BOW, 120.0);
        buyPrices.put(Material.ARROW, 2.0);
        buyPrices.put(Material.OAK_PLANKS, 2.0);
        buyPrices.put(Material.COBBLESTONE, 1.0);
        buyPrices.put(Material.STONE, 2.0);
        buyPrices.put(Material.GLASS, 3.0);
        buyPrices.put(Material.TORCH, 1.0);
        buyPrices.put(Material.CHEST, 8.0);
        buyPrices.put(Material.FURNACE, 15.0);
        buyPrices.put(Material.CRAFTING_TABLE, 5.0);
        buyPrices.put(Material.BUCKET, 10.0);
        buyPrices.put(Material.WATER_BUCKET, 12.0);
        buyPrices.put(Material.LAVA_BUCKET, 15.0);
        buyPrices.put(Material.REDSTONE, 5.0);
        buyPrices.put(Material.PISTON, 20.0);
        buyPrices.put(Material.HOPPER, 30.0);
        buyPrices.put(Material.RAIL, 5.0);
        buyPrices.put(Material.MINECART, 20.0);
    }

    public void giveMenuCompass(Player player) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "§l经济菜单");
            meta.setLore(Arrays.asList(ChatColor.GRAY + "右键打开经济系统菜单"));
            compass.setItemMeta(meta);
        }
        player.getInventory().addItem(compass);
        player.sendMessage(ChatColor.GREEN + "已获得经济菜单指南针，右键打开");
    }

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GOLD + "§l经济系统 - 主菜单");
        ItemStack balanceItem = createGuiItem(Material.GOLD_INGOT, ChatColor.YELLOW + "§l你的余额",
            ChatColor.GREEN + plugin.getEconomyManager().format(plugin.getEconomyManager().getBalance(player)) + " 金币");
        inv.setItem(4, balanceItem);
        ItemStack shopItem = createGuiItem(Material.CHEST, ChatColor.AQUA + "§l系统商店",
            ChatColor.GRAY + "购买食物、工具、武器、材料", ChatColor.YELLOW + "点击进入");
        inv.setItem(10, shopItem);
        ItemStack quickListItem = createGuiItem(Material.DROPPER, ChatColor.LIGHT_PURPLE + "§l快捷上架",
            ChatColor.GRAY + "拖入物品后点确认上架到玩家市场",
            ChatColor.GRAY + "系统物品价格=收购价×1.12",
            ChatColor.GRAY + "其他物品默认50金币", ChatColor.YELLOW + "点击进入");
        inv.setItem(11, quickListItem);
        ItemStack quickSellItem = createGuiItem(Material.HOPPER, ChatColor.GOLD + "§l快捷出售",
            ChatColor.GRAY + "拖入物品后点确认直接出售",
            ChatColor.GRAY + "无需输入指令", ChatColor.YELLOW + "点击进入");
        inv.setItem(12, quickSellItem);
        ItemStack marketItem = createGuiItem(Material.EMERALD, ChatColor.LIGHT_PURPLE + "§l玩家市场",
            ChatColor.GRAY + "浏览其他玩家上架的物品", ChatColor.YELLOW + "点击进入");
        inv.setItem(14, marketItem);
        ItemStack myListingItem = createGuiItem(Material.WRITABLE_BOOK, ChatColor.GREEN + "§l我的上架",
            ChatColor.GRAY + "查看和管理你上架的物品", ChatColor.YELLOW + "点击进入");
        inv.setItem(16, myListingItem);
        ItemStack helpItem = createGuiItem(Material.BOOK, ChatColor.WHITE + "§l帮助",
            ChatColor.GRAY + "/money - 查看余额",
            ChatColor.GRAY + "/sell - 出售手持一个矿物",
            ChatColor.GRAY + "/sell [数量] - 出售手持矿物（1-64，超量卖全部）",
            ChatColor.GRAY + "/sellall - 出售背包所有可出售物品",
            ChatColor.GRAY + "/mall <物品ID> [数量] - 按ID出售指定物品",
            ChatColor.GRAY + "/mall yes - 出售背包所有可出售物品",
            ChatColor.GRAY + "/shop - 打开商店",
            ChatColor.GRAY + "/market <价格> - 上架手持物品",
            ChatColor.GRAY + "/pay <玩家> <金额> - 转账");
        inv.setItem(22, helpItem);
        player.openInventory(inv);
    }

    public void openCategoryMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GOLD + "§l系统商店 - 商品分类");
        inv.setItem(10, createGuiItem(Material.COOKED_BEEF, ChatColor.GREEN + "§l食物", ChatColor.YELLOW + "点击进入"));
        inv.setItem(12, createGuiItem(Material.DIAMOND_SWORD, ChatColor.RED + "§l工具武器", ChatColor.YELLOW + "点击进入"));
        inv.setItem(14, createGuiItem(Material.DIAMOND_CHESTPLATE, ChatColor.AQUA + "§l装备护甲", ChatColor.YELLOW + "点击进入"));
        inv.setItem(16, createGuiItem(Material.COBBLESTONE, ChatColor.GRAY + "§l材料", ChatColor.YELLOW + "点击进入"));
        inv.setItem(22, createGuiItem(Material.ARROW, ChatColor.RED + "§l返回主菜单", ""));
        player.openInventory(inv);
    }

    public void openSystemShop(Player player, String category) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "§l系统商店 - " + getCategoryName(category));
        List<Map.Entry<Material, Double>> items = new ArrayList<>();
        for (Map.Entry<Material, Double> entry : buyPrices.entrySet()) {
            if (isInCategory(entry.getKey(), category)) items.add(entry);
        }
        int slot = 0;
        for (Map.Entry<Material, Double> entry : items) {
            if (slot >= 45) break;
            ItemStack item = new ItemStack(entry.getKey());
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.YELLOW + "价格: " + ChatColor.GREEN + plugin.getEconomyManager().format(entry.getValue()) + " 金币");
                lore.add("");
                lore.add(ChatColor.GRAY + "左键购买1个");
                lore.add(ChatColor.GRAY + "右键购买10个");
                lore.add(ChatColor.GRAY + "Shift+左键购买一组");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(slot, item);
            slot++;
        }
        ItemStack back = createGuiItem(Material.ARROW, ChatColor.RED + "§l返回主菜单", "");
        inv.setItem(49, back);
        player.openInventory(inv);
    }

    public void openPlayerMarket(Player player, int page) {
        List<PlayerMarketManager.MarketListing> allListings = plugin.getPlayerMarketManager().getAllListings();
        int totalPages = (int) Math.ceil(allListings.size() / 45.0);
        if (totalPages == 0) totalPages = 1;
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;

        Inventory inv = Bukkit.createInventory(null, 54,
            ChatColor.LIGHT_PURPLE + "§l玩家市场 - 第" + (page + 1) + "/" + totalPages + "页");

        int start = page * 45;
        for (int i = 0; i < 45 && start + i < allListings.size(); i++) {
            PlayerMarketManager.MarketListing listing = allListings.get(start + i);
            ItemStack item = listing.item.clone();
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                List<String> lore = new ArrayList<>();
                if (meta.hasLore()) lore.addAll(meta.getLore());
                lore.add("");
                lore.add(ChatColor.GOLD + "卖家: " + ChatColor.WHITE + listing.sellerName);
                lore.add(ChatColor.GOLD + "价格: " + ChatColor.GREEN + plugin.getEconomyManager().format(listing.price) + " 金币");
                lore.add(ChatColor.GOLD + "物品ID: " + ChatColor.GRAY + listing.id);
                lore.add("");
                lore.add(ChatColor.YELLOW + "左键购买");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(i, item);
        }

        if (page > 0) inv.setItem(45, createGuiItem(Material.ARROW, ChatColor.YELLOW + "§l上一页", ""));
        if (page < totalPages - 1) inv.setItem(53, createGuiItem(Material.ARROW, ChatColor.YELLOW + "§l下一页", ""));
        inv.setItem(49, createGuiItem(Material.BARRIER, ChatColor.RED + "§l返回主菜单", ""));
        player.openInventory(inv);
    }

    public void openMyListings(Player player) {
        List<PlayerMarketManager.MarketListing> myListings = plugin.getPlayerMarketManager().getPlayerListings(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.GREEN + "§l我的上架 (" + myListings.size() + ")");
        for (int i = 0; i < 45 && i < myListings.size(); i++) {
            PlayerMarketManager.MarketListing listing = myListings.get(i);
            ItemStack item = listing.item.clone();
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                List<String> lore = new ArrayList<>();
                if (meta.hasLore()) lore.addAll(meta.getLore());
                lore.add("");
                lore.add(ChatColor.GOLD + "价格: " + ChatColor.GREEN + plugin.getEconomyManager().format(listing.price) + " 金币");
                lore.add(ChatColor.GOLD + "物品ID: " + ChatColor.GRAY + listing.id);
                lore.add("");
                lore.add(ChatColor.RED + "左键下架");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(i, item);
        }
        inv.setItem(49, createGuiItem(Material.ARROW, ChatColor.RED + "§l返回主菜单", ""));
        player.openInventory(inv);
    }

    private static final String QUICK_SELL_TITLE = ChatColor.GOLD + "§l快捷出售 - 拖入物品后点确认";
    private static final int[] QUICK_SELL_SLOTS = {9, 10, 11, 12, 13, 14, 15, 16, 17};
    private static final int QUICK_SELL_CONFIRM_SLOT = 21;
    private static final int QUICK_SELL_CANCEL_SLOT = 23;

    public void openQuickSell(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, QUICK_SELL_TITLE);
        ItemStack glass = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ", "");
        for (int i = 0; i < 9; i++) inv.setItem(i, glass);
        for (int i = 18; i < 27; i++) inv.setItem(i, glass);
        ItemStack hint = createGuiItem(Material.HOPPER,
            ChatColor.YELLOW + "§l将可出售物品拖入中间格子",
            ChatColor.GRAY + "仅支持矿物和矿物成品",
            ChatColor.GRAY + "不可出售的物品会自动返还");
        inv.setItem(4, hint);
        ItemStack confirm = createGuiItem(Material.GREEN_WOOL, ChatColor.GREEN + "§l确认出售",
            ChatColor.GRAY + "出售中间所有可出售物品", ChatColor.YELLOW + "点击确认");
        inv.setItem(QUICK_SELL_CONFIRM_SLOT, confirm);
        ItemStack cancel = createGuiItem(Material.RED_WOOL, ChatColor.RED + "§l取消",
            ChatColor.GRAY + "返还所有物品并关闭");
        inv.setItem(QUICK_SELL_CANCEL_SLOT, cancel);
        player.openInventory(inv);
    }

    private void handleQuickSellClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        Inventory inv = event.getInventory();
        Inventory clickedInv = event.getClickedInventory();
        if (clickedInv != null && clickedInv.equals(player.getInventory())) return;
        if (slot == QUICK_SELL_CONFIRM_SLOT) {
            event.setCancelled(true);
            int totalAmount = 0;
            double totalPrice = 0;
            for (int s : QUICK_SELL_SLOTS) {
                ItemStack item = inv.getItem(s);
                if (item == null || item.getType() == Material.AIR) continue;
                Material type = item.getType();
                if (plugin.getSellManager().isSellable(type)) {
                    int amount = item.getAmount();
                    double price = plugin.getSellManager().getPrice(type) * amount;
                    totalAmount += amount;
                    totalPrice += price;
                    inv.setItem(s, null);
                }
            }
            if (totalAmount > 0) {
                plugin.getEconomyManager().addBalance(player, totalPrice);
                player.sendMessage(ChatColor.GREEN + "快捷出售成功！卖出 " + totalAmount + " 个物品，获得 " +
                    ChatColor.GOLD + plugin.getEconomyManager().format(totalPrice) + ChatColor.GREEN + " 金币");
            } else {
                player.sendMessage(ChatColor.RED + "没有可出售的物品！");
            }
            boolean hasUnsold = false;
            for (int s : QUICK_SELL_SLOTS) {
                if (inv.getItem(s) != null && inv.getItem(s).getType() != Material.AIR) { hasUnsold = true; break; }
            }
            if (hasUnsold) player.sendMessage(ChatColor.YELLOW + "部分物品不可出售，已返还到背包");
            returnUnsoldItems(player, inv, QUICK_SELL_SLOTS);
            player.closeInventory();
            return;
        }
        if (slot == QUICK_SELL_CANCEL_SLOT) {
            event.setCancelled(true);
            returnUnsoldItems(player, inv, QUICK_SELL_SLOTS);
            player.closeInventory();
            player.sendMessage(ChatColor.GRAY + "已取消快捷出售");
            return;
        }
        if (isQuickSellSlot(slot)) return;
        event.setCancelled(true);
    }

    private static final String QUICK_LIST_TITLE = ChatColor.LIGHT_PURPLE + "§l快捷上架 - 拖入物品后点确认";
    private static final int[] QUICK_LIST_SLOTS = {9, 10, 11, 12, 13, 14, 15, 16, 17};
    private static final int QUICK_LIST_CONFIRM_SLOT = 21;
    private static final int QUICK_LIST_CANCEL_SLOT = 23;
    private static final double LIST_PRICE_MULTIPLIER = 1.12;
    private static final double DEFAULT_LIST_PRICE = 50.0;

    public void openQuickList(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, QUICK_LIST_TITLE);
        ItemStack glass = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ", "");
        for (int i = 0; i < 9; i++) inv.setItem(i, glass);
        for (int i = 18; i < 27; i++) inv.setItem(i, glass);
        ItemStack hint = createGuiItem(Material.DROPPER,
            ChatColor.YELLOW + "§l将物品拖入中间格子",
            ChatColor.GRAY + "系统物品价格=收购价×1.12",
            ChatColor.GRAY + "其他物品默认50金币",
            ChatColor.GRAY + "确认后上架到玩家市场");
        inv.setItem(4, hint);
        ItemStack confirm = createGuiItem(Material.GREEN_WOOL, ChatColor.GREEN + "§l确认上架",
            ChatColor.GRAY + "上架中间所有物品", ChatColor.YELLOW + "点击确认");
        inv.setItem(QUICK_LIST_CONFIRM_SLOT, confirm);
        ItemStack cancel = createGuiItem(Material.RED_WOOL, ChatColor.RED + "§l取消",
            ChatColor.GRAY + "返还所有物品并关闭");
        inv.setItem(QUICK_LIST_CANCEL_SLOT, cancel);
        player.openInventory(inv);
    }

    private void handleQuickListClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        Inventory inv = event.getInventory();
        Inventory clickedInv = event.getClickedInventory();
        if (clickedInv != null && clickedInv.equals(player.getInventory())) return;
        if (slot == QUICK_LIST_CONFIRM_SLOT) {
            event.setCancelled(true);
            int listedCount = 0;
            double totalValue = 0;
            for (int s : QUICK_LIST_SLOTS) {
                ItemStack item = inv.getItem(s);
                if (item == null || item.getType() == Material.AIR) continue;
                Material type = item.getType();
                double unitPrice;
                if (plugin.getSellManager().isSellable(type)) {
                    unitPrice = plugin.getSellManager().getPrice(type) * LIST_PRICE_MULTIPLIER;
                } else {
                    unitPrice = DEFAULT_LIST_PRICE;
                }
                double totalPrice = unitPrice * item.getAmount();
                String result = plugin.getPlayerMarketManager().listItemDirect(player, item.clone(), totalPrice);
                if (result.contains("成功")) {
                    listedCount++;
                    totalValue += totalPrice;
                    inv.setItem(s, null);
                }
            }
            if (listedCount > 0) {
                player.sendMessage(ChatColor.GREEN + "快捷上架成功！上架 " + listedCount + " 组物品，总价值 " +
                    ChatColor.GOLD + plugin.getEconomyManager().format(totalValue) + ChatColor.GREEN + " 金币");
            } else {
                player.sendMessage(ChatColor.RED + "没有可上架的物品！");
            }
            boolean hasRemaining = false;
            for (int s : QUICK_LIST_SLOTS) {
                if (inv.getItem(s) != null && inv.getItem(s).getType() != Material.AIR) { hasRemaining = true; break; }
            }
            if (hasRemaining) player.sendMessage(ChatColor.YELLOW + "部分物品上架失败，已返还到背包");
            returnUnsoldItems(player, inv, QUICK_LIST_SLOTS);
            player.closeInventory();
            return;
        }
        if (slot == QUICK_LIST_CANCEL_SLOT) {
            event.setCancelled(true);
            returnUnsoldItems(player, inv, QUICK_LIST_SLOTS);
            player.closeInventory();
            player.sendMessage(ChatColor.GRAY + "已取消快捷上架");
            return;
        }
        if (isQuickListSlot(slot)) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        if (!title.contains("经济系统") && !title.contains("系统商店") && !title.contains("玩家市场") &&
            !title.contains("我的上架") && !title.contains("快捷出售") && !title.contains("快捷上架")) return;
        if (title.contains("快捷出售")) { handleQuickSellClick(event); return; }
        if (title.contains("快捷上架")) { handleQuickListClick(event); return; }
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        if (title.contains("主菜单")) {
            int slot = event.getSlot();
            if (slot == 10) openCategoryMenu(player);
            else if (slot == 11) openQuickList(player);
            else if (slot == 12) openQuickSell(player);
            else if (slot == 14) openPlayerMarket(player, 0);
            else if (slot == 16) openMyListings(player);
            return;
        }
        if (title.contains("商品分类")) {
            String category = getCategoryFromItem(clicked.getType());
            if (category != null) openSystemShop(player, category);
            else if (clicked.getType() == Material.ARROW) openMainMenu(player);
            return;
        }
        if (title.contains("系统商店")) {
            if (clicked.getType() == Material.ARROW || clicked.getType() == Material.BARRIER) { openCategoryMenu(player); return; }
            if (clicked.getType() == Material.GOLD_INGOT) return;
            Material type = clicked.getType();
            if (!buyPrices.containsKey(type)) return;
            double price = buyPrices.get(type);
            int amount = 1;
            if (event.isRightClick()) amount = 10;
            if (event.isShiftClick() && event.isLeftClick()) amount = type.getMaxStackSize();
            double totalCost = price * amount;
            EconomyManager eco = plugin.getEconomyManager();
            if (eco.getBalance(player) < totalCost) {
                player.sendMessage(ChatColor.RED + "余额不足，需要 " + eco.format(totalCost) + " 金币");
                return;
            }
            eco.removeBalance(player, totalCost);
            ItemStack buyItem = new ItemStack(type, amount);
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(buyItem);
            if (!leftover.isEmpty()) {
                for (ItemStack item : leftover.values()) player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
            player.sendMessage(ChatColor.GREEN + "购买成功！花费 " + ChatColor.GOLD +
                eco.format(totalCost) + ChatColor.GREEN + " 金币，获得 " + amount + " 个 " + type.name());
            return;
        }
        if (title.contains("玩家市场")) {
            if (clicked.getType() == Material.ARROW) {
                int currentPage = extractPage(title);
                if (event.getSlot() == 45) openPlayerMarket(player, currentPage - 1);
                else if (event.getSlot() == 53) openPlayerMarket(player, currentPage + 1);
                return;
            }
            if (clicked.getType() == Material.BARRIER) { openMainMenu(player); return; }
            ItemMeta meta = clicked.getItemMeta();
            if (meta != null && meta.hasLore()) {
                for (String lore : meta.getLore()) {
                    if (lore.contains("物品ID:")) {
                        try {
                            int id = Integer.parseInt(lore.replaceAll("[^0-9]", ""));
                            String result = plugin.getPlayerMarketManager().buyItem(player, id);
                            if (result.contains("成功")) player.sendMessage(ChatColor.GREEN + result);
                            else player.sendMessage(ChatColor.RED + result);
                            openPlayerMarket(player, extractPage(title));
                        } catch (NumberFormatException ignored) {}
                        return;
                    }
                }
            }
            return;
        }
        if (title.contains("我的上架")) {
            if (clicked.getType() == Material.ARROW) { openMainMenu(player); return; }
            ItemMeta meta = clicked.getItemMeta();
            if (meta != null && meta.hasLore()) {
                for (String lore : meta.getLore()) {
                    if (lore.contains("物品ID:")) {
                        try {
                            int id = Integer.parseInt(lore.replaceAll("[^0-9]", ""));
                            String result = plugin.getPlayerMarketManager().removeListing(player, id);
                            if (result.contains("成功")) player.sendMessage(ChatColor.GREEN + result);
                            else player.sendMessage(ChatColor.RED + result);
                            openMyListings(player);
                        } catch (NumberFormatException ignored) {}
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();
        if (title.contains("快捷出售")) returnUnsoldItems(player, event.getInventory(), QUICK_SELL_SLOTS);
        if (title.contains("快捷上架")) returnUnsoldItems(player, event.getInventory(), QUICK_LIST_SLOTS);
    }

    private void returnUnsoldItems(Player player, Inventory inv, int[] slots) {
        for (int slot : slots) {
            ItemStack item = inv.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);
                if (!leftover.isEmpty()) player.getWorld().dropItemNaturally(player.getLocation(), item);
                inv.setItem(slot, null);
            }
        }
    }

    private boolean isQuickSellSlot(int slot) {
        for (int s : QUICK_SELL_SLOTS) if (s == slot) return true;
        return false;
    }

    private boolean isQuickListSlot(int slot) {
        for (int s : QUICK_LIST_SLOTS) if (s == slot) return true;
        return false;
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0 && !lore[0].isEmpty()) meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    private String getCategoryName(String category) {
        switch (category) {
            case "food": return "食物";
            case "tools": return "工具武器";
            case "armor": return "装备护甲";
            case "materials": return "材料";
            default: return "全部";
        }
    }

    private String getCategoryFromItem(Material material) {
        switch (material) {
            case COOKED_BEEF: return "food";
            case DIAMOND_SWORD: return "tools";
            case DIAMOND_CHESTPLATE: return "armor";
            case COBBLESTONE: return "materials";
            default: return null;
        }
    }

    private boolean isInCategory(Material material, String category) {
        switch (category) {
            case "food": return material.isEdible();
            case "tools":
                return material.name().contains("PICKAXE") || material.name().contains("AXE") ||
                       material.name().contains("SWORD") || material.name().contains("SHOVEL") ||
                       material.name().contains("HOE") || material == Material.BOW || material == Material.SHIELD;
            case "armor":
                return material.name().contains("HELMET") || material.name().contains("CHESTPLATE") ||
                       material.name().contains("LEGGINGS") || material.name().contains("BOOTS");
            case "materials":
                return !material.isEdible() && !material.name().contains("PICKAXE") &&
                       !material.name().contains("AXE") && !material.name().contains("SWORD") &&
                       !material.name().contains("HELMET") && !material.name().contains("CHESTPLATE") &&
                       !material.name().contains("LEGGINGS") && !material.name().contains("BOOTS");
            default: return true;
        }
    }

    private int extractPage(String title) {
        try {
            String pageStr = title.replaceAll("[^0-9/]", "");
            String[] parts = pageStr.split("/");
            return Integer.parseInt(parts[0]) - 1;
        } catch (Exception e) {
            return 0;
        }
    }
}
