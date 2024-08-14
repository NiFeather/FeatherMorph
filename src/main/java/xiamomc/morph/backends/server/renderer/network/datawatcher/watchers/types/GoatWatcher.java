package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;
import xiamomc.morph.misc.disguiseProperty.DisguiseProperties;
import xiamomc.morph.misc.disguiseProperty.SingleProperty;
import xiamomc.morph.misc.disguiseProperty.values.GoatProperties;

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
            writeOverride(ValueIndex.GOAT.HAS_LEFT_HORN, (Boolean) value);

        if (property.equals(properties.HAS_RIGHT_HORN))
            writeOverride(ValueIndex.GOAT.HAS_RIGHT_HORN, (Boolean) value);

        super.onPropertyWrite(property, value);
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("HasLeftHorn"))
            writeOverride(ValueIndex.GOAT.HAS_LEFT_HORN, nbt.getBoolean("HasLeftHorn"));

        if (nbt.contains("HasRightHorn"))
            writeOverride(ValueIndex.GOAT.HAS_RIGHT_HORN, nbt.getBoolean("HasRightHorn"));

        if (nbt.contains("IsScreamingGoat"))
            writeOverride(ValueIndex.GOAT.IS_SCREAMING, nbt.getBoolean("IsScreamingGoat"));
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        nbt.putBoolean("HasLeftHorn", get(ValueIndex.GOAT.HAS_LEFT_HORN));
        nbt.putBoolean("HasRightHorn", get(ValueIndex.GOAT.HAS_RIGHT_HORN));
        nbt.putBoolean("IsScreamingGoat", get(ValueIndex.GOAT.IS_SCREAMING));
    }
}
