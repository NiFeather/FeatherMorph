package xyz.nifeather.netherite.network.commands.S2C.set;

import xyz.nifeather.netherite.network.BasicServerHandler;
import xyz.nifeather.netherite.network.annotations.Environment;
import xyz.nifeather.netherite.network.annotations.EnvironmentType;
import xyz.nifeather.netherite.network.commands.S2C.NetheriteS2CCommandNames;

public class NetheriteS2CSetSkillCooldownCommand extends NetheriteS2CSetCommand<Long>
{
    public NetheriteS2CSetSkillCooldownCommand(long value)
    {
        super(value);
    }

    @Override
    public String getBaseName()
    {
        return NetheriteS2CCommandNames.SetSkillCooldown;
    }

    @Environment(EnvironmentType.CLIENT)
    @Override
    public void onCommand(BasicServerHandler<?> handler)
    {
        handler.onSetSkillCooldownCommand(this);
    }
}
