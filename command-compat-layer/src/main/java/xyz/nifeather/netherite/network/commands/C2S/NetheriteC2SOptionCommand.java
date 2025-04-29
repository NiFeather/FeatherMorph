package xyz.nifeather.netherite.network.commands.C2S;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.netherite.network.BasicClientHandler;
import xyz.nifeather.netherite.network.annotations.Environment;
import xyz.nifeather.netherite.network.annotations.EnvironmentType;

import java.util.Arrays;
import java.util.Objects;

public class NetheriteC2SOptionCommand extends NetheriteC2SCommand<NetheriteC2SOptionCommand.ClientOptions>
{
    public NetheriteC2SOptionCommand(@NotNull ClientOptions option)
    {
        super(option);
    }

    @Environment(EnvironmentType.SERVER)
    @Override
    public void onCommand(BasicClientHandler<?> listener)
    {
        listener.onOptionCommand(this);
    }

    private String value;

    public NetheriteC2SOptionCommand setValue(String value)
    {
        this.value = value;

        return this;
    }

    public NetheriteC2SOptionCommand setValue(Boolean value)
    {
        return this.setValue(value ? "true" : "false");
    }

    public ClientOptions getOption()
    {
        return getArgumentAt(0);
    }

    public String getValue()
    {
        return value;
    }

    @Nullable
    public static NetheriteC2SOptionCommand fromString(String rawArgs)
    {
        var spilt = rawArgs.split(" ", 2);

        if (spilt.length < 2) return null;

        var option = Arrays.stream(ClientOptions.values())
                .filter(v -> v.networkName.equalsIgnoreCase(spilt[0]))
                .findFirst().orElse(null);

        if (option == null) return null;

        var value = spilt[1];

        var instance = new NetheriteC2SOptionCommand(option);
        instance.setValue(value);

        return instance;
    }

    @Override
    public String getBaseName()
    {
        return NetheriteC2SCommandNames.Option;
    }

    @Override
    public String buildCommand()
    {
        return getBaseName() + " " + Objects.requireNonNull(getArgumentAt(0)).networkName + " " + value;
    }

    public enum ClientOptions
    {
        CLIENTVIEW("clientview"),
        HUD("hud");

        private ClientOptions(String optionNetworkName)
        {
            this.networkName = optionNetworkName;
        }

        public final String networkName;
    }
}
