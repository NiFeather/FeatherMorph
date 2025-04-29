package xyz.nifeather.netherite.network.commands.S2C.clientrender;

import xyz.nifeather.netherite.network.BasicServerHandler;
import xyz.nifeather.netherite.network.commands.S2C.NetheriteS2CCommand;
import xyz.nifeather.netherite.network.commands.S2C.NetheriteS2CCommandNames;

public class NetheriteS2CRenderMapRemoveCommand extends NetheriteS2CCommand<Integer>
{
    public NetheriteS2CRenderMapRemoveCommand(Integer playerNetworkId)
    {
        super(playerNetworkId);
    }

    @Override
    public String getBaseName()
    {
        return NetheriteS2CCommandNames.CRRemove;
    }

    @Override
    public void onCommand(BasicServerHandler<?> handler)
    {
        handler.onClientMapRemoveCommand(this);
    }

    public boolean isValid()
    {
        return getArgumentAt(0, -1) != -1;
    }

    public int getPlayerNetworkId()
    {
        if (!isValid()) throw new IllegalArgumentException("Trying to get a network id from an invalid packet.");

        return getArgumentAt(0, -1);
    }

    private static final NetheriteS2CRenderMapRemoveCommand invalidPacket = new NetheriteS2CRenderMapRemoveCommand(-1);

    public static NetheriteS2CRenderMapRemoveCommand of(Integer networkId)
    {
        return new NetheriteS2CRenderMapRemoveCommand(networkId);
    }

    public static NetheriteS2CRenderMapRemoveCommand of(String arg)
    {
        var argSplit = arg.split(" ");

        int networkId = -1;

        try
        {
            networkId = Integer.parseInt(argSplit[0]);
        }
        catch (Throwable t)
        {
            return invalidPacket;
        }

        return NetheriteS2CRenderMapRemoveCommand.of(networkId);
    }
}
