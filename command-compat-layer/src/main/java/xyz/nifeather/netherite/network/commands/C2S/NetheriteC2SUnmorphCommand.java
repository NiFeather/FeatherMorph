package xyz.nifeather.netherite.network.commands.C2S;

import xyz.nifeather.netherite.network.BasicClientHandler;
import xyz.nifeather.netherite.network.annotations.Environment;
import xyz.nifeather.netherite.network.annotations.EnvironmentType;

public class NetheriteC2SUnmorphCommand extends NetheriteC2SCommand<String>
{
    @Override
    public String getBaseName()
    {
        return NetheriteC2SCommandNames.Unmorph;
    }

    @Environment(EnvironmentType.SERVER)
    @Override
    public void onCommand(BasicClientHandler<?> listener)
    {
        listener.onUnmorphCommand(this);
    }
}
