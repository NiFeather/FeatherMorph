package xiamomc.morph.network.commands.S2C;

public class S2CSwapCommand extends AbstractS2CCommand<Object>
{
    public S2CSwapCommand()
    {
        super(null);
    }

    @Override
    public String getBaseName()
    {
        return "swap";
    }
}
