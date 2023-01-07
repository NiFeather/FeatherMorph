# Morph
基于LibsDisguises的伪装插件

[Wiki](https://github.com/XiaMoZhiShi/MorphPlugin/wiki)

## 使用
1. 首先你需要用[最新版本的Paper](https://papermc.io/downloads)作为你的服务端
2. 访问[Releases](https://github.com/XiaMoZhiShi/MorphPlugin/releases/latest)下载最新版本或前往[Actions](https://github.com/XiaMoZhiShi/MorphPlugin/actions/workflows/build.yml?query=branch%3Amaster+is%3Acompleted)下载最新构建
3. 下载最新版本的[PluginBase](https://github.com/XiaMoZhiShi/PluginBase/releases/latest)和[LibsDisguises](https://ci.md-5.net/job/LibsDisguises/)
4. 丢进服务器插件目录中
5. 重启服务器
    * 或者先加载PluginBase再加载此插件（
6. Go!

## 实现的功能
- [x] 下蹲时左、右键特定物品可以快速伪装、取消伪装或者使用技能
    - 手持玩家头颅时下蹲+右键支持设置伪装为特定皮肤
- [x] 主动、被动技能
- [x] 玩家伪装以及聊天覆盖 ！
    - 启用聊天覆盖后伪装成任意玩家，你在公屏发出的消息将不会显示你为所有者（不会显示是你发的）
        - 但控制台和拥有相关权限的人仍然可以看到是谁真正发的消息
    - 伪装为玩家后还可以在一定范围内控制被伪装的玩家的一些动作
        - 相关设置可以在[配置](./src/main/resources/config.yml#L58)中看到
- [x] 几乎每一条消息都可自定义
    - 支持[MiniMessage消息格式！](https://docs.adventure.kyori.net/minimessage/index.html)
    - *剩下的那些不支持的都是控制台报错*
- [x] 方便地管理每个人的伪装
    - 给与/移除某个玩家的伪装
    - 查询玩家伪装状态
    - 运行`/feathermorph help feathermorph`即可查看！

## 构建
```bash
#!/usr/bin/env bash
git clone https://github.com/XiaMoZhiShi/MorphPlugin
cd MorphPlugin

./gradlew build
```

生成的文件将位于`build/libs`中，`FeatherMorph-x.x.x.jar`就是构建出来的插件。
