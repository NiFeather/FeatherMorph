package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types.llama;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.LlamaProperties;
import xyz.nifeather.morph.misc.disguiseProperty.values.TraderLlamaProperties;

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
