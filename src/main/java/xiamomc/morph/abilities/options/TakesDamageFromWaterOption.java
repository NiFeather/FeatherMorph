package xiamomc.morph.abilities.options;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.storage.skill.ISkillOption;

import java.util.Map;

public class TakesDamageFromWaterOption implements ISkillOption
{
    public TakesDamageFromWaterOption()
    {
    }

    @Expose
    @SerializedName("damage")
    public double damageAmount = 1d;

    @Override
    public boolean isValid()
    {
        return !Double.isNaN(damageAmount);
    }
}
