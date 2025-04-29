package xyz.nifeather.netherite.network.commands.S2C.clientrender;

import xyz.nifeather.netherite.network.BasicServerHandler;
import xyz.nifeather.netherite.network.commands.S2C.NetheriteS2CCommand;
import xyz.nifeather.netherite.network.commands.S2C.MapCommandHelper;
import xyz.nifeather.netherite.network.commands.S2C.NetheriteS2CCommandNames;

import java.util.Map;

public class NetheriteS2CRenderMapSyncCommand extends NetheriteS2CCommand<String>
{
    public NetheriteS2CRenderMapSyncCommand(Map<Integer, String> uuidToPlayerMap)
    {
        super(gson().toJson(uuidToPlayerMap));
    }

    @Override
    public String getBaseName()
    {
        return NetheriteS2CCommandNames.CRMap;
    }

    @Override
    public void onCommand(BasicServerHandler<?> handler)
    {
        handler.onClientMapSyncCommand(this);
    }

    public Map<Integer, String> getMap()
    {
        return MapCommandHelper.parseMapIntegerString(this);
    }

    public static NetheriteS2CRenderMapSyncCommand of(Map<Integer, String> idToPlayerMap)
    {
        return new NetheriteS2CRenderMapSyncCommand(idToPlayerMap);
    }

    public static NetheriteS2CRenderMapSyncCommand ofStr(String arg)
    {
        return new NetheriteS2CRenderMapSyncCommand(MapCommandHelper.parseMapIntegerString(arg));
    }
}
