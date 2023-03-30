package xiamomc.morph.network.server.commands.S2C;

import xiamomc.morph.network.commands.S2C.AbstractS2CCommand;

public abstract class S2CQueryCommand<T> extends AbstractS2CCommand<T>
{
    public S2CQueryCommand(T argument)
    {
        super(argument);
    }

    public S2CQueryCommand(T... arguments)
    {
        super(arguments);
    }

    @Override
    public String buildCommand()
    {
        return "query" + " " + getBaseName();
    }

    protected String serializeArguments()
    {
        var builder = new StringBuilder();

        for (var a : arguments)
            builder.append(a).append(" ");

        return builder.toString().trim();
    }
}
