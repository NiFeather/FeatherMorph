package xiamomc.morph.network.commands.S2C;

public class S2CReAuthCommand extends AbstractS2CCommand<String>
{
    public S2CReAuthCommand()
    {
        super("");
    }

    @Override
    public String getBaseName()
    {
        return "reauth";
    }
}
