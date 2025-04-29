package xyz.nifeather.netherite.network.commands.S2C.map;

import xyz.nifeather.netherite.network.BasicServerHandler;
import xyz.nifeather.netherite.network.commands.S2C.NetheriteS2CCommand;
import xyz.nifeather.netherite.network.commands.S2C.MapCommandHelper;
import xyz.nifeather.netherite.network.commands.S2C.NetheriteS2CCommandNames;

import java.util.Map;

/**
 * Disguise UUID <-> Player Map Command
 */
public class NetheriteS2CMapCommand extends NetheriteS2CCommand<String>
{
    public NetheriteS2CMapCommand(Map<Integer, String> uuidToPlayerMap)
    {
        super(gson().toJson(uuidToPlayerMap));
    }

    @Override
    public String getBaseName()
    {
        return NetheriteS2CCommandNames.Map;
    }

    @Override
    public void onCommand(BasicServerHandler<?> handler)
    {
        handler.onMapCommand(this);
    }

    public Map<Integer, String> getMap()
    {
        return MapCommandHelper.parseMapIntegerString(this);
    }

    public static NetheriteS2CMapCommand of(Map<Integer, String> idToPlayerMap)
    {
        return new NetheriteS2CMapCommand(idToPlayerMap);
    }

    public static NetheriteS2CMapCommand ofStr(String arg)
    {
        return new NetheriteS2CMapCommand(MapCommandHelper.parseMapIntegerString(arg));
    }
}
