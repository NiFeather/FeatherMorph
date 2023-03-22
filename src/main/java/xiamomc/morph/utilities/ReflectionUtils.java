package xiamomc.morph.utilities;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class ReflectionUtils
{
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
