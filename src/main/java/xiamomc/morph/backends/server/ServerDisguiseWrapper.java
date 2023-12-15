package xiamomc.morph.backends.server;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.backends.DisguiseWrapper;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.AgeableMobWatcher;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.ArmorStandWatcher;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.GhastWatcher;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.InventoryLivingWatcher;
import xiamomc.morph.backends.server.renderer.network.registries.EntryIndex;
import xiamomc.morph.backends.server.renderer.utilties.WatcherUtils;
import xiamomc.morph.misc.DisguiseEquipment;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.utilities.NbtUtils;

import java.util.Objects;

public class ServerDisguiseWrapper extends DisguiseWrapper<ServerDisguise>
{
    public ServerDisguiseWrapper(@NotNull ServerDisguise instance, ServerBackend backend)
    {
        super(instance, backend);
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
                bindingWatcher.write(ValueIndex.AGEABLE_MOB.IS_BABY, instance.isBaby);

            instance.armorStandSmall = compoundTag.getBoolean("Small");
            instance.armorStandNoBasePlate = compoundTag.getBoolean("NoBasePlate");
            instance.armorStandShowArms = compoundTag.getBoolean("ShowArms");
        }
    }

    @Override
    public CompoundTag getCompound()
    {
        return instance.compoundTag.copy();
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
    public EntityEquipment getDisplayingEquipments()
    {
        return equipment;
    }

    @Override
    public void setFakeEquipments(@NotNull EntityEquipment newEquipment)
    {
        this.equipment.setArmorContents(newEquipment.getArmorContents());

        this.equipment.setHandItems(newEquipment.getItemInMainHand(), newEquipment.getItemInOffHand());

        if (bindingWatcher != null)
            bindingWatcher.write(EntryIndex.EQUIPMENT, this.equipment);
    }

    private boolean shouldDisplayCustomEquipment;

    @Override
    public void setDisplayingFakeEquipments(boolean newVal)
    {
        shouldDisplayCustomEquipment = newVal;

        if (bindingWatcher != null)
            bindingWatcher.write(EntryIndex.DISPLAY_FAKE_EQUIPMENT, newVal);
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
        var newInstance = new ServerDisguiseWrapper(this.copyInstance(), (ServerBackend) getBackend());
        newInstance.mergeCompound(this.getCompound());

        newInstance.shouldDisplayCustomEquipment = this.shouldDisplayCustomEquipment;
        newInstance.setFakeEquipments(this.equipment);

        return newInstance;
    }

    /**
     * 返回此伪装的名称
     *
     * @return 伪装名称
     */
    @Override
    public String getDisguiseName()
    {
        return instance.name;
    }

    @Override
    public void setDisguiseName(String name)
    {
        this.instance.name = name;

        if (bindingWatcher != null)
            bindingWatcher.write(EntryIndex.DISGUISE_NAME, name);
    }

    @Override
    public boolean isBaby()
    {
        return instance.isBaby;
    }

    @Override
    public void setGlowingColor(ChatColor glowingColor)
    {
        instance.glowingColor = glowingColor;
    }

    @Override
    public void setGlowing(boolean glowing)
    {
    }

    @Override
    public ChatColor getGlowingColor()
    {
        return instance.glowingColor;
    }

    @Override
    public void addCustomData(String key, Object data)
    {
        instance.customData.put(key, data);
    }

    @Override
    public Object getCustomData(String key)
    {
        return instance.customData.getOrDefault(key, null);
    }

    @Override
    public void applySkin(GameProfile profile)
    {
        if (this.getEntityType() != EntityType.PLAYER) return;

        this.instance.profile = profile;

        if (bindingWatcher != null)
            bindingWatcher.write(EntryIndex.PROFILE, this.instance.profile);
    }

    @Override
    public @Nullable GameProfile getSkin()
    {
        return instance.profile;
    }

    @Override
    public void onPostConstructDisguise(DisguiseState state, @Nullable Entity targetEntity)
    {
    }

    @Override
    public void update(boolean isClone, DisguiseState state, Player player)
    {
    }

    @Override
    public void setSaddled(boolean saddled)
    {
        instance.saddled = saddled;
    }

    @Override
    public boolean isSaddled()
    {
        return instance.saddled;
    }

    private boolean aggressive;

    @Override
    public void setAggressive(boolean aggressive)
    {
        super.setAggressive(aggressive);

        this.aggressive = aggressive;
        if (bindingWatcher instanceof GhastWatcher ghastWatcher)
            ghastWatcher.write(ValueIndex.GHAST.CHARGING, aggressive);
    }

    @Override
    public void setShowArms(boolean showArms)
    {
        super.setShowArms(showArms);

        instance.armorStandShowArms = showArms;
        if (bindingWatcher instanceof ArmorStandWatcher armorStandWatcher)
        {
            armorStandWatcher.write(
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

    public void setRenderParameters(@NotNull Player newBinding, @NotNull SingleWatcher bindingWatcher)
    {
        Objects.requireNonNull(bindingWatcher, "Null Watcher!");

        bindingPlayer = newBinding;
        this.bindingWatcher = bindingWatcher;

        refreshRegistry();
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

        //先和watcher同步一下我们的NBT
        bindingWatcher.mergeFromCompound(getCompound());

        //todo: 激活刷新时也刷新到玩家
        if (bindingWatcher instanceof InventoryLivingWatcher)
        {
            bindingWatcher.write(EntryIndex.PROFILE, this.instance.profile);
            bindingWatcher.write(EntryIndex.DISPLAY_FAKE_EQUIPMENT, shouldDisplayCustomEquipment);
            bindingWatcher.write(EntryIndex.EQUIPMENT, this.equipment);
        }

        if (bindingWatcher.getEntityType() == EntityType.GHAST)
            bindingWatcher.write(ValueIndex.GHAST.CHARGING, aggressive);

        //然后从watcher拉取他们的NBT。
        //如果watcher有存在会随机的值，此举会将随机的值同步给我们
        this.instance.compoundTag.merge(WatcherUtils.buildCompoundFromWatcher(bindingWatcher));
    }
}
