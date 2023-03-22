package xiamomc.morph.network.commands.S2C;

public class S2CQuerySetCommand extends S2CQueryCommand<String>
{
    public S2CQuerySetCommand(String... arguments)
    {
        super(arguments);
    }

    public S2CQuerySetCommand(String argument)
    {
        super(argument);
    }

    @Override
    public String getBaseName()
    {
        return "set";
    }

    @Override
    public String buildCommand()
    {
        return super.buildCommand() + " " + this.serializeArguments();
    }
}
