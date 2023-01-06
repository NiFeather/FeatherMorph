package xiamomc.morph.network.commands.S2C;

public class S2CCurrentCommand extends AbstractS2CCommand<String>
{
    public S2CCurrentCommand(String arguments)
    {
        super(arguments);
    }

    @Override
    public String getBaseName()
    {
        return "current";
    }

    @Override
    public String buildCommand()
    {
        return super.buildCommand() + " " + this.getArgumentAt(0, "");
    }
}
