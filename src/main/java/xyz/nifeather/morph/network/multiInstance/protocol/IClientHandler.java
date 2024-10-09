package xyz.nifeather.morph.network.multiInstance.protocol;

import org.java_websocket.WebSocket;
import xyz.nifeather.morph.network.multiInstance.master.InstanceServer;
import xyz.nifeather.morph.network.multiInstance.protocol.c2s.MIC2SDisguiseMetaCommand;
import xyz.nifeather.morph.network.multiInstance.protocol.c2s.MIC2SLoginCommand;

public interface IClientHandler
{
    public void onLoginCommand(MIC2SLoginCommand cProtocolCommand);

    public void onDisguiseMetaCommand(MIC2SDisguiseMetaCommand cDisguiseMetaCommand);

    public void onMessage(InstanceServer.WsRecord wsRecord, InstanceServer server);

    public void onServerStart(InstanceServer server);

    public void onConnectionClose(WebSocket socket);
}
