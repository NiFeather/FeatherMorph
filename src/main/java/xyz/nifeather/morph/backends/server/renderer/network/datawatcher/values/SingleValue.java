package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataType;
import org.jetbrains.annotations.NotNull;

public class SingleValue<T>
{
    private final EntityDataType<T> type;

    public EntityDataType<T> type()
    {
        return type;
    }

    private final int index;

    public int index()
    {
        return index;
    }

    private final T defaultValue;

    @NotNull
    public T defaultValue()
    {
        return defaultValue;
    }

    private final String name;

    public String name()
    {
        return name;
    }

    public SingleValue(String name, EntityDataType<T> type, int index, @NotNull T defaultValue)
    {
        this.name = name;
        this.type = type;
        this.index = index;
        this.defaultValue = defaultValue;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!(obj instanceof SingleValue<?> other)) return false;

        return this.index == other.index && this.type.equals(other.type);
    }

    public boolean equalsStrict(Object obj)
    {
        if (this.equals(obj)) return true;
        if (!(obj instanceof SingleValue<?> other)) return false;

        return this.index == other.index
                && this.type.equals(other.type)
                && this.name.equals(other.name)
                && this.defaultValue.equals(other.defaultValue);
    }

    @Override
    public String toString()
    {
        return "SingleValue[name='%s', type='%s', index='%s']@%s".formatted(name, type, index, this.hashCode());
    }

    public static <TVal> SingleValue<TVal> of(String name, int index, @NotNull TVal val, EntityDataType<TVal> dataType)
    {
        return new SingleValue<>(name, dataType, index, val);
    }
}
