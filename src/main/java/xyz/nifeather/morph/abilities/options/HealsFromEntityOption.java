package xyz.nifeather.morph.abilities.options;

import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.abilities.ISkillAbilityOptionHandler;
import xyz.nifeather.morph.misc.disguiseProperty.ParseErrorException;
import xyz.nifeather.morph.storage.skill.ISkillAbilityOption;

import java.util.Arrays;
import java.util.Map;

public class HealsFromEntityOption implements ISkillAbilityOption
{
    public static class HealsFromEntityOptionHandler implements ISkillAbilityOptionHandler<HealsFromEntityOption>
    {
        @Override
        public Class<HealsFromEntityOption> getOptionClass()
        {
            return HealsFromEntityOption.class;
        }

        @Override
        public void writeOption(HealsFromEntityOption option, @NotNull Map<String, Object> gsonMap)
        {
            gsonMap.put("max_percentage", option.maxPercentage);
            gsonMap.put("damage_when_destroyed", option.damageWhenDestroyed);
            gsonMap.put("amount", option.healAmount);
            gsonMap.put("distance", option.distance);
            gsonMap.put("entity_type", option.entityIdentifier);
        }

        private void throwIfNotFinite(String name, float f) throws ParseErrorException
        {
            if (!Float.isFinite(f))
                throw new ParseErrorException(this.getClass().getSimpleName(), "Non-Finite value '%s'".formatted(name));
        }

        private void throwIfNotFinite(String name, double f) throws ParseErrorException
        {
            if (!Double.isFinite(f))
                throw new ParseErrorException(this.getClass().getSimpleName(), "Non-Finite value '%s'".formatted(name));
        }

        @Override
        public @NotNull HealsFromEntityOption readOption(@NotNull Map<String, Object> gsonMap) throws ParseErrorException
        {
            double maxPercentage = utilGetTypedOrThrow("max_percentage", gsonMap, Number.class).doubleValue();
            float damageWhenDestroyed = utilGetTypedOrThrow("damage_when_destroyed", gsonMap, Number.class).floatValue();
            double amount = utilGetTypedOrThrow("amount", gsonMap, Number.class).doubleValue();
            double distance = utilGetTypedOrThrow("distance", gsonMap, Number.class).doubleValue();
            String entityID = utilGetTypedOrThrow("entity_type", gsonMap, String.class);

            throwIfNotFinite("max_percentage", maxPercentage);
            throwIfNotFinite("damage_when_destroyed", damageWhenDestroyed);
            throwIfNotFinite("amount", amount);
            throwIfNotFinite("distance", distance);

            EntityType entityType = Arrays.stream(EntityType.values())
                    .filter(t -> t.key().asString().equals(entityID))
                    .findFirst()
                    .orElseThrow(() -> new ParseErrorException(this.getClass().getSimpleName(), "No entity matched for type '%s'".formatted(entityID)));

            return new HealsFromEntityOption(maxPercentage, damageWhenDestroyed, amount, distance, entityType);
        }
    }

    public static final HealsFromEntityOptionHandler OPTION_HANDLER = new HealsFromEntityOptionHandler();

    @Override
    public boolean isValid()
    {
        return maxPercentage <= 1d && Float.isFinite(damageWhenDestroyed)
                && Double.isFinite(healAmount) && Double.isFinite(distance) && entityIdentifier != null;
    }

    public final double maxPercentage;
    public final float damageWhenDestroyed;
    public final double healAmount;
    public final double distance;
    public final String entityIdentifier;
    public final EntityType entityType;

    public HealsFromEntityOption(double maxPercentage, float damageWhenDestroyed, double healAmount, double distance, EntityType entityType)
    {
        this.maxPercentage = maxPercentage;
        this.damageWhenDestroyed = damageWhenDestroyed;
        this.healAmount = healAmount;
        this.distance = distance;
        this.entityIdentifier = entityType.key().asString();
        this.entityType = entityType;
    }
}
