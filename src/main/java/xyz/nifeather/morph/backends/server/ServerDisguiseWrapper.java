package xyz.nifeather.morph.backends.server;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import xiamomc.pluginbase.Exceptions.NullDependencyException;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.backends.DisguiseWrapper;
import xyz.nifeather.morph.backends.EventWrapper;
import xyz.nifeather.morph.backends.WrapperEvent;
import xyz.nifeather.morph.backends.WrapperProperties;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types.AgeableMobWatcher;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.backends.server.renderer.utilties.WatcherUtils;
import xyz.nifeather.morph.misc.AnimationNames;
import xyz.nifeather.morph.misc.DisguiseEquipment;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.OffTreeProperties;

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
        this.writeProperty(OffTreeProperties.FAKE_EQUIPMENT, new DisguiseEquipment());
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

    private static final Logger logger = FeatherMorphMain.getInstance().getSLF4JLogger();

    @Override
    public EntityEquipment getFakeEquipments()
    {
        return this.readPropertyOrThrow(OffTreeProperties.FAKE_EQUIPMENT);
    }

    @Override
    public void setFakeEquipments(@NotNull EntityEquipment value)
    {
        var newEquipment = new DisguiseEquipment();
        newEquipment.setArmorContents(value.getArmorContents());
        newEquipment.setHandItems(value.getItemInMainHand(), value.getItemInOffHand());

        this.writeProperty(OffTreeProperties.FAKE_EQUIPMENT, newEquipment);
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
        newInstance.writeProperty(OffTreeProperties.FAKE_EQUIPMENT, this.readPropertyOrThrow(OffTreeProperties.FAKE_EQUIPMENT));

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

        if (!property.id().startsWith("wrapper_") && bindingWatcher != null)
            bindingWatcher.writeProperty(property, value);
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
    public void applySkin(@NotNull GameProfile profile)
    {
        if (this.getEntityType() != EntityType.PLAYER) return;

        writeProperty(WrapperProperties.PROFILE, Optional.of(profile));

        if (bindingWatcher != null)
            bindingWatcher.writeEntry(CustomEntries.PROFILE, profile);

        callEvent(WrapperEvent.SKIN_SET, profile);
    }

    @Override
    public void onPostConstructDisguise(DisguiseState state, @Nullable Entity targetEntity)
    {
    }

    @Override
    public void update(DisguiseState state, Player player)
    {
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

        refreshRegistry(newBinding, bindingWatcher);

        this.bindingWatcher = bindingWatcher;
    }

    private void refreshRegistry(@NotNull Player bindingPlayer, @NotNull SingleWatcher bindingWatcher)
    {
        this.disguiseProperties.forEach((property, value) ->
        {
            bindingWatcher.writeProperty((SingleProperty<Object>) property, value);
        });

        if (getEntityType() == EntityType.PLAYER)
        {
            var profileOptional = readProperty(WrapperProperties.PROFILE);
            profileOptional.ifPresent(p -> bindingWatcher.writeEntry(CustomEntries.PROFILE, p));
        }

        bindingWatcher.writeEntry(CustomEntries.DISPLAY_FAKE_EQUIPMENT, readProperty(OffTreeProperties.DISPLAY_FAKE_EQUIPMENT));
        bindingWatcher.writeEntry(CustomEntries.EQUIPMENT, readPropertyOrThrow(OffTreeProperties.FAKE_EQUIPMENT));

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
