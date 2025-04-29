package xyz.nifeather.netherite.network.commands.C2S;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.netherite.network.BasicClientHandler;
import xyz.nifeather.netherite.network.annotations.Environment;
import xyz.nifeather.netherite.network.annotations.EnvironmentType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class NetheriteC2SCommand<T>
{
    public abstract String getBaseName();

    public NetheriteC2SCommand()
    {
        arguments = new ArrayList<>();
    }

    public NetheriteC2SCommand(@Nullable T argument)
    {
        this.arguments = toList(argument);
    }

    public NetheriteC2SCommand(@Nullable T[] arguments)
    {
        this.arguments = toList(arguments);
    }

    @NotNull
    protected List<T> arguments;

    protected List<T> toList(T... elements)
    {
        if (elements == null)
            return new ArrayList<>();

        return Arrays.stream(elements).toList();
    }

    @Environment(EnvironmentType.SERVER)
    public abstract void onCommand(BasicClientHandler<?> listener);

    private Object owner;
    public <TPlayer> void setOwner(TPlayer player)
    {
        this.owner = player;
    }

    public <TPlayer> TPlayer getOwner()
    {
        return (TPlayer) owner;
    }

    public String buildCommand()
    {
        return (getBaseName() + " " + serializeArguments()).trim();
    }

    //region Utilities

    public String serializeArguments()
    {
        if (arguments.size() == 0) return "";

        var builder = new StringBuilder();
        for (T argument : arguments)
            builder.append(argument).append(" ");

        return builder.toString();
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
