package xiamomc.morph.skills;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.messages.SkillStrings;

public class EndermanMorphSkill extends MorphSkill
{
    @Override
    public int executeSkill(Player player)
    {
        var targetBlock = player.getTargetBlock(32);
        if (targetBlock == null || targetBlock.getBlockData().getMaterial().isAir())
        {
            sendDenyMessageToPlayer(player, SkillStrings.targetNotSuitableString().toComponent());
            return 20;
        }

        //获取位置
        var loc = targetBlock.getLocation();
        var face = player.getTargetBlockFace(32);

        var commonOffset = 0.5f;
        var xOffset = 0f;
        var yOffset = 0f;
        var zOffset = 0f;

        //获取位移
        assert face != null;
        xOffset = face.getModX();
        zOffset = face.getModZ();
        yOffset = face == BlockFace.UP
                ? (float) targetBlock.getBoundingBox().getHeight()
                : face.getModY();

        loc.setX(loc.getX() + xOffset + commonOffset);
        loc.setY(loc.getY() + yOffset);
        loc.setZ(loc.getZ() + zOffset + commonOffset);

        loc.setDirection(player.getEyeLocation().getDirection());

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
