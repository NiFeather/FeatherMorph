package xyz.nifeather.morph.misc.disguiseProperty.struct;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public record MorphResolvableProfileStruct(@Nullable UUID id, @Nullable String name, List<String> properties)
{
    public boolean isStatic()
    {
        return id != null && name != null && !properties.isEmpty();
    }

    public boolean dynamic()
    {
        return !isStatic();
    }
}
