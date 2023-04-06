![cover](./assets/cover.png)

<p align="right">
  <img src="https://github.com/XiaMoZhiShi/MorphPlugin/actions/workflows/build.yml/badge.svg">
  <img src="https://img.shields.io/github/release/XiaMoZhiShi/MorphPlugin.svg">
</p>

<!-- [Wiki](https://github.com/XiaMoZhiShi/MorphPlugin/wiki) -->

# FeatherMorph

[[中文](./README.md)]

A simple morph plugin for Paper servers.

### Features
- [x] Mob abilities and skills
- [x] Hides your name when chatting if disguised as another player
- [x] (Con)troll another player by disguising as them
- [x] **Supports [Client integration](https://github.com/XiaMoZhiShi/MorphPluginClient)**

### Dependencies
FeatherMorph requires these to work properly:
- A Paper or Purpur 1.19.4 server.
- [LibsDisguises](https://ci.md-5.net/job/LibsDisguises/) >= 1170 <!--[^ld]-->

<!-- [^ld]: 我们建议使用Jenkins上版本至少为#1154的构建，Spigot页面上的最新版本并不支持1.19.3。-->

We also provide extra functions if these are present:
- [GSit](https://www.spigotmc.org/resources/gsit-modern-sit-seat-and-chair-lay-and-crawl-plugin-1-13-x-1-19-x.62325/) - Automatic hides server-side SelfView when sitting.
- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) - You can find all available placeholders on our [Wiki](https://github.com/XiaMoZhiShi/MorphPlugin/wiki/PlaceholderAPI)

### Build
```bash
#!/usr/bin/env bash
git clone https://github.com/XiaMoZhiShi/MorphPlugin
cd MorphPlugin

./gradlew build --no-daemon
```

The artifacts should be located in `build/libs` after Gradle finished building.