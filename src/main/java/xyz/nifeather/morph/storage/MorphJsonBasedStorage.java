package xyz.nifeather.morph.storage;

import xyz.nifeather.morph.MorphPlugin;
import xiamomc.pluginbase.JsonBasedStorage;

public abstract class MorphJsonBasedStorage<T> extends JsonBasedStorage<T, MorphPlugin>
{
    @Override
    protected String getPluginNamespace()
    {
        return MorphPlugin.getMorphNameSpace();
    }
}
