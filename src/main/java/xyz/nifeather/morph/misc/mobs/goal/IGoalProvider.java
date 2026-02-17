package xyz.nifeather.morph.misc.mobs.goal;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;

import java.util.Optional;

public interface IGoalProvider<M extends Mob> extends IGoalSupplier<M>
{
    Optional<M> tryCast(Entity entity);
}
