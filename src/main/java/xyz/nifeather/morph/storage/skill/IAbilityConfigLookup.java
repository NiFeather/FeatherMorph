package xyz.nifeather.morph.storage.skill;

import org.jetbrains.annotations.Nullable;

public interface IAbilityConfigLookup
{
    @Nullable
    public <X> X lookupAbilityConfig(String identifier, Class<X> expectedClass);

    public void setAbilityConfig(String identifier, Object config);
}
