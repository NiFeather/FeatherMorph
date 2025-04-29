package xyz.nifeather.netherite.network.commands.C2S;

import xyz.nifeather.netherite.network.BasicClientHandler;
import xyz.nifeather.netherite.network.annotations.Environment;
import xyz.nifeather.netherite.network.annotations.EnvironmentType;
import xyz.nifeather.netherite.network.commands.S2C.NetheriteS2CCommandNames;

import java.util.Arrays;

public class NetheriteC2SRequestCommand extends NetheriteC2SCommand<String>
{
    @Environment(EnvironmentType.SERVER)
    public NetheriteC2SRequestCommand(String rawArg)
    {
        super(rawArg.split(" "));
    }

    public NetheriteC2SRequestCommand(Decision decision, String targetRequestName)
    {
        super(new String[]{decision.name().toLowerCase(), targetRequestName});
    }

    @Override
    public String getBaseName()
    {
        return NetheriteS2CCommandNames.Request;
    }

    @Environment(EnvironmentType.SERVER)
    public Decision decision;

    @Environment(EnvironmentType.SERVER)
    public String targetRequestName;

    @Environment(EnvironmentType.SERVER)
    @Override
    public void onCommand(BasicClientHandler<?> listener)
    {
        this.decision = Arrays.stream(Decision.values()).filter(v -> v.name().equalsIgnoreCase(getArgumentAt(0, "unknown")))
                .findFirst().orElse(Decision.UNKNOWN);

        this.targetRequestName = getArgumentAt(1, "unknown");

        listener.onRequestCommand(this);
    }

    public enum Decision
    {
        ACCEPT,
        DENY,
        UNKNOWN;
    }
}
