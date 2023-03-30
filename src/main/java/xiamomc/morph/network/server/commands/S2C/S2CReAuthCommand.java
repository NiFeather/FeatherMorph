package xiamomc.morph.network.server.commands.S2C;

import xiamomc.morph.network.commands.S2C.AbstractS2CCommand;

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
