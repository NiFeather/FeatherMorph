package xyz.nifeather.morph.abilities.options;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.abilities.ISkillAbilityOptionHandler;
import xyz.nifeather.morph.misc.disguiseProperty.ParseErrorException;
import xyz.nifeather.morph.storage.skill.ISkillAbilityOption;

import java.util.Map;

public class TakesDamageFromWaterOption implements ISkillAbilityOption
{
    public static class TakesDamageFromWaterOptionHandler implements ISkillAbilityOptionHandler<TakesDamageFromWaterOption>
    {
        @Override
        public Class<TakesDamageFromWaterOption> getOptionClass()
        {
            return TakesDamageFromWaterOption.class;
        }

        @Override
        public boolean acceptNullableOptions()
        {
            return true;
        }

        @Override
        public void writeOption(TakesDamageFromWaterOption option, @NotNull Map<String, Object> gsonMap)
        {
            gsonMap.put("damage", option.damageAmount);
        }

        @Override
        public TakesDamageFromWaterOption readOptionNullable(@Nullable Map<String, Object> gsonMap) throws NullPointerException, UnsupportedOperationException, ParseErrorException
        {
            return gsonMap == null
                    ? new TakesDamageFromWaterOption(1d)
                    : readOption(gsonMap);
        }

        @Override
        public @NotNull TakesDamageFromWaterOption readOption(@NotNull Map<String, Object> gsonMap) throws ParseErrorException
        {
            double damage = utilGetTypedOrThrow("damage", gsonMap, Number.class).doubleValue();

            if (!Double.isFinite(damage))
                throw new ParseErrorException(this.getClass().getSimpleName(), "Non-Finite value 'damage'");

            return new TakesDamageFromWaterOption(damage);
        }
    }

    public static final TakesDamageFromWaterOptionHandler OPTION_HANDLER = new TakesDamageFromWaterOptionHandler();

    public TakesDamageFromWaterOption(double val)
    {
        this.damageAmount = val;
    }

    public final double damageAmount;

    @Override
    public boolean isValid()
    {
        return !Double.isNaN(damageAmount);
    }
}
