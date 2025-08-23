package xyz.nifeather.morph.skills.options;

import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.abilities.ISkillAbilityOptionHandler;
import xyz.nifeather.morph.misc.disguiseProperty.ParseErrorException;
import xyz.nifeather.morph.storage.skill.ISkillAbilityOption;

import java.util.Arrays;
import java.util.Map;

public class ProjectileConfiguration implements ISkillAbilityOption
{
    public static class ProjectileOptionHandler implements ISkillAbilityOptionHandler<ProjectileConfiguration>
    {
        @Override
        public Class<ProjectileConfiguration> getOptionClass()
        {
            return ProjectileConfiguration.class;
        }

        @Override
        public void writeOption(ProjectileConfiguration option, @NotNull Map<String, Object> gsonMap)
        {
            gsonMap.put("name", option.entityType());
            gsonMap.put("speed_multiplier", option.getVectorMultiplier());

            gsonMap.put("sound_name", option.getSoundName());
            gsonMap.put("sound_distance", option.getSoundDistance());
            gsonMap.put("warning_sound_name", option.getPreLaunchSoundName());

            gsonMap.put("max_target_distance", option.getDistanceLimit());
            gsonMap.put("delay", option.executeDelay);
        }

        @Override
        public @NotNull ProjectileConfiguration readOption(@NotNull Map<String, Object> gsonMap) throws ParseErrorException
        {
            String name = utilGetTypedOrThrow("name", gsonMap, String.class);
            float speedMultiplier = utilGetTypedOrThrow("speed_multiplier", gsonMap, Number.class).floatValue();

            String soundName = utilGetTypedOrThrow("sound_name", gsonMap, String.class);
            int soundDistance = utilGetTypedOrThrow("sound_distance", gsonMap, Number.class).intValue();
            String preLaunchSoundName = utilGetTypedOrThrow("warning_sound_name", gsonMap, String.class);

            int distanceLimit = utilGetTypedOrThrow("max_target_distance", gsonMap, Number.class).intValue();
            int executeDelay = utilGetTypedOrThrow("delay", gsonMap, Number.class).intValue();

            var entityType = Arrays.stream(EntityType.values())
                    .filter(type -> type.key().asString().equalsIgnoreCase(name))
                    .findFirst()
                    .orElseThrow(() -> new ParseErrorException(this.getClass().getSimpleName(), "No matching entity found for type '%s'".formatted(name)));

            return new ProjectileConfiguration(entityType, speedMultiplier, soundName, soundDistance, distanceLimit, executeDelay)
                    .withWarningSound(preLaunchSoundName);
        }
    }

    public static final ProjectileOptionHandler OPTION_HANDLER = new ProjectileOptionHandler();

    public ProjectileConfiguration(EntityType entityType,
                                   float multiplier,
                                   String soundName, int soundDistance,
                                   int distanceLimit,
                                   int executeDelay)
    {
        this.entityType = entityType;
        this.multiplier = multiplier;
        this.soundName = soundName;
        this.soundDistance = soundDistance;
        this.distanceLimit = distanceLimit;
        this.executeDelay = executeDelay;
    }

    public ProjectileConfiguration(EntityType entityType, float multiplier, String soundName, int soundDistance)
    {
        this(entityType, multiplier, soundName, soundDistance, 0);
    }

    public ProjectileConfiguration(EntityType entityType,
                                   float multiplier,
                                   String soundName, int soundDistance,
                                   int distanceLimit)
    {
        this(entityType, multiplier, soundName, soundDistance, distanceLimit, 0);
    }

    public ProjectileConfiguration withWarningSound(String soundName)
    {
        this.preLaunchSoundName = soundName;
        return this;
    }

    private final EntityType entityType;

    public EntityType entityType()
    {
        return entityType;
    }

    private final float multiplier;

    public float getVectorMultiplier()
    {
        return multiplier;
    }

    private String preLaunchSoundName;

    public String getPreLaunchSoundName()
    {
        return preLaunchSoundName == null ? "" : preLaunchSoundName;
    }

    private final String soundName;

    public String getSoundName()
    {
        return soundName == null ? "" : soundName;
    }

    private final int soundDistance;

    public int getSoundDistance()
    {
        return soundDistance;
    }

    private final int distanceLimit;

    public final int executeDelay;

    public int getDistanceLimit()
    {
        return distanceLimit;
    }

    @Override
    public boolean isValid()
    {
        return true;
    }
}
