package xiamomc.morph.network.commands.S2C;

import java.util.Arrays;

public class S2CQueryAddCommand extends S2CQueryCommand<String>
{
    public S2CQueryAddCommand(String... arguments)
    {
        super(arguments);
    }

    public S2CQueryAddCommand(String argument)
    {
        super(argument);
    }

    @Override
    public String getBaseName()
    {
        return "with";
    }

    @Override
    public String buildCommand()
    {
        return super.buildCommand() + " " + this.serializeArguments();
    }
}
