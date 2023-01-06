package xiamomc.morph.network.commands.C2S;

import org.bukkit.entity.Player;
import xiamomc.morph.network.commands.S2C.S2CDenyCommand;

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
        else
            clientHandler().sendClientCommand(player, new S2CDenyCommand("morph"));
    }
}
