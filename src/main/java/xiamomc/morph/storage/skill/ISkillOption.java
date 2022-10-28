package xiamomc.morph.storage.skill;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

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
}
