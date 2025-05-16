package xyz.nifeather.morph.network.multiInstance.protocol.c2s;

import xyz.nifeather.morph.network.multiInstance.protocol.IInstanceClientHandler;

import java.util.Map;

public class MIC2SRequestSyncCommand extends MIC2SCommand
{
    public MIC2SRequestSyncCommand()
    {
        super("request_meta_sync");
    }

    @Override
    public Map<String, String> generateArgumentMap()
    {
        return Map.of();
    }

    public static MIC2SRequestSyncCommand fromArguments(Map<String, String> argument)
    {
        return new MIC2SRequestSyncCommand();
    }

    @Override
    public void onCommand(IInstanceClientHandler handler)
    {
        handler.onSlaveRequestMetaSync(this);
    }
}
