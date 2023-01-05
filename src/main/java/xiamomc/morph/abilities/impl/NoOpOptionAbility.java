package xiamomc.morph.abilities.impl;

import xiamomc.morph.abilities.MorphAbility;
import xiamomc.morph.storage.skill.ISkillOption;
import xiamomc.morph.storage.skill.NoOpConfiguration;

public abstract class NoOpOptionAbility extends MorphAbility<NoOpConfiguration>
{
    @Override
    protected NoOpConfiguration createOption()
    {
        return new NoOpConfiguration();
    }
}
