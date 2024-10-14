package xyz.nifeather.morph.backends;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;

public class WrapperAttribute<T>
{
    public static final WrapperAttribute<String> disguiseIdentifier = new WrapperAttribute<>("disguiseIdentifier", (o) -> "nil");
    public static final WrapperAttribute<Boolean> showArms = new WrapperAttribute<>("show_arms", (o) -> false);
    public static final WrapperAttribute<Optional<GameProfile>> profile = new WrapperAttribute<>("profile", (o) -> Optional.empty());
    public static final WrapperAttribute<CompoundTag> nbt = new WrapperAttribute<>("nbt", (o) -> new CompoundTag());
    public static final WrapperAttribute<Boolean> displayFakeEquip = new WrapperAttribute<>("display_fake_equip", (o) -> false);
    public static final WrapperAttribute<String> disguiseName = new WrapperAttribute<>("disguise_name", (o) -> "");
    public static final WrapperAttribute<Boolean> saddled = new WrapperAttribute<>("saddled", (o) -> false);

    @NotNull
    private final Function<Object, T> defaultFunc;

    private final String id;

    public WrapperAttribute(String id, @NotNull Function<Object, T> defaultFunc)
    {
        this.id = id;
        this.defaultFunc = defaultFunc;
    }

    public T createDefault()
    {
        return defaultFunc.apply(null);
    }

    public String getIdentifier()
    {
        return id;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof WrapperAttribute<?> other)) return false;

        return this.id.equals(other.id);
    }
}
