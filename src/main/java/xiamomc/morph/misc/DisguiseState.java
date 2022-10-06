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

    /**
     * 伪装技能冷却
     */
    private int abilityCooldown = 0;

    private int defaultCooldown;

    public int getAbilityCooldown()
    {
        return abilityCooldown;
    }

    public void setAbilityCooldown(int value)
    {
        abilityCooldown = value;
    }

    public void resetCooldown()
    {
        abilityCooldown = defaultCooldown;
    }
    public int getDefaultCooldown()
    {
        return defaultCooldown;
    }

    private short flag;
    public short getFlag()
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

        //更新技能Flag
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

        //更新冷却
        defaultCooldown = switch (disgType)
        {
            case ELDER_GUARDIAN -> 1200;
            case ENDER_DRAGON -> 100;
            case ENDERMAN, GHAST -> 40;
            case BLAZE -> 10;
            case SHULKER, DOLPHIN -> 80;
            default -> 20;
        };

        abilityCooldown = 40;
    }

    //region flag
    public static final short canBreatheUnderWater = 1 << 0;
    public static final short hasFireResistance = 1 << 1;
    public static final short canFly = 1 << 2;
    public static final short burnsUnderSun = 1 << 3;
    public static final short takesDamageFromWater = 1 << 4;
    public static final short alwaysNightVision = 1 << 5;

    public boolean isFlagSet(short value)
    {
        return (flag & value) == value;
    }
    //endregion flag
}
