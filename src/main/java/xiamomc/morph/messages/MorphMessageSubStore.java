package xiamomc.morph.messages;

import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.utilities.PluginAssetUtils;
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
        //从插件资源获取
        var path = PluginAssetUtils.langPath(locale);
        var asset = PluginAssetUtils.getFileStrings(path);

        if (!asset.isEmpty() && !asset.isBlank())
        {
            try
            {
                var defaults = createGson().fromJson(asset, this.storingObject.getClass());
                defaults.forEach((o1, o2) ->
                {
                    if (o1 instanceof String key
                            && !storingObject.containsKey(key)
                            && o2 instanceof String msg)
                    {
                        storingObject.put(key, msg);
                    }
                });
            }
            catch (Throwable t)
            {
                logger.error("Error occurred while updating localization for locale '" + locale + "': " + t.getMessage());
                t.printStackTrace();
            }
        }

        saveConfiguration();
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
