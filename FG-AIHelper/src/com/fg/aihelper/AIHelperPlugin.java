package com.fg.aihelper;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AIHelperPlugin extends JavaPlugin implements Listener, CommandExecutor {

    private final Set<UUID> enabledPlayers = new HashSet<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final long COOLDOWN_MS = 3000;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("no").setExecutor(this);
        getLogger().info("FG-AIHelper 已启用 - 关键词匹配助手，默认开启，/no关闭");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        enabledPlayers.add(event.getPlayer().getUniqueId());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以使用");
            return true;
        }
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        if (enabledPlayers.contains(uuid)) {
            enabledPlayers.remove(uuid);
            player.sendMessage(ChatColor.YELLOW + "AI助手已关闭");
        } else {
            enabledPlayers.add(uuid);
            player.sendMessage(ChatColor.GREEN + "AI助手已开启");
        }
        return true;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!enabledPlayers.contains(uuid)) return;

        // 冷却检查
        long now = System.currentTimeMillis();
        if (cooldowns.containsKey(uuid) && now - cooldowns.get(uuid) < COOLDOWN_MS) {
            return;
        }

        String message = event.getMessage().toLowerCase();
        String response = processCommand(player, message);
        if (response != null) {
            cooldowns.put(uuid, now);
            Bukkit.getScheduler().runTask(this, () -> {
                player.sendMessage(ChatColor.AQUA + "[AI助手] " + ChatColor.WHITE + response);
            });
        }
    }

    private String processCommand(Player player, String msg) {
        // 出售相关
        if (msg.contains("卖") || msg.contains("出售") || msg.contains("sell")) {
            return handleSell(player, msg);
        }

        // 连锁挖矿
        if (msg.contains("连锁") || msg.contains("挖矿") || msg.contains("vein")) {
            player.performCommand("veinminer");
            return "已切换连锁挖掘状态";
        }

        // 时间
        if (msg.contains("白天") || msg.contains("早上") || msg.contains("day")) {
            player.setPlayerTime(0, false);
            return "已设置为白天";
        }
        if (msg.contains("晚上") || msg.contains("夜晚") || msg.contains("night")) {
            player.setPlayerTime(13000, false);
            return "已设置为晚上";
        }

        // 天气
        if (msg.contains("晴天") || msg.contains("不下雨") || msg.contains("sun")) {
            player.setPlayerWeather(org.bukkit.WeatherType.CLEAR);
            return "已设置为晴天";
        }
        if (msg.contains("下雨") || msg.contains("rain")) {
            player.setPlayerWeather(org.bukkit.WeatherType.DOWNFALL);
            return "已设置为下雨";
        }

        // 游戏模式（只能改自己，且只能生存）
        if (msg.contains("生存") || msg.contains("survival")) {
            player.setGameMode(GameMode.SURVIVAL);
            return "已切换到生存模式";
        }

        // 清怪（只清自己周围的敌对生物）
        if (msg.contains("清怪") || msg.contains("杀怪") || msg.contains("clear")) {
            final int[] count = {0};
            player.getNearbyEntities(20, 20, 20).forEach(e -> {
                if (e instanceof org.bukkit.entity.Monster) {
                    e.remove();
                    count[0]++;
                }
            });
            return "已清除周围 " + count[0] + " 个敌对生物";
        }

        // 药水效果
        if (msg.contains("速度") || msg.contains("speed")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600, 1));
            return "已给予速度II效果（30秒）";
        }
        if (msg.contains("力量") || msg.contains("strength")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 600, 1));
            return "已给予力量II效果（30秒）";
        }
        if (msg.contains("夜视") || msg.contains("night vision")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 600, 0));
            return "已给予夜视效果（30秒）";
        }

        // 回家/传送（传送到出生点）
        if (msg.contains("回家") || msg.contains("spawn") || msg.contains("回去")) {
            player.teleport(player.getWorld().getSpawnLocation());
            return "已传送回出生点";
        }

        // 难度（只能改自己客户端感知，实际服务器难度不变）
        if (msg.contains("和平") || msg.contains("peaceful")) {
            return "403 无权限：难度调整需要OP权限";
        }

        // 余额查询
        if (msg.contains("余额") || msg.contains("多少钱") || msg.contains("money") || msg.contains("金币")) {
            player.performCommand("money");
            return null; // 让经济插件自己回复
        }

        return null;
    }

    private String handleSell(Player player, String msg) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() == Material.AIR) {
            return "手里没有物品";
        }

        // 检查是否是可出售物品
        Material type = hand.getType();
        if (!isSellable(type)) {
            return "该物品不可出售（仅支持矿物和矿物成品）";
        }

        // 解析数量
        int amount = 1;
        if (msg.contains("全部") || msg.contains("所有") || msg.contains("all")) {
            amount = hand.getAmount();
        } else {
            // 尝试提取数字
            for (String word : msg.split("[^0-9]+")) {
                if (!word.isEmpty()) {
                    try {
                        amount = Integer.parseInt(word);
                        break;
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        if (amount > hand.getAmount()) amount = hand.getAmount();

        // 调用经济插件的出售命令
        player.performCommand("sell " + amount);
        return null;
    }

    private boolean isSellable(Material type) {
        return type == Material.COAL || type == Material.CHARCOAL ||
               type == Material.RAW_IRON || type == Material.IRON_INGOT || type == Material.IRON_BLOCK ||
               type == Material.RAW_COPPER || type == Material.COPPER_INGOT || type == Material.COPPER_BLOCK ||
               type == Material.RAW_GOLD || type == Material.GOLD_INGOT || type == Material.GOLD_BLOCK ||
               type == Material.DIAMOND || type == Material.DIAMOND_BLOCK ||
               type == Material.EMERALD || type == Material.EMERALD_BLOCK ||
               type == Material.LAPIS_LAZULI || type == Material.LAPIS_BLOCK ||
               type == Material.REDSTONE || type == Material.REDSTONE_BLOCK ||
               type == Material.QUARTZ || type == Material.QUARTZ_BLOCK ||
               type == Material.NETHERITE_SCRAP || type == Material.NETHERITE_INGOT || type == Material.NETHERITE_BLOCK ||
               type == Material.AMETHYST_SHARD;
    }
}
