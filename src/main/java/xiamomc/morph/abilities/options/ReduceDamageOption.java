package xiamomc.morph.abilities.options;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.storage.skill.ISkillOption;

import java.util.Map;

public class ReduceDamageOption implements ISkillOption
{
    private double reduceAmount = 0d;
    private boolean isPercentage = false;

    public ReduceDamageOption()
    {
    }

    public ReduceDamageOption(double amount)
    {
        this(amount, false);
    }

    public ReduceDamageOption(double amount, boolean isPercentage)
    {
        this.reduceAmount = amount;
        this.isPercentage = isPercentage;
    }

    public double getReduceAmount()
    {
        return reduceAmount;
    }

    public boolean isPercentage()
    {
        return isPercentage;
    }

    @Override
    public Map<String, Object> toMap()
    {
        var map = new Object2ObjectOpenHashMap<String, Object>();

        map.put("amount", reduceAmount);
        map.put("is_percentage", isPercentage);

        return map;
    }

    protected ReduceDamageOption createInstance()
    {
        return new ReduceDamageOption();
    }

    @Override
    public @Nullable ISkillOption fromMap(@Nullable Map<String, Object> map)
    {
        if (map == null) return null;

        var instance = createInstance();

        instance.isPercentage = (boolean) map.get("is_percentage");
        instance.reduceAmount = (double) map.get("amount");

        return instance;
    }
}
