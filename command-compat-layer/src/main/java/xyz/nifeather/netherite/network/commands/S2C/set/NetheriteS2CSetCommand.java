package xyz.nifeather.netherite.network.commands.S2C.set;

import xyz.nifeather.netherite.network.commands.S2C.NetheriteS2CCommand;
import xyz.nifeather.netherite.network.commands.S2C.NetheriteS2CCommandNames;

import java.util.List;

public abstract class NetheriteS2CSetCommand<T> extends NetheriteS2CCommand<T>
{
    public NetheriteS2CSetCommand(T argument)
    {
        super(argument);
    }

    public NetheriteS2CSetCommand(T... arguments)
    {
        super(arguments);
    }

    public List<T> getArguments()
    {
        return arguments;
    }

    @Override
    public String buildCommand()
    {
        return (NetheriteS2CCommandNames.BaseSet + " " + getBaseName() + " " + serializeArguments()).trim();
    }
}
