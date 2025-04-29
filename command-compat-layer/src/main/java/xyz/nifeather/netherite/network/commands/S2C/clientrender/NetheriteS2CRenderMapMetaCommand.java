package xyz.nifeather.netherite.network.commands.S2C.clientrender;

import xyz.nifeather.netherite.network.BasicServerHandler;
import xyz.nifeather.netherite.network.commands.S2C.NetheriteS2CCommand;
import xyz.nifeather.netherite.network.commands.S2C.NetheriteS2CCommandNames;

public class NetheriteS2CRenderMapMetaCommand extends NetheriteS2CCommand<NetheriteS2CRenderMeta>
{
    public NetheriteS2CRenderMapMetaCommand(NetheriteS2CRenderMeta meta)
    {
        super(meta);
    }

    @Override
    public String getBaseName()
    {
        return NetheriteS2CCommandNames.CRMeta;
    }

    @Override
    public void onCommand(BasicServerHandler<?> listener)
    {
        listener.onClientMapMetaNbtCommand(this);
    }

    public static NetheriteS2CRenderMapMetaCommand fromStr(String arg)
    {
        var meta = NetheriteS2CRenderMeta.fromString(arg);
        return new NetheriteS2CRenderMapMetaCommand(meta);
    }
}

