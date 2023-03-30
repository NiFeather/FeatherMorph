package xiamomc.morph.network.server.commands.S2C;

import xiamomc.morph.network.commands.S2C.AbstractS2CCommand;

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
