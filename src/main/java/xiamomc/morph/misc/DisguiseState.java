package xiamomc.morph.misc;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DisguiseState
{
    public DisguiseState(Player player, Disguise disguiseInstance)
    {
        this.player = player;
        this.playerUniqueID = player.getUniqueId();
        this.setDisguise(disguiseInstance);
    }

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
    private Component displayName;

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

    private int flag;
    public int getFlag()
    {
        return flag;
    }

    public void setDisguise(Disguise d)
    {
        if (!DisguiseUtils.isTracing(d))
            throw new RuntimeException("此Disguise不能由插件管理");

        disguise = d;

        displayName = d.isPlayerDisguise()
                ? Component.text(((PlayerDisguise) d).getName())
                : Component.translatable(d.getType().getEntityType().translationKey());

        this.flag = 0;

        var disgType = d.getType().getEntityType();

        if (EntityTypeUtils.canBreatheUnderWater(disgType))
            this.flag |= canBreatheUnderWater;

        if (EntityTypeUtils.hasFireResistance(disgType))
            this.flag |= hasFireResistance;

        if (EntityTypeUtils.takesDamageFromWater(disgType))
            this.flag |= takesDamageFromWater;

        if (EntityTypeUtils.burnsUnderSun(disgType))
            this.flag |= burnsUnderSun;

        if (EntityTypeUtils.alwaysNightVision(disgType))
            this.flag |= alwaysNightVision;

        if (EntityTypeUtils.canFly(disgType))
            this.flag |= canFly;
    }

    //region flag
    public static final int canBreatheUnderWater = 1 << 0;
    public static final int hasFireResistance = 1 << 1;
    public static final int canFly = 1 << 2;
    public static final int burnsUnderSun = 1 << 3;
    public static final int takesDamageFromWater = 1 << 4;
    public static final int alwaysNightVision = 1 << 5;

    public boolean isFlagSet(int value)
    {
        return (flag & value) == value;
    }
    //endregion flag
}
