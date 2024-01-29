package xiamomc.morph.backends.server.renderer.network.registries;

import org.bukkit.entity.EntityType;

public record RegisterParameters(EntityType entityType, String playerDisguiseName)
{
}
