![cover](./assets/cover.png)

<div align="center">

![Pic](https://cdn.modrinth.com/data/ydNDeiDX/images/9e71cabf14eb3c0ccaef48d7b81410d79dc04261.png)

[客户端集成Mod](https://modrinth.com/mod/feathermorphclient) | [Documents (WIP)](./docs)

</div>

---
![CI Status](https://github.com/NiFeather/FeatherMorph/actions/workflows/build.yml/badge.svg)
![Release](https://img.shields.io/github/release/NiFeather/FeatherMorph.svg)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/NiFeather/FeatherMorph)

### 功能和特色

- 变形成玩家和游戏中的各种生物

- [**能增强使用体验的客户端集成，例如形态选择界面和技能快捷键**](https://modrinth.com/mod/feathermorphclient)

- 多语言支持！根据玩家客户端语言自动切换！

- **涵盖了绝大多数原版特性的技能和天赋实现**

- 聊天覆盖 —— 在聊天中改变自己的名称

### 依赖

此插件需要以下这些东西来正常运作：

- 一个Paper系（Paper或基于Paper）的服务端

为了在服务端向其他人显示变形形态，根据当前服务器安装的插件版本，我们需要以下这些插件：

#### 2.x
- [PacketEvents 2.8.0](https://modrinth.com/plugin/packetevents)

#### 1.x
- [ProtocolLib](https://ci.dmulloy2.net/job/ProtocolLib)

#### 0.x
- [ProtocolLib](https://ci.dmulloy2.net/job/ProtocolLib)
- [LibsDisguises](https://www.spigotmc.org/resources/libs-disguises-free.81/)

### 下载

[![Available on Modrinth](https://raw.githubusercontent.com/intergrav/devins-badges/v3/assets/cozy/available/modrinth_vector.svg)](https://modrinth.com/plugin/feathermorph/changelog) [![Available on GitHub](https://raw.githubusercontent.com/intergrav/devins-badges/v3/assets/cozy/available/github_vector.svg)](https://github.com/NiFeather/FeatherMorph/releases)

[Modrinth](https://modrinth.com/plugin/feathermorph/changelog) | [GitHub（中文更新日志可在这里查看）](https://github.com/NiFeather/FeatherMorph/releases)

---

### 快速上手

1. 你可以通过 `/morph` 指令来使用变形功能，用 `/unmorph` 来变回自己

2. 如果没有安装客户端集成，默认情况下使用技能的方法是***手持羽毛潜行+使用***

3. 默认情况下，客户端集成打开选择界面的按键是 `N`，使用技能是 `V`，你可以在键位设置中了解更多

4. 你可以用 `/request` 指令来管理交换请求，接受别人的交换请求后双方都可以变成对方的样子

5. 插件的剩余指令均在 `/fm`（或 `/feathermorph`）中，你可以通过 `help` 子指令来查看更多信息

6. **插件的大多数功能都可以通过指令配置，用法是 `/fm option <id> <值>`**

### 注意！
- 如果不安装ProtocolLib插件也能运行，但需要依赖客户端集成来向别人显示变形形态

- **若服务器安装了其他聊天插件，聊天覆盖功能可能会失效！**

---
### 构建
```bash
#!/usr/bin/env bash
git clone https://github.com/NiFeather/FeatherMorph
cd FeatherMorph

./gradlew build --no-daemon
```

生成的文件将位于`build/libs`中，`FeatherMorph-x.x.x-all.jar`就是构建出来的插件。

### 特别鸣谢 Credits
- [LibsDisguises](https://github.com/libraryaddict/LibsDisguises): For making this project possible, and for reference about how to make the server renderer
- [VeinMiner](https://github.com/2008Choco/VeinMiner): For the reference about how to implement *Client <-> Server* communication.
- [ProtocolLib](https://github.com/dmulloy2/ProtocolLib): For making server renderer possible in 1.x releases
- [Paper docs](https://docs.papermc.io/): For how to make paper plugins
- [PacketEvents](https://github.com/retrooper/packetevents): For making server renderer possible