package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.github.retrooper.packetevents.protocol.entity.cat.CatVariant;
import com.github.retrooper.packetevents.protocol.entity.cat.CatVariants;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.DyeColor;
import org.bukkit.entity.Cat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntry;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.AnimationNames;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.CatProperties;
import xyz.nifeather.morph.utilities.Uuids;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class CatWatcher extends TameableAnimalWatcher
{
    public CatWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.CAT);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.CAT);
    }

    private CatVariant getCatVariant(String id)
    {
        return Objects.requireNonNull(CatVariants.getRegistry().getByName(id),
                "No cat variant for id: " + id);
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        var properties = DisguiseProperties.INSTANCE.getOrThrow(CatProperties.class);

        if (property.equals(properties.CAT_VARIANT))
        {
            var variant = (Cat.Type) value;
            writePersistent(ValueIndex.CAT.CAT_VARIANT, getCatVariant(variant.key().asString()));
        }
        else if (property.equals(properties.OWNER))
        {
            var uuid = (UUID) value;
            writePersistent(ValueIndex.CAT.OWNER, Optional.ofNullable(uuid));

            var uuidValid = !Uuids.NIL_UUID.equals(uuid);
            this.writeTamed(uuidValid);
        }
        else if (property.equals(properties.COLLAR_COLOR))
        {
            var dyeColor = (DyeColor) value;
            writePersistent(ValueIndex.CAT.COLLAR_COLOR, (int)dyeColor.getWoolData());
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
                case AnimationNames.LAY_START -> this.writePersistent(ValueIndex.CAT.IS_LYING, true);
                case AnimationNames.SIT -> this.writePersistent(ValueIndex.CAT.TAMEABLE_FLAGS, (byte)0x01);
                case AnimationNames.STANDUP, AnimationNames.RESET ->
                {
                    if (this.readOr(ValueIndex.CAT.IS_LYING, false))
                        this.writePersistent(ValueIndex.CAT.IS_LYING, false);

                    if ((this.readOr(ValueIndex.CAT.TAMEABLE_FLAGS, (byte)0x00) & 1) != 0)
                        this.writePersistent(ValueIndex.CAT.TAMEABLE_FLAGS, (byte)0x00);
                }
            }
        }
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        var variant = read(ValueIndex.CAT.CAT_VARIANT).getName().toString();
        nbt.putString("variant", variant);

        var collarColor = read(ValueIndex.CAT.COLLAR_COLOR);
        nbt.putInt("CollarColor", collarColor);
    }
}
