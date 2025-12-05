package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Nautilus;
import org.jetbrains.annotations.Nullable;

public class NautilusPropertyCollection extends AbstractNautilusPropertyCollection<Nautilus>
{
    @Override
    protected @Nullable Nautilus tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Nautilus nautilus ? nautilus : null;
    }
}
