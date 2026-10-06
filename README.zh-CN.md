# FG Minecraft 插件集

面向 **Spigot/Paper 1.20.1** 服务端开发的自研 Minecraft 插件合集，基于 Java 17 编写，以 **GNU GPL v3** 协议开源。

> 本文件为 README 的中文翻译版，英文原版请见 [README.md](README.md)。

## 插件一览

| 插件 | 功能 | 指令 |
|------|------|------|
| **SimpleVeinMiner** | 连锁挖矿，26向连接（点/棱/面接触均可），掉落物直接进背包，支持矿石、深板岩矿石、粗金属块、全部原木与树叶。 | `/veinminer` |
| **FG_Economy** | 完整经济系统：余额、出售、系统商店、玩家市场、快捷出售/快捷上架GUI、`/mall` 批量出售、转账、管理。 | `/money` `/sell` `/sellall` `/mall` `/shop` `/market` `/pay` `/menu` `/ecoadmin` |
| **FG-AIHelper** | 聊天关键词助手。自动识别玩家聊天意图并执行对应指令，默认开启，3秒冷却。 | `/no` |
| **GameModeLock** | 强制所有玩家（含OP）保持生存模式，任何切换到创造/冒险/旁观的操作都会被取消。 | — |
| **FG-Domain** | 基于 Cloudflare API 的游戏内 DDNS 服务，`/https` 申请子域名，`/a` 绑定公网IP。 | `/https` `/a` |

## 环境要求

- **服务端**：Spigot / Paper **1.20.1**
- **Java**：JDK **17+**（编译），运行 Paper 1.20.1 建议 Java **21**
- **依赖**：`spigot-api-1.20.1-R0.1-SNAPSHOT.jar`

## 编译

### Linux / macOS
```bash
chmod +x build.sh
./build.sh
```

### Windows
双击 `build.bat`，或在终端中运行。

编译脚本会自动下载 `spigot-api-1.20.1-R0.1-SNAPSHOT.jar`（失败时可手动放入 `libs/` 目录）。编译产物在 `build/` 目录，复制到服务端 `plugins/` 文件夹后重启即可。

## 插件详细说明

### SimpleVeinMiner 连锁挖矿
- 所有玩家默认开启。
- 26向 BFS 连通（面、棱、顶点接触都算）。
- 单次连锁上限 500 个方块（安全限制）。
- 掉落物直接进入背包，背包满则掉落在地。
- 支持方块：煤/铁/金/钻石/绿宝石/青金石/红石/铜矿石、下界石英/金矿石、远古残骸、全部深板岩矿石、粗铁/金/铜块、全部原木与树叶（含绯红/诡异菌柄）。

### FG_Economy 经济系统
- **出售手持物品**：`/sell [数量]`（1-64，超出手持数量则卖全部），`/sellall` 出售背包全部。
- **按ID批量出售**：`/mall <物品ID> [数量]`（如 `/mall minecraft:diamond 10`），`/mall yes` 出售背包中所有可出售物品。
- **快捷出售GUI**：打开经济菜单（指南针或 `/menu`），把物品拖入中间一行，点确认。
- **快捷上架GUI**：把物品拖入中间一行，点确认即可上架玩家市场。
  - 系统物品（矿物等）：价格 = 收购价 × 1.12。
  - 其他物品：默认 50 金币。
- **系统商店**：购买食物、工具、武器、护甲、材料。
- **玩家市场**：玩家间交易，`/market <价格>` 上架手持物品。
- **转账**：`/pay <玩家> <金额>`。
- **经济菜单**：`/menu` 获得指南针，右键打开GUI。
- **管理**：`/ecoadmin <set|add|remove> <玩家> <金额>`（需要OP）。
- 经济数据存储在 `plugins/FG_Economy/`（balances.yml、market.yml）。

### FG-AIHelper AI助手
- 默认开启，`/no` 按玩家切换开关。
- 监听聊天关键词并执行对应动作：出售、切换连锁挖掘、白天/夜晚、天气、游戏模式、清怪、药水效果、传送回出生点、余额查询。
- 3秒冷却防止刷屏。
- 需要OP的操作返回 `403` 提示。

### GameModeLock 游戏模式锁定
- 监听 `PlayerGameModeChangeEvent`，任何切换到非生存模式的操作都会被取消并提示。
- 配合 server.properties 中 `force-gamemode=true` 使用。

### FG-Domain 域名服务
- 需要在 `plugins/FG-Domain/config.yml` 中配置 Cloudflare API Token 和 Zone ID。
- `/https [子域名]` — 申请 2-10 位子域名（不填随机6位）。
- `/a <公网IP>` — 将公网IP绑定到你的子域名。
- 每个出口IP限一个子域名。
- 说明：Cloudflare API 调用为异步执行。

## 第三方插件（不包含在本仓库）

以下插件不属于本仓库，请从官方渠道自行下载：

| 插件 | 用途 | 下载地址 |
|------|------|----------|
| ViaVersion | Java版本兼容 | https://hangar.papermc.io/ViaVersion/ViaVersion |
| ViaBackwards | Java版本兼容 | https://hangar.papermc.io/ViaVersion/ViaBackwards |
| Geyser-Spigot | 基岩版互通 | https://geysermc.org/download |
| floodgate-spigot | 基岩版玩家UUID处理 | https://geysermc.org/download |
| WaterdogPE | 基岩版代理（离线模式） | https://github.com/WaterdogPE/WaterdogPE |

## 参考 server.properties

```properties
server-port=25565
online-mode=false
difficulty=peaceful
max-players=5
view-distance=2
allow-flight=true
enable-rcon=true
rcon.port=25575
rcon.password=change-me
force-gamemode=true
gamemode=survival
```

### 基岩版 ↔ Java版互通架构
```
基岩玩家 → 服务器IP:53 (WaterdogPE 离线) → 127.0.0.1:19133 (Geyser) → 127.0.0.1:25565 (Java)
```

## 开源协议

本仓库所有自研插件源码均采用 **GNU General Public License v3.0** 协议开源，详见 [LICENSE](LICENSE)。

```
Copyright (C) 2026 FG

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
```