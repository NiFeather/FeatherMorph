package xyz.nifeather.morph.backends.server;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Exceptions.NullDependencyException;
import xyz.nifeather.morph.backends.DisguiseWrapper;
import xyz.nifeather.morph.backends.EventWrapper;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types.AgeableMobWatcher;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.backends.server.renderer.utilties.WatcherUtils;
import xyz.nifeather.morph.misc.AnimationNames;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.PlayerPropertyCollection;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ServerDisguiseWrapper extends EventWrapper<ServerDisguise>
{
    private final ServerBackend backend;

    public ServerDisguiseWrapper(@NotNull ServerDisguise instance, ServerBackend backend)
    {
        super(instance, backend);
        this.backend = backend;
    }

    @Override
    public CompoundTag getCompound()
    {
        var tagCopy = new CompoundTag();

        if (bindingWatcher != null)
            tagCopy.merge(WatcherUtils.buildCompoundFromWatcher(bindingWatcher));

        return tagCopy;
    }

    @Override
    public @NotNull UUID getVirtualEntityUUID()
    {
        var uuid = bindingWatcher == null ? null : bindingWatcher.readEntryOrThrow(CustomEntries.SPAWN_UUID);
        return Objects.requireNonNull(uuid, "VirtualEntityUUID is not set for an instance of ServerDisguiseWrapper");
    }

    @Override
    public void setServerSelfView(boolean enabled)
    {
    }

    @Override
    public EntityType getEntityType()
    {
        return instance.type;
    }

    @Override
    public ServerDisguise copyInstance()
    {
        return instance.clone();
    }

    @Override
    public DisguiseWrapper<ServerDisguise> clone()
    {
        var newInstance = cloneFromExternal(this, (ServerBackend) getBackend());
        newInstance.disguiseProperties.putAll(this.disguiseProperties);
        return newInstance;
    }

    public static ServerDisguiseWrapper cloneFromExternal(DisguiseWrapper<?> other, ServerBackend backend)
    {
        var newInstance = new ServerDisguiseWrapper(new ServerDisguise(other.getEntityType()), backend);

        newInstance.disguiseProperties.putAll(other.getProperties());

        return newInstance;
    }

    private final Map<SingleProperty<?>, Object> disguiseProperties = new ConcurrentHashMap<>();

    @Override
    public <X> void writeProperty(SingleProperty<X> property, X value)
    {
        disguiseProperties.put(property, value);

        if (bindingWatcher == null) return;
        applyProperty(property, value);
    }

    protected <X> void applyProperty(SingleProperty<X> property, X value)
    {
        if (!property.id().startsWith("wrapper_"))
            bindingWatcher.writeProperty(property, value);

        switch (property.id())
        {
            case PropertyNames.PLAYER_SKIN -> bindingWatcher.writeEntry(CustomEntries.PROFILE, (GameProfile) value);
        }
    }

    @Override
    public <X> @NotNull X readProperty(SingleProperty<X> property)
    {
        return this.readPropertyOr(property, property.defaultVal());
    }

    @Override
    public <X> X readPropertyOr(SingleProperty<X> property, X defaultVal)
    {
        return (X) disguiseProperties.getOrDefault(property, defaultVal);
    }

    @Override
    public <X> X readPropertyOrThrow(SingleProperty<X> property)
    {
        var val = disguiseProperties.getOrDefault(property, null);
        if (val == null) throw new NullDependencyException("The requested property '%s' was not found in %s".formatted(property.id(), this));

        return (X) val;
    }

    @Override
    public Map<SingleProperty<?>, Object> getProperties()
    {
        return new Object2ObjectOpenHashMap<>(this.disguiseProperties);
    }

    @Override
    public void setDisguiseName(String name)
    {
        super.setDisguiseName(name);

        if (bindingWatcher != null)
            bindingWatcher.writeEntry(CustomEntries.DISGUISE_NAME, name);
    }

    @Override
    public boolean isBaby()
    {
        if (!(bindingWatcher instanceof AgeableMobWatcher))
            return false;

        return bindingWatcher.readOr(ValueIndex.AGEABLE_MOB.IS_BABY, false);
    }

    @Override
    public void postBuildDisguise(DisguiseState state, @Nullable Entity targetEntity)
    {
    }

    @Override
    public void update()
    {
        if (bindingWatcher != null)
            bindingWatcher.update();
    }

    private boolean aggressive;

    @Override
    public void setAggressive(boolean aggressive)
    {
        super.setAggressive(aggressive);

        this.aggressive = aggressive;
        bindingWatcher.writeEntry(CustomEntries.IS_AGGRESSIVE, aggressive);
    }

    @Override
    public void playAttackAnimation()
    {
        super.playAttackAnimation();
        bindingWatcher.writeEntry(CustomEntries.ATTACK_ANIMATION, true);
    }

    private Player bindingPlayer;

    public Player getBindingPlayer()
    {
        return bindingPlayer;
    }

    private SingleWatcher bindingWatcher;

    @Nullable
    public SingleWatcher getBindingWatcher()
    {
        return bindingWatcher;
    }

    public void setRenderParameters(@NotNull Player newBinding, @NotNull SingleWatcher bindingWatcher)
    {
        Objects.requireNonNull(bindingWatcher, "Null Watcher!");

        bindingPlayer = newBinding;

        if (this.bindingWatcher != null)
        {
            this.bindingWatcher.dispose();
            this.bindingWatcher = null;
        }

        this.bindingWatcher = bindingWatcher;
        refreshRegistry(bindingWatcher);
    }

    private void refreshRegistry(@NotNull SingleWatcher bindingWatcher)
    {
        this.disguiseProperties.forEach((property, value) -> applyProperty((SingleProperty<Object>) property, value));

        if (getEntityType() == EntityType.PLAYER)
        {
            var properties = DisguiseProperties.INSTANCE.getOrThrow(PlayerPropertyCollection.class);
            var profileOptional = Optional.ofNullable(readPropertyOr(properties.SKIN, null));
            profileOptional.ifPresent(p -> bindingWatcher.writeEntry(CustomEntries.PROFILE, p));
        }

        if (bindingWatcher.getEntityType() == EntityType.GHAST)
            bindingWatcher.writePersistent(ValueIndex.GHAST.CHARGING, aggressive);
    }

    @Override
    public void playAnimation(String animationId)
    {
        if (bindingWatcher != null)
            bindingWatcher.writeEntry(CustomEntries.ANIMATION, animationId);
    }

    @Override
    public void onPlayerJoin(Player newInstance)
    {
        if (bindingWatcher == null)
            return;

        this.bindingWatcher.writeEntry(CustomEntries.SPAWN_ID, newInstance.getEntityId());
        this.bindingPlayer = newInstance;

        if (bindingWatcher.readEntryOrDefault(CustomEntries.WARDEN_VANISHED, false))
            bindingWatcher.writeEntry(CustomEntries.ANIMATION, AnimationNames.APPEAR);
    }
}
