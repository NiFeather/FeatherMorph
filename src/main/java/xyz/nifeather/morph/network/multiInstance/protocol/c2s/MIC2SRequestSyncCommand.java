package xyz.nifeather.morph.network.multiInstance.protocol.c2s;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import xyz.nifeather.morph.network.multiInstance.protocol.IInstanceClientHandler;
import xyz.nifeather.morph.network.utils.Asserts;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MIC2SRequestSyncCommand extends MIC2SCommand
{
    public final List<UUID> requestedUUIDs;

    public MIC2SRequestSyncCommand(List<UUID> requestedUUIDs)
    {
        super("request_meta_sync");
        this.requestedUUIDs = ImmutableList.copyOf(requestedUUIDs);
    }

    @Override
    public Map<String, String> generateArgumentMap()
    {
        return Map.of(
                "requested_uuids", gson().toJson(requestedUUIDs)
        );
    }

    public static MIC2SRequestSyncCommand fromArguments(Map<String, String> argument)
    {
        List<UUID> uuids = new ObjectArrayList<>();
        if (argument.containsKey("requested_uuids"))
        {
            var list = Asserts.getStringListOrThrow(argument, "requested_uuids")
                    .stream().map(UUID::fromString)
                    .toList();

            uuids.addAll(list);
        }

        return new MIC2SRequestSyncCommand(uuids);
    }

    @Override
    public void onCommand(IInstanceClientHandler handler)
    {
        handler.onSlaveRequestMetaSync(this);
    }
}
