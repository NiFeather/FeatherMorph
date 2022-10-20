package xiamomc.morph.messages;

import xiamomc.morph.MorphPlugin;
import xiamomc.pluginbase.messages.FormattableMessage;
import xiamomc.pluginbase.messages.IStrings;

public abstract class AbstractMorphStrings implements IStrings
{
    private static final String nameSpace = MorphPlugin.getMorphNameSpace();

    protected static FormattableMessage getFormattable(String key, String defaultValue)
    {
        return new FormattableMessage(nameSpace, key, defaultValue);
    }
}
