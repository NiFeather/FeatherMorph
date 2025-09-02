package xyz.nifeather.morph.abilities.options;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.abilities.ISkillAbilityOptionHandler;
import xyz.nifeather.morph.misc.disguiseProperty.ParseErrorException;
import xyz.nifeather.morph.storage.skill.ISkillAbilityOption;

import java.util.Arrays;
import java.util.Map;

public class PotionEffectOption implements ISkillAbilityOption
{
    /**
     * Read potion effect in Enum Name, rather than NamespacedKey
     */
    public static class LegacyPotionEffectOptionHandler extends PotionEffectOptionHandler
    {
        @Override
        protected PotionEffectType readPotion(String input) throws ParseErrorException
        {
            return Arrays.stream(PotionEffectType.values())
                    .filter(v -> v.getName().equalsIgnoreCase(input))
                    .findFirst().orElseThrow(() -> new ParseErrorException(this.getClass().getSimpleName(), "Not a valid Identifier for string '%s'".formatted(input)));
        }
    }

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
            gsonMap.put("name", option.effectType.key().asString());
            gsonMap.put("duration", option.duration);
            gsonMap.put("amplifier", option.amplifier);
        }

        protected PotionEffectType readPotion(String input) throws ParseErrorException
        {
            var namespaced = NamespacedKey.fromString(input);
            if (namespaced == null)
                throw new ParseErrorException(this.getClass().getSimpleName(), "Not a valid Identifier for string '%s'".formatted(input));

            var effect = Registry.EFFECT.get(namespaced);
            if (effect == null)
                throw new ParseErrorException(this.getClass().getSimpleName(), "Not a valid effect for string '%s'".formatted(input));

            return effect;
        }

        @Override
        public @NotNull PotionEffectOption readOption(@NotNull Map<String, Object> gsonMap) throws ParseErrorException
        {
            String id = utilGetTypedOrThrow("name", gsonMap, String.class);
            int duration = utilGetTypedOrThrow("duration", gsonMap, Number.class).intValue();
            int amplifier = utilGetTypedOrThrow("amplifier", gsonMap, Number.class).intValue();

            PotionEffectType effect = readPotion(id);

            return PotionEffectOption.from(effect, duration, amplifier);
        }
    }

    public static final PotionEffectOptionHandler OPTION_HANDLER = new PotionEffectOptionHandler();
    public static final LegacyPotionEffectOptionHandler LEGACY_OPTION_HANDLER = new LegacyPotionEffectOptionHandler();

    public static PotionEffectOption from(PotionEffectType type, int duration, int amplifier)
    {
        return new PotionEffectOption(type, duration, amplifier);
    }

    public PotionEffectOption(PotionEffectType effectType, int duration, int amplifier)
    {
        this.effectType = effectType;
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
        return duration > 0 && amplifier > -1;
    }

    public final PotionEffectType effectType;
    public final int duration;
    public final int amplifier;
}
