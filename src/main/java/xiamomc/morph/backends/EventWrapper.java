package xiamomc.morph.backends;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class EventWrapper<TInstance> extends DisguiseWrapper<TInstance>
{
    public EventWrapper(@NotNull TInstance tInstance, DisguiseBackend<TInstance, ? extends DisguiseWrapper<TInstance>> backend)
    {
        super(tInstance, backend);
    }

    protected final Map<WrapperEvent<?>, List<ActionRecord<?>>> eventListMap = new Object2ObjectArrayMap<>();

    public record ActionRecord<T>(Object source, Consumer<T> consumer)
    {
    }

    protected <T> void callEvent(WrapperEvent<T> wrapperEvent, T value)
    {
        var list = eventListMap.getOrDefault(wrapperEvent, null);
        if (list == null) return;

        list.forEach(ar -> ((Consumer<T>)ar.consumer()).accept(value));
    }

    @Override
    public <T> void subscribeEvent(Object source, WrapperEvent<T> wrapperEvent, Consumer<T> c)
    {
        var list = eventListMap.getOrDefault(wrapperEvent, null);

        if (list == null)
        {
            list = new ObjectArrayList<>();
            eventListMap.put(wrapperEvent, list);
        }

        if (list.stream().noneMatch(ar -> ar.source() == source))
            list.add(new ActionRecord<>(source, c));
    }

    @Override
    public void unSubscribeEvent(Object source, WrapperEvent<?> wrapperEvent)
    {
        var list = eventListMap.getOrDefault(wrapperEvent, null);
        if (list == null) return;

        list.removeIf(ar -> ar.source() == source);
    }

    @Override
    public void dispose()
    {
        super.dispose();

        eventListMap.clear();
    }
}
