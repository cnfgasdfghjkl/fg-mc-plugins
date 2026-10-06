# FG Minecraft Plugins

A collection of custom Minecraft plugins for **Spigot/Paper 1.20.1** servers, developed in Java 17 and open-sourced under the **GNU GPL v3** license.

## Plugins Overview

| Plugin | Description | Commands |
|--------|-------------|----------|
| **SimpleVeinMiner** | Vein mining with 26-direction connectivity (vertex/edge/face touching). Drops go directly into the player's inventory. Supports ores, deepslate ores, raw metal blocks, all wood types and leaves. | `/veinminer` |
| **FG_Economy** | Full economy system: balance, selling, system shop, player market, quick-sell / quick-list GUIs, `/mall` bulk selling, transfer, admin management. | `/money` `/sell` `/sellall` `/mall` `/shop` `/market` `/pay` `/menu` `/ecoadmin` |
| **FG-AIHelper** | Chat keyword assistant. Automatically detects player intent from chat messages and executes the matching command. Enabled by default, 3-second cooldown. | `/no` |
| **GameModeLock** | Forces every player (including OPs) to stay in survival mode. Any attempt to switch to creative/adventure/spectator is cancelled. | — |
| **FG-Domain** | In-game DDNS service backed by the Cloudflare API. Players apply for a subdomain with `/https` and bind their public IP with `/a`. | `/https` `/a` |

## Requirements

- **Server**: Spigot / Paper **1.20.1**
- **Java**: JDK **17+** (to compile), Java **21** (recommended to run Paper 1.20.1)
- **API**: `spigot-api-1.20.1-R0.1-SNAPSHOT.jar`

## Build

### Linux / macOS
```bash
chmod +x build.sh
./build.sh
```

### Windows
Double-click `build.bat`, or run it from a terminal.

The build script downloads `spigot-api-1.20.1-R0.1-SNAPSHOT.jar` automatically (fallback: put it manually in `libs/`). Compiled jars are output to `build/`. Copy them into your server's `plugins/` folder and restart.

## Plugin Details

### SimpleVeinMiner
- Enabled for all players by default.
- 26-direction BFS connectivity (face, edge and corner touching).
- Max 500 blocks per vein (safety limit).
- Drops are inserted directly into the inventory; overflow drops on the ground.
- Supported blocks: coal/iron/gold/diamond/emerald/lapis/redstone/copper ore, nether quartz/gold ore, ancient debris, all deepslate ores, raw iron/gold/copper blocks, all logs and leaves (including nether stems).

### FG_Economy
- **Sell hand item**: `/sell [amount]` (1-64; more than held sells all) or `/sellall` for the whole inventory.
- **Bulk sell by ID**: `/mall <item-id> [amount]` (e.g. `/mall minecraft:diamond 10`), `/mall yes` sells every sellable item in the inventory.
- **Quick sell GUI**: open the economy menu (compass or `/menu`), drag items into the middle row, click confirm.
- **Quick list GUI**: drag items into the middle row to list them on the player market.
  - System items (minerals etc.): price = buyback price × 1.12.
  - Other items: default 50 coins.
- **System shop**: buy food, tools, weapons, armor and materials.
- **Player market**: player-to-player trading, `/market <price>` lists your hand item.
- **Transfer**: `/pay <player> <amount>`.
- **Economy menu**: `/menu` gives you a compass; right-click it to open the GUI.
- **Admin**: `/ecoadmin <set|add|remove> <player> <amount>` (requires OP).
- Economy data is stored in `plugins/FG_Economy/` (balances.yml, market.yml).

### FG-AIHelper
- Enabled by default; `/no` toggles it per player.
- Watches chat for keywords and executes the matching action: selling, vein mining toggle, day/night, weather, game mode, mob clearing, potion effects, teleport to spawn, balance query.
- 3-second cooldown to prevent spam.
- Actions that need OP return a `403` message.

### GameModeLock
- Listens to `PlayerGameModeChangeEvent`; any switch to a non-survival mode is cancelled with a message.
- Pairs with `force-gamemode=true` in server.properties.

### FG-Domain
- Requires Cloudflare API Token and Zone ID in `plugins/FG-Domain/config.yml`.
- `/https [subdomain]` — apply for a 2-10 character subdomain (random 6 chars if omitted).
- `/a <public-ip>` — bind your public IP to your subdomain.
- One subdomain per egress IP.
- Note: calls the Cloudflare API asynchronously.

## Third-party plugins (not included)

These are not part of this repository — download them from their official sources:

| Plugin | Purpose | Source |
|--------|---------|--------|
| ViaVersion | Java version compatibility | https://hangar.papermc.io/ViaVersion/ViaVersion |
| ViaBackwards | Java version compatibility | https://hangar.papermc.io/ViaVersion/ViaBackwards |
| Geyser-Spigot | Bedrock edition bridge | https://geysermc.org/download |
| floodgate-spigot | Bedrock player UUID handling | https://geysermc.org/download |
| WaterdogPE | Bedrock proxy (offline mode) | https://github.com/WaterdogPE/WaterdogPE |

## Reference server.properties

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

### Bedrock ↔ Java bridging architecture
```
Bedrock player → server-ip:53 (WaterdogPE, offline) → 127.0.0.1:19133 (Geyser) → 127.0.0.1:25565 (Java)
```

## License

All self-written plugin source code in this repository is licensed under the **GNU General Public License v3.0**. See [LICENSE](LICENSE).

```
Copyright (C) 2026 FG

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
```