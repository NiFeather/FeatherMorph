package xyz.nifeather.morph.network.multiInstance.protocol.s2c;

import xyz.nifeather.morph.network.multiInstance.protocol.IMasterHandler;

import java.util.Map;

public class MIS2CLoginResultCommand extends MIS2CCommand<Boolean>
{
    public final boolean loginAllowed;

    public MIS2CLoginResultCommand(boolean allowed)
    {
        super("r_login");

        this.loginAllowed = allowed;
    }

    @Override
    public Map<String, String> generateArgumentMap()
    {
        return Map.of(
                "allowed", Boolean.toString(loginAllowed)
        );
    }

    public boolean isAllowed()
    {
        return loginAllowed;
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
