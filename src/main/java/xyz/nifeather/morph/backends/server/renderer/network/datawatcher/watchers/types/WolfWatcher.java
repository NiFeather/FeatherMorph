package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.github.retrooper.packetevents.protocol.entity.wolfvariant.WolfVariant;
import com.github.retrooper.packetevents.protocol.entity.wolfvariant.WolfVariants;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.DyeColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntry;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.AnimationNames;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.WolfPropertyCollection;
import xyz.nifeather.morph.utilities.Uuids;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

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
        var properties = DisguiseProperties.INSTANCE.getCollectionOrThrow(WolfPropertyCollection.class);

        if (property.equals(properties.VARIANT))
        {
            var val = (Wolf.Variant) value;
            this.writePersistent(ValueIndex.WOLF.WOLF_VARIANT, getWolfVariant(val.key().asString()));
        }
        else if (property.equals(properties.OWNER))
        {
            var uuid = (UUID) value;
            if (Uuids.NIL_UUID.equals(uuid))
                uuid = null;

            writePersistent(ValueIndex.WOLF.OWNER, Optional.ofNullable(uuid));
            this.writeTamed(uuid != null && !Uuids.NIL_UUID.equals(uuid));
        }
        else if (property.equals(properties.COLLAR_COLOR))
        {
            var dyeColor = (DyeColor) value;
            writePersistent(ValueIndex.WOLF.COLLAR_COLOR, (int)dyeColor.getWoolData());

            this.readOr(ValueIndex.CAT.OWNER, Optional.empty())
                    .ifPresentOrElse(uuid -> {}, () -> this.writeProperty(properties.OWNER, UUID.randomUUID()));
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
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        nbt.putByte("CollarColor", read(ValueIndex.WOLF.COLLAR_COLOR).byteValue());
        nbt.putString("variant", read(ValueIndex.WOLF.WOLF_VARIANT).getName().toString());
    }
}
