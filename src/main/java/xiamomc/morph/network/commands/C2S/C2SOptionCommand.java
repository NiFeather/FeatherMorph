package xiamomc.morph.network.commands.C2S;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class C2SOptionCommand extends AbstractC2SCommand
{
    @Override
    public String getBaseName()
    {
        return "option";
    }

    @Override
    public void onCommand(Player player, String[] str)
    {
        if (str.length < 2) return;

        var node = str[1].split(" ", 2);

        if (node.length < 2) return;

        var baseName = node[0];
        var value = node[1];

        switch (baseName)
        {
            case "clientview" ->
            {
                var val = Boolean.parseBoolean(value);

                clientHandler().getPlayerOption(player).setClientSideSelfView(val);

                var state = morphManager().getDisguiseStateFor(player);
                if (state != null) state.setServerSideSelfVisible(!val);
            }
            case "hud" ->
            {
                var val = Boolean.parseBoolean(value);
                var option = clientHandler().getPlayerOption(player);

                if (option.displayDisguiseOnHUD != val)
                {
                    clientHandler().getPlayerOption(player).displayDisguiseOnHUD = Boolean.parseBoolean(value);
                    player.sendActionBar(Component.empty());
                }
            }
        }
    }
}
