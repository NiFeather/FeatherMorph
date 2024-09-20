package xiamomc.morph.backends.server;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.backends.DisguiseWrapper;
import xiamomc.morph.backends.EventWrapper;
import xiamomc.morph.backends.WrapperAttribute;
import xiamomc.morph.backends.WrapperEvent;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.AgeableMobWatcher;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.ArmorStandWatcher;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.InventoryLivingWatcher;
import xiamomc.morph.backends.server.renderer.network.registries.CustomEntries;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;
import xiamomc.morph.backends.server.renderer.utilties.WatcherUtils;
import xiamomc.morph.misc.DisguiseEquipment;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.misc.PlayerTabHandler;
import xiamomc.morph.misc.disguiseProperty.SingleProperty;
import xiamomc.morph.utilities.NbtUtils;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ServerDisguiseWrapper extends EventWrapper<ServerDisguise>
{
    private final ServerBackend backend;

    public ServerDisguiseWrapper(@NotNull ServerDisguise instance, ServerBackend backend)
    {
        super(instance, backend);

        this.backend = backend;
    }

    private final DisguiseEquipment equipment = new DisguiseEquipment();

    @Override
    public void mergeCompound(CompoundTag compoundTag)
    {
        this.instance.compoundTag.merge(compoundTag);
        this.instance.isBaby = NbtUtils.isBabyForType(getEntityType(), compoundTag);

        if (this.getEntityType() == EntityType.MAGMA_CUBE || this.getEntityType() == EntityType.SLIME)
            resetDimensions();

        if (bindingWatcher != null)
        {
            bindingWatcher.mergeFromCompound(compoundTag);

            if (bindingWatcher instanceof AgeableMobWatcher)
                bindingWatcher.writePersistent(ValueIndex.AGEABLE_MOB.IS_BABY, instance.isBaby);

            if (compoundTag.contains("Small")) instance.armorStandSmall = compoundTag.getBoolean("Small");
            if (compoundTag.contains("NoBasePlate")) instance.armorStandNoBasePlate = compoundTag.getBoolean("NoBasePlate");
            if (compoundTag.contains("ShowArms")) instance.armorStandShowArms = compoundTag.getBoolean("ShowArms");
        }
    }

    private CompoundTag getCompound(boolean includeWatcher)
    {
        var tagCopy = this.instance.compoundTag.copy();

        if (bindingWatcher != null && includeWatcher)
            tagCopy.merge(WatcherUtils.buildCompoundFromWatcher(bindingWatcher));

        return tagCopy;
    }

    @Override
    public CompoundTag getCompound()
    {
        return this.getCompound(true);
    }

    /**
     * Gets network id of this disguise displayed to other players
     *
     * @return The network id of this disguise
     */
    @Override
    public int getNetworkEntityId()
    {
        return bindingPlayer.getEntityId();
    }

    @Nullable
    @Override
    public <R extends Tag> R getTag(@NotNull String path, TagType<R> type)
    {
        try
        {
            var obj = instance.compoundTag.get(path);

            if (obj != null && obj.getType() == type)
                return (R) obj;

            return null;
        }
        catch (Throwable t)
        {
            logger.error("Unable to read NBT '%s' from instance:".formatted(path));
            t.printStackTrace();

            return null;
        }
    }

    private static final Logger logger = MorphPlugin.getInstance().getSLF4JLogger();

    @Override
    public EntityEquipment getFakeEquipments()
    {
        return equipment;
    }

    @Override
    public void setFakeEquipments(@NotNull EntityEquipment newEquipment)
    {
        this.equipment.setArmorContents(newEquipment.getArmorContents());

        this.equipment.setHandItems(newEquipment.getItemInMainHand(), newEquipment.getItemInOffHand());

        if (bindingWatcher != null)
            bindingWatcher.writeEntry(CustomEntries.EQUIPMENT, this.equipment);
    }

    @Override
    public void setDisplayingFakeEquipments(boolean newVal)
    {
        super.setDisplayingFakeEquipments(newVal);

        if (bindingWatcher != null)
            bindingWatcher.writeEntry(CustomEntries.DISPLAY_FAKE_EQUIPMENT, newVal);
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

        newInstance.mergeCompound(this.getCompound());
        newInstance.setFakeEquipments(this.equipment);

        return newInstance;
    }

    public static ServerDisguiseWrapper cloneFromExternal(DisguiseWrapper<?> other, ServerBackend backend)
    {
        var newInstance = new ServerDisguiseWrapper(new ServerDisguise(other.getEntityType()), backend);

        other.getAttributes().forEach(newInstance::writeInternal);

        return newInstance;
    }

    private final Map<SingleProperty<?>, Object> disguiseProperties = new ConcurrentHashMap<>();

    @Override
    public <X> void writeProperty(SingleProperty<X> property, X value)
    {
        disguiseProperties.put(property, value);

        if (bindingWatcher != null)
            bindingWatcher.writeProperty(property, value);
    }

    @Override
    public <X> X readProperty(SingleProperty<X> property)
    {
        return this.readPropertyOr(property, property.defaultVal());
    }

    @Override
    public <X> X readPropertyOr(SingleProperty<X> property, X defaultVal)
    {
        return (X) disguiseProperties.getOrDefault(property, defaultVal);
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
        return instance.isBaby;
    }

    @Override
    public void applySkin(GameProfile profile)
    {
        if (this.getEntityType() != EntityType.PLAYER) return;

        write(WrapperAttribute.profile, Optional.of(profile));

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

    @Override
    public void onPlayerOffline()
    {
        PlayerTabHandler.instance().hideFakePlayer(bindingWatcher.readEntryOrThrow(CustomEntries.SPAWN_UUID));

        super.onPlayerOffline();
    }

    private boolean aggressive;

    @Override
    public void setAggressive(boolean aggressive)
    {
        super.setAggressive(aggressive);

        this.aggressive = aggressive;
        if (getEntityType() == EntityType.GHAST)
            bindingWatcher.writePersistent(ValueIndex.GHAST.CHARGING, aggressive);

        if (getEntityType() == EntityType.CREEPER)
        {
            bindingWatcher.writePersistent(ValueIndex.CREEPER.STATE, aggressive ? 1 : -1);
            bindingWatcher.writePersistent(ValueIndex.CREEPER.IGNITED, aggressive);
        }

        if (getEntityType() == EntityType.WARDEN)
            bindingWatcher.writeEntry(CustomEntries.WARDEN_CHARGING_ATTACK, aggressive);
    }

    @Override
    public void playAttackAnimation()
    {
        super.playAttackAnimation();
        bindingWatcher.writeEntry(CustomEntries.ATTACK_ANIMATION, true);
    }

    @Override
    public void setShowArms(boolean showArms)
    {
        super.setShowArms(showArms);

        instance.armorStandShowArms = showArms;
        if (bindingWatcher instanceof ArmorStandWatcher armorStandWatcher)
        {
            armorStandWatcher.writePersistent(
                    ValueIndex.ARMOR_STAND.DATA_FLAGS,
                    armorStandWatcher.getArmorStandFlags(instance.armorStandSmall,
                            instance.armorStandShowArms, instance.armorStandNoBasePlate));
        }
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
            this.bindingWatcher.dispose();

        this.bindingWatcher = bindingWatcher;

        this.disguiseProperties.forEach((property, value) ->
        {
            bindingWatcher.writeProperty((SingleProperty<Object>) property, value);
        });

        refreshRegistry();
    }

    @Override
    public void playAnimation(String animationId)
    {
        if (bindingWatcher != null)
            bindingWatcher.writeEntry(CustomEntries.ANIMATION, animationId);
    }

    private void refreshRegistry()
    {
        if (bindingPlayer == null)
            return;

        if (bindingWatcher == null)
        {
            logger.warn("Have a bindingPlayer but no bindingWatcher?!");
            Thread.dumpStack();
            return;
        }

        //和watcher同步我们的NBT
        bindingWatcher.mergeFromCompound(getCompound());

        if (getEntityType() == EntityType.PLAYER)
        {
            var profileOptional = readOrDefault(WrapperAttribute.profile);
            profileOptional.ifPresent(p -> bindingWatcher.writeEntry(CustomEntries.PROFILE, p));
        }

        //todo: 激活刷新时也刷新到玩家
        if (bindingWatcher instanceof InventoryLivingWatcher)
        {
            bindingWatcher.writeEntry(CustomEntries.DISPLAY_FAKE_EQUIPMENT, readOrDefault(WrapperAttribute.displayFakeEquip));
            bindingWatcher.writeEntry(CustomEntries.EQUIPMENT, this.equipment);
        }

        if (bindingWatcher.getEntityType() == EntityType.GHAST)
            bindingWatcher.writePersistent(ValueIndex.GHAST.CHARGING, aggressive);
    }
}
