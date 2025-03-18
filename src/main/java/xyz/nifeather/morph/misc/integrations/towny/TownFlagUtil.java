package xyz.nifeather.morph.misc.integrations.towny;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class TownFlagUtil
{
    private static final Cache<String, CustomDataField<?>> fieldCache = CacheBuilder.newBuilder()
            .expireAfterAccess(1L, TimeUnit.HOURS)
            .build();

    public static <X extends CustomDataField<?>> X getOrPushField(String id, Class<X> type, Supplier<X> supplier)
    {
        var cache = getField(id, type);
        if (cache != null)
            return cache;

        var field = supplier.get();
        fieldCache.put(id, field);

        return field;
    }

    public static <X> X getField(String id, Class<X> type)
    {
        var cache = fieldCache.getIfPresent(id);
        if (cache == null)
            return null;

        if (type.isInstance(cache))
            return (X) cache;

        return null;
    }
}
