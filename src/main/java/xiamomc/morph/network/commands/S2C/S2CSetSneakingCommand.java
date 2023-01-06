package xiamomc.morph.network.commands.S2C;

public class S2CSetSneakingCommand extends S2CSetCommand<Boolean>
{
    public S2CSetSneakingCommand(boolean val)
    {
        super(val);
    }
    @Override
    public String getBaseName()
    {
        return "sneaking";
    }

    @Override
    public String buildCommand()
    {
        return super.buildCommand() + " " + this.getArgumentAt(0, false);
    }
}
