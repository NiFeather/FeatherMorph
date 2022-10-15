package xiamomc.morph.messages;

import xiamomc.morph.MorphPlugin;
import xiamomc.pluginbase.messages.FormattableMessage;
import xiamomc.pluginbase.messages.IStrings;

public abstract class AbstractMorphStrings implements IStrings
{
    protected static FormattableMessage getFormattable(String key, String defaultValue)
    {
        return new FormattableMessage(MorphPlugin.getMorphNameSpace(), key, defaultValue);
    }
}
