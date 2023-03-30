package xiamomc.morph.network.server.commands.C2S;

import org.bukkit.entity.Player;

public class C2SUnmorphCommand extends MorphC2SCommand
{
    @Override
    public String getBaseName()
    {
        return "unmorph";
    }

    @Override
    public void onCommand(Player player, String arguments)
    {
        var manager = morphManager();

        if (manager.getDisguiseStateFor(player) != null)
            manager.unMorph(player);
    }
}
