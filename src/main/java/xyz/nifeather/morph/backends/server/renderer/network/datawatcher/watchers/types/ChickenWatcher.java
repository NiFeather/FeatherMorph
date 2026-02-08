package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.github.retrooper.packetevents.protocol.entity.chicken.ChickenVariant;
import com.github.retrooper.packetevents.protocol.entity.chicken.ChickenVariants;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.syncing.IBindTarget;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.ChickenPropertyCollection;

import java.util.Objects;

public class ChickenWatcher extends AgeableMobWatcher
{
    private final ChickenPropertyCollection chickenProperties;

    public ChickenWatcher(IBindTarget bindTarget)
    {
        super(bindTarget, EntityType.CHICKEN);

        chickenProperties = DisguiseProperties.INSTANCE.getCollectionOrThrow(ChickenPropertyCollection.class);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.CHICKEN);
    }

    private ChickenVariant getChickenVariant(String id)
    {
        return Objects.requireNonNull(ChickenVariants.getRegistry().getByName(id),
                "No chicken variant for id: " + id);
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        if (Objects.equals(property, chickenProperties.VARIANT))
        {
            var variant = (Chicken.Variant) value;

            writePersistent(ValueIndex.CHICKEN.CHICKEN_VARIANT, getChickenVariant(variant.getKey().asString()));
        }

        super.onPropertyWrite(property, value);
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        var variant = this.readOr(ValueIndex.CHICKEN.CHICKEN_VARIANT, null);

        if (variant != null)
            nbt.putString("variant", variant.getName().toString());

        super.writeToCompound(nbt);
    }
}
