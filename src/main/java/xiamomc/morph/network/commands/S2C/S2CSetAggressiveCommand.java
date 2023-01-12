package xiamomc.morph.network.commands.S2C;

public class S2CSetAggressiveCommand extends S2CSetCommand<Boolean>
{
    public S2CSetAggressiveCommand(boolean val)
    {
        super(val);
    }

    @Override
    public String getBaseName()
    {
        return "aggressive";
    }

    @Override
    public String buildCommand()
    {
        return super.buildCommand() + " " + getArgumentAt(0, false);
    }
}
