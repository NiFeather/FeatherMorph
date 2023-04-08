package xiamomc.morph;

import xiamomc.pluginbase.PluginObject;

public class MorphPluginObject extends PluginObject<MorphPlugin>
{
    protected MorphPlugin morphPlugin()
    {
        return (MorphPlugin) plugin;
    }

    @Override
    protected String getPluginNamespace()
    {
        return MorphPlugin.getMorphNameSpace();
    }
}
