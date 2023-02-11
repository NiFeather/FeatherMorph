package xiamomc.morph.abilities.impl;

import xiamomc.morph.abilities.MorphAbility;
import xiamomc.morph.skills.options.NoOpConfiguration;

public abstract class NoOpOptionAbility extends MorphAbility<NoOpConfiguration>
{
    @Override
    protected NoOpConfiguration createOption()
    {
        return new NoOpConfiguration();
    }
}
