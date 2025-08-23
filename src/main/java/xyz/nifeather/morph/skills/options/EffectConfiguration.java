package xyz.nifeather.morph.skills.options;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.abilities.ISkillAbilityOptionHandler;
import xyz.nifeather.morph.misc.disguiseProperty.ParseErrorException;
import xyz.nifeather.morph.storage.skill.ISkillAbilityOption;

import java.util.Map;

public class EffectConfiguration implements ISkillAbilityOption
{
    public static class ApplyEffectSkillConfigurationHandler implements ISkillAbilityOptionHandler<EffectConfiguration>
    {
        @Override
        public Class<EffectConfiguration> getOptionClass()
        {
            return EffectConfiguration.class;
        }

        @Override
        public void writeOption(EffectConfiguration option, @NotNull Map<String, Object> gsonMap)
        {
            gsonMap.put("name", option.getName());
            gsonMap.put("multiplier", option.getMultiplier());
            gsonMap.put("duration", option.getDuration());

            gsonMap.put("acquires_water", option.acquiresWater());
            gsonMap.put("show_guardian", option.showGuardian());

            gsonMap.put("sound", option.getSoundName());
            gsonMap.put("sound_distance", option.getSoundDistance());
            gsonMap.put("apply_distance", option.getApplyDistance());
        }

        @Override
        public @NotNull EffectConfiguration readOption(@NotNull Map<String, Object> gsonMap) throws ParseErrorException
        {
            String name = utilGetTypedOrThrow("name", gsonMap, String.class);
            int multiplier = utilGetTypedOrThrow("multiplier", gsonMap, Number.class).intValue();
            int duration = utilGetTypedOrThrow("duration", gsonMap, Number.class).intValue();

            boolean acquiresWater = utilGetTypedOrThrow("acquires_water", gsonMap, Boolean.class);
            boolean showGuardian = utilGetTypedOrThrow("show_guardian", gsonMap, Boolean.class);

            String soundName = utilGetTypedOrNull("sound", gsonMap, String.class);
            int soundDistance = utilGetTypedOrThrow("sound_distance", gsonMap, Number.class).intValue();
            int applyDistance = utilGetTypedOrThrow("apply_distance", gsonMap, Number.class).intValue();

            return new EffectConfiguration(name, multiplier, duration, acquiresWater, showGuardian, soundName, soundDistance, applyDistance);
        }
    }

    public static final ApplyEffectSkillConfigurationHandler OPTION_HANDLER = new ApplyEffectSkillConfigurationHandler();

    public EffectConfiguration(String name, int multiplier, int duration,
                               boolean requiresWater, boolean showGuardian,
                               @Nullable String soundName, int soundDistance, int applyDistance)
    {
        this.name = name;
        this.multiplier = multiplier;
        this.duration = duration;
        this.acquiresWater = requiresWater;
        this.showGuardian = showGuardian;
        this.soundName = soundName;
        this.soundDistance = soundDistance;
        this.applyDistance = applyDistance;
    }

    //效果名称
    private final String name;

    public String getName()
    {
        return name;
    }

    private final int multiplier;

    public int getMultiplier()
    {
        return multiplier;
    }

    private final int duration;

    public int getDuration()
    {
        return duration;
    }

    private final boolean acquiresWater;

    public boolean acquiresWater()
    {
        return acquiresWater;
    }

    private final boolean showGuardian;

    public boolean showGuardian()
    {
        return showGuardian;
    }

    @Nullable
    private final String soundName;

    @NotNull
    public String getSoundName()
    {
        return soundName == null ? "" : soundName;
    }

    private final int soundDistance;

    private final int applyDistance;

    public int getApplyDistance()
    {
        return applyDistance;
    }

    public int getSoundDistance()
    {
        return soundDistance;
    }

    /**
     * 检查此Option是否合法
     *
     * @return 此Option是否合法
     */
    @Override
    public boolean isValid()
    {
        return true;
    }
}
