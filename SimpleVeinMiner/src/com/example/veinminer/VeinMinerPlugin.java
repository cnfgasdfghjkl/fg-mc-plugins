package com.example.veinminer;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class VeinMinerPlugin extends JavaPlugin implements Listener {

    private final Set<UUID> enabledPlayers = new HashSet<>();
    private final Set<Material> veinMaterials = new HashSet<>();

    // 26个方向（包含顶点、棱、面接触）
    private static final BlockFace[] FACES_26 = {
        BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH,
        BlockFace.EAST, BlockFace.WEST,
        // 棱
        BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST,
        BlockFace.UP_NORTH, BlockFace.UP_SOUTH, BlockFace.UP_EAST, BlockFace.UP_WEST,
        BlockFace.DOWN_NORTH, BlockFace.DOWN_SOUTH, BlockFace.DOWN_EAST, BlockFace.DOWN_WEST,
        // 顶点
        BlockFace.UP_NORTH_EAST, BlockFace.UP_NORTH_WEST, BlockFace.UP_SOUTH_EAST, BlockFace.UP_SOUTH_WEST,
        BlockFace.DOWN_NORTH_EAST, BlockFace.DOWN_NORTH_WEST, BlockFace.DOWN_SOUTH_EAST, BlockFace.DOWN_SOUTH_WEST
    };

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("veinminer").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§c只有玩家可以使用此命令");
                return true;
            }
            Player player = (Player) sender;
            UUID uuid = player.getUniqueId();
            if (enabledPlayers.contains(uuid)) {
                enabledPlayers.remove(uuid);
                player.sendMessage("§c连锁挖掘已关闭");
            } else {
                enabledPlayers.add(uuid);
                player.sendMessage("§a连锁挖掘已开启");
            }
            return true;
        });

        // 注册可连锁的方块
        registerVeinMaterials();
        getLogger().info("SimpleVeinMiner 已启用 - 26向连接，掉落物直接进背包");
    }

    private void registerVeinMaterials() {
        // 矿石
        veinMaterials.add(Material.COAL_ORE);
        veinMaterials.add(Material.IRON_ORE);
        veinMaterials.add(Material.GOLD_ORE);
        veinMaterials.add(Material.DIAMOND_ORE);
        veinMaterials.add(Material.EMERALD_ORE);
        veinMaterials.add(Material.LAPIS_ORE);
        veinMaterials.add(Material.REDSTONE_ORE);
        veinMaterials.add(Material.COPPER_ORE);
        veinMaterials.add(Material.NETHER_QUARTZ_ORE);
        veinMaterials.add(Material.NETHER_GOLD_ORE);
        veinMaterials.add(Material.ANCIENT_DEBRIS);
        // 深层矿石
        veinMaterials.add(Material.DEEPSLATE_COAL_ORE);
        veinMaterials.add(Material.DEEPSLATE_IRON_ORE);
        veinMaterials.add(Material.DEEPSLATE_GOLD_ORE);
        veinMaterials.add(Material.DEEPSLATE_DIAMOND_ORE);
        veinMaterials.add(Material.DEEPSLATE_EMERALD_ORE);
        veinMaterials.add(Material.DEEPSLATE_LAPIS_ORE);
        veinMaterials.add(Material.DEEPSLATE_REDSTONE_ORE);
        veinMaterials.add(Material.DEEPSLATE_COPPER_ORE);
        // 粗金属块
        veinMaterials.add(Material.RAW_IRON_BLOCK);
        veinMaterials.add(Material.RAW_GOLD_BLOCK);
        veinMaterials.add(Material.RAW_COPPER_BLOCK);
        // 木头
        veinMaterials.add(Material.OAK_LOG);
        veinMaterials.add(Material.SPRUCE_LOG);
        veinMaterials.add(Material.BIRCH_LOG);
        veinMaterials.add(Material.JUNGLE_LOG);
        veinMaterials.add(Material.ACACIA_LOG);
        veinMaterials.add(Material.DARK_OAK_LOG);
        veinMaterials.add(Material.MANGROVE_LOG);
        veinMaterials.add(Material.CHERRY_LOG);
        veinMaterials.add(Material.CRIMSON_STEM);
        veinMaterials.add(Material.WARPED_STEM);
        // 树叶
        veinMaterials.add(Material.OAK_LEAVES);
        veinMaterials.add(Material.SPRUCE_LEAVES);
        veinMaterials.add(Material.BIRCH_LEAVES);
        veinMaterials.add(Material.JUNGLE_LEAVES);
        veinMaterials.add(Material.ACACIA_LEAVES);
        veinMaterials.add(Material.DARK_OAK_LEAVES);
        veinMaterials.add(Material.MANGROVE_LEAVES);
        veinMaterials.add(Material.CHERRY_LEAVES);
        veinMaterials.add(Material.AZALEA_LEAVES);
        veinMaterials.add(Material.FLOWERING_AZALEA_LEAVES);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // 默认开启连锁挖掘
        enabledPlayers.add(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!enabledPlayers.contains(player.getUniqueId())) return;

        Block startBlock = event.getBlock();
        Material type = startBlock.getType();
        if (!veinMaterials.contains(type)) return;

        // BFS搜索所有相连的同类方块
        Set<Block> connected = new HashSet<>();
        Deque<Block> queue = new ArrayDeque<>();
        queue.add(startBlock);
        connected.add(startBlock);

        int maxBlocks = 500; // 防止无限连锁

        while (!queue.isEmpty() && connected.size() < maxBlocks) {
            Block current = queue.poll();
            for (BlockFace face : FACES_26) {
                Block neighbor = current.getRelative(face);
                if (neighbor.getType() == type && !connected.contains(neighbor)) {
                    connected.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        // 破坏所有相连方块，掉落物直接进背包
        for (Block block : connected) {
            if (block.equals(startBlock)) continue; // 第一个已经被事件处理
            Material blockType = block.getType();
            block.setType(Material.AIR);

            // 获取掉落物
            List<ItemStack> drops = new ArrayList<>(block.getDrops(player.getInventory().getItemInMainHand()));
            for (ItemStack drop : drops) {
                player.getInventory().addItem(drop).forEach((slot, leftover) ->
                    player.getWorld().dropItemNaturally(player.getLocation(), leftover)
                );
            }
        }

        if (connected.size() > 1) {
            player.sendMessage("§a连锁挖掘了 " + connected.size() + " 个方块");
        }
    }
}
