package xyz.nifeather.morph.skills.options;

import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.abilities.ISkillAbilityOptionHandler;
import xyz.nifeather.morph.misc.disguiseProperty.ParseErrorException;
import xyz.nifeather.morph.storage.skill.ISkillAbilityOption;

import java.util.Map;

public class ExplosionConfiguration implements ISkillAbilityOption
{
    public static class ExplosionOptionHandler implements ISkillAbilityOptionHandler<ExplosionConfiguration>
    {
        @Override
        public Class<ExplosionConfiguration> getOptionClass()
        {
            return ExplosionConfiguration.class;
        }

        @Override
        public void writeOption(ExplosionConfiguration option, @NotNull Map<String, Object> gsonMap)
        {
            gsonMap.put("kills_self", option.killsSelf());
            gsonMap.put("strength", option.getStrength());
            gsonMap.put("sets_fire", option.setsFire());
            gsonMap.put("delay", option.executeDelay);
            gsonMap.put("primed_sound", option.getPrimedSound());
        }

        @Override
        public @NotNull ExplosionConfiguration readOption(@NotNull Map<String, Object> gsonMap) throws ParseErrorException
        {
            boolean killsSelf = utilGetTypedOrThrow("kills_self", gsonMap, Boolean.class);
            int strength = utilGetTypedOrThrow("strength", gsonMap, Number.class).intValue();
            boolean setsFire = utilGetTypedOrThrow("sets_fire", gsonMap, Boolean.class);
            int delay = utilGetTypedOrThrow("delay", gsonMap, Number.class).intValue();
            String primedSound = utilGetTypedOrThrow("primed_sound", gsonMap, String.class);

            return new ExplosionConfiguration(killsSelf, strength, setsFire, delay, primedSound);
        }
    }

    public static final ExplosionOptionHandler OPTION_HANDLER = new ExplosionOptionHandler();

    public ExplosionConfiguration(boolean killsSelf, int strength, boolean setsFire, int delay, String primedSound)
    {
        this.killsSelf = killsSelf;
        this.strength = strength;
        this.setsFire = setsFire;
        this.executeDelay = delay;

        this.primedSound = primedSound;
    }

    private final boolean killsSelf;

    public int executeDelay;

    public boolean killsSelf()
    {
        return killsSelf;
    }

    private final int strength;

    public int getStrength()
    {
        return strength;
    }

    private final boolean setsFire;

    public boolean setsFire()
    {
        return setsFire;
    }

    public final String primedSound;

    public String getPrimedSound()
    {
        return primedSound == null ? "" : primedSound;
    }

    @Override
    public boolean isValid()
    {
        return true;
    }
}
