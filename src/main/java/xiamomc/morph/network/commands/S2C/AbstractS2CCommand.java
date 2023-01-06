package xiamomc.morph.network.commands.S2C;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPluginObject;

import java.util.Arrays;
import java.util.List;

public abstract class AbstractS2CCommand<T> extends MorphPluginObject
{
    public AbstractS2CCommand(T argument)
    {
        this.arguments = toList(argument);
    }

    public AbstractS2CCommand(T[] arguments)
    {
        this.arguments = toList(arguments);
    }

    @NotNull
    protected List<T> arguments;

    @SafeVarargs
    private List<T> toList(T... elements)
    {
        return Arrays.stream(elements).toList();
    }

    public abstract String getBaseName();

    public void onCommand(Player player, String[] arguments) { }

    public String buildCommand()
    {
        return getBaseName();
    }

    //region Utilities
    @Nullable
    protected T getArgumentAt(int index)
    {
        return index >= arguments.size() ? null : arguments.get(index);
    }

    @NotNull
    protected T getArgumentAt(int index, @NotNull T defaultValue)
    {
        var val = this.getArgumentAt(index);

        return val == null ? defaultValue : val;
    }
    //endregion Utilities
}
