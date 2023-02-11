package xiamomc.morph.abilities.options;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
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

    @Expose
    @SerializedName("message_pattern")
    private String messagePattern = "[<who>] <message>";

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
