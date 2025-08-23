package xyz.nifeather.morph.storage.skill;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.misc.disguiseProperty.ParseErrorException;

import java.util.Map;
import java.util.function.Function;

/**
 * 技能设置
 */
public interface ISkillAbilityOption
{
    /**
     * 检查此Option是否合法
     * @return 此Option是否合法
     */
    boolean isValid();

    /**
     * 获取技能设置的默认值
     *
     * @return 默认值
     */
    default Map<String, Object> getDefault()
    {
        return new Object2ObjectOpenHashMap<>();
    }

    default <X> X requireNonNull(String propertyName, @Nullable X val) throws ParseErrorException
    {
        if (val == null)
            throw new ParseErrorException(propertyName, "requireNonNull: Null value!");

        return val;
    }
}
