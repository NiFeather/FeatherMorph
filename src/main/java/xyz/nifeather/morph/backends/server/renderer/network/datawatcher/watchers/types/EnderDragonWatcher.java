package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntry;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.EnderDragonPropertyCollection;

import java.util.Objects;

public class EnderDragonWatcher extends LivingEntityWatcher
{
    public EnderDragonWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.ENDER_DRAGON);

        this.properties = DisguiseProperties.INSTANCE.getOrThrow(EnderDragonPropertyCollection.class);
    }

    private final EnderDragonPropertyCollection properties;

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        this.register(ValueIndex.ENDER_DRAGON);
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        if (Objects.equals(property, properties.DRAGON_PHASE))
            this.writePersistent(ValueIndex.ENDER_DRAGON.DRAGON_PHASE, (Integer) value);

        super.onPropertyWrite(property, value);
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        nbt.putInt("DragonPhase", this.read(ValueIndex.ENDER_DRAGON.DRAGON_PHASE));

        super.writeToCompound(nbt);
    }

    @Override
    public <X> @Nullable X readEntry(CustomEntry<X> entry)
    {
        if (Objects.equals(entry, CustomEntries.OVERLAYED_YAW))
            return (X) Float.valueOf(180f + getBindingPlayer().getYaw());

        return super.readEntry(entry);
    }
}
