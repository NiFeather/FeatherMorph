package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types.llama;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.syncing.IBindTarget;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types.horses.ChestedHorseWatcher;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.LlamaPropertyCollection;

public class LlamaWatcher extends ChestedHorseWatcher
{
    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.LLAMA);
    }

    public LlamaWatcher(IBindTarget bindTarget, EntityType type)
    {
        super(bindTarget, type);
    }

    public LlamaWatcher(IBindTarget bindTarget)
    {
        super(bindTarget, EntityType.LLAMA);
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        var properties = DisguiseProperties.INSTANCE.getCollectionOrThrow(LlamaPropertyCollection.class);

        if (property.equals(properties.COLOR))
        {
            var val = (Llama.Color) value;

            writePersistent(ValueIndex.LLAMA.VARIANT, val.ordinal());
        }

        super.onPropertyWrite(property, value);
    }
}
