package xyz.nifeather.morph.abilities.options;

import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.abilities.ISkillAbilityOptionHandler;
import xyz.nifeather.morph.misc.disguiseProperty.ParseErrorException;
import xyz.nifeather.morph.storage.skill.ISkillAbilityOption;

import java.util.Map;

public class ReduceDamageOption implements ISkillAbilityOption
{
    public static class ReduceDamageOptionHandler implements ISkillAbilityOptionHandler<ReduceDamageOption>
    {
        @Override
        public Class<ReduceDamageOption> getOptionClass()
        {
            return ReduceDamageOption.class;
        }

        @Override
        public void writeOption(ReduceDamageOption option, @NotNull Map<String, Object> gsonMap)
        {
            gsonMap.put("amount", option.getReduceAmount());
            gsonMap.put("is_percentage", option.isPercentage());
        }

        @Override
        public @NotNull ReduceDamageOption readOption(@NotNull Map<String, Object> gsonMap) throws ParseErrorException
        {
            double amount = utilGetTypedOrThrow("amount", gsonMap, Number.class).doubleValue();
            boolean isPercentage = utilGetTypedOrThrow("is_percentage", gsonMap, Boolean.class);

            return new ReduceDamageOption(amount, isPercentage);
        }
    }

    public static final ReduceDamageOptionHandler OPTION_HANDLER = new ReduceDamageOptionHandler();

    private final double reduceAmount;

    private final boolean isPercentage;

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
    public boolean isValid()
    {
        return !Double.isNaN(reduceAmount);
    }
}
