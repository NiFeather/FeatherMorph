package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.api.FeatherMorphAPI;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.DisguiseTypes;
import xyz.nifeather.morph.misc.disguiseProperty.*;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public abstract class PropertyCollection<E extends Entity>
{
    protected final Logger logger = FeatherMorphMain.getInstance().getSLF4JLogger();

    protected final Map<String, SingleProperty<?>> values = new ConcurrentHashMap<>();

    protected void registerSingle(SingleProperty<?>... value)
    {
        for (SingleProperty<?> property : value)
            registerSingle(property);
    }

    protected void registerSingle(SingleProperty<?> value)
    {
        var duplicateValue = values.getOrDefault(value.id(), null);
        if (duplicateValue != null)
            throw new IllegalArgumentException("Already contains a value with ID '%s'".formatted(value.id()));

        values.put(value.id(), value);
    }

    public Map<String, SingleProperty<?>> getRegisteredProperties()
    {
        return Map.copyOf(this.values);
    }

    @Nullable
    protected abstract E tryCastEntity(@Nullable Entity targetEntity);

    /**
     * @return {@code true} If the given entity is available for read by the given {@link DisguiseMeta}
     */
    protected boolean validateEntity(DisguiseMeta disguiseMeta, E entityToSetup)
    {
        return true;
    }

    /**
     * @return {@code true} If the other disguise is available for us to clone properties
     */
    protected boolean validateOtherDisguise(DisguiseState our, DisguiseState other)
    {
        if (other.getDisguiseType() == DisguiseTypes.PLAYER)
            return our.getDisguiseType() == DisguiseTypes.PLAYER;

        return other.getDisguiseIdentifier().equals(our.getDisguiseIdentifier());
    }

    /**
     * Setup Disguise Properties for the given {@link DisguiseState} from the given {@link Entity}.
     * <br>
     * This function will try these steps:
     * <br>
     * 1. If the given entity has a {@link DisguiseState} tracked in {@link MorphManager}, this will call {@link PropertyCollection#validateOtherDisguise(DisguiseState, DisguiseState)} to check
     *    if their disguise is available to clone properties from. Otherwise, this will do nothing and return immediately.
     * <br>
     * 2. If the given entity matches the target type of this PropertyCollection, we will run an additional check in {@link PropertyCollection#validateEntity(DisguiseMeta, Entity)},
     *    thereby other implementations (typically, {@link PlayerPropertyCollection#validateEntity(DisguiseMeta, Player)}) can have additional checks.
     * <br>
     * 3. If all two steps above have failed, this PropertyCollection will then fallback to {@link PropertyCollection#setupDefaultProperties(PropertyHandler)} to set up default properties.
     */
    public final void setupProperties(DisguiseState state, @Nullable Entity targetEntity)
    {
        var theirDisguise = Objects.requireNonNull(FeatherMorphAPI.instance())
                .directAccess()
                .morphManager()
                .getDisguiseStateFor(targetEntity);

        // Clone if the target entity is disguised.
        // If their disguise is not compatible with ours, we don't want to continue cloning from them anyway
        if (theirDisguise != null)
        {
            if (validateOtherDisguise(state, theirDisguise))
                this.setupFromOtherDisguise(state, theirDisguise);

            return;
        }

        var disguiseMeta = new DisguiseMeta(state.getDisguiseIdentifier(), DisguiseTypes.fromId(state.getDisguiseIdentifier()));

        var cast = tryCastEntity(targetEntity);

        if (cast != null && validateEntity(disguiseMeta, cast))
            setupPropertiesFromEntity(state.disguisePropertyHandler(), cast);
        else
            setupDefaultProperties(state.disguisePropertyHandler());
    }

    /**
     * Setup property for the given disguise from the given entity
     *
     * @param propertyHandler The {@link PropertyHandler} for the given disguise
     * @param targetEntity    The targeted entity
     */
    protected abstract void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull E targetEntity);
    protected abstract void setupDefaultProperties(PropertyHandler propertyHandler);

    protected void setupFromOtherDisguise(DisguiseState ourState, DisguiseState theirState)
    {
        var ourHandler = ourState.disguisePropertyHandler();
        var theirHandler = theirState.disguisePropertyHandler();

        theirHandler.copyTo(ourHandler);
    }
}
