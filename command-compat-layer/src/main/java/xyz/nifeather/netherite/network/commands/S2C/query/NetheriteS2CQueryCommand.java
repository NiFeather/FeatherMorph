package xyz.nifeather.netherite.network.commands.S2C.query;

import org.jetbrains.annotations.Nullable;
import xyz.nifeather.netherite.network.BasicServerHandler;
import xyz.nifeather.netherite.network.annotations.Environment;
import xyz.nifeather.netherite.network.annotations.EnvironmentType;
import xyz.nifeather.netherite.network.commands.S2C.NetheriteS2CCommand;
import xyz.nifeather.netherite.network.commands.S2C.NetheriteS2CCommandNames;

import java.util.List;

public class NetheriteS2CQueryCommand extends NetheriteS2CCommand<String>
{
    @Nullable
    public static NetheriteS2CQueryCommand from(String rawArg)
    {
        var spilt = rawArg.split(" ", 2);
        if (spilt.length < 1) return null;

        var type = NetheriteQueryType.tryValueOf(spilt[0].toUpperCase());
        return new NetheriteS2CQueryCommand(type, spilt.length == 2 ? spilt[1].split(" ") : new String[]{});
    }

    public NetheriteS2CQueryCommand(NetheriteQueryType netheriteQueryType, String... diff)
    {
        super(diff);

        this.netheriteQueryType = netheriteQueryType;
    }

    private final NetheriteQueryType netheriteQueryType;

    public NetheriteQueryType queryType()
    {
        return this.netheriteQueryType;
    }

    public List<String> getDiff()
    {
        return arguments;
    }

    @Override
    public String getBaseName()
    {
        return NetheriteS2CCommandNames.Query;
    }

    @Override
    public String buildCommand()
    {
        return this.getBaseName() + " " + netheriteQueryType.name().toLowerCase()
                + (arguments.size() > 0 ? (" " + serializeArguments()) : "");
    }

    @Environment(EnvironmentType.CLIENT)
    @Override
    public void onCommand(BasicServerHandler<?> handler)
    {
        handler.onQueryCommand(this);
    }
}

