package xyz.nifeather.netherite.network.commands.C2S;

import org.jetbrains.annotations.Nullable;
import xyz.nifeather.netherite.network.BasicClientHandler;
import xyz.nifeather.netherite.network.annotations.Environment;
import xyz.nifeather.netherite.network.annotations.EnvironmentType;

public class NetheriteC2SMorphCommand extends NetheriteC2SCommand<String>
{
    public NetheriteC2SMorphCommand(@Nullable String identifier)
    {
        super(identifier == null ? "" : identifier);
    }

    @Override
    public String getBaseName()
    {
        return NetheriteC2SCommandNames.Morph;
    }

    @Environment(EnvironmentType.SERVER)
    @Override
    public void onCommand(BasicClientHandler<?> listener)
    {
        listener.onMorphCommand(this);
    }
}
