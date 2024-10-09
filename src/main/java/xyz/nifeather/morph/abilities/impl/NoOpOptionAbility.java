package xyz.nifeather.morph.abilities.impl;

import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.abilities.MorphAbility;
import xyz.nifeather.morph.skills.options.NoOpConfiguration;

public abstract class NoOpOptionAbility extends MorphAbility<NoOpConfiguration>
{
    @Override
    protected @NotNull NoOpConfiguration createOption()
    {
        return new NoOpConfiguration();
    }
}
