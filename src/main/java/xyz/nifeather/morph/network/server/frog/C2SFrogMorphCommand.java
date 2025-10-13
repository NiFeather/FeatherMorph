package xyz.nifeather.morph.network.server.frog;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.network.commands.C2S.C2SMorphCommand;
import xyz.nifeather.morph.network.utils.Asserts;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class C2SFrogMorphCommand extends C2SMorphCommand
{
    private final Map<String, String> properties = new ConcurrentHashMap<>();

    public Map<String, String> propertyInputs()
    {
        return properties;
    }

    public C2SFrogMorphCommand(@Nullable String identifier, Map<String, String> propertyInputs)
    {
        super(identifier);
        this.properties.putAll(propertyInputs);
    }

    @Override
    public Map<String, String> generateArgumentMap()
    {
        var map = super.generateArgumentMap();
        map.put("properties", gson.toJson(properties));

        return map;
    }

    private static final Gson gson = new GsonBuilder().create();

    public static C2SFrogMorphCommand fromArguments(Map<String, String> arguments) throws RuntimeException
    {
        Map<String, String> properties = new HashMap<>();

        if (arguments.containsKey("properties"))
        {
            gson.fromJson(Asserts.getStringOrThrow(arguments, "properties"), Map.class)
                    .forEach((k, v) -> properties.put("" + k, "" + v));
        }

        return new C2SFrogMorphCommand(Asserts.getStringOrThrow(arguments, "id"), properties);
    }
}
