package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.llama;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;

public class TraderLlamaWatcher extends LlamaWatcher
{
    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.LLAMA);
    }

    public TraderLlamaWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.TRADER_LLAMA);
    }
}
