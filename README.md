# Morph
基于LibsDisguises的伪装插件

[Wiki](https://github.com/XiaMoZhiShi/MorphPlugin/wiki)

## 使用
1. 首先你需要用[最新版本的Paper](https://papermc.io/downloads)作为你的服务端
2. 访问[Releases](https://github.com/XiaMoZhiShi/MorphPlugin/releases/latest)下载最新版本或前往[Actions](https://github.com/XiaMoZhiShi/MorphPlugin/actions/workflows/build.yml?query=branch%3Amaster+is%3Acompleted)下载最新构建
3. 下载最新版本的[PluginBase](https://github.com/XiaMoZhiShi/PluginBase/releases/latest)和[LibsDisguises](https://www.spigotmc.org/resources/libs-disguises-free.81/)
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
        - 相关设置可以在[配置](./src/main/resources/config.yml#L24)中看到
- [x] 几乎每一条消息都可自定义
    - 支持[MiniMessage消息格式！](https://docs.adventure.kyori.net/minimessage/index.html)
        - *但最好不要用`gradient`和`rainbow`这两个Tag，目前会有兼容性问题*
    - *剩下的那些不支持的都是控制台报错*
- [x] 方便地管理每个人的伪装
    - 给与/移除某个玩家的伪装
    - 查询玩家伪装状态
    - 运行`/mmorph help mmorph`即可查看！

## TODO List（从上往下按优先级排列）
- [ ] 多语言支持？
- [x] 移动JSON配置到插件的配置目录下
    * ~~是的这个插件到现在还在用`/dev/shm/test`当JSON配置存储（~~
      * 现在是插件目录下面的`data.json`了（
- [x] 跟踪由此插件生成的伪装
- [x] 调整底层以更好地适应最新变动
- [x] 优化帮助信息
    * 根据需求显示而非全部输出到屏幕
- [x] 实现伪装的主动技能
    * 像末影人可以传送，烈焰人投射火球等
    * [x] 修复末影人伪装可以传送到方块里面的问题
- [x] 实现伪装的被动技能
    * 鱼类在水下呼吸，蝙蝠夜视等
- [x] 使聊天覆盖可配置/和其他聊天插件兼容
- [x] 使插件消息可配置
    * ~~可能需要更方便的实现，现在直接调用MiniMessage和MorphConfigManager实现的非常麻烦~~
- [ ] 实现离线玩家存储
    * [x] 实现离线玩家伪装存储
    * [ ] Make it more generic
- [x] 合并`/morph`和`/morphplayer`?
- [x] 允许通过指令为玩家赋予或移除某一伪装?
- [ ] ~~交换请求过期时通知双方？~~
    - 目前没实现是因为还没做离线玩家存储
