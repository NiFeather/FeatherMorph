package xiamomc.morph.abilities.options;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.abilities.impl.ChatOverrideAbility;
import xiamomc.morph.storage.skill.ISkillOption;

import java.util.Map;

public class ChatOverrideOption implements ISkillOption
{
    public ChatOverrideOption()
    {
    }

    public ChatOverrideOption(String messagePattern)
    {
        this.messagePattern = messagePattern;
    }

    private String messagePattern;

    public String getMessagePattern()
    {
        return messagePattern;
    }

    @Override
    public Map<String, Object> toMap()
    {
        var map = new Object2ObjectOpenHashMap<String, Object>();

        map.put("message_pattern", messagePattern);

        return map;
    }

    @Override
    public @Nullable ISkillOption fromMap(@Nullable Map<String, Object> map)
    {
        if (map == null) return null;

        var instance = new ChatOverrideOption();

        instance.messagePattern = "" + map.getOrDefault("message_pattern", null);

        return instance;
    }
}
