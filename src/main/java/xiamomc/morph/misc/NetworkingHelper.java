package xiamomc.morph.misc;

import org.bukkit.Bukkit;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.misc.permissions.CommonPermissions;
import xiamomc.morph.network.commands.S2C.AbstractS2CCommand;
import xiamomc.morph.network.commands.S2C.clientrender.S2CRenderMapAddCommand;
import xiamomc.morph.network.commands.S2C.map.S2CPartialMapCommand;
import xiamomc.morph.network.server.MorphClientHandler;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.HashMap;

public class NetworkingHelper extends MorphPluginObject
{
    @Resolved
    private MorphClientHandler clientHandler;

    /**
     * 生成用于橙字显示的部分map(mapp)指令
     * @param diff 用于生成的伪装状态
     */
    public S2CPartialMapCommand genPartialMapCommand(DisguiseState... diff)
    {
        var map = new HashMap<Integer, String>();
        for (DisguiseState disguiseState : diff)
        {
            var player = disguiseState.getPlayer();
            map.put(player.getEntityId(), player.getName());
        }

        return new S2CPartialMapCommand(map);
    }

    public S2CRenderMapAddCommand genClientRenderAddCommand(DisguiseState diff)
    {
        var player = diff.getPlayer();
        return new S2CRenderMapAddCommand(player.getEntityId(), diff.getDisguiseIdentifier());
    }

    /**
     * 将某一客户端指令发送给所有拥有橙字显示权限的玩家
     * @param cmd 目标指令
     */
    public void sendCommandToRevealablePlayers(AbstractS2CCommand<?> cmd)
    {
        var target = Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission(CommonPermissions.DISGUISE_REVEALING))
                .toList();

        target.forEach(p -> clientHandler.sendCommand(p, cmd));
    }

    public void sendCommandToAllPlayers(AbstractS2CCommand<?> cmd)
    {
        Bukkit.getOnlinePlayers().forEach(p -> clientHandler.sendCommand(p, cmd));
    }
}
