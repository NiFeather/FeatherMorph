![cover](./assets/cover.png)

<div align="center">

![Pic](https://cdn.modrinth.com/data/ydNDeiDX/images/9e71cabf14eb3c0ccaef48d7b81410d79dc04261.png)

[客户端集成Mod](https://modrinth.com/mod/feathermorphclient) | [Wiki (施工中)](https://github.com/XiaMoZhiShi/FeatherMorph/wiki)

<h3> ~ 72变，但是MC ~ </h3>

<p align="center">
  <img src="https://github.com/XiaMoZhiShi/MorphPlugin/actions/workflows/build.yml/badge.svg">
  <img src="https://img.shields.io/github/release/XiaMoZhiShi/MorphPlugin.svg">
</p>

</div>

---

### 功能和特色

- 变形成游戏中的各种生物

- 支持伪装为玩家！

- [**能增强使用体验的客户端集成，例如伪装选择界面和技能快捷键**](https://modrinth.com/mod/feathermorphclient)

- 多语言支持！根据玩家客户端语言自动切换！

- **涵盖了绝大多数原版特性的技能和天赋实现**

- 聊天覆盖 —— 在聊天中伪装自己的名称

- 交互镜像 —— 将你的操作镜像到别人的身上 （~~好像这两个都不太属于一个变形插件该有的功能~~）

### 依赖

此插件需要以下这些东西来正常运作：

- 一个Paper系（Paper或基于Paper）的服务端

为了在服务端向其他人显示伪装，我们需要以下这些插件：

- [ProtocolLib](https://ci.dmulloy2.net/job/ProtocolLib)

若要运行之前的版本（非1.x和0.13.x），还需安装[LibsDisguises](https://www.spigotmc.org/resources/libs-disguises-free.81/)

### 下载

[![Available on Modrinth](https://raw.githubusercontent.com/intergrav/devins-badges/v3/assets/cozy/available/modrinth_vector.svg)](https://modrinth.com/plugin/feathermorph/changelog) [![Available on GitHub](https://raw.githubusercontent.com/intergrav/devins-badges/v3/assets/cozy/available/github_vector.svg)](https://github.com/XiaMoZhiShi/FeatherMorph/releases)

[Modrinth](https://modrinth.com/plugin/feathermorph/changelog) | [GitHub（中文更新日志可在这里查看）](https://github.com/XiaMoZhiShi/FeatherMorph/releases)

---

### 快速上手

1. 你可以通过 `/morph` 指令来使用变形功能，用 `/unmorph` 来变回自己

2. 如果没有安装客户端集成，默认情况下使用技能的方法是***手持羽毛潜行+使用***

3. 默认情况下，客户端集成打开选择界面的按键是 `N`，使用技能是 `V`，你可以在键位设置中了解更多

4. 你可以用 `/request` 指令来管理交换请求，接受别人的交换请求后双方都可以变成对方的样子

5. 插件的剩余指令均在 `/fm`（或 `/feathermorph`）中，你可以通过 `help` 子指令来查看更多信息

6. **插件的大多数功能都可以通过指令配置，用法是 `/fm option <id> <值>`**

### 注意！
- 如果不安装ProtocolLib插件也能运行，但需要依赖客户端集成来向别人显示伪装

- **若服务器安装了其他聊天插件，聊天覆盖功能可能会失效！**

---
### 构建
```bash
#!/usr/bin/env bash
git clone https://github.com/XiaMoZhiShi/FeatherMorph
cd FeatherMorph

./gradlew build --no-daemon
```

生成的文件将位于`build/libs`中，`FeatherMorph-x.x.x-all.jar`就是构建出来的插件。
