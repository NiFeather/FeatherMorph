package xiamomc.morph.network.commands.C2S;

import net.minecraft.server.MinecraftServer;
import org.bukkit.entity.Player;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.network.MorphClientHandler;
import xiamomc.pluginbase.Annotations.Resolved;

public abstract class AbstractC2SCommand extends MorphPluginObject
{
    @Resolved(shouldSolveImmediately = true)
    private MorphManager manager;

    protected MorphManager morphManager()
    {
        return manager;
    }

    @Resolved(shouldSolveImmediately = true)
    private MorphClientHandler clientHandler;

    protected MorphClientHandler clientHandler()
    {
        return clientHandler;
    }

    //public abstract NamespacedKey getChannelIdentifier();

    public abstract String getBaseName();

    public void onCommand(Player player, String[] arguments)
    {
    }

    public String buildCommand(String[] arguments)
    {
        return getBaseName();
    }
}
