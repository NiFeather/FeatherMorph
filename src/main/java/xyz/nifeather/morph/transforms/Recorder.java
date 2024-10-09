package xyz.nifeather.morph.transforms;

import java.util.function.Consumer;

public class Recorder<T>
{
    private T val;

    public T get()
    {
        return val;
    }

    public void set(T val)
    {
        this.val = val;

        if (onUpdate != null)
            onUpdate.accept(val);
    }

    public Recorder(T val) {
        this.val = val;
    }

    public Consumer<T> onUpdate;

    public static <TValue> Recorder<TValue> of(TValue value) {
        return new Recorder<>(value);
    }
}
