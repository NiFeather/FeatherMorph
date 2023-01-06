package xiamomc.morph.network.commands.C2S;

import org.bukkit.entity.Player;
import xiamomc.morph.network.ConnectionState;
import xiamomc.morph.network.InitializeState;
import xiamomc.morph.network.MorphClientHandler;
import xiamomc.morph.network.commands.S2C.S2CSetToggleSelfCommand;

import java.util.Map;

public class C2SInitialCommand extends AbstractC2SCommand
{
    public C2SInitialCommand(Map<Player, ConnectionState> stateMap, Map<Player, InitializeState> connectionStates)
    {
        this.playerStateMap = stateMap;
        this.playerConnectionStates = connectionStates;
    }

    private final Map<Player, ConnectionState> playerStateMap;

    private final Map<Player, InitializeState> playerConnectionStates;

    @Override
    public String getBaseName()
    {
        return "initial";
    }

    @Override
    public void onCommand(Player player, String[] str)
    {
        var clientHandler = clientHandler();
        var manager = morphManager();

        //检查一遍玩家有没有初始化完成，如果有则忽略此指令
        if (clientHandler.clientInitialized(player))
            return;

        if (playerStateMap.getOrDefault(player, null) != ConnectionState.JOINED)
            playerStateMap.put(player, ConnectionState.CONNECTING);

        //等待玩家加入再发包
        clientHandler.waitUntilReady(player, () ->
        {
            //再检查一遍玩家有没有初始化完成
            if (clientHandler.clientInitialized(player))
                return;

            var config = manager.getPlayerConfiguration(player);
            var list = config.getUnlockedDisguiseIdentifiers();
            clientHandler.refreshPlayerClientMorphs(list, player);

            var state = manager.getDisguiseStateFor(player);

            if (state != null)
                manager.refreshClientState(state);

            clientHandler.sendClientCommand(player, new S2CSetToggleSelfCommand(config.showDisguiseToSelf));
            playerConnectionStates.put(player, InitializeState.DONE);
        });
    }
}
