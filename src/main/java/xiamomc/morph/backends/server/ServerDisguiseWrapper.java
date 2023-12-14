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
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.SlimeWatcher;
import xiamomc.morph.backends.server.renderer.network.registries.EntryIndex;
import xiamomc.morph.misc.DisguiseEquipment;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.utilities.NbtUtils;

import java.util.UUID;

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
            if (bindingWatcher instanceof SlimeWatcher slimeWatcher)
            {
                var size = Math.max(1, getCompound().getInt("Size"));
                slimeWatcher.write(ValueIndex.SLIME_MAGMA.SIZE, size);
            }
        }
    }

    @Override
    public CompoundTag getCompound()
    {
        return instance.compoundTag.copy();
    }

    private static final UUID nilUUID = UUID.fromString("0-0-0-0-0");

    /**
     * Gets network id of this disguise displayed to other players
     *
     * @return The network id of this disguise
     */
    @Override
    public int getNetworkEntityId()
    {
        return -1;
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
    public void setDisplayingEquipments(@NotNull EntityEquipment newEquipment)
    {
        this.equipment.setArmorContents(newEquipment.getArmorContents());

        this.equipment.setHandItems(newEquipment.getItemInMainHand(), newEquipment.getItemInOffHand());

        if (bindingWatcher != null)
            bindingWatcher.write(EntryIndex.EQUIPMENT, this.equipment);
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
        return new ServerDisguiseWrapper(this.copyInstance(), (ServerBackend) getBackend());
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
            bindingWatcher.write(EntryIndex.CUSTOM_NAME, name);
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

    private Player bindingPlayer;

    public Player getBindingPlayer()
    {
        return bindingPlayer;
    }

    private SingleWatcher bindingWatcher;

    public void setRenderParameters(Player newBinding, SingleWatcher bindingWatcher)
    {
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

        //todo: 激活刷新时也刷新到玩家
        bindingWatcher.write(EntryIndex.PROFILE, this.instance.profile);
    }
}
