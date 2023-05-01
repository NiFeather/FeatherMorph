package xiamomc.morph.abilities.impl;

import org.jetbrains.annotations.NotNull;
import xiamomc.morph.abilities.MorphAbility;
import xiamomc.morph.skills.options.NoOpConfiguration;

public abstract class NoOpOptionAbility extends MorphAbility<NoOpConfiguration>
{
    @Override
    protected @NotNull NoOpConfiguration createOption()
    {
        return new NoOpConfiguration();
    }
}
