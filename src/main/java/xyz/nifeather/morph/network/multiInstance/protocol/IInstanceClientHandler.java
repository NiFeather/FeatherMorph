package xyz.nifeather.morph.network.multiInstance.protocol;

import org.java_websocket.WebSocket;
import xyz.nifeather.morph.network.multiInstance.master.InstanceServer;
import xyz.nifeather.morph.network.multiInstance.protocol.c2s.MIC2SLoginCommand;
import xyz.nifeather.morph.network.multiInstance.protocol.c2s.MIC2SRequestSyncCommand;
import xyz.nifeather.morph.network.multiInstance.protocol.c2s.MIC2SSyncDisguiseCommand;

public interface IInstanceClientHandler
{
    public void onLoginCommand(MIC2SLoginCommand cProtocolCommand);

    public void onDisguiseMetaCommand(MIC2SSyncDisguiseCommand cDisguiseMetaCommand);

    public void onSlaveRequestMetaSync(MIC2SRequestSyncCommand command);

    public void onMessage(InstanceServer.WsRecord wsRecord, InstanceServer server);

    public void onServerStart(InstanceServer server);

    public void onConnectionClose(WebSocket socket);
}
