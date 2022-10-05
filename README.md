# Morph
基于LibsDisguises的伪装插件

## 使用
1. 访问[Actions](https://github.com/XiaMoZhiShi/MorphPlugin/actions/workflows/build.yml?query=branch%3Amaster+is%3Acompleted)下载最新构建
2. 下载最新版本的[PluginBase](https://github.com/XiaMoZhiShi/PluginBase/releases/latest)
3. 丢进服务器插件目录中
4. 重启服务器
    * 或者先加载PluginBase再加载此插件（
5. Go!

## TODO List
- [x] 移动JSON配置到插件的配置目录下
    * ~~是的这个插件到现在还在用`/dev/shm/test`当JSON配置存储（~~
      * 现在是插件目录下面的`data.json`了（
- [x] 跟踪由此插件生成的伪装
- [x] 优化帮助信息
    * 根据需求显示而非全部输出到屏幕
- [x] 合并`/morph`和`/morphplayer`?
- [x] 允许通过指令为玩家赋予或移除某一伪装?
- [ ] 交换请求过期时通知双方？
    - 目前没实现是因为还没做离线玩家存储
        - [ ] 实现离线玩家存储?