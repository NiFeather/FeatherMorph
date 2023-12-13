package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import xiamomc.morph.MorphPlugin;

import java.util.List;

public abstract class AbstractValues
{
    protected final List<SingleValue<?>> values = new ObjectArrayList<>();

    protected void registerValue(SingleValue<?>... value)
    {
        for (SingleValue<?> singleValue : value)
            registerSingle(singleValue);
    }

    protected void registerSingle(SingleValue<?> value)
    {
        var duplicateValue = values.stream().filter(sv -> sv.index() == value.index()).findFirst().orElse(null);
        if (duplicateValue != null)
            throw new IllegalArgumentException("Already contains a value with index '%s'".formatted(value.index()));

        try
        {
            WrappedDataWatcher.Registry.get(value.type());
        }
        catch (Throwable t)
        {
            MorphPlugin.getInstance().getSLF4JLogger().warn("No serializer for type '%s'!".formatted(value.type()));
        }

        values.add(value);
    }

    public List<SingleValue<?>> getValues()
    {
        return values;
    }
}
