package xiamomc.morph.utilities;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityDimensions;
import xiamomc.pluginbase.Exceptions.NullDependencyException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ReflectionUtils
{
    public static void cleanCaches()
    {
        var toRemove = new ObjectArrayList<ServerPlayer>();
        playerDimensionFieldMap.forEach((s, f) ->
        {
            if (!s.getBukkitEntity().isOnline())
                toRemove.add(s);
        });

        playerEHFieldMap.forEach((s, f) ->
        {
            if (!s.getBukkitEntity().isOnline())
                toRemove.add(s);
        });

        for (ServerPlayer serverPlayer : toRemove)
        {
            playerDimensionFieldMap.remove(serverPlayer);
            playerEHFieldMap.remove(serverPlayer);
        }
    }

    private static final Map<ServerPlayer, Field> playerDimensionFieldMap = new Object2ObjectOpenHashMap<>();

    // private EntitySize bh; //Entity#dimensions
    // private float bi; //Entity#eyeHeight
    // 特征：EntitySize下面跟一个float和三个boolean，上面是个long
    private static final String dimensionsFieldName = "dimensions";
    private static final String playerEyeHeightFieldName = "eyeHeight";

    public static Field getPlayerDimensionsField(ServerPlayer player)
            throws NullDependencyException
    {
        var field = playerDimensionFieldMap.getOrDefault(player, null);
        if (field != null) return field;

        field = ReflectionUtils.getFields(player, EntityDimensions.class, true)
                .stream().filter(f ->
                {
                    return !Modifier.isStatic(f.getModifiers())
                            && f.getName().equals(dimensionsFieldName);
                })
                .findFirst().orElse(null);

        if (field == null)
            throw new NullDependencyException("Unable to find player dimension: No such field 'EntitySize %s'".formatted(dimensionsFieldName));

        playerDimensionFieldMap.put(player, field);
        field.setAccessible(true);

        return field;
    }

    private static final Map<ServerPlayer, Field> playerEHFieldMap = new Object2ObjectOpenHashMap<>();

    public static Field getPlayerEyeHeightField(ServerPlayer player)
            throws NullDependencyException
    {
        var field = playerEHFieldMap.getOrDefault(player, null);
        if (field != null) return field;

        field = ReflectionUtils.getFields(player, float.class, true)
                .stream().filter(f ->
                {
                    return !Modifier.isStatic(f.getModifiers())
                            && Modifier.isPrivate(f.getModifiers())
                            && f.getName().equals(playerEyeHeightFieldName);
                })
                .findFirst().orElse(null);

        if (field == null)
            throw new NullDependencyException("Unable to find player eye height: No such field 'EntitySize %s'".formatted(playerEyeHeightFieldName));

        field.setAccessible(true);

        playerEHFieldMap.put(player, field);
        return field;
    }

    public static List<Field> getFields(Object obj, Class<?> type, boolean searchBaseClass)
    {
        var clazz = obj.getClass();
        var fields = new ObjectArrayList<Field>();

        searchFields(clazz, fields);

        return fields.stream().filter(f -> f.getType() == type).toList();
    }

    private static void searchFields(Class<?> clazz, List<Field> fields)
    {
        fields.addAll(Arrays.stream(clazz.getDeclaredFields()).toList());

        if (clazz.getSuperclass() != null)
            searchFields(clazz.getSuperclass(), fields);
    }
}
