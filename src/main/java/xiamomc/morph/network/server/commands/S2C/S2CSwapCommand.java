package xiamomc.morph.network.server.commands.S2C;

import xiamomc.morph.network.commands.S2C.AbstractS2CCommand;

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
