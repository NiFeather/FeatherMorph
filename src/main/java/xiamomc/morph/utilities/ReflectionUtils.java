package xiamomc.morph.utilities;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityDimensions;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

public class ReflectionUtils
{
    @Nullable
    public static Field getPlayerDimensionsField(ServerPlayer player)
    {
        return ReflectionUtils.getFields(player, EntityDimensions.class, true)
                .stream().filter(f -> !Modifier.isStatic(f.getModifiers())).findFirst().orElse(null);
    }

    public static List<Field> getFields(Object obj, Class<?> type, boolean searchBaseClass)
    {
        var clazz = obj.getClass();
        var fields = new ObjectArrayList<Field>();

        aaa(clazz, fields);

        return fields.stream().filter(f -> f.getType() == type).toList();
    }

    private static void aaa(Class<?> clazz, List<Field> fields)
    {
        fields.addAll(Arrays.stream(clazz.getDeclaredFields()).toList());

        if (clazz.getSuperclass() != null)
            aaa(clazz.getSuperclass(), fields);
    }
}
