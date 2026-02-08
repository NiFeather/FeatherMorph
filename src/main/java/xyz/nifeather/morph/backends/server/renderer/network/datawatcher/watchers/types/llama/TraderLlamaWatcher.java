package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types.llama;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.syncing.IBindTarget;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;

public class TraderLlamaWatcher extends LlamaWatcher
{
    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.LLAMA);
    }

    public TraderLlamaWatcher(IBindTarget bindTarget)
    {
        super(bindTarget, EntityType.TRADER_LLAMA);
    }
}
