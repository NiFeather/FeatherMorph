package xiamomc.morph.network.server.commands.S2C;

public class S2CSetProfileCommand extends S2CSetCommand<String>
{
    public S2CSetProfileCommand(String nbtTag)
    {
        super(nbtTag);
    }

    @Override
    public String getBaseName()
    {
        return "profile";
    }

    @Override
    public String buildCommand()
    {
        return super.buildCommand() + " " + this.getArgumentAt(0, "{}");
    }
}
