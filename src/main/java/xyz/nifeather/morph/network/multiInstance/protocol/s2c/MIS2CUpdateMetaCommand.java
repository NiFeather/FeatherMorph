package xyz.nifeather.morph.network.multiInstance.protocol.s2c;

import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.network.multiInstance.protocol.IMasterHandler;
import xyz.nifeather.morph.network.multiInstance.protocol.SocketPlayerMeta;
import xyz.nifeather.morph.network.utils.Asserts;

import java.util.Map;

public class MIS2CUpdateMetaCommand extends MIS2CCommand
{
    @NotNull
    public final SocketPlayerMeta disguiseMeta;

    public MIS2CUpdateMetaCommand(@NotNull SocketPlayerMeta meta)
    {
        super("dmeta");

        this.disguiseMeta = meta;
    }

    @Override
    public Map<String, String> generateArgumentMap()
    {
        return Map.of(
                "socket_meta", gson().toJson(disguiseMeta)
        );
    }

    public static MIS2CUpdateMetaCommand fromArguments(Map<String, String> arguments)
    {
        var metaString = Asserts.getStringOrThrow(arguments, "socket_meta");

        return new MIS2CUpdateMetaCommand(gson().fromJson(metaString, SocketPlayerMeta.class));
    }

    @Override
    public void onCommand(IMasterHandler handler)
    {
        handler.onUpdateMetaCommand(this);
    }

    @NotNull
    public SocketPlayerMeta getMeta()
    {
        return disguiseMeta;
    }
}
