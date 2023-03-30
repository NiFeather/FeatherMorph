package xiamomc.morph.network.server.commands.S2C;

import xiamomc.morph.network.commands.S2C.AbstractS2CCommand;

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
}
