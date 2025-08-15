package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.github.retrooper.packetevents.protocol.entity.cow.CowVariants;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.Cow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.CowProperties;

import java.util.Objects;

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
        if (Objects.equals(property, cowProperties.VARIANT))
        {
            var bukkitVariant = (Cow.Variant) value;
            var packetVariant = Objects.requireNonNull(CowVariants.getRegistry().getByName(bukkitVariant.key().asString()),
                    "No packet version for bukkit variant %s!".formatted(bukkitVariant));

            writePersistent(ValueIndex.COW.COW_VARIANT, packetVariant);
        }

        super.onPropertyWrite(property, value);
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        var variant = this.readOr(ValueIndex.COW.COW_VARIANT, null);

        if (variant != null)
            nbt.putString("variant", variant.getName().toString());

        super.writeToCompound(nbt);
    }
}
