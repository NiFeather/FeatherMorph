package xiamomc.morph.misc;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class DisguiseState
{
    /**
     * 谁在伪装
     */
    public Player player;

    /**
     * 伪装的显示名称
     */
    public Component displayName;

    /**
     * 伪装的实例
     */
    public Disguise disguise;
}
