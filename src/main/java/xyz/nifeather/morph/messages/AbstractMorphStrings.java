package xyz.nifeather.morph.messages;

import xyz.nifeather.morph.MorphPlugin;
import xiamomc.pluginbase.Messages.FormattableMessage;
import xiamomc.pluginbase.Messages.IStrings;

public abstract class AbstractMorphStrings implements IStrings
{
    private static final String nameSpace = MorphPlugin.getMorphNameSpace();

    protected static FormattableMessage getFormattable(String key, String defaultValue)
    {
        return new FormattableMessage(nameSpace, key, defaultValue);
    }
}
