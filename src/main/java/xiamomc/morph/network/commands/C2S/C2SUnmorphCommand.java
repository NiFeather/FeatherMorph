package xiamomc.morph.network.commands.C2S;

import org.bukkit.entity.Player;

public class C2SUnmorphCommand extends AbstractC2SCommand
{
    @Override
    public String getBaseName()
    {
        return "unmorph";
    }

    @Override
    public void onCommand(Player player, String[] arguments)
    {
        var manager = morphManager();

        if (manager.getDisguiseStateFor(player) != null)
            manager.unMorph(player);
    }
}
