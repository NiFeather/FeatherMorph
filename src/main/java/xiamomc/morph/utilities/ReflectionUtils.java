package xiamomc.morph.utilities;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityDimensions;
import org.jetbrains.annotations.Nullable;

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

    @Nullable
    public static Field getPlayerDimensionsField(ServerPlayer player)
    {
        var field = playerDimensionFieldMap.getOrDefault(player, null);
        if (field != null) return field;

        field = ReflectionUtils.getFields(player, EntityDimensions.class, true)
                .stream().filter(f -> !Modifier.isStatic(f.getModifiers()))
                .findFirst().orElse(null);

        playerDimensionFieldMap.put(player, field);

        return field;
    }

    private static final Map<ServerPlayer, Field> playerEHFieldMap = new Object2ObjectOpenHashMap<>();

    private static final String playerEyeHeightFieldName = "bf";

    @Nullable
    public static Field getPlayerEyeHeightField(ServerPlayer player)
    {
        var field = playerEHFieldMap.getOrDefault(player, null);
        if (field != null) return field;

        field = ReflectionUtils.getFields(player, float.class, true)
                .stream().filter(f -> !Modifier.isStatic(f.getModifiers()) && Modifier.isPrivate(f.getModifiers()) && f.getName().equals(playerEyeHeightFieldName))
                .findFirst().orElse(null);

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
