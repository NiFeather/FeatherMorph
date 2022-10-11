package xiamomc.morph.messages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Formattable;
import java.util.List;

public class FormattableMessage implements Comparable<FormattableMessage>
{
    private final String key;

    private final String defaultString;

    public FormattableMessage(@NotNull String key, @NotNull String defaultString)
    {
        this.defaultString = defaultString;
        this.key = key;
    }

    private final List<TagResolver> resolvers = new ArrayList<>();

    /**
     * 获取消息的Key
     * @return 消息的key
     */
    public String getKey()
    {
        return key;
    }

    /**
     * 获取消息的默认消息
     * @return 默认消息
     */
    public String getDefaultString()
    {
        return defaultString;
    }

    /**
     * 添加解析
     * @param target 要解析的Key
     * @param value 要解析的值
     * @return 对此对象的引用
     */
    public FormattableMessage resolve(String target, String value)
    {
        resolvers.add(Placeholder.parsed(target, value));

        return this;
    }

    /**
     * 添加解析
     * @param target 要解析的Key
     * @param value 要解析的值
     * @return 对此对象的引用
     */
    public FormattableMessage resolve(String target, Component value)
    {
        resolvers.add(Placeholder.component(target, value));

        return this;
    }

    /**
     * 添加解析
     * @param target 要解析的Key
     * @param formattable 要解析的值
     * @return 对此对象的引用
     */
    public FormattableMessage resolve(String target, FormattableMessage formattable)
    {
        resolvers.add(Placeholder.component(target, formattable.toComponent()));

        return this;
    }

    /**
     * 转换为Component
     * @return 可以显示的Component
     */
    public Component toComponent()
    {
        var msg = MessageStore.getInstance().get(key, defaultString);

        return MiniMessage.miniMessage().deserialize(msg, TagResolver.resolver(resolvers));
    }

    @Override
    public int compareTo(@NotNull FormattableMessage formattableMessage)
    {
        return this.key.compareTo(formattableMessage.key);
    }
}
