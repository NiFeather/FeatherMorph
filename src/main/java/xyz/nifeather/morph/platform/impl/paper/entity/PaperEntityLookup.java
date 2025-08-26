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
        return new PaperPlayer(player);
    }

    @Override
    public Player getNativePlayer(@Nullable IPlatformPlayer platformPlayer)
    {
        return ((PaperPlayer) platformPlayer).handle();
    }

    @Override
    public PaperEntity getPlatformEntity(LivingEntity entity)
    {
        return new PaperEntity(entity);
    }

    @Override
    public LivingEntity getNativeEntity(@Nullable IPlatformEntity platformEntity)
    {
        return ((PaperEntity)platformEntity).getHandle();
    }
}
