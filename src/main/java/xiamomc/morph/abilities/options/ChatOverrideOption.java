package xiamomc.morph.abilities.options;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.storage.skill.ISkillOption;

public class ChatOverrideOption implements ISkillOption
{
    public ChatOverrideOption()
    {
    }

    public ChatOverrideOption(@Nullable String messagePattern)
    {
        this.messagePattern = messagePattern;
    }

    @Expose
    @Nullable
    @SerializedName("message_pattern")
    private String messagePattern;

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
