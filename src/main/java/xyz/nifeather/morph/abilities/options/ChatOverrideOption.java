package xyz.nifeather.morph.abilities.options;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.abilities.ISkillAbilityOptionHandler;
import xyz.nifeather.morph.misc.disguiseProperty.ParseErrorException;
import xyz.nifeather.morph.storage.skill.ISkillAbilityOption;

import java.util.Map;

public class ChatOverrideOption implements ISkillAbilityOption
{
    public static class ChatOverrideOptionHandler implements ISkillAbilityOptionHandler<ChatOverrideOption>
    {
        @Override
        public Class<ChatOverrideOption> getOptionClass()
        {
            return ChatOverrideOption.class;
        }

        @Override
        public boolean acceptNullableOptions()
        {
            return true;
        }

        @Override
        public ChatOverrideOption readOptionNullable(@Nullable Map<String, Object> gsonMap) throws NullPointerException, UnsupportedOperationException, ParseErrorException
        {
            return gsonMap == null
                    ? new ChatOverrideOption(null)
                    : readOption(gsonMap);
        }

        @Override
        public void writeOption(ChatOverrideOption option, @NotNull Map<String, Object> gsonMap)
        {
            gsonMap.put("message_pattern", option.messagePattern);
        }

        @Override
        public @NotNull ChatOverrideOption readOption(@NotNull Map<String, Object> gsonMap) throws ParseErrorException
        {
            return new ChatOverrideOption(utilGetTypedOrThrow("message_pattern", gsonMap, String.class));
        }
    }

    public static final ChatOverrideOptionHandler OPTION_HANDLER = new ChatOverrideOptionHandler();

    public ChatOverrideOption(@Nullable String messagePattern)
    {
        this.messagePattern = messagePattern;
    }

    @Nullable
    private final String messagePattern;

    @Nullable
    public String getMessagePattern()
    {
        return messagePattern;
    }

    @Override
    public boolean isValid()
    {
        return true;
    }
}
