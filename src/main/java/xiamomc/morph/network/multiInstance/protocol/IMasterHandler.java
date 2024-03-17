package xiamomc.morph.network.multiInstance.protocol;

import xiamomc.morph.network.multiInstance.protocol.s2c.MIS2CDisconnectCommand;
import xiamomc.morph.network.multiInstance.protocol.s2c.MIS2CDisguiseMetaCommand;
import xiamomc.morph.network.multiInstance.protocol.s2c.MIS2CLoginResultCommand;
import xiamomc.morph.network.multiInstance.protocol.s2c.MIS2CStateCommand;

public interface IMasterHandler
{
    public void onDisguiseMetaCommand(MIS2CDisguiseMetaCommand metaCommand);

    public void onDisconnectCommand(MIS2CDisconnectCommand cDenyCommand);

    public void onLoginResultCommand(MIS2CLoginResultCommand cLoginResultCommand);

    public void onConnectionOpen();
    public void onText(String rawCommand);
}
