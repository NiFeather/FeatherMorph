package xyz.nifeather.netherite.network.commands.S2C;

import xyz.nifeather.netherite.network.BasicServerHandler;
import xyz.nifeather.netherite.network.annotations.Environment;
import xyz.nifeather.netherite.network.annotations.EnvironmentType;

import java.util.Arrays;

public class NetheriteS2CRequestCommand extends NetheriteS2CCommand<String>
{
    @Environment(EnvironmentType.CLIENT)
    public NetheriteS2CRequestCommand(String rawArgs)
    {
        super(rawArgs.split(" "));
    }

    public NetheriteS2CRequestCommand(NetheriteRequestType requestNetheriteRequestType, String source)
    {
        super(new String[]{requestNetheriteRequestType.commandName, source});
    }

    @Override
    public String getBaseName()
    {
        return NetheriteS2CCommandNames.Request;
    }

    @Environment(EnvironmentType.CLIENT)
    public NetheriteRequestType netheriteRequestType;

    @Environment(EnvironmentType.CLIENT)
    public String sourcePlayer;

    @Environment(EnvironmentType.CLIENT)
    @Override
    public void onCommand(BasicServerHandler<?> handler)
    {
        var typeStr = getArgumentAt(0, "?");

        System.out.println("typeStr: " + typeStr);

        this.netheriteRequestType = Arrays.stream(NetheriteRequestType.values()).filter(t -> t.commandName.equalsIgnoreCase(typeStr))
                    .findFirst().orElse(NetheriteRequestType.Unknown);

        this.sourcePlayer = getArgumentAt(1, "Unknown source");

        handler.onExchangeRequestReceive(this);
    }

    public enum NetheriteRequestType
    {
        NewRequest(NetheriteS2CCommandNames.RequestNew),
        RequestSend(NetheriteS2CCommandNames.RequestSend),
        RequestExpired(NetheriteS2CCommandNames.RequestExpire),
        RequestExpiredOwner(NetheriteS2CCommandNames.RequestExpireOwner),

        RequestAccepted(NetheriteS2CCommandNames.RequestAccept),
        RequestDenied(NetheriteS2CCommandNames.RequestDenied),
        Unknown("unknown_type");

        public final String commandName;

        public boolean isRequestOwner()
        {
            return this == RequestExpiredOwner || this == RequestSend || this == RequestAccepted || this == RequestDenied;
        }

        NetheriteRequestType(String cmdName)
        {
            this.commandName = cmdName;
        }
    }
}
