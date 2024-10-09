package xyz.nifeather.morph.abilities.options;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.potion.PotionEffectType;
import xyz.nifeather.morph.storage.skill.ISkillOption;

public class PotionEffectOption implements ISkillOption
{
    public static PotionEffectOption from(PotionEffectType type, int duration, int amplifier)
    {
        var instance = new PotionEffectOption();
        instance.effectId = type.getName();
        instance.duration = duration;
        instance.amplifier = amplifier;

        return instance;
    }

    /**
     * 检查此Option是否合法
     *
     * @return 此Option是否合法
     */
    @Override
    public boolean isValid()
    {
        return effectId != null && !effectId.isEmpty() && duration > 0 && amplifier > -1;
    }

    @Expose
    @SerializedName("name")
    public String effectId;

    @Expose
    public int duration;

    @Expose
    public int amplifier;
}
