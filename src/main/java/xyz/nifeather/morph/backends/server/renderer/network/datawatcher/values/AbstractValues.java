package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataType;
import org.slf4j.Logger;
import xyz.nifeather.morph.FeatherMorphMain;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractValues
{
    private int currentIndex = 0;
    protected <X> SingleValue<X> createSingle(String name, X val, EntityDataType<X> dataType)
    {
        if (val == null)
            throw new IllegalArgumentException("May not pass a null value to getIndex()");

        var sv = SingleValue.of(name, currentIndex, val, dataType);

        currentIndex++;
        return sv;
    }

    protected final Logger logger = FeatherMorphMain.getInstance().getSLF4JLogger();

    protected final List<SingleValue<?>> values = new CopyOnWriteArrayList<>();

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

        values.add(value);
    }

    public List<SingleValue<?>> getValues()
    {
        return values;
    }
}
