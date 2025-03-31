package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Cow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.CowProperties;

public class CowWatcher extends AgeableMobWatcher
{
    private final CowProperties cowProperties;

    public CowWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.COW);

        cowProperties = DisguiseProperties.INSTANCE.getOrThrow(CowProperties.class);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.COW);
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        if (property == cowProperties.VARIANT)
        {
            var variant = (Cow.Variant) value;

            writePersistent(ValueIndex.COW.COW_VARIANT, variant);
        }

        super.onPropertyWrite(property, value);
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        if (nbt.contains("variant"))
        {
            var idString = nbt.getString("variant").orElseThrow();
            var idKey = NamespacedKey.fromString(idString);

            if (idKey == null)
                return;

            var variant = RegistryAccess.registryAccess()
                    .getRegistry(RegistryKey.COW_VARIANT)
                    .getOrThrow(idKey);

            writePersistent(ValueIndex.COW.COW_VARIANT, variant);
        }

        super.mergeFromCompound(nbt);
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        var variant = this.readOr(ValueIndex.COW.COW_VARIANT, null);

        if (variant != null)
            nbt.putString("variant", variant.getKey().asString());

        super.writeToCompound(nbt);
    }
}
