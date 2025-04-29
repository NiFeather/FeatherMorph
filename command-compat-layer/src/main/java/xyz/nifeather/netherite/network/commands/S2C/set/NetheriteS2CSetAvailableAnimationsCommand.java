package xyz.nifeather.netherite.network.commands.S2C.set;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import xyz.nifeather.netherite.network.BasicServerHandler;

import java.util.List;

public class NetheriteS2CSetAvailableAnimationsCommand extends NetheriteS2CSetCommand<String>
{
    public NetheriteS2CSetAvailableAnimationsCommand()
    {
    }

    public NetheriteS2CSetAvailableAnimationsCommand(List<String> ids)
    {
        super(ids.toArray(new String[]{}));
    }

    public NetheriteS2CSetAvailableAnimationsCommand(String... ids)
    {
        super(ids);
    }

    @Override
    public String getBaseName()
    {
        return "avail_anim";
    }

    public List<String> getAvailableAnimations()
    {
        return new ObjectArrayList<>(this.getArguments());
    }

    @Override
    public void onCommand(BasicServerHandler<?> handler)
    {
        handler.onValidAnimationsCommand(this);
    }

    public static NetheriteS2CSetAvailableAnimationsCommand fromString(String arg)
    {
        return new NetheriteS2CSetAvailableAnimationsCommand(arg.split(" "));
    }
}
