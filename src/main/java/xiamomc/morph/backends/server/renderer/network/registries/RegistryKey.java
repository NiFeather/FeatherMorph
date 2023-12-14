package xiamomc.morph.backends.server.renderer.network.registries;

import org.jetbrains.annotations.NotNull;

public class RegistryKey<T>
{
    public final Class<T> type;
    public final String name;

    private boolean requireNonNull;

    public boolean requireNonNull()
    {
        return requireNonNull;
    }

    public RegistryKey<T> doRequireNonNull()
    {
        this.requireNonNull = true;
        return this;
    }

    public RegistryKey(String name, Class<T> type)
    {
        this.name = name;
        this.type = type;
    }

    public static <X> RegistryKey<X> of(String name, X val)
    {
        return new RegistryKey<>(name, (Class<X>)val.getClass());
    }

    @Override
    public String toString()
    {
        return "RegistryKey[name='%s', type='%s']".formatted(name, type);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!(obj instanceof RegistryKey<?> other)) return false;

        return other.name.equals(this.name);
    }
}
