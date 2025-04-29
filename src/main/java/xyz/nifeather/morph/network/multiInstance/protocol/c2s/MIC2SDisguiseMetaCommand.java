package xyz.nifeather.morph.network.multiInstance.protocol.c2s;

import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.network.multiInstance.protocol.IClientHandler;
import xyz.nifeather.morph.network.multiInstance.protocol.Operation;
import xyz.nifeather.morph.network.multiInstance.protocol.SocketDisguiseMeta;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MIC2SDisguiseMetaCommand extends MIC2SCommand<SocketDisguiseMeta>
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
        var gson = new GsonBuilder().disableHtmlEscaping().create();

        return Map.of(
                "socket_meta", gson.toJson(socketDisguiseMeta)
        );
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
    public void onCommand(IClientHandler handler)
    {
        handler.onDisguiseMetaCommand(this);
    }

    private static final MIC2SDisguiseMetaCommand placeholder = new MIC2SDisguiseMetaCommand(Operation.INVALID, List.of(), UUID.randomUUID());

    public static MIC2SDisguiseMetaCommand from(String text)
    {
        var logger = FeatherMorphMain.getInstance().getSLF4JLogger();
        try
        {
            var gson = new GsonBuilder().disableHtmlEscaping().create();

            var meta = gson.fromJson(text, SocketDisguiseMeta.class);
            return new MIC2SDisguiseMetaCommand(meta);
        }
        catch (Throwable t)
        {
            logger.warn("Unable to deserialize list or UUID! Returning placeholder.." + t.getMessage());
            t.printStackTrace();

            return placeholder;
        }
    }
}
