![cover](./assets/cover.png)

<p align="right">
  <img src="https://github.com/XiaMoZhiShi/MorphPlugin/actions/workflows/build.yml/badge.svg">
  <img src="https://img.shields.io/github/release/XiaMoZhiShi/MorphPlugin.svg">
</p>

<!-- [Wiki](https://github.com/XiaMoZhiShi/MorphPlugin/wiki) -->

# FeatherMorph

[[Modrinth](https://modrinth.com/plugin/feathermorph)]

一个适用于Paper的变形插件。

### 功能
- [x] 生物技能
- [x] 玩家伪装、操作镜像与聊天覆盖
- [x] **支持[客户端模组](https://github.com/XiaMoZhiShi/MorphPluginClient)**

### 依赖关系
FeatherMorph至少需要下面这些依赖才能运行：
- 一个Paper或基于Paper的服务器
- **[1.0.0+ / 0.13.10+]**: [ProtocolLib](https://ci.dmulloy2.net/job/ProtocolLib) >= 678
- **[0.x]** [LibsDisguises](https://www.spigotmc.org/resources/libs-disguises-free.81/) >= 10.0.32 <!--[^ld]-->

<!-- [^ld]: 我们建议使用Jenkins上版本至少为#1154的构建，Spigot页面上的最新版本并不支持1.19.3。-->

另外，我们还支持以下这些插件。这些插件不是必须的，但装上后能让我们提供一些额外功能：
- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) - 占位符可以在[Wiki](https://github.com/XiaMoZhiShi/MorphPlugin/wiki/PlaceholderAPI)中查询到

### 构建
```bash
#!/usr/bin/env bash
git clone https://github.com/XiaMoZhiShi/FeatherMorph
cd FeatherMorph

./gradlew build --no-daemon
```

生成的文件将位于`build/libs`中，`FeatherMorph-x.x.x.jar`就是构建出来的插件。
