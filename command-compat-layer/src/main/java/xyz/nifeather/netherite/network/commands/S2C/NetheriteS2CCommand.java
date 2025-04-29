package xyz.nifeather.netherite.network.commands.S2C;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.netherite.network.BasicServerHandler;
import xyz.nifeather.netherite.network.annotations.Environment;
import xyz.nifeather.netherite.network.annotations.EnvironmentType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class NetheriteS2CCommand<T>
{
    public NetheriteS2CCommand()
    {
        arguments = new ArrayList<>();
    }

    public NetheriteS2CCommand(@Nullable T argument)
    {
        this.arguments = toList(argument);
    }

    public NetheriteS2CCommand(@Nullable T[] arguments)
    {
        this.arguments = toList(arguments);
    }

    @NotNull
    protected List<T> arguments;

    @SafeVarargs
    private List<T> toList(T... elements)
    {
        if (elements == null)
            return new ArrayList<>();

        return Arrays.stream(elements).toList();
    }

    public abstract String getBaseName();

    @Environment(EnvironmentType.CLIENT)
    public abstract void onCommand(BasicServerHandler<?> handler);

    public String buildCommand()
    {
        return (getBaseName() + " " + serializeArguments()).trim();
    }

    //region Utilities

    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    protected static Gson gson()
    {
        return gson;
    }

    public String serializeArguments()
    {
        if (arguments.size() == 0) return "";

        var builder = new StringBuilder();
        for (T argument : arguments)
            builder.append(argument).append(" ");

        return builder.toString().trim();
    }

    @Nullable
    public T getArgumentAt(int index)
    {
        return index >= arguments.size() ? null : arguments.get(index);
    }

    @NotNull
    public T getArgumentAt(int index, @NotNull T defaultValue)
    {
        var val = this.getArgumentAt(index);

        return val == null ? defaultValue : val;
    }
    //endregion Utilities
}
