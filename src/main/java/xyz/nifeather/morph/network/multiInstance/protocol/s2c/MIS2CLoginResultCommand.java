package xyz.nifeather.morph.network.multiInstance.protocol.s2c;

import xyz.nifeather.morph.network.multiInstance.protocol.IMasterHandler;

public class MIS2CLoginResultCommand extends MIS2CCommand<Boolean>
{
    public MIS2CLoginResultCommand(boolean allowed)
    {
        super("r_login", allowed);
    }

    public boolean isAllowed()
    {
        return this.getArgumentAt(0, false);
    }

    @Override
    public void onCommand(IMasterHandler handler)
    {
        handler.onLoginResultCommand(this);
    }

    public static MIS2CLoginResultCommand from(String text)
    {
        return new MIS2CLoginResultCommand(Boolean.parseBoolean(text));
    }
}
