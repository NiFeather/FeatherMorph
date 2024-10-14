package xyz.nifeather.morph.network.multiInstance.protocol;

import xyz.nifeather.morph.network.multiInstance.protocol.s2c.MIS2CDisconnectCommand;
import xyz.nifeather.morph.network.multiInstance.protocol.s2c.MIS2CLoginResultCommand;
import xyz.nifeather.morph.network.multiInstance.protocol.s2c.MIS2CStateCommand;
import xyz.nifeather.morph.network.multiInstance.protocol.s2c.MIS2CSyncMetaCommand;
import xyz.nifeather.morph.network.multiInstance.slave.InstanceClient;

public interface IMasterHandler
{
    public void onSyncMetaCommand(MIS2CSyncMetaCommand metaCommand);

    public void onDisconnectCommand(MIS2CDisconnectCommand cDenyCommand);

    public void onLoginResultCommand(MIS2CLoginResultCommand cLoginResultCommand);
    public void onStateCommand(MIS2CStateCommand cStateCommand);

    public void onConnectionOpen();
    public void onConnectionClose(int code);
    public void onText(String rawCommand);
    public void onClientError(Exception e, InstanceClient client);
}
