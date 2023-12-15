package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.core.registries.BuiltInRegistries;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;

import java.util.Arrays;

public class FrogWatcher extends LivingEntityWatcher
{
    public FrogWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.FROG);
    }

    public Frog.Variant getFrogVariant()
    {
        var index = BuiltInRegistries.FROG_VARIANT.getId(get(ValueIndex.FROG.FROG_VARIANT));

        return Arrays.stream(Frog.Variant.values()).toList().get(index);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.FROG);
    }
}
