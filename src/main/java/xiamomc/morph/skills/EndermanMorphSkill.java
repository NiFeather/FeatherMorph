package xiamomc.morph.skills;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.messages.SkillStrings;

import java.util.Set;

public class EndermanMorphSkill extends MorphSkill
{
    @Override
    public int executeSkill(Player player)
    {
        var targetBlock = player.getEyeLocation().getBlock().getType() == Material.WATER
                ? player.getTargetBlock(Set.of(Material.WATER), 32) //只能从水里传到水里
                : player.getTargetBlock(null, 32); //在空气中时阻止传送到水里

        var targetMaterial = targetBlock.getBlockData().getMaterial();
        if (targetMaterial.isAir() || targetMaterial.equals(Material.WATER))
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
