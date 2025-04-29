package xyz.nifeather.netherite.network.commands.S2C.map;

import xyz.nifeather.netherite.network.BasicServerHandler;
import xyz.nifeather.netherite.network.commands.S2C.NetheriteS2CCommand;
import xyz.nifeather.netherite.network.commands.S2C.NetheriteS2CCommandNames;

public class NetheriteS2CMapRemoveCommand extends NetheriteS2CCommand<Integer>
{
    public NetheriteS2CMapRemoveCommand(int id)
    {
        super(id);
    }

    @Override
    public String getBaseName()
    {
        return NetheriteS2CCommandNames.MapRemove;
    }

    public int getTargetId()
    {
        return super.getArgumentAt(0, -1);
    }

    @Override
    public void onCommand(BasicServerHandler<?> listener)
    {
        listener.onMapRemoveCommand(this);
    }
}
