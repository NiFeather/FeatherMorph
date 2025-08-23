package xyz.nifeather.morph.abilities.options;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.abilities.ISkillAbilityOptionHandler;
import xyz.nifeather.morph.misc.disguiseProperty.ParseErrorException;
import xyz.nifeather.morph.storage.skill.ISkillAbilityOption;

import java.util.Map;

public class PotionEffectOption implements ISkillAbilityOption
{
    public static class PotionEffectOptionHandler implements ISkillAbilityOptionHandler<PotionEffectOption>
    {
        @Override
        public Class<PotionEffectOption> getOptionClass()
        {
            return PotionEffectOption.class;
        }

        @Override
        public void writeOption(PotionEffectOption option, @NotNull Map<String, Object> gsonMap)
        {
            gsonMap.put("name", option.effectId);
            gsonMap.put("duration", option.duration);
            gsonMap.put("amplifier", option.amplifier);
        }

        @Override
        public @NotNull PotionEffectOption readOption(@NotNull Map<String, Object> gsonMap) throws ParseErrorException
        {
            String id = utilGetTypedOrThrow("name", gsonMap, String.class);
            int duration = utilGetTypedOrThrow("duration", gsonMap, Number.class).intValue();
            int amplifier = utilGetTypedOrThrow("amplifier", gsonMap, Number.class).intValue();

            var namespaced = NamespacedKey.fromString(id);
            if (namespaced == null)
                throw new ParseErrorException(this.getClass().getSimpleName(), "Not a valid Identifier for string '%s'".formatted(id));

            var effect = Registry.EFFECT.get(namespaced);
            if (effect == null)
                throw new ParseErrorException(this.getClass().getSimpleName(), "Not a valid effect for string '%s'".formatted(id));

            return PotionEffectOption.from(effect, duration, amplifier);
        }
    }

    public static final PotionEffectOptionHandler OPTION_HANDLER = new PotionEffectOptionHandler();

    public static PotionEffectOption from(PotionEffectType type, int duration, int amplifier)
    {
        return new PotionEffectOption(type.getName(), duration, amplifier);
    }

    public PotionEffectOption(String effectId, int duration, int amplifier)
    {
        this.effectId = effectId;
        this.duration = duration;
        this.amplifier = amplifier;
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

    public final String effectId;
    public final int duration;
    public final int amplifier;
}
