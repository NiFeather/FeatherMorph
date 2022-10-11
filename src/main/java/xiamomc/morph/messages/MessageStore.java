package xiamomc.morph.messages;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.storage.JsonBasedStorage;
import xiamomc.pluginbase.Annotations.Initializer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageStore extends JsonBasedStorage<Map<String, String>>
{
    public MessageStore()
    {
        instance = this;
    }

    private static MessageStore instance;

    public static MessageStore getInstance()
    {
        return instance;
    }

    public String get(String key, String defaultValue)
    {
        var val = storingObject.get(key);

        if (val == null)
        {
            val = defaultValue;
            storingObject.put(key, defaultValue);
            saveConfiguration();
        }

        return val;
    }

    @Initializer
    private void load()
    {
    }

    @Override
    protected @NotNull String getFileName()
    {
        return "messages.json";
    }

    @Override
    protected @NotNull Map<String, String> createDefault()
    {
        return new ConcurrentHashMap<>();
    }

    @Override
    protected @NotNull String getDisplayName()
    {
        return "消息存储";
    }
}
