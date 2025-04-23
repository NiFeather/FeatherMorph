package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.CreeperProperties;

public class CreeperWatcher extends LivingEntityWatcher
{
    public CreeperWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.CREEPER);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.CREEPER);
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        var properties = DisguiseProperties.INSTANCE.getOrThrow(CreeperProperties.class);

        if (property.equals(properties.CHARGED))
        {
            var isCharged = Boolean.TRUE.equals(value);
            this.writePersistent(ValueIndex.CREEPER.IS_CHARGED_CREEPER, isCharged);
        }

        super.onPropertyWrite(property, value);
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("powered"))
            this.writePersistent(ValueIndex.CREEPER.IS_CHARGED_CREEPER, nbt.getBoolean("powered"));
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        nbt.putBoolean("powered", this.read(ValueIndex.CREEPER.IS_CHARGED_CREEPER));
    }
}
