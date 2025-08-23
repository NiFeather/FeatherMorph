package xyz.nifeather.morph.abilities.options;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.abilities.ISkillAbilityOptionHandler;
import xyz.nifeather.morph.misc.disguiseProperty.ParseErrorException;
import xyz.nifeather.morph.storage.skill.ISkillAbilityOption;

import java.util.Map;

public class DryoutAbilityOption implements ISkillAbilityOption
{
    public static class DryoutOptionHandler implements ISkillAbilityOptionHandler<DryoutAbilityOption>
    {
        @Override
        public Class<DryoutAbilityOption> getOptionClass()
        {
            return DryoutAbilityOption.class;
        }

        @Override
        public void writeOption(DryoutAbilityOption option, @NotNull Map<String, Object> gsonMap)
        {
            gsonMap.put("include_rain", option.includeRain);
        }

        @Override
        public boolean acceptNullableOptions()
        {
            return true;
        }

        @Override
        public DryoutAbilityOption readOptionNullable(@Nullable Map<String, Object> gsonMap) throws NullPointerException, UnsupportedOperationException, ParseErrorException
        {
            return gsonMap == null
                    ? new DryoutAbilityOption(true)
                    : readOption(gsonMap);
        }

        @Override
        public @NotNull DryoutAbilityOption readOption(@NotNull Map<String, Object> gsonMap) throws ParseErrorException
        {
            var includeRain = utilGetTypedOrThrow("include_rain", gsonMap, Boolean.class);
            return new DryoutAbilityOption(includeRain);
        }
    }

    public static final DryoutOptionHandler OPTION_HANDLER = new DryoutOptionHandler();

    public DryoutAbilityOption(boolean includeRain)
    {
        this.includeRain = includeRain;
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

    /**
     * 是否在雨中也会脱水？
     */
    public final boolean includeRain;
}
