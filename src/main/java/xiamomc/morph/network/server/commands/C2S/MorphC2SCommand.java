package xiamomc.morph.network.server.commands.C2S;

import org.bukkit.entity.Player;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.network.commands.C2S.AbstractC2SCommand;
import xiamomc.morph.network.server.MorphClientHandler;
import xiamomc.pluginbase.Managers.DependencyManager;

public abstract class MorphC2SCommand extends AbstractC2SCommand<Player, String>
{
    private static boolean deps;
    private static MorphManager morphManager;
    private static MorphClientHandler morphClientHandler;

    protected MorphC2SCommand()
    {
        if (!deps)
        {
            var depMgr = DependencyManager.getInstance(MorphPlugin.getMorphNameSpace());
            morphManager = depMgr.get(MorphManager.class);
            morphClientHandler = depMgr.get(MorphClientHandler.class);

            deps = true;
        }
    }

    protected MorphClientHandler clientHandler()
    {
        return morphClientHandler;
    }

    protected MorphManager morphManager()
    {
        return morphManager;
    }

    @Override
    public String buildCommand()
    {
        throw new RuntimeException("No C2S Command shall be built on server environment!");
    }
}
