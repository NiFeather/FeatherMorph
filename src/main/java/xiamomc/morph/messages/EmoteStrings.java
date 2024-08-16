package xiamomc.morph.messages;

import xiamomc.morph.MorphPlugin;
import xiamomc.pluginbase.Messages.FormattableMessage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EmoteStrings extends AbstractMorphStrings
{
    public static FormattableMessage notAvailable()
    {
        return getFormattable(getKey("not_available"), "当前不能使用动作");
    }

    private static final Map<String, FormattableMessage> map = new ConcurrentHashMap<>();

    private static FormattableMessage register(String id)
    {
        var formattable = getFormattable(getKey(id), "animation:" + id);
        map.put(id, formattable);

        return formattable;
    }

    public static FormattableMessage Unknown()
    {
        return getFormattable("unknown", "animation:unknown");
    }

    public static FormattableMessage get(String animationId)
    {
        var value = map.getOrDefault(animationId, Unknown());

        return new FormattableMessage(MorphPlugin.getInstance(), value.getKey(), value.getDefaultString());
    }

    public static String getKey(String key)
    {
        return "emote.morphclient." + key;
    }

    static
    {
        register("sniff");
        register("roar");
        register("rollup");
        register("dance");
        register("inflate");
        register("deflate");
        register("lay");
        register("standup");
        register("sit");
        register("peek");
        register("open");
        register("eat");
        register("sleep");
        register("stop");
        register("prostrate");
        register("digdown");
        register("appear");
    }
}
