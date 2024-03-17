package xiamomc.morph.network.multiInstance.protocol;

import xiamomc.morph.network.multiInstance.protocol.s2c.MIS2CDisconnectCommand;
import xiamomc.morph.network.multiInstance.protocol.s2c.MIS2CStateCommand;
import xiamomc.morph.network.multiInstance.protocol.s2c.MIS2CSyncMetaCommand;
import xiamomc.morph.network.multiInstance.protocol.s2c.MIS2CLoginResultCommand;
import xiamomc.morph.network.multiInstance.slave.InstanceClient;

public interface IMasterHandler
{
    public void onSyncMetaCommand(MIS2CSyncMetaCommand metaCommand);

    public void onDisconnectCommand(MIS2CDisconnectCommand cDenyCommand);

    public void onLoginResultCommand(MIS2CLoginResultCommand cLoginResultCommand);
    public void onStateCommand(MIS2CStateCommand cStateCommand);

    public void onConnectionOpen();
    public void onText(String rawCommand);
    public void onClientError(Exception e, InstanceClient client);
}
