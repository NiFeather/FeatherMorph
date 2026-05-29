Archived, feel free to fork, although I would suggest making your own, as there aren't many documents about this project :D

![cover](./assets/cover.png)

<div align="center">

![Pic](https://cdn.modrinth.com/data/ydNDeiDX/images/9e71cabf14eb3c0ccaef48d7b81410d79dc04261.png)

[Client integration mod](https://modrinth.com/mod/feathermorphclient) | [Documents (WIP)](./docs)

</div>

---
![CI Status](https://github.com/NiFeather/FeatherMorph/actions/workflows/build.yml/badge.svg)
![Release](https://img.shields.io/github/release/NiFeather/FeatherMorph.svg)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/NiFeather/FeatherMorph)

### Features

This plugin allows you and your friends to disguise as various mobs and players in-game. Several disguise forms also have their corresponding skills and abilities in addition.

If also installed the Client integration mod, you can also have a GUI disguise selection screen and skill/actions hotkey.

We also support sending messages to players depending on their client language selection.

### Support
You can seek support on our issues page by opening a new bug report or feature request.

We may use NMS and new APIs introduced in various versions while developing FeatherMorph, making it hard to support all Minecraft versions at once.

Therefore, we can only support the latest one or two Minecraft releases, sorry!

### Dependencies

FeatherMorph requires these things to work:

- A Paper or Paper-based server.

To display disguise server-side, we also need these plugins, depending on which plugin version you're on.

#### 2.x
- [PacketEvents 2.8.0](https://modrinth.com/plugin/packetevents)

#### 1.x
- [ProtocolLib](https://ci.dmulloy2.net/job/ProtocolLib)

#### 0.x
- [ProtocolLib](https://ci.dmulloy2.net/job/ProtocolLib)
- [LibsDisguises](https://www.spigotmc.org/resources/libs-disguises-free.81/)

### Download

[![Available on Modrinth](https://raw.githubusercontent.com/intergrav/devins-badges/v3/assets/cozy/available/modrinth_vector.svg)](https://modrinth.com/plugin/feathermorph/changelog) [![Available on GitHub](https://raw.githubusercontent.com/intergrav/devins-badges/v3/assets/cozy/available/github_vector.svg)](https://github.com/NiFeather/FeatherMorph/releases)

[Modrinth](https://modrinth.com/plugin/feathermorph/changelog) | [GitHub](https://github.com/NiFeather/FeatherMorph/releases)

---

### Getting started

See [Gameplay](./docs/Gameplay.md).

---
### Building
```bash
#!/usr/bin/env bash
git clone https://github.com/NiFeather/FeatherMorph
cd FeatherMorph

./gradlew build --no-daemon
```

The file located at `build/libs` that ends with `-final` is the file that you should use.

### Credits
- [LibsDisguises](https://github.com/libraryaddict/LibsDisguises): For making this project possible, and for reference about how to make the server renderer
- [VeinMiner](https://github.com/2008Choco/VeinMiner): For the reference about how to implement *Client <-> Server* communication.
- [ProtocolLib](https://github.com/dmulloy2/ProtocolLib): For making server renderer possible in 1.x releases
- [Paper docs](https://docs.papermc.io/): For how to make paper plugins
- [PacketEvents](https://github.com/retrooper/packetevents): For making server renderer possible
