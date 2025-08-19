package xyz.nifeather.morph.network.multiInstance.protocol.c2s;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.network.multiInstance.protocol.IInstanceClientHandler;
import xyz.nifeather.morph.network.multiInstance.protocol.ProtocolLevel;
import xyz.nifeather.morph.network.utils.Asserts;

import java.util.Map;

public class MIC2SLoginCommand extends MIC2SCommand
{
    public final ProtocolLevel clientProtocolLevel;
    public final String secret;

    public MIC2SLoginCommand(@NotNull ProtocolLevel protocolLevel, @NotNull String secret)
    {
        super("login");

        this.clientProtocolLevel = protocolLevel;
        this.secret = secret;
    }

    @Override
    public Map<String, String> generateArgumentMap()
    {
        return Map.of(
                "protocol", clientProtocolLevel.name(),
                "secret", secret
        );
    }

    public static MIC2SLoginCommand fromArguments(Map<String, String> arguments) throws RuntimeException
    {
        var protoLevel = Asserts.getStringOrThrow(arguments, "protocol");
        var secret = Asserts.getStringOrThrow(arguments, "secret");

        var proto = ProtocolLevel.valueOf(protoLevel.toUpperCase());
        return new MIC2SLoginCommand(proto, secret);
    }

    public int getVersion()
    {
        return clientProtocolLevel.version();
    }

    @Nullable
    public String getSecret()
    {
        return secret;
    }

    @Override
    public void onCommand(IInstanceClientHandler handler)
    {
        handler.onLoginCommand(this);
    }
}
