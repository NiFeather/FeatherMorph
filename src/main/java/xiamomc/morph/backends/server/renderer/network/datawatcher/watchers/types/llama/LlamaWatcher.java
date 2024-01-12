package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.llama;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.horses.ChestedHorseWatcher;

public class LlamaWatcher extends ChestedHorseWatcher
{
    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.LLAMA);
    }

    public LlamaWatcher(Player bindingPlayer, EntityType type)
    {
        super(bindingPlayer, type);
    }

    public LlamaWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.LLAMA);
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("Strength"))
            write(ValueIndex.LLAMA.SLOTS, nbt.getInt("Strength"));

        if (nbt.contains("DecorItem"))
            logger.warn("todo: Llama DecorItem is not implemented.");

        if (nbt.contains("Variant"))
            write(ValueIndex.LLAMA.VARIANT, nbt.getInt("Variant"));
    }
}
