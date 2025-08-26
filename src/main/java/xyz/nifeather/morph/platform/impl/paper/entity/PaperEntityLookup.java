package xyz.nifeather.morph.platform.impl.paper.entity;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.platform.entity.IPlatformEntity;
import xyz.nifeather.morph.platform.entity.IPlatformEntityLookup;
import xyz.nifeather.morph.platform.entity.IPlatformPlayer;

public class PaperEntityLookup implements IPlatformEntityLookup<LivingEntity, Player>
{
    @Override
    public @Nullable IPlatformPlayer getPlatformPlayer(Player player)
    {
        if (player == null) return null;

        return new PaperPlayer(player);
    }

    @Override
    public Player getNativePlayer(@Nullable IPlatformPlayer platformPlayer)
    {
        return platformPlayer == null ? null : ((PaperPlayer) platformPlayer).handle();
    }

    @Override
    public PaperEntity getPlatformEntity(@Nullable LivingEntity entity)
    {
        if (entity == null) return null;
        if (entity instanceof Player player)
            return (PaperPlayer) getPlatformPlayer(player);

        return new PaperEntity(entity);
    }

    @Override
    public LivingEntity getNativeEntity(@Nullable IPlatformEntity platformEntity)
    {
        return platformEntity == null ? null : ((PaperEntity)platformEntity).getHandle();
    }
}
