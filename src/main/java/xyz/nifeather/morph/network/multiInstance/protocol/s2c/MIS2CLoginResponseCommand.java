package xyz.nifeather.morph.network.multiInstance.protocol.s2c;

import xyz.nifeather.morph.network.multiInstance.protocol.IMasterHandler;
import xyz.nifeather.morph.network.utils.Asserts;

import java.util.Map;

public class MIS2CLoginResponseCommand extends MIS2CCommand
{
    public final boolean loginAllowed;

    public MIS2CLoginResponseCommand(boolean allowed)
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

    public static MIS2CLoginResponseCommand fromArguments(Map<String, String> arguments) throws RuntimeException
    {
        return new MIS2CLoginResponseCommand(
                Boolean.parseBoolean(Asserts.getStringOrThrow(arguments, "allowed"))
        );
    }

    public boolean isAllowed()
    {
        return loginAllowed;
    }

    @Override
    public void onCommand(IMasterHandler handler)
    {
        handler.onLoginResponse(this);
    }
}
