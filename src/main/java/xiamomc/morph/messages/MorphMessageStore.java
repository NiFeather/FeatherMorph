package xiamomc.morph.messages;

import xiamomc.morph.MorphPlugin;
import xiamomc.pluginbase.messages.IStrings;
import xiamomc.pluginbase.messages.MessageStore;

import java.util.ArrayList;
import java.util.List;

public class MorphMessageStore extends MessageStore<MorphPlugin>
{
    private final List<Class<IStrings>> cachedClassList = new ArrayList<>();

    private final List<Class<?>> rawClassList = List.of(
            CommonStrings.class,
            CommandStrings.class,
            HelpStrings.class,
            MorphStrings.class,
            RequestStrings.class,
            SkillStrings.class
    );

    @Override
    protected List<Class<IStrings>> getStrings()
    {
        if (cachedClassList.size() == 0)
        {
            rawClassList.forEach(c -> cachedClassList.add((Class<IStrings>) c));
        }

        return cachedClassList;
    }

    @Override
    protected String getPluginNamespace()
    {
        return MorphPlugin.getMorphNameSpace();
    }
}
