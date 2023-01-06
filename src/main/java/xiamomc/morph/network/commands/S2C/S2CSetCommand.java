package xiamomc.morph.network.commands.S2C;

public abstract class S2CSetCommand<T> extends AbstractS2CCommand<T>
{
    public S2CSetCommand(T argument)
    {
        super(argument);
    }

    public S2CSetCommand(T... arguments)
    {
        super(arguments);
    }

    @Override
    public String buildCommand()
    {
        return "set " + getBaseName();
    }
}
