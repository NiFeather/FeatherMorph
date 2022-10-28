package xiamomc.morph.abilities.impl;

import org.jetbrains.annotations.NotNull;
import xiamomc.morph.abilities.MorphAbility;
import xiamomc.morph.storage.skill.ISkillOption;
import xiamomc.morph.storage.skill.NoOpConfiguration;

public abstract class NoOpOptionAbility extends MorphAbility<NoOpConfiguration>
{
    @Override
    public boolean setOption(@NotNull String disguiseIdentifier, NoOpConfiguration option)
    {
        return true;
    }

    private final NoOpConfiguration option = new NoOpConfiguration();

    @Override
    public ISkillOption getOption()
    {
        return option;
    }

    @Override
    public void clearOptions()
    {
    }
}
