package xyz.nifeather.morph.skills.options;

import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.abilities.ISkillAbilityOptionHandler;
import xyz.nifeather.morph.misc.disguiseProperty.ParseErrorException;
import xyz.nifeather.morph.storage.skill.ISkillAbilityOption;

import java.util.Map;

public record DashConfiguration(boolean requireWater, double dashMultiplier, String dashSound) implements ISkillAbilityOption
{
    public static class DashConfigurationOptionHandler implements ISkillAbilityOptionHandler<DashConfiguration>
    {
        @Override
        public void writeOption(DashConfiguration option, @NotNull Map<String, Object> gsonMap)
        {
            gsonMap.put("require_water", option.requireWater());
            gsonMap.put("dash_multiplier", option.dashMultiplier());
            gsonMap.put("dash_sound", option.dashSound());
        }

        @Override
        public Class<DashConfiguration> getOptionClass()
        {
            return DashConfiguration.class;
        }

        @Override
        public @NotNull DashConfiguration readOption(@NotNull Map<String, Object> gsonMap)
                throws ParseErrorException
        {
            var requireWater = utilGetTypedOrThrow("require_water", gsonMap, Boolean.class);
            var dashMultiplier = utilGetTypedOrThrow("dash_multiplier", gsonMap, Number.class).doubleValue();
            var dashSound = utilGetTypedOrThrow("dash_sound", gsonMap, String.class);

            return new DashConfiguration(requireWater, dashMultiplier, dashSound);
        }
    }

    public static final ISkillAbilityOptionHandler<DashConfiguration> OPTION_HANDLER = new DashConfigurationOptionHandler();

    /**
     * 检查此Option是否合法
     *
     * @return 此Option是否合法
     */
    @Override
    public boolean isValid()
    {
        return Double.isFinite(dashMultiplier) && dashSound != null;
    }
}
