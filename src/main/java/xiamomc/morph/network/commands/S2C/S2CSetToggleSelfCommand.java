package xiamomc.morph.network.commands.S2C;

public class S2CSetToggleSelfCommand extends S2CSetCommand<Boolean>
{
    public S2CSetToggleSelfCommand(boolean val)
    {
        super(val);
    }

    @Override
    public String getBaseName()
    {
        return "toggleself";
    }

    @Override
    public String buildCommand()
    {
        return super.buildCommand() + " " + this.getArgumentAt(0, false);
    }
}
