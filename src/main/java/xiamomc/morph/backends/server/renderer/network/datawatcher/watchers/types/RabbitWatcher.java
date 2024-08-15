package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;
import xiamomc.morph.misc.disguiseProperty.DisguiseProperties;
import xiamomc.morph.misc.disguiseProperty.SingleProperty;
import xiamomc.morph.misc.disguiseProperty.values.RabbitProperties;

import java.util.Arrays;

public class RabbitWatcher extends LivingEntityWatcher
{
    public RabbitWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.RABBIT);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.RABBIT);
    }

    public Rabbit.Type getType()
    {
        return Arrays.stream(Rabbit.Type.values()).toList().get(read(ValueIndex.RABBIT.RABBIT_TYPE));
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        var properties = DisguiseProperties.INSTANCE.getOrThrow(RabbitProperties.class);

        if (property.equals(properties.VARIANT))
        {
            var val = (Rabbit.Type) value;

            writePersistent(ValueIndex.RABBIT.RABBIT_TYPE, val == Rabbit.Type.THE_KILLER_BUNNY ? 99 : val.ordinal());
        }

        super.onPropertyWrite(property, value);
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("RabbitType"))
            writePersistent(ValueIndex.RABBIT.RABBIT_TYPE, nbt.getInt("RabbitType"));
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        nbt.putInt("RabbitType", read(ValueIndex.RABBIT.RABBIT_TYPE));
    }
}
