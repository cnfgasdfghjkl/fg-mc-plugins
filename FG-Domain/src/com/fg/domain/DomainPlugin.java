package com.fg.domain;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class DomainPlugin extends JavaPlugin implements CommandExecutor {

    private File dataFile;
    private FileConfiguration data;
    private final Map<String, String> ipToSubdomain = new HashMap<>();
    private static final String CF_API = "https://api.cloudflare.com/client/v4";
    private String apiToken;
    private String zoneId;
    private String baseDomain;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        apiToken = getConfig().getString("cloudflare.api-token", "");
        zoneId = getConfig().getString("cloudflare.zone-id", "");
        baseDomain = getConfig().getString("cloudflare.base-domain", "fgpan.cc.cd");

        dataFile = new File(getDataFolder(), "domains.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);

        if (data.contains("mappings")) {
            for (String key : data.getConfigurationSection("mappings").getKeys(false)) {
                ipToSubdomain.put(key, data.getString("mappings." + key));
            }
        }

        getCommand("https").setExecutor(this);
        getCommand("a").setExecutor(this);
        getLogger().info("FG-Domain 已启用 - 域名: " + baseDomain);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以使用");
            return true;
        }
        Player player = (Player) sender;
        String ip = player.getAddress().getAddress().getHostAddress();

        if (command.getName().equalsIgnoreCase("https")) {
            String subdomain;
            if (args.length > 0) {
                subdomain = args[0].toLowerCase();
                if (!subdomain.matches("^[a-z0-9]{2,10}$")) {
                    player.sendMessage(ChatColor.RED + "子域名只能是2-10位字母或数字");
                    return true;
                }
            } else {
                subdomain = generateRandomSubdomain();
            }

            if (ipToSubdomain.containsKey(ip)) {
                player.sendMessage(ChatColor.YELLOW + "你已有子域名: " + ChatColor.GREEN +
                    ipToSubdomain.get(ip) + "." + baseDomain);
                return true;
            }

            // 检查是否已被占用
            if (ipToSubdomain.containsValue(subdomain)) {
                player.sendMessage(ChatColor.RED + "该子域名已被占用，请换一个");
                return true;
            }

            // 异步调用Cloudflare API
            final String finalSubdomain = subdomain;
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                boolean success = createDnsRecord(finalSubdomain, "127.0.0.1");
                Bukkit.getScheduler().runTask(this, () -> {
                    if (success) {
                        ipToSubdomain.put(ip, finalSubdomain);
                        saveData();
                        player.sendMessage(ChatColor.GREEN + "子域名创建成功！");
                        player.sendMessage(ChatColor.YELLOW + "你的域名: " + ChatColor.AQUA +
                            finalSubdomain + "." + baseDomain);
                        player.sendMessage(ChatColor.GRAY + "使用 /a <你的公网IP> 绑定IP地址");
                    } else {
                        player.sendMessage(ChatColor.RED + "创建失败，请稍后重试");
                    }
                });
            });
            return true;
        }

        if (command.getName().equalsIgnoreCase("a")) {
            if (args.length < 1) {
                player.sendMessage(ChatColor.RED + "用法: /a <你的公网IP>");
                return true;
            }
            String targetIp = args[0];
            if (!targetIp.matches("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$")) {
                player.sendMessage(ChatColor.RED + "无效的IP地址格式");
                return true;
            }

            String subdomain = ipToSubdomain.get(ip);
            if (subdomain == null) {
                player.sendMessage(ChatColor.RED + "你还没有子域名，请先使用 /https 创建");
                return true;
            }

            final String finalSubdomain = subdomain;
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                boolean success = updateDnsRecord(finalSubdomain, targetIp);
                Bukkit.getScheduler().runTask(this, () -> {
                    if (success) {
                        player.sendMessage(ChatColor.GREEN + "IP绑定成功！");
                        player.sendMessage(ChatColor.YELLOW + "现在可以用 " + ChatColor.AQUA +
                            finalSubdomain + "." + baseDomain + ChatColor.YELLOW + " 代替 " + targetIp);
                    } else {
                        player.sendMessage(ChatColor.RED + "绑定失败，请稍后重试");
                    }
                });
            });
            return true;
        }

        return false;
    }

    private String generateRandomSubdomain() {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random rand = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(rand.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private boolean createDnsRecord(String subdomain, String content) {
        try {
            String json = "{\"type\":\"A\",\"name\":\"" + subdomain + "." + baseDomain +
                "\",\"content\":\"" + content + "\",\"ttl\":300,\"proxied\":false}";
            URL url = new URL(CF_API + "/zones/" + zoneId + "/dns_records");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiToken);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }
            int code = conn.getResponseCode();
            return code == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean updateDnsRecord(String subdomain, String content) {
        try {
            // 先查找记录ID
            String recordId = findDnsRecord(subdomain);
            if (recordId == null) return false;

            String json = "{\"type\":\"A\",\"name\":\"" + subdomain + "." + baseDomain +
                "\",\"content\":\"" + content + "\",\"ttl\":300,\"proxied\":false}";
            URL url = new URL(CF_API + "/zones/" + zoneId + "/dns_records/" + recordId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Authorization", "Bearer " + apiToken);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }
            int code = conn.getResponseCode();
            return code == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String findDnsRecord(String subdomain) {
        try {
            URL url = new URL(CF_API + "/zones/" + zoneId + "/dns_records?name=" +
                subdomain + "." + baseDomain);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + apiToken);
            Scanner scanner = new Scanner(conn.getInputStream());
            String response = scanner.useDelimiter("\\A").next();
            scanner.close();
            // 简单解析id
            int idIndex = response.indexOf("\"id\":\"");
            if (idIndex >= 0) {
                int start = idIndex + 6;
                int end = response.indexOf("\"", start);
                return response.substring(start, end);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private void saveData() {
        try {
            for (String key : ipToSubdomain.keySet()) {
                data.set("mappings." + key, ipToSubdomain.get(key));
            }
            data.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
