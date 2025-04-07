package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.github.retrooper.packetevents.protocol.entity.frog.FrogVariant;
import com.github.retrooper.packetevents.protocol.entity.frog.FrogVariants;
import com.github.retrooper.packetevents.protocol.entity.pose.EntityPose;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntry;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.AnimationNames;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.FrogProperties;

import java.util.Objects;

public class FrogWatcher extends LivingEntityWatcher
{
    public FrogWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.FROG);
    }

    private FrogVariant getFrogVariant(NamespacedKey key)
    {
        return Objects.requireNonNull(
                FrogVariants.getRegistry().getByName(key.asString()),
                "No packet version frog variant: %s".formatted(key.asString())
        );
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.FROG);
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("variant"))
        {
            var typeString = nbt.getString("variant");
            NamespacedKey key = NamespacedKey.fromString(typeString);

            if (key != null)
                writePersistent(ValueIndex.FROG.FROG_VARIANT, getFrogVariant(key));
        }
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        var properties = DisguiseProperties.INSTANCE.getOrThrow(FrogProperties.class);

        if (property.equals(properties.VARIANT))
        {
            var variant = (Frog.Variant) value;
            writePersistent(ValueIndex.FROG.FROG_VARIANT, getFrogVariant(variant.getKey()));
        }
    }

    @Override
    protected <X> void onEntryWrite(CustomEntry<X> entry, X oldVal, X newVal)
    {
        super.onEntryWrite(entry, oldVal, newVal);

        if (entry.equals(CustomEntries.ANIMATION))
        {
            var animId = newVal.toString();
            var player = getBindingPlayer();
            var world = player.getWorld();

            switch (animId)
            {
                case AnimationNames.EAT ->
                {
                    this.writePersistent(ValueIndex.FROG.POSE, EntityPose.USING_TONGUE);
                    world.playSound(player.getLocation(), Sound.ENTITY_FROG_EAT, 1, 1);
                }
                case AnimationNames.RESET -> this.remove(ValueIndex.FROG.POSE);
            }
        }
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        var variant = read(ValueIndex.FROG.FROG_VARIANT).getName().toString();
        nbt.putString("variant", variant);
    }
}
