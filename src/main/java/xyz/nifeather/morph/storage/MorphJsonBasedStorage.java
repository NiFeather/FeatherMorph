package xyz.nifeather.morph.storage;

import xiamomc.pluginbase.storage.JsonBasedStorage;
import xyz.nifeather.morph.FeatherMorphMain;

public abstract class MorphJsonBasedStorage<T> extends JsonBasedStorage<T, FeatherMorphMain>
{
    @Override
    protected String getPluginNamespace()
    {
        return FeatherMorphMain.getMorphNameSpace();
    }
}
