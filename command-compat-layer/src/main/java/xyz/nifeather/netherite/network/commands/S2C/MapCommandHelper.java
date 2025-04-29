package xyz.nifeather.netherite.network.commands.S2C;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

public class MapCommandHelper
{
    private static final String defaultMapStr = "{}";

    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    public static Map<Integer, String> parseMapIntegerString(NetheriteS2CCommand<String> command)
    {
        return parseMapIntegerString(command.getArgumentAt(0, defaultMapStr));
    }

    public static Map<Integer, String> parseMapIntegerString(String arg)
    {
        if (arg.equals(defaultMapStr)) return new HashMap<>();
        var mapConv = gson.fromJson(arg, HashMap.class);

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
}
