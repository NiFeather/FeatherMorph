package xyz.nifeather.morph.skills.options;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.abilities.ISkillAbilityOptionHandler;
import xyz.nifeather.morph.misc.disguiseProperty.ParseErrorException;
import xyz.nifeather.morph.storage.skill.ISkillAbilityOption;

import java.util.Map;

public class NoOpConfiguration implements ISkillAbilityOption
{
    public static class NoOpOptionHandler implements ISkillAbilityOptionHandler<NoOpConfiguration>
    {
        @Override
        public Class<NoOpConfiguration> getOptionClass()
        {
            return NoOpConfiguration.class;
        }

        @Override
        public boolean acceptNullableOptions()
        {
            return true;
        }

        @Override
        public void writeOption(NoOpConfiguration option, @NotNull Map<String, Object> gsonMap)
        {
        }

        @Override
        public NoOpConfiguration readOptionNullable(@Nullable Map<String, Object> gsonMap) throws NullPointerException, UnsupportedOperationException, ParseErrorException
        {
            return NoOpConfiguration.instance;
        }

        @Override
        public @NotNull NoOpConfiguration readOption(@NotNull Map<String, Object> gsonMap) throws ParseErrorException
        {
            return NoOpConfiguration.instance;
        }
    }

    public static final NoOpOptionHandler OPTION_HANDLER = new NoOpOptionHandler();

    public static NoOpConfiguration instance = new NoOpConfiguration();

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
