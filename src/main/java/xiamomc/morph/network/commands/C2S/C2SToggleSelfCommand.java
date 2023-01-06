package xiamomc.morph.network.commands.C2S;

import org.bukkit.entity.Player;

public class C2SToggleSelfCommand extends AbstractC2SCommand
{
    @Override
    public String getBaseName()
    {
        return "toggleself";
    }

    @Override
    public void onCommand(Player player, String[] str)
    {
        if (str.length != 2) return;

        var subData = str[1].split(" ");

        if (subData.length < 1) return;

        var manager = morphManager();
        var clientHandler = clientHandler();

        //获取客户端选项
        var playerOption = clientHandler.getPlayerOption(player);
        var playerConfig = manager.getPlayerConfiguration(player);

        if (subData[0].equals("client"))
        {
            if (subData.length < 2) return;

            var isClient = Boolean.parseBoolean(subData[1]);

            playerOption.setClientSideSelfView(isClient);

            //如果客户端打开了本地预览，则隐藏伪装，否则显示伪装
            var state = manager.getDisguiseStateFor(player);
            if (state != null) state.setServerSideSelfVisible(!isClient && playerConfig.showDisguiseToSelf);
        }
        else
        {
            var val = Boolean.parseBoolean(subData[0]);

            if (val == playerConfig.showDisguiseToSelf) return;
            manager.setSelfDisguiseVisible(player, val, true, playerOption.isClientSideSelfView(), false);
        }
    }
}
