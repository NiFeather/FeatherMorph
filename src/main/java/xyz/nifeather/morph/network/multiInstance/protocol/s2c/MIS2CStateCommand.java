package xyz.nifeather.morph.network.multiInstance.protocol.s2c;

import xyz.nifeather.morph.network.multiInstance.protocol.IMasterHandler;
import xyz.nifeather.morph.network.utils.Asserts;

import java.util.Arrays;
import java.util.Map;

public class MIS2CStateCommand extends MIS2CCommand
{
    public final ProtocolState newState;

    public MIS2CStateCommand(ProtocolState newState)
    {
        super("state");

        this.newState = newState;
    }

    @Override
    public Map<String, String> generateArgumentMap()
    {
        return Map.of(
                "new_state", newState.name()
        );
    }

    public static MIS2CStateCommand fromArguments(Map<String, String> arguments) throws RuntimeException
    {
        return new MIS2CStateCommand(
                ProtocolState.valueOf(Asserts.getStringOrThrow(arguments, "new_state").toUpperCase())
        );
    }

    @Override
    public void onCommand(IMasterHandler handler)
    {
        handler.onStateCommand(this);
    }

    public ProtocolState getState()
    {
        return newState;
    }

    public static MIS2CStateCommand from(String text)
    {
        var match = Arrays.stream(ProtocolState.values()).filter(v -> v.name().equalsIgnoreCase(text))
                .findFirst().orElse(ProtocolState.INVALID);

        return new MIS2CStateCommand(match);
    }
}
