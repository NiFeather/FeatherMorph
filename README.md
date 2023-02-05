![cover](./assets/cover.png)

<p align="right">
  <img src="https://github.com/XiaMoZhiShi/MorphPlugin/actions/workflows/build.yml/badge.svg">
  <img src="https://img.shields.io/github/release/XiaMoZhiShi/MorphPlugin.svg">
</p>

<!-- [Wiki](https://github.com/XiaMoZhiShi/MorphPlugin/wiki) -->

# FeatherMorph

一个旨在提供诸多开箱即用功能的变形插件。

### 功能
- [x] 最基础的伪装功能
- [x] 玩家伪装、操作镜像与聊天覆盖
- [x] 生物技能
- [x] **支持[客户端模组](https://github.com/XiaMoZhiShi/MorphPluginClient)**

### 依赖关系
FeatherMorph至少需要下面这些依赖才能运行：
- Paper或基于Paper的1.19.3服务器
- [LibsDisguises](https://www.spigotmc.org/resources/libs-disguises-free.81/) >= 10.0.32 <!--[^ld]-->

<!-- [^ld]: 我们建议使用Jenkins上版本至少为#1154的构建，Spigot页面上的最新版本并不支持1.19.3。-->

另外，我们还支持以下这些插件。这些插件不是必须的，但装上后能让我们提供一些额外功能：
- [GSit](https://www.spigotmc.org/resources/gsit-modern-sit-seat-and-chair-lay-and-crawl-plugin-1-13-x-1-19-x.62325/) - 提供与LibsDisguises之间的一些冲突修复
- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) - 占位符可以在[Wiki](https://github.com/XiaMoZhiShi/MorphPlugin/wiki/PlaceholderAPI)中查询到

### 构建
```bash
#!/usr/bin/env bash
git clone https://github.com/XiaMoZhiShi/MorphPlugin
cd MorphPlugin

./gradlew build --no-daemon
```

生成的文件将位于`build/libs`中，`FeatherMorph-x.x.x.jar`就是构建出来的插件。