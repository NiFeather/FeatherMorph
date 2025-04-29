package xyz.nifeather.netherite.network;

import xyz.nifeather.netherite.network.commands.C2S.*;
import xyz.nifeather.netherite.network.commands.S2C.*;
import xyz.nifeather.netherite.network.commands.S2C.clientrender.*;
import xyz.nifeather.netherite.network.commands.S2C.map.*;
import xyz.nifeather.netherite.network.commands.S2C.query.NetheriteS2CQueryCommand;
import xyz.nifeather.netherite.network.commands.S2C.set.*;

/**
 * A handler that process commands from servers send to the client
 * @param <TPlatformPlayer> The class type of player on the client platform
 */
public interface BasicServerHandler<TPlatformPlayer>
{
    public abstract void connect();

    public abstract void disconnect();

    public abstract boolean sendCommand(NetheriteC2SCommand<?> c2SCommand);

    public abstract int getServerApiVersion();

    public abstract int getImplmentingApiVersion();

    //region Commands

    void onCurrentCommand(NetheriteS2CCurrentCommand command);
    void onReAuthCommand(NetheriteS2CReAuthCommand command);
    void onUnAuthCommand(NetheriteS2CUnAuthCommand command);
    void onSwapCommand(NetheriteS2CSwapCommand command);

    void onQueryCommand(NetheriteS2CQueryCommand command);

    void onSetAggressiveCommand(NetheriteS2CSetAggressiveCommand command);
    void onSetFakeEquipCommand(NetheriteS2CSetFakeEquipCommand<?> command);
    void onSetDisplayingFakeEquipCommand(NetheriteS2CSetDisplayingFakeEquipCommand command);
    void onSetSNbtCommand(NetheriteS2CSetSNbtCommand command); //NBT和SNBT用的是同一个指令名和格式，不需要单独设置
    void onSetProfileCommand(NetheriteS2CSetProfileCommand command);
    void onSetSelfViewIdentifierCommand(NetheriteS2CSetSelfViewIdentifierCommand command);
    void onSetSkillCooldownCommand(NetheriteS2CSetSkillCooldownCommand command);
    void onSetSneakingCommand(NetheriteS2CSetSneakingCommand command);
    void onSetSelfViewingCommand(NetheriteS2CSetSelfViewingCommand command);
    void onSetModifyBoundingBox(NetheriteS2CSetModifyBoundingBoxCommand command);
    void onSetReach(NetheriteS2CSetReachCommand command);
    void onSetRevealing(NetheriteS2CSetRevealingCommand command);

    void onExchangeRequestReceive(NetheriteS2CRequestCommand command);

    //region MapCommands

    void onMapCommand(NetheriteS2CMapCommand command);
    void onMapPartialCommand(NetheriteS2CPartialMapCommand command);
    void onMapClearCommand(NetheriteS2CMapClearCommand command);
    void onMapRemoveCommand(NetheriteS2CMapRemoveCommand command);

    //endregion MapCommands

    //region ClientRenderer

    void onClientMapSyncCommand(NetheriteS2CRenderMapSyncCommand command);
    void onClientMapAddCommand(NetheriteS2CRenderMapAddCommand command);
    void onClientMapRemoveCommand(NetheriteS2CRenderMapRemoveCommand command);
    void onClientMapClearCommand(NetheriteS2CRenderMapClearCommand command);
    void onClientMapMetaNbtCommand(NetheriteS2CRenderMapMetaCommand command);

    //endregion ClientRenderer

    void onAnimationCommand(NetheriteS2CAnimationCommand command);
    void onValidAnimationsCommand(NetheriteS2CSetAvailableAnimationsCommand command);
    void onSetAnimationDisplayCommand(NetheriteS2CSetAnimationDisplayNameCommand command);

    //endregion Commands
}
