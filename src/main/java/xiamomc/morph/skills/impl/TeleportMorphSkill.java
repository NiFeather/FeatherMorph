package xiamomc.morph.skills.impl;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.SkillStrings;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.skills.MorphSkill;
import xiamomc.morph.skills.SkillType;
import xiamomc.morph.skills.options.TeleportConfiguration;
import xiamomc.morph.storage.skill.SkillAbilityConfiguration;

public class TeleportMorphSkill extends MorphSkill<TeleportConfiguration>
{
    @Override
    public int executeSkill(Player player, DisguiseState state, SkillAbilityConfiguration configuration, TeleportConfiguration option)
    {
        if (option == null)
        {
            printErrorMessage(player, configuration + "没有传送设置");
            return 10;
        }

        //目标方块
        var targetBlock = player.getTargetBlockExact(
                option.getMaxDistance(),
                FluidCollisionMode.ALWAYS);

        if (targetBlock == null
                || targetBlock.getBlockData().getMaterial().isAir()
                || targetBlock.getBlockData().getMaterial().equals(Material.WATER))
        {
            sendDenyMessageToPlayer(player, SkillStrings.targetNotSuitableString()
                    .withLocale(MessageUtils.getLocale(player))
                    .toComponent(null));

            return 20;
        }

        //获取位置
        var loc = targetBlock.getLocation();
        var face = player.getTargetBlockFace(32);

        var commonOffset = 0.5f;
        var xOffset = 0f;
        var zOffset = 0f;
        var yOffset = 0f;

        //获取位移并设置目的地
        assert face != null;
        xOffset = face.getModX();
        zOffset = face.getModZ();
        yOffset = face.getModY();

        //目标X/Z + 0.5 + 从方块朝向获取的ModX/Z
        loc.setX(loc.getX() + xOffset + commonOffset);
        loc.setZ(loc.getZ() + zOffset + commonOffset);

        //目的地
        var destBlock = loc.getBlock();

        //从目的地的方块获取应该设置的高度
        loc.setY(face == BlockFace.DOWN ? loc.getY() + yOffset : getTopY(destBlock));

        //设置眼睛方向
        loc.setDirection(player.getEyeLocation().getDirection());

        //传送
        playSoundToNearbyPlayers(player, 10,
                Key.key("minecraft", "entity.enderman.teleport"), Sound.Source.HOSTILE);

        player.teleport(loc);

        playSoundToNearbyPlayers(player, 10,
                Key.key("minecraft", "entity.enderman.teleport"), Sound.Source.HOSTILE);

        //重设下落距离
        player.setFallDistance(0);

        return configuration.getCooldown();
    }

    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return SkillType.TELEPORT;
    }

    private final TeleportConfiguration option = new TeleportConfiguration();

    @Override
    public TeleportConfiguration getOption()
    {
        return option;
    }
}
