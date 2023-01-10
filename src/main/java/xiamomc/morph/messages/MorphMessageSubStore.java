package xiamomc.morph.messages;

import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphPlugin;
import xiamomc.pluginbase.Messages.IStrings;
import xiamomc.pluginbase.Messages.MessageStore;

import java.util.List;

public class MorphMessageSubStore extends MessageStore<MorphPlugin>
{
    public MorphMessageSubStore(String locale, List<Class<? extends IStrings>> strings, MorphMessageStore parentStore)
    {
        this.locale = locale;
        this.strings = strings;

        this.parentStore = parentStore;
    }

    private final List<Class<? extends IStrings>> strings;

    private final String locale;

    private final MorphMessageStore parentStore;

    @Override
    public void addMissingStrings()
    {
        parentStore.getAllMessages().forEach((key, msg) ->
        {
            if (!storingObject.containsKey(key))
                storingObject.put(key, msg);
        });
    }

    @Override
    protected List<Class<? extends IStrings>> getStrings()
    {
        return strings;
    }

    @Override
    protected @NotNull String getFileName()
    {
        return "messages/" + locale + ".json";
    }

    @Override
    protected String getPluginNamespace()
    {
        return MorphPlugin.getMorphNameSpace();
    }
}
