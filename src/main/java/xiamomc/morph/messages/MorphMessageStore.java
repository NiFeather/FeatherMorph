package xiamomc.morph.messages;

import xiamomc.morph.MorphPlugin;
import xiamomc.pluginbase.messages.IStrings;
import xiamomc.pluginbase.messages.MessageStore;

import java.util.ArrayList;
import java.util.List;

public class MorphMessageStore extends MessageStore<MorphPlugin>
{
    private final List<Class<? extends IStrings>> strings = List.of(
            CommonStrings.class,
            CommandStrings.class,
            HelpStrings.class,
            MorphStrings.class,
            RequestStrings.class,
            SkillStrings.class
    );

    @Override
    protected List<Class<? extends IStrings>> getStrings()
    {
        return strings;
    }

    @Override
    protected String getPluginNamespace()
    {
        return MorphPlugin.getMorphNameSpace();
    }
}
