package xiamomc.morph.network.server.commands.S2C;

public class S2CSetSNbtCommand extends S2CSetCommand<String>
{
    public S2CSetSNbtCommand(String tag)
    {
        super(tag);
    }

    @Override
    public String getBaseName()
    {
        return "nbt";
    }

    @Override
    public String buildCommand()
    {
        return super.buildCommand() + " " + this.getArgumentAt(0, "{}");
    }
}
