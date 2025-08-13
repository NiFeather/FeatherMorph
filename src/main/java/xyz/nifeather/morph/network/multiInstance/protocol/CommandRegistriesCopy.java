package xyz.nifeather.morph.network.multiInstance.protocol;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.network.multiInstance.protocol.c2s.MIC2SCommand;
import xyz.nifeather.morph.network.multiInstance.protocol.s2c.MIS2CCommand;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class CommandRegistriesCopy
{
    private final Object2ObjectArrayMap<String, Function<Map<String, String>, MIC2SCommand>> c2sCmds = new Object2ObjectArrayMap<>();
    private final Object2ObjectArrayMap<String, Function<Map<String, String>, MIS2CCommand>> s2cCmds = new Object2ObjectArrayMap<>();

    /**
     *
     * @param name
     * @param function args -> function
     * @return
     */
    public CommandRegistriesCopy registerC2S(String name, Function<Map<String, String>, MIC2SCommand> function)
    {
        c2sCmds.put(name, function);

        return this;
    }

    public CommandRegistriesCopy registerS2C(String name, Function<Map<String, String>, MIS2CCommand> function)
    {
        s2cCmds.put(name, function);

        return this;
    }

    @NotNull
    public MIS2CCommand createS2CCommand(String baseName, Map<String, String> args) throws RuntimeException
    {
        var func = s2cCmds.getOrDefault(baseName, null);
        return Objects.requireNonNull(func, "No Func found for command name '%s'".formatted(baseName)).apply(args);
    }

    @NotNull
    public MIC2SCommand createC2SCommand(String baseName, Map<String, String> args) throws RuntimeException
    {
        var func = c2sCmds.getOrDefault(baseName, null);
        return Objects.requireNonNull(func, "No Func found for command name '%s'".formatted(baseName)).apply(args);
    }
}

