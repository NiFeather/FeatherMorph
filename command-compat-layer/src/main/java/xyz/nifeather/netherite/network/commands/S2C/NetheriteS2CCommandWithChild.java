package xyz.nifeather.netherite.network.commands.S2C;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.netherite.network.BasicServerHandler;
import xyz.nifeather.netherite.network.annotations.Environment;
import xyz.nifeather.netherite.network.annotations.EnvironmentType;
import xyz.nifeather.netherite.network.commands.S2C.set.NetheriteS2CSetCommand;

import java.util.Map;
import java.util.function.Function;

public abstract class NetheriteS2CCommandWithChild<T> extends NetheriteS2CCommand<T>
{
    public NetheriteS2CCommandWithChild(T... arguments)
    {
        super(arguments);
    }

    private final Map<String, Function<String, NetheriteS2CSetCommand<?>>> nameToCmdMap = new Object2ObjectOpenHashMap<>();

    protected NetheriteS2CCommandWithChild<T> register(String baseName, Function<String, NetheriteS2CSetCommand<?>> func)
    {
        nameToCmdMap.put(baseName, func);

        return this;
    }

    @Environment(EnvironmentType.CLIENT)
    @Override
    public final void onCommand(BasicServerHandler<?> handler)
    {
        var rawArguments = serializeArguments();

        var arguments = rawArguments.split(" ", 2);
        if (arguments.length < 1) return;

        var subBaseName = arguments[0];
        var subCmd = nameToCmdMap.getOrDefault(subBaseName, null).apply(arguments.length == 2 ? arguments[1] : "");

        if (subCmd != null)
        {
            subCmd.onCommand(handler);
            return;
        }

        onCommandUnknown(rawArguments);
    }

    @Nullable
    public NetheriteS2CSetCommand<?> getCommand(String args)
    {
        var str = args.split(" ", 2);

        if (str.length < 1) return null;
        var baseName = str[0];

        var func = nameToCmdMap.getOrDefault(baseName, null);
        return func == null ? null : func.apply(str.length == 2 ? str[1] : "");
    }

    protected void onCommandUnknown(String rawArguments)
    {
    }
}
