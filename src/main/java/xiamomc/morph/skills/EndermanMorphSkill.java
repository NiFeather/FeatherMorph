package xiamomc.morph.skills;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.messages.SkillStrings;

public class EndermanMorphSkill extends MorphSkill
{
    @Override
    public int executeSkill(Player player)
    {
        //目标方块
        var targetBlock = player.getTargetBlockExact(32, FluidCollisionMode.ALWAYS);

        if (targetBlock == null
                || targetBlock.getBlockData().getMaterial().isAir()
                || targetBlock.getBlockData().getMaterial().equals(Material.WATER))
        {
            sendDenyMessageToPlayer(player, SkillStrings.targetNotSuitableString().toComponent());
            return 20;
        }

        //获取位置
        var loc = targetBlock.getLocation();
        var face = player.getTargetBlockFace(32);

        var commonOffset = 0.5f;
        var xOffset = 0f;
        var zOffset = 0f;

        //获取位移并设置目的地
        assert face != null;
        xOffset = face.getModX();
        zOffset = face.getModZ();

        //目标X/Z + 0.5 + 从方块朝向获取的ModX/Z
        loc.setX(loc.getX() + xOffset + commonOffset);
        loc.setZ(loc.getZ() + zOffset + commonOffset);

        //目的地
        var destBlock = loc.getBlock();

        //从目的地的方块获取应该设置的高度
        loc.setY(getTopY(destBlock));

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

        return 40;
    }

    @Override
    public EntityType getType()
    {
        return EntityType.ENDERMAN;
    }
}
