package xiamomc.morph.misc;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * NMS Record for a player
 *
 * @param nmsPlayer ServerPlayer in fabric mojang mappings
 * @param nmsWorld ServerLevel in fabric mojang mappings
 * @param nmsEntity ??? in fabric mojang mappings
 * @param interactManager GameMode in fabric mojang mappings
 */
public record NmsRecord(ServerPlayer nmsPlayer, ServerLevel nmsWorld,
                        @Nullable net.minecraft.world.entity.Entity nmsEntity,
                        ServerPlayerGameMode interactManager)
{
    public static NmsRecord of(Player player)
    {
        var craftPlayer = (CraftPlayer) player;

        return new NmsRecord(craftPlayer.getHandle(), ((CraftWorld) craftPlayer.getWorld()).getHandle(),
                null, craftPlayer.getHandle().gameMode);
    }

    public static ServerPlayer ofPlayer(Player player)
    {
        return ((CraftPlayer) player).getHandle();
    }

    public static NmsRecord of(Player player, @Nullable Entity targetEntity)
    {
        if (targetEntity == null) return of(player);

        var craftPlayer = (CraftPlayer) player;
        var craftEntity = (CraftEntity) targetEntity;

        return new NmsRecord(craftPlayer.getHandle(), ((CraftWorld) craftPlayer.getWorld()).getHandle(),
                craftEntity.getHandle(), craftPlayer.getHandle().gameMode);
    }
}