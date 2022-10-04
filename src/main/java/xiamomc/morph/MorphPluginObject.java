package xiamomc.morph;

import xiamomc.pluginbase.PluginObject;

public class MorphPluginObject extends PluginObject<MorphPlugin>
{
    @Override
    protected String getPluginNamespace()
    {
        return MorphPlugin.getMorphNameSpace();
    }
}
