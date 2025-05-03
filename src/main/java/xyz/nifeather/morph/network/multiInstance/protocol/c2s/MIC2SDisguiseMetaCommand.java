package xyz.nifeather.morph.network.multiInstance.protocol.c2s;

import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.network.multiInstance.protocol.IInstanceClientHandler;
import xyz.nifeather.morph.network.multiInstance.protocol.Operation;
import xyz.nifeather.morph.network.multiInstance.protocol.SocketDisguiseMeta;
import xyz.nifeather.morph.network.utils.Asserts;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MIC2SDisguiseMetaCommand extends MIC2SCommand
{
    public final SocketDisguiseMeta socketDisguiseMeta;

    public MIC2SDisguiseMetaCommand(SocketDisguiseMeta meta)
    {
        super("dmeta");

        this.socketDisguiseMeta = meta;
    }

    @Override
    public Map<String, String> generateArgumentMap()
    {
        return Map.of(
                "socket_meta", gson().toJson(socketDisguiseMeta)
        );
    }

    public static MIC2SDisguiseMetaCommand fromArguments(Map<String, String> arguments) throws RuntimeException
    {
        return new MIC2SDisguiseMetaCommand(gson().fromJson(Asserts.getStringOrThrow(arguments, "socket_meta"), SocketDisguiseMeta.class));
    }

    public MIC2SDisguiseMetaCommand(Operation operation, List<String> identifiers, UUID bindingUUID)
    {
        this(new SocketDisguiseMeta(operation, identifiers, bindingUUID));
    }

    @Nullable
    public SocketDisguiseMeta getMeta()
    {
        return socketDisguiseMeta;
    }

    @Override
    public void onCommand(IInstanceClientHandler handler)
    {
        handler.onDisguiseMetaCommand(this);
    }
}
