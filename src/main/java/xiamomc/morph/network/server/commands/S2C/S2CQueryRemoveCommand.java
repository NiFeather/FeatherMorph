package xiamomc.morph.network.server.commands.S2C;

public class S2CQueryRemoveCommand extends S2CQueryCommand<String>
{
    public S2CQueryRemoveCommand(String... args)
    {
        super(args);
    }

    @Override
    public String getBaseName()
    {
        return "remove";
    }

    @Override
    public String buildCommand()
    {
        return super.buildCommand() + " " + this.serializeArguments();
    }
}
