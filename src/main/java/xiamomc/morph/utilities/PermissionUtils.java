package xiamomc.morph.utilities;

import org.bukkit.entity.Player;

public class PermissionUtils
{
    public static boolean hasPermission(Player player, String node, boolean defaultValue)
    {
        if (!player.isPermissionSet(node))
            return defaultValue;

        return player.hasPermission(node);
    }
}
