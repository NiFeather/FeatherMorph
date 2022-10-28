package xiamomc.morph.storage.skill;

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
     * 从Map创建一个新实例
     *
     * @param map Map
     * @return 实例
     */
    @Nullable
    ISkillOption fromMap(@Nullable Map<String, Object> map);
}
