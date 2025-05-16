package xyz.nifeather.morph.network.multiInstance.protocol.c2s;

import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.network.multiInstance.protocol.IInstanceClientHandler;
import xyz.nifeather.morph.network.multiInstance.protocol.Operation;
import xyz.nifeather.morph.network.multiInstance.protocol.SocketPlayerMeta;
import xyz.nifeather.morph.network.utils.Asserts;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MIC2SSyncDisguiseCommand extends MIC2SCommand
{
    public final SocketPlayerMeta socketPlayerMeta;

    public MIC2SSyncDisguiseCommand(SocketPlayerMeta meta)
    {
        super("dmeta");

        this.socketPlayerMeta = meta;
    }

    @Override
    public Map<String, String> generateArgumentMap()
    {
        return Map.of(
                "socket_meta", gson().toJson(socketPlayerMeta)
        );
    }

    public static MIC2SSyncDisguiseCommand fromArguments(Map<String, String> arguments) throws RuntimeException
    {
        return new MIC2SSyncDisguiseCommand(gson().fromJson(Asserts.getStringOrThrow(arguments, "socket_meta"), SocketPlayerMeta.class));
    }

    public MIC2SSyncDisguiseCommand(Operation operation, List<String> identifiers, UUID bindingUUID)
    {
        this(new SocketPlayerMeta(operation, identifiers, bindingUUID));
    }

    @Nullable
    public SocketPlayerMeta getMeta()
    {
        return socketPlayerMeta;
    }

    @Override
    public void onCommand(IInstanceClientHandler handler)
    {
        handler.onDisguiseMetaCommand(this);
    }
}
