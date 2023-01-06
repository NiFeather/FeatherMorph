package xiamomc.morph.network.commands.C2S;

import org.bukkit.entity.Player;
import xiamomc.morph.network.commands.S2C.S2CDenyCommand;

public class C2SMorphCommand extends AbstractC2SCommand
{
    @Override
    public String getBaseName()
    {
        return "morph";
    }

    @Override
    public void onCommand(Player player, String[] str)
    {
        var manager = morphManager();
        String id = str.length == 2 ? str[1] : "";

        if (id.isEmpty() || id.isBlank())
            manager.doQuickDisguise(player, null);
        else if (manager.canMorph(player))
            manager.morph(player, id, player.getTargetEntity(5));
        else
            clientHandler().sendClientCommand(player, new S2CDenyCommand("morph"));
    }
}
