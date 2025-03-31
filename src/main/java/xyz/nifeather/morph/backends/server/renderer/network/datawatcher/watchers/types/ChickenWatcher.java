package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.ChickenProperties;

public class ChickenWatcher extends AgeableMobWatcher
{
    private final ChickenProperties chickenProperties;

    public ChickenWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.CHICKEN);

        chickenProperties = DisguiseProperties.INSTANCE.getOrThrow(ChickenProperties.class);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.CHICKEN);
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        if (property == chickenProperties.VARIANT)
        {
            var variant = (Chicken.Variant) value;

            writePersistent(ValueIndex.CHICKEN.CHICKEN_VARIANT, variant);
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
                    .getRegistry(RegistryKey.CHICKEN_VARIANT)
                    .getOrThrow(idKey);

            writePersistent(ValueIndex.CHICKEN.CHICKEN_VARIANT, variant);
        }

        super.mergeFromCompound(nbt);
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        var variant = this.readOr(ValueIndex.CHICKEN.CHICKEN_VARIANT, null);

        if (variant != null)
            nbt.putString("variant", variant.getKey().asString());

        super.writeToCompound(nbt);
    }
}
