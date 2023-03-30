package xiamomc.morph.network.server.commands.S2C;

public class S2CSetSelfViewCommand extends S2CSetCommand<String>
{
    public S2CSetSelfViewCommand(String identifier)
    {
        super(identifier);
    }

    @Override
    public String getBaseName()
    {
        return "selfview";
    }

    @Override
    public String buildCommand()
    {
        return super.buildCommand() + " " + this.getArgumentAt(0, "");
    }
}
