package xyz.nifeather.morph.misc.mobs.goal.handles;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;

import java.util.Optional;

public interface IEntityGoalHandle<M extends Mob>
{
    Optional<M> tryCast(Entity entity);
    void apply(M mob);
}