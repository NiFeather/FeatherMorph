package xiamomc.morph.storage;

import xiamomc.morph.MorphPlugin;
import xiamomc.pluginbase.JsonBasedStorage;

public abstract class MorphJsonBasedStorage<T> extends JsonBasedStorage<T, MorphPlugin>
{
    @Override
    protected String getPluginNamespace()
    {
        return MorphPlugin.getMorphNameSpace();
    }
}
