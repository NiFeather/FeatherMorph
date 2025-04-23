package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.github.retrooper.packetevents.protocol.entity.pig.PigVariant;
import com.github.retrooper.packetevents.protocol.entity.pig.PigVariants;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

import java.util.Objects;

public class PigWatcher extends AgeableMobWatcher
{
    public PigWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.PIG);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.PIG);
    }

    private PigVariant getPigVariant(String id)
    {
        return Objects.requireNonNull(PigVariants.getRegistry().getByName(id),
                "No pig variant for id: " + id);
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        super.onPropertyWrite(property, value);
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        var variant = this.readOr(ValueIndex.PIG.PIG_VARIANT, null);

        if (variant != null)
            nbt.putString("variant", variant.getName().toString());

        super.writeToCompound(nbt);
    }
}
