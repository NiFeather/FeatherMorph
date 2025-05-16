package xyz.nifeather.morph.network.multiInstance.protocol;

import xyz.nifeather.morph.network.multiInstance.protocol.s2c.*;
import xyz.nifeather.morph.network.multiInstance.slave.InstanceClient;

public interface IMasterHandler
{
    public void onUpdateMetaCommand(MIS2CUpdateMetaCommand metaCommand);
    public void onSyncMeta(MIS2CSyncMetaCommand command);

    public void onDisconnectCommand(MIS2CDisconnectCommand cDenyCommand);

    public void onLoginResponse(MIS2CLoginResponseCommand cLoginResultCommand);
    public void onStateCommand(MIS2CSwitchStateCommand cStateCommand);

    public void onConnectionOpen();
    public void onConnectionClose(int code);
    public void onText(String rawCommand);
    public void onClientError(Exception e, InstanceClient client);
}
