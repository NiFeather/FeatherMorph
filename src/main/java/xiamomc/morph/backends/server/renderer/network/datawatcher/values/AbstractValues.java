package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.slf4j.Logger;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.backends.server.renderer.utilties.ProtocolRegistryUtils;

import java.util.List;

public abstract class AbstractValues
{
    private int currentIndex = 0;
    protected <X> SingleValue<X> getSingle(X val)
    {
        if (val == null)
            throw new IllegalArgumentException("May not pass a null value to getIndex()");

        var sv = SingleValue.of(currentIndex, val);

        currentIndex++;
        return sv;
    }

    protected final Logger logger = MorphPlugin.getInstance().getSLF4JLogger();

    protected final List<SingleValue<?>> values = new ObjectArrayList<>();

    protected void registerSingle(SingleValue<?>... value)
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
            ProtocolRegistryUtils.getSerializer(value);
        }
        catch (Throwable t)
        {
            logger.warn("No serializer for type '%s'!".formatted(value.type()));
        }

        values.add(value);
    }

    public List<SingleValue<?>> getValues()
    {
        return values;
    }
}
