package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.GoatProperties;

public class GoatWatcher extends LivingEntityWatcher
{
    public GoatWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.GOAT);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.GOAT);
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        var properties = DisguiseProperties.INSTANCE.getOrThrow(GoatProperties.class);

        if (property.equals(properties.HAS_LEFT_HORN))
            writePersistent(ValueIndex.GOAT.HAS_LEFT_HORN, (Boolean) value);

        if (property.equals(properties.HAS_RIGHT_HORN))
            writePersistent(ValueIndex.GOAT.HAS_RIGHT_HORN, (Boolean) value);

        super.onPropertyWrite(property, value);
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("HasLeftHorn"))
            writePersistent(ValueIndex.GOAT.HAS_LEFT_HORN, nbt.getBoolean("HasLeftHorn"));

        if (nbt.contains("HasRightHorn"))
            writePersistent(ValueIndex.GOAT.HAS_RIGHT_HORN, nbt.getBoolean("HasRightHorn"));

        if (nbt.contains("IsScreamingGoat"))
            writePersistent(ValueIndex.GOAT.IS_SCREAMING, nbt.getBoolean("IsScreamingGoat"));
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        nbt.putBoolean("HasLeftHorn", read(ValueIndex.GOAT.HAS_LEFT_HORN));
        nbt.putBoolean("HasRightHorn", read(ValueIndex.GOAT.HAS_RIGHT_HORN));
        nbt.putBoolean("IsScreamingGoat", read(ValueIndex.GOAT.IS_SCREAMING));
    }
}
