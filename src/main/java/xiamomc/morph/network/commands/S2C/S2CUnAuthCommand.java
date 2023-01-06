package xiamomc.morph.network.commands.S2C;

public class S2CUnAuthCommand extends AbstractS2CCommand<String>
{
    public S2CUnAuthCommand()
    {
        super("");
    }

    @Override
    public String getBaseName()
    {
        return "unauth";
    }
}
