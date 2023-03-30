package xiamomc.morph.network.server.commands.C2S;

import org.bukkit.entity.Player;

public class C2SMorphCommand extends MorphC2SCommand
{
    @Override
    public String getBaseName()
    {
        return "morph";
    }

    @Override
    public void onCommand(Player player, String str)
    {
        var manager = morphManager();
        String id = str;

        if (id.isEmpty() || id.isBlank())
            manager.doQuickDisguise(player, null);
        else if (manager.canMorph(player))
            manager.morph(player, id, player.getTargetEntity(5));
    }
}
