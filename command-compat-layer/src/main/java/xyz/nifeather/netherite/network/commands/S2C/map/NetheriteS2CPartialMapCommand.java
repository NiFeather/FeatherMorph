package xyz.nifeather.netherite.network.commands.S2C.map;

import xyz.nifeather.netherite.network.BasicServerHandler;
import xyz.nifeather.netherite.network.commands.S2C.NetheriteS2CCommand;
import xyz.nifeather.netherite.network.commands.S2C.NetheriteS2CCommandNames;

import java.util.HashMap;
import java.util.Map;

/**
 * Disguise UUID <-> Player Map Command
 */
public class NetheriteS2CPartialMapCommand extends NetheriteS2CCommand<String>
{
    public NetheriteS2CPartialMapCommand(Map<Integer, String> uuidToPlayerMap)
    {
        super(gson().toJson(uuidToPlayerMap));
    }

    @Override
    public String getBaseName()
    {
        return NetheriteS2CCommandNames.MapPartial;
    }

    @Override
    public void onCommand(BasicServerHandler<?> handler)
    {
        handler.onMapPartialCommand(this);
    }

    private static final String defaultMapStr = "{}";

    public Map<Integer, String> getMap()
    {
        var arg = this.getArgumentAt(0, defaultMapStr);

        if (arg.equals(defaultMapStr)) return new HashMap<>();
        var mapConv = gson().fromJson(arg, HashMap.class);

        var map = new HashMap<Integer, String>();
        mapConv.forEach((k, v) ->
        {
            try
            {
                var uuid = Integer.parseInt(k.toString());

                map.put(uuid, v.toString());
            }
            catch (Throwable t)
            {
                System.out.println("Unable to convert %s to Integer ID: %s".formatted(k, t.getMessage()));
            }
        });

        return map;
    }

    public static NetheriteS2CPartialMapCommand of(Map<Integer, String> uuidToPlayerMap)
    {
        return new NetheriteS2CPartialMapCommand(uuidToPlayerMap);
    }

    public static NetheriteS2CPartialMapCommand ofStr(String arg)
    {
        if (arg.equals(defaultMapStr)) return new NetheriteS2CPartialMapCommand(new HashMap<>());
        var mapConv = gson().fromJson(arg, HashMap.class);

        var map = new HashMap<Integer, String>();
        mapConv.forEach((k, v) ->
        {
            try
            {
                var uuid = Integer.parseInt(k.toString());

                map.put(uuid, v.toString());
            }
            catch (Throwable t)
            {
                System.out.println("Unable to convert %s to UUID: %s".formatted(k, t.getMessage()));
            }
        });

        return new NetheriteS2CPartialMapCommand(map);
    }
}
