package xyz.nifeather.netherite.network.commands.S2C;

import xyz.nifeather.netherite.network.commands.S2C.set.*;

import java.util.function.Function;

public class NetheriteS2CSetCommandsAgent extends NetheriteS2CCommandWithChild<Object>
{
    public NetheriteS2CSetCommandsAgent()
    {
        this.register(NetheriteS2CCommandNames.SetAggressive, a -> new NetheriteS2CSetAggressiveCommand(Boolean.parseBoolean(a)))
                .register(NetheriteS2CCommandNames.SetDisplayingFakeEquip, a -> new NetheriteS2CSetDisplayingFakeEquipCommand(Boolean.parseBoolean(a)))
                .register(NetheriteS2CCommandNames.SetProfile, NetheriteS2CSetProfileCommand::new)
                .register(NetheriteS2CCommandNames.SetSelfViewIdentifier, NetheriteS2CSetSelfViewIdentifierCommand::new)
                .register(NetheriteS2CCommandNames.SetSkillCooldown, a -> new NetheriteS2CSetSkillCooldownCommand(Long.parseLong(a)))
                .register(NetheriteS2CCommandNames.SetSNbt, NetheriteS2CSetSNbtCommand::new)
                .register(NetheriteS2CCommandNames.SetSneaking, a -> new NetheriteS2CSetSneakingCommand(Boolean.parseBoolean(a)))
                .register(NetheriteS2CCommandNames.SetSelfViewing, a -> new NetheriteS2CSetSelfViewingCommand(Boolean.parseBoolean(a)))
                .register(NetheriteS2CCommandNames.SetModifyBoundingBox, a -> new NetheriteS2CSetModifyBoundingBoxCommand(Boolean.parseBoolean(a)))
                .register(NetheriteS2CCommandNames.SetReach, a -> new NetheriteS2CSetReachCommand(Integer.parseInt(a)))
                .register(NetheriteS2CCommandNames.SetAvailableAnimations, NetheriteS2CSetAvailableAnimationsCommand::fromString)
                .register(NetheriteS2CCommandNames.SetAnimationDisplayName, NetheriteS2CSetAnimationDisplayNameCommand::new);
    }

    public NetheriteS2CSetCommandsAgent register(String baseName, Function<String, NetheriteS2CSetCommand<?>> func)
    {
        super.register(baseName, func);

        return this;
    }

    @Override
    public String getBaseName()
    {
        return NetheriteS2CCommandNames.BaseSet;
    }
}
