package xyz.nifeather.netherite.network.commands.S2C.clientrender;

import xyz.nifeather.netherite.network.BasicServerHandler;
import xyz.nifeather.netherite.network.commands.S2C.NetheriteS2CCommand;
import xyz.nifeather.netherite.network.commands.S2C.NetheriteS2CCommandNames;

public class NetheriteS2CRenderMapAddCommand extends NetheriteS2CCommand<String>
{
    public NetheriteS2CRenderMapAddCommand(Integer playerNetworkId, String mobId)
    {
        super(new String[]{ playerNetworkId.toString(), mobId });
    }

    @Override
    public String getBaseName()
    {
        return NetheriteS2CCommandNames.CRAdd;
    }

    @Override
    public void onCommand(BasicServerHandler<?> handler)
    {
        handler.onClientMapAddCommand(this);
    }

    public boolean isValid()
    {
        return getArgumentAt(0) != null && getArgumentAt(1) != null;
    }

    public int getPlayerNetworkId()
    {
        if (!isValid()) throw new IllegalArgumentException("Trying to get a network id from an invalid packet.");

        return Integer.parseInt(getArgumentAt(0, "-1"));
    }

    public String getMobId()
    {
        if (!isValid()) throw new IllegalArgumentException("Trying to get mob id from an invalid packet.");

        return getArgumentAt(1, "morph:unknown");
    }

    private static final NetheriteS2CRenderMapAddCommand invalidPacket = new NetheriteS2CRenderMapAddCommand(-1, null);

    public static NetheriteS2CRenderMapAddCommand of(Integer networkId, String mobId)
    {
        return new NetheriteS2CRenderMapAddCommand(networkId, mobId);
    }

    public static NetheriteS2CRenderMapAddCommand of(String arg)
    {
        var argSplit = arg.split(" ");

        if (argSplit.length < 2)
            return invalidPacket;

        int networkId = -1;

        try
        {
            networkId = Integer.parseInt(argSplit[0]);
        }
        catch (Throwable t)
        {
            return invalidPacket;
        }

        return NetheriteS2CRenderMapAddCommand.of(networkId, argSplit[1]);
    }
}
