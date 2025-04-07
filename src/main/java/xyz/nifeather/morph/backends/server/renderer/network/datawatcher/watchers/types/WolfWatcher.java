package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.github.retrooper.packetevents.protocol.entity.wolfvariant.WolfVariant;
import com.github.retrooper.packetevents.protocol.entity.wolfvariant.WolfVariants;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntry;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.AnimationNames;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.WolfProperties;

import java.util.Objects;

public class WolfWatcher extends TameableAnimalWatcher
{
    public WolfWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.WOLF);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.WOLF);
    }

    private WolfVariant getWolfVariant(String id)
    {
        return Objects.requireNonNull(WolfVariants.getRegistry().getByName(id),
                "No pig variant for id: " + id);
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        var properties = DisguiseProperties.INSTANCE.getOrThrow(WolfProperties.class);

        if (property.equals(properties.VARIANT))
        {
            var val = (Wolf.Variant) value;

            this.writePersistent(ValueIndex.WOLF.WOLF_VARIANT, getWolfVariant(val.key().asString()));
        }

        super.onPropertyWrite(property, value);
    }

    @Override
    protected <X> void onEntryWrite(CustomEntry<X> entry, X oldVal, X newVal)
    {
        super.onEntryWrite(entry, oldVal, newVal);

        if (entry.equals(CustomEntries.ANIMATION))
        {
            var animId = newVal.toString();

            switch (animId)
            {
                case AnimationNames.SIT -> this.writePersistent(ValueIndex.WOLF.TAMEABLE_FLAGS, (byte)0x01);
                case AnimationNames.STANDUP -> this.writePersistent(ValueIndex.WOLF.TAMEABLE_FLAGS, (byte)0x00);
            }
        }
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("CollarColor"))
            writePersistent(ValueIndex.WOLF.COLLAR_COLOR, (int)nbt.getByte("CollarColor"));

        if (nbt.contains("variant"))
        {
            var typeString = nbt.getString("variant");
            NamespacedKey key = NamespacedKey.fromString(typeString);;

            if (key == null && FeatherMorphMain.getInstance().doInternalDebugOutput)
            {
                logger.warn("[DEBUG] Ignoring unknown variant: " + typeString);
                return;
            }

            writePersistent(ValueIndex.WOLF.WOLF_VARIANT, getWolfVariant(typeString));
        }
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        nbt.putByte("CollarColor", read(ValueIndex.WOLF.COLLAR_COLOR).byteValue());
        nbt.putString("variant", read(ValueIndex.WOLF.WOLF_VARIANT).getName().toString());
    }
}
