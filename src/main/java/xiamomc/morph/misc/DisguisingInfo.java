package xiamomc.morph.misc;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class DisguisingInfo
{
    public Player player;
    public Component displayName;
    public Disguise disguise;

    /**
     * 伪装时是否坐着
     */
    public boolean startSitting;
}
