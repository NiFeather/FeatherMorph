package xiamomc.morph.storage.skill;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPlugin;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 技能设置
 */
public interface ISkillOption
{
    /**
     * 将此技能设置转换为Map用于储存
     *
     * @return 一个Map
     * @apiNote Map中的所有对象都必须可序列化为JSON
     */
    Map<String, Object> toMap();

    /**
     * 获取技能设置的默认值
     *
     * @return 默认值
     */
    default Map<String, Object> getDefault()
    {
        return new Object2ObjectOpenHashMap<>();
    }


    /**
     * 从Map创建一个新实例
     *
     * @param map Map
     * @return 实例
     */
    @Nullable
    ISkillOption fromMap(@Nullable Map<String, Object> map);

    /**
     * 尝试从map中获取值
     *
     * @param map {@link Map}
     * @param key 目标键
     * @param defaultValue 默认值
     *
     * @return 目标值
     * @param <T> 值的类型
     */
    default <T> T tryGet(Map<String, Object> map, String key, T defaultValue)
    {
        var logger = MorphPlugin.getInstance(MorphPlugin.getMorphNameSpace()).getSLF4JLogger();

        return tryGet(map, key, defaultValue, o ->
        {
            return (T) defaultValue.getClass().cast(o);
        });
    }

    default <T> T tryGet(Map<String, Object> map, String key, Class<T> tClass)
    {
        return tryGet(map, key, null, tClass::cast);
    }

    /**
     * 尝试从map中获取值
     *
     * @param map {@link Map}
     * @param key 目标键
     * @param defaultValue 默认值
     * @param castMethod 转换方法
     *
     * @return 目标值
     * @param <T> 值的类型
     */
    default <T> T tryGet(Map<String, Object> map, String key, T defaultValue, Function<Object, T> castMethod)
    {
        var val = map.get(key);

        if (val == null) return defaultValue;

        T value;

        try
        {
            value = castMethod.apply(val);
        }
        catch (Throwable t)
        {
            var logger = MorphPlugin.getInstance(MorphPlugin.getMorphNameSpace()).getSLF4JLogger();
            logger.warn("无法解析设置键 " + key + ": " + t.getMessage());
            t.printStackTrace();

            value = null;
        }

        return value == null ? defaultValue : value;
    }

    default int tryGetInt(Map<String, Object> map, String key, int defaultValue)
    {
        return tryGet(map, key, defaultValue, o -> Double.valueOf("" + o).intValue());
    }

    default float tryGetFloat(Map<String, Object> map, String key, float defaultValue)
    {
        return tryGet(map, key, defaultValue, o -> Float.valueOf("" + o));
    }
}
