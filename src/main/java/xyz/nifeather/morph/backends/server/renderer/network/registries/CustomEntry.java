package xyz.nifeather.morph.backends.server.renderer.network.registries;

public class CustomEntry<T>
{
    public final Class<T> type;
    public final String name;

    private boolean requireNonNull;

    public boolean requireNonNull()
    {
        return requireNonNull;
    }

    public CustomEntry<T> doRequireNonNull()
    {
        this.requireNonNull = true;
        return this;
    }

    public CustomEntry(String name, Class<T> type)
    {
        this.name = name;
        this.type = type;
    }

    public static <X> CustomEntry<X> of(String name, X val)
    {
        return new CustomEntry<>(name, (Class<X>)val.getClass());
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
        if (!(obj instanceof CustomEntry<?> other)) return false;

        return other.name.equals(this.name);
    }
}
