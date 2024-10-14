package xyz.nifeather.morph.network.multiInstance.protocol.s2c;

import xyz.nifeather.morph.network.multiInstance.protocol.IMasterHandler;

import java.util.Arrays;

public class MIS2CStateCommand extends MIS2CCommand<String>
{
    public MIS2CStateCommand(ProtocolState newState)
    {
        super("state", newState.name());
    }

    @Override
    public void onCommand(IMasterHandler handler)
    {
        handler.onStateCommand(this);
    }

    public ProtocolState getState()
    {
        return Arrays.stream(ProtocolState.values()).filter(s -> s.name().equalsIgnoreCase(getArgumentAt(0, "INVALID")))
                .findFirst().orElse(ProtocolState.INVALID);
    }

    public static MIS2CStateCommand from(String text)
    {
        var match = Arrays.stream(ProtocolState.values()).filter(v -> v.name().equalsIgnoreCase(text))
                .findFirst().orElse(ProtocolState.INVALID);

        return new MIS2CStateCommand(match);
    }
}
