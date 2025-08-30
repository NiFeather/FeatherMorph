package xyz.nifeather.morph.abilities.options;

import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.abilities.ISkillAbilityOptionHandler;
import xyz.nifeather.morph.misc.disguiseProperty.ParseErrorException;
import xyz.nifeather.morph.storage.skill.ISkillAbilityOption;

import java.util.Map;

public class ExtraAirOption implements ISkillAbilityOption
{
    public static class ExtraAirOptionHandler implements ISkillAbilityOptionHandler<ExtraAirOption>
    {
        @Override
        public void writeOption(ExtraAirOption option, @NotNull Map<String, Object> gsonMap)
        {
            gsonMap.put("maximum_air", option.maxAir);
        }

        @Override
        public Class<ExtraAirOption> getOptionClass()
        {
            return ExtraAirOption.class;
        }

        @Override
        public @NotNull ExtraAirOption readOption(@NotNull Map<String, Object> gsonMap) throws ParseErrorException
        {
            var value = utilGetTypedOrThrow("maximum_air", gsonMap, Number.class).intValue();
            return new ExtraAirOption(value);
        }
    }

    public static final ExtraAirOptionHandler OPTION_HANDLER = new ExtraAirOptionHandler();

    public ExtraAirOption(int maxAir)
    {
        this.maxAir = maxAir;
    }

    protected final int maxAir;

    public int maxAir()
    {
        return maxAir;
    }

    /**
     * 检查此Option是否合法
     *
     * @return 此Option是否合法
     */
    @Override
    public boolean isValid()
    {
        return maxAir > 0;
    }
}
