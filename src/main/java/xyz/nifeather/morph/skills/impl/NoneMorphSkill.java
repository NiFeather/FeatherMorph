package xyz.nifeather.morph.skills.impl;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.abilities.ISkillAbilityOptionHandler;
import xyz.nifeather.morph.api.morphs.skills.SkillNames;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.ExecutionErrorException;
import xyz.nifeather.morph.skills.MorphSkill;
import xyz.nifeather.morph.skills.options.NoOpConfiguration;

public final class NoneMorphSkill extends MorphSkill<NoOpConfiguration>
{
    public static final NoneMorphSkill instance = new NoneMorphSkill();

    @Override
    public ISkillAbilityOptionHandler<NoOpConfiguration> optionHandler()
    {
        return NoOpConfiguration.OPTION_HANDLER;
    }

    @Override
    public int executeSkill(Player player, DisguiseState state, NoOpConfiguration option) throws ExecutionErrorException
    {
        throw ExecutionErrorException.forMethod("executeSkill")
                .withMessage("Method called for an instance of %s !".formatted(this.getClass().getSimpleName()))
                .create();
    }

    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return SkillNames.NONE;
    }
}
