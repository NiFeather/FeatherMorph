package xiamomc.morph.misc;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DisguiseState
{
    /**
     * 谁在伪装
     */
    private Player player;

    public Player getPlayer()
    {
        return player;
    }

    public void setPlayer(Player p)
    {
        if (!p.getUniqueId().equals(playerUniqueID)) throw new RuntimeException("玩家实例与UUID不符");

        player = p;
    }

    /**
     * 谁在伪装（UUID）
     */
    private final UUID playerUniqueID;

    public UUID getPlayerUniqueID()
    {
        return playerUniqueID;
    }

    /**
     * 伪装的显示名称
     */
    private final Component displayName;

    public Component getDisplayName()
    {
        return displayName;
    }

    /**
     * 伪装的实例
     */
    private Disguise disguise;

    public Disguise getDisguise()
    {
        return disguise;
    }

    public void setDisguise(Disguise d)
    {
        if (!DisguiseUtils.isTracing(d))
            throw new RuntimeException("此Disguise不能由插件管理");

        disguise = d;
    }

    public DisguiseState(Player player, Component displayName, Disguise disguiseInstance)
    {
        this.player = player;
        this.playerUniqueID = player.getUniqueId();
        this.disguise = disguiseInstance;
        this.displayName = displayName;
    }
}
