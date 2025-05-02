package xyz.nifeather.morph.network.multiInstance.protocol.c2s;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.network.multiInstance.protocol.IClientHandler;
import xyz.nifeather.morph.network.multiInstance.protocol.ProtocolLevel;

import java.util.Map;

public class MIC2SLoginCommand extends MIC2SCommand<String>
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
    public void onCommand(IClientHandler handler)
    {
        handler.onLoginCommand(this);
    }

    @Deprecated
    public static MIC2SLoginCommand from(String arg)
    {
        var args = arg.split(" ", 2);
        int ver = 0;

        try
        {
            ver = Integer.parseInt(args[0]);
        }
        catch (Throwable t)
        {
            var logger = FeatherMorphMain.getInstance().getSLF4JLogger();
            logger.warn("Error occurred processing arguments: " + t.getMessage());
        }

        return new MIC2SLoginCommand(ProtocolLevel.V1, args.length == 2 ? args[1] : "~NULL");
    }
}
