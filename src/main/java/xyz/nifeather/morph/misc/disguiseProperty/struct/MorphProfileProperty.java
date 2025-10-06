package xyz.nifeather.morph.misc.disguiseProperty.struct;

import com.destroystokyo.paper.profile.ProfileProperty;
import org.jetbrains.annotations.Nullable;

public record MorphProfileProperty(String name, String value, @Nullable String signature)
{
    public static MorphProfileProperty fromPaperProperty(ProfileProperty property)
    {
        return new MorphProfileProperty(
                property.getName(),
                property.getValue(),
                property.getSignature()
        );
    }
}
