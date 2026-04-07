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
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.CatPropertyCollection;
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
        var properties = DisguiseProperties.INSTANCE.getCollectionOrThrow(CatPropertyCollection.class);

        switch (property.id())
        {
            case PropertyNames.CAT_VARIANT ->
            {
                var variant = (Cat.Type) value;
                writePersistent(ValueIndex.CAT.CAT_VARIANT, getCatVariant(variant.key().asString()));
            }

            case PropertyNames.CAT_OWNER ->
            {
                var uuid = (UUID) value;
                if (Uuids.NIL_UUID.equals(uuid))
                    uuid = null;

                writePersistent(ValueIndex.CAT.OWNER, Optional.ofNullable(uuid));
                this.writeTamed(uuid != null && !Uuids.NIL_UUID.equals(uuid));
            }

            case PropertyNames.CAT_COLLAR_COLOR ->
            {
                var dyeColor = (DyeColor) value;
                writePersistent(ValueIndex.CAT.COLLAR_COLOR, (int)dyeColor.getWoolData());

                this.readOr(ValueIndex.CAT.OWNER, Optional.empty())
                        .ifPresentOrElse(uuid -> {}, () -> this.writeProperty(properties.OWNER, UUID.randomUUID()));
            }

            case PropertyNames.CAT_SITTING ->
            {
                var sitting = (Boolean) value;
                int flag = this.read(ValueIndex.CAT.TAMEABLE_FLAGS);

                if (sitting)
                    flag |= 0x01;
                else if ((flag & 0x01) == 0x01)
                    flag ^= 0x01;

                this.writePersistent(ValueIndex.CAT.TAMEABLE_FLAGS, (byte) flag);
            }

            case PropertyNames.CAT_LYING ->
            {
                var lying = (Boolean) value;
                this.writePersistent(ValueIndex.CAT.IS_LYING, lying);
            }
        }

        super.onPropertyWrite(property, value);
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
