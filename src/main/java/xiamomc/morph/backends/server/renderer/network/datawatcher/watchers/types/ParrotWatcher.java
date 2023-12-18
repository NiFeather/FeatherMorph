package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;

public class ParrotWatcher extends TameableAnimalWatcher
{
    public ParrotWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.PARROT);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.PARROT);
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("Variant"))
        {
            var variant = nbt.getInt("Variant");
            this.write(ValueIndex.PARROT.PARROT_VARIANT, variant);
        }
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        nbt.putInt("Variant", get(ValueIndex.PARROT.PARROT_VARIANT));
    }
}
