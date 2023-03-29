package xiamomc.morph.network.commands.S2C;

@Deprecated(forRemoval = true)
public class S2CDenyCommand extends AbstractS2CCommand<String>
{
    public S2CDenyCommand(String argument)
    {
        super(argument);
    }

    @Override
    public String getBaseName()
    {
        return "deny";
    }

    @Override
    public String buildCommand()
    {
        return super.buildCommand() + " " + this.getArgumentAt(0, "");
    }
}
