package xyz.nifeather.morph.skills.options;

import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.abilities.ISkillAbilityOptionHandler;
import xyz.nifeather.morph.misc.disguiseProperty.ParseErrorException;
import xyz.nifeather.morph.storage.skill.ISkillAbilityOption;

import java.util.Map;

public class TeleportConfiguration implements ISkillAbilityOption
{
    public static class TeleportOptionHandler implements ISkillAbilityOptionHandler<TeleportConfiguration>
    {
        @Override
        public Class<TeleportConfiguration> getOptionClass()
        {
            return TeleportConfiguration.class;
        }

        @Override
        public void writeOption(TeleportConfiguration option, @NotNull Map<String, Object> gsonMap)
        {
            gsonMap.put("max_distance", option.getMaxDistance());
        }

        @Override
        public @NotNull TeleportConfiguration readOption(@NotNull Map<String, Object> gsonMap) throws ParseErrorException
        {
            int maxDistance = utilGetTypedOrThrow("max_distance", gsonMap, Number.class).intValue();
            return new TeleportConfiguration(maxDistance);
        }
    }

    public static TeleportOptionHandler OPTION_HANDLER = new TeleportOptionHandler();

    public TeleportConfiguration(int maxDistance)
    {
        this.maxDistance = maxDistance;
    }

    private final int maxDistance;

    public int getMaxDistance()
    {
        return maxDistance;
    }

    @Override
    public boolean isValid()
    {
        return true;
    }
}
