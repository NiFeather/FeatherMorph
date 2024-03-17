package xiamomc.morph.network.multiInstance.protocol;

import xiamomc.morph.network.multiInstance.master.InstanceServer;
import xiamomc.morph.network.multiInstance.protocol.c2s.MIC2SDisguiseMetaCommand;
import xiamomc.morph.network.multiInstance.protocol.c2s.MIC2SLoginCommand;

public interface IClientHandler
{
    public void onLoginCommand(MIC2SLoginCommand cProtocolCommand);

    public void onDisguiseMetaCommand(MIC2SDisguiseMetaCommand cDisguiseMetaCommand);

    public void onMessage(InstanceServer.WsRecord wsRecord, InstanceServer server);

    public void onServerStart(InstanceServer server);
}
