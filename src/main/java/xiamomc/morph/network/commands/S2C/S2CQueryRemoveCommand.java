package xiamomc.morph.network.commands.S2C;

import java.util.Arrays;

public class S2CQueryRemoveCommand extends S2CQueryCommand<String>
{
    public S2CQueryRemoveCommand(String... args)
    {
        super(args);
    }

    @Override
    public String getBaseName()
    {
        return "remove";
    }

    @Override
    public String buildCommand()
    {
        return super.buildCommand() + " " + this.serializeArguments();
    }
}
