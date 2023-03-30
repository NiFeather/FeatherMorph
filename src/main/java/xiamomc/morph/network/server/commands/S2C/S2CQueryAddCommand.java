package xiamomc.morph.network.server.commands.S2C;

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
        return "add";
    }

    @Override
    public String buildCommand()
    {
        return super.buildCommand() + " " + this.serializeArguments();
    }
}
