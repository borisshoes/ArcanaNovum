<div align="center">

![Arcana Novum](https://raw.githubusercontent.com/borisshoes/ArcanaNovum/main/non-mod-resources/ArcanaNovum_Banner.png)

# Minecraft's Biggest Server-Side Magic Mod

**Powerful magic items, multiblock structures, custom bosses, and deep lore.<br/>All Server-Sided; Players don't need any mods.**

[![Minecraft Version](https://img.shields.io/modrinth/game-versions/9J7sCd3t?style=for-the-badge&label=Minecraft&color=8A2BE2)](https://modrinth.com/mod/arcana-novum/versions)
[![Server-side](https://img.shields.io/badge/Environment-Server--side-C65135?style=for-the-badge)](https://modrinth.com/mod/arcana-novum/versions)
[![Requires Fabric API](https://img.shields.io/badge/Requires-Fabric_API-DBD0B4?style=for-the-badge)](https://modrinth.com/mod/fabric-api)

[![Wiki](https://img.shields.io/badge/Wiki-Read_the_docs-8A2BE2?style=for-the-badge&logo=bookstack&logoColor=white)](https://arcanawiki.borisshoes.net)
[![Discord](https://img.shields.io/discord/1403978074550439966?style=for-the-badge&logo=discord&logoColor=white&label=Discord&color=5865F2)](https://discord.gg/dTKPeNEg6r)
[![Source](https://img.shields.io/badge/Source-GitHub-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/borisshoes/ArcanaNovum)

</div>

## Features
- **80+ Arcana Items** across five rarities, from Mundane to Divine: charms, runic arrows, weapons, armor, tools and utility blocks.
- **The Starlight Forge**, a multiblock crafting station that grows with you through five Forge Additions.
- **Altars and multiblocks**, such as the Starpath Altar that teleports you across the world, and the Transmutation Altar with over 100 transmutations.
- **Research and progression.** Complete research tasks to unlock recipes, earn Arcana XP by using your items, and level up to wield more of them at once.
- **190+ Augmentations.** Spend skill points to unlock augments that strengthen an item or change how it works.
- **160+ Achievements** that reward XP and skill points.
- **Boss fights and Divine items.** Custom boss encounters and rare events are the only way to obtain Divine items.
- **Built for servers.** No client mods, in-game GUIs for everything, a resource pack served by the server, 400+ config options and a permission node for every command.

| Forging at the Starlight Forge | The Starpath Altar | Late-game gear |
|:---:|:---:|:---:|
| <img src="https://cdn.modrinth.com/data/9J7sCd3t/images/06303032d581ee3983e1d116136640a56e86d5f0_350.webp" alt="A Starlight Forge making a set of Runic Blink Arrows" width="230"> | <img src="https://cdn.modrinth.com/data/9J7sCd3t/images/ca16fd2499c7ecbb0501fa96c1eb699cf42189ca_350.webp" alt="The Starpath Altar about to teleport a player" width="230"> | <img src="https://cdn.modrinth.com/data/9J7sCd3t/images/71752bcef59af242f0c009316f1fe70f87dfe67f_350.webp" alt="A player geared with late-game Arcana items" width="230"> |

More screenshots are in the [gallery](https://modrinth.com/mod/arcana-novum/gallery).

## Getting Started
Everything in the mod is explained by the **Tome of Arcana Novum**, your in-game guide book. To make one:

1. **Craft Mundane Arcane Paper.** Put any Enchanted Book in the middle of a crafting table with 4 Paper around it, one on each side. This gives you 4 Mundane Arcane Paper.
2. **Bind the Tome.** Throw all 4 Mundane Arcane Paper and an Eye of Ender onto an Enchanting Table. After a moment they combine into the Tome of Arcana Novum.
3. **Open the Tome** by right clicking with it. It shows your profile and the Compendium, which lists every Arcana Item with its recipe and research tasks.
4. **Research and build the Starlight Forge.** This is your first big goal, since it is where Arcana Items are crafted.

`/arcana guide` opens a short introduction book at any time, even without a Tome.

## Wiki
The **[Arcana Novum Wiki](https://arcanawiki.borisshoes.net)** documents every [item](https://arcanawiki.borisshoes.net/wiki/Items), [multiblock](https://arcanawiki.borisshoes.net/wiki/Multiblocks) and [mechanic](https://arcanawiki.borisshoes.net/wiki/Mechanics) in the mod, along with its recipes, transmutations, config options and lore.

## Videos
| 3.0 Spotlight / Tutorial | Arcana 3.1 Spotlight |
|:---:|:---:|
| [![Mod Tutorial](https://img.youtube.com/vi/JU-iLKURhzw/mqdefault.jpg)](https://www.youtube.com/watch?v=JU-iLKURhzw) | [![3.1 Changes Showcase](https://img.youtube.com/vi/z-8MGpFFSkQ/mqdefault.jpg)](https://www.youtube.com/watch?v=z-8MGpFFSkQ) |

## Server Setup
1. Install Fabric Loader and [Fabric API](https://modrinth.com/mod/fabric-api) on your server, then put the Arcana Novum jar in the `mods` folder. Every other library is bundled, and players do not need to install anything.
2. Launch the server once to generate the configs.
3. Open `config/polymer/auto-host.json` and change `"enabled": false` to `true`. The server now generates the mod's resource pack and sends it to players when they connect.
4. (Optional) In the same file you can make the resource pack required, or set the `"message"` that players see when they are asked to download it.

## Commands
* `/arcana guide` Opens the Arcana Guide Book (`/arcana help` does the same).
* `/arcana items` Shows your Arcane Concentration usage and the items taking concentration.
* `/arcana show` Shows off the Arcana Item in your main hand to all players on the server.
* `/arcana blocks` Lists all of your placed Arcana Blocks, including their location and dimension.
* `/arcana version` Shows the current Arcana Novum mod version.

Admins have commands for creating items, managing XP, skill points, research, achievements and augments, and running boss fights. Every config value can be viewed and changed in game with `/arcana config <setting> [value]`, and every command has a [Fabric Permissions API](https://github.com/lucko/fabric-permissions-api) node with a vanilla permission level fallback.

The full reference is on GitHub: [commands](https://github.com/borisshoes/ArcanaNovum#commands), [configuration](https://github.com/borisshoes/ArcanaNovum#configuration) and [permission nodes](https://github.com/borisshoes/ArcanaNovum#permission-nodes).

## Try My Other Mods!
All server-side Fabric mods — no client installation required.

| | Mod | Description | Links |
|:---:|---|---|---|
| <img src="https://cdn.modrinth.com/data/xHHbHfVj/c6c224a3d8068cfb9b054e2a03eb9704906dd8cb_96.webp" alt="Ancestral Archetypes" width="64"> | **Ancestral Archetypes** | A highly configurable, Origins-style mod that lets players pick a mob to gain unique abilities! | [![GitHub](https://img.shields.io/badge/GitHub-181717?logo=github&logoColor=white)](https://github.com/borisshoes/AncestralArchetypes) [![Modrinth](https://img.shields.io/badge/Modrinth-00AF5C?logo=modrinth&logoColor=white)](https://modrinth.com/mod/ancestral-archetypes) [![CurseForge](https://img.shields.io/badge/CurseForge-F16436?logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/ancestral-archetypes) |
| <img src="https://cdn.modrinth.com/data/QfXOzeIK/b35cbf33da842f170d0aa562033aaddc2a9ab653_96.webp" alt="Ender Nexus" width="64"> | **Ender Nexus** | Highly configurable /home, /spawn, /warp, /tpa and /rtp commands all in one, and individually disablable. | [![GitHub](https://img.shields.io/badge/GitHub-181717?logo=github&logoColor=white)](https://github.com/borisshoes/EnderNexus/) [![Modrinth](https://img.shields.io/badge/Modrinth-00AF5C?logo=modrinth&logoColor=white)](https://modrinth.com/mod/ender-nexus) [![CurseForge](https://img.shields.io/badge/CurseForge-F16436?logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/ender-nexus-fabric-teleports) |
| <img src="https://cdn.modrinth.com/data/Z63eULDV/dae01789d609498b8f1637ab31d8fe20b6108020_96.webp" alt="Fabric Mail" width="64"> | **Fabric Mail** | An in-game virtual mailbox system for sending packages and messages between online and offline players. | [![GitHub](https://img.shields.io/badge/GitHub-181717?logo=github&logoColor=white)](https://github.com/borisshoes/fabric-mail/) [![Modrinth](https://img.shields.io/badge/Modrinth-00AF5C?logo=modrinth&logoColor=white)](https://modrinth.com/mod/fabric-mail) [![CurseForge](https://img.shields.io/badge/CurseForge-F16436?logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/fabric-mail) |
| <img src="https://cdn.modrinth.com/data/u40ARaBc/028062616fc2fb729afdbdc697d60f93ff61a918_96.webp" alt="Fabric Trade" width="64"> | **Fabric Trade** | Adds /trade, a secure player-to-player trading interface. | [![GitHub](https://img.shields.io/badge/GitHub-181717?logo=github&logoColor=white)](https://github.com/borisshoes/fabric-trade/) [![Modrinth](https://img.shields.io/badge/Modrinth-00AF5C?logo=modrinth&logoColor=white)](https://modrinth.com/mod/fabric-trade) [![CurseForge](https://img.shields.io/badge/CurseForge-F16436?logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/fabric-trade) |
| <img src="https://cdn.modrinth.com/data/WdlqG9Gd/a401b9bf08c33d85c907025d6689c657b5168508_96.webp" alt="Limited AFK" width="64"> | **Limited AFK** | AFK detection and management with configurable kick thresholds for servers. | [![GitHub](https://img.shields.io/badge/GitHub-181717?logo=github&logoColor=white)](https://github.com/borisshoes/LimitedAFK/) [![Modrinth](https://img.shields.io/badge/Modrinth-00AF5C?logo=modrinth&logoColor=white)](https://modrinth.com/mod/limited-afk) [![CurseForge](https://img.shields.io/badge/CurseForge-F16436?logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/limited-afk) |
| <img src="https://cdn.modrinth.com/data/klpvLefw/97afbda2e56c3f14e04d0f9e0e1fe99db6bd2f27_96.webp" alt="Links in Chat" width="64"> | **Links in Chat** | Makes URLs posted in chat clickable. | [![GitHub](https://img.shields.io/badge/GitHub-181717?logo=github&logoColor=white)](https://github.com/borisshoes/fabric-linksinchat/) [![Modrinth](https://img.shields.io/badge/Modrinth-00AF5C?logo=modrinth&logoColor=white)](https://modrinth.com/mod/links-in-chat) [![CurseForge](https://img.shields.io/badge/CurseForge-F16436?logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/links-in-chat) |

## Support
Questions go to the [Discord](https://discord.gg/dTKPeNEg6r), and bug reports and suggestions go to [GitHub Issues](https://github.com/borisshoes/ArcanaNovum/issues).

### License Notice
By using this project in any form, you hereby give your "express assent" for the terms of the license of this project, and acknowledge that I, BorisShoes, have fulfilled my obligation under the license to "make a reasonable effort under the circumstances to obtain the express assent of recipients to the terms of this License."
