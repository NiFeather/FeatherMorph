package xiamomc.morph.misc;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.UUID;

public class DisguiseState
{
    public DisguiseState(Player player, Disguise disguiseInstance, boolean isClone)
    {
        this.player = player;
        this.playerUniqueID = player.getUniqueId();
        this.setDisguise(disguiseInstance, isClone);
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

    /**
     * 伪装被动技能Flag
     */
    private short abilityFlag;
    public short getAbilityFlag()
    {
        return abilityFlag;
    }

    /**
     * 伪装主动技能Flag
     */
    private boolean hasSkill;
    public boolean hasSkill()
    {
        return hasSkill;
    }

    /**
     * 要不要手动更新伪装Pose？
     */
    private boolean shouldHandlePose;
    public boolean isShouldHandlePose()
    {
        return shouldHandlePose;
    }

    public void setDisguise(Disguise d, boolean isClone)
    {
        if (!DisguiseUtils.isTracing(d))
            throw new RuntimeException("此Disguise不能由插件管理");

        disguise = d;
        this.shouldHandlePose = isClone;

        displayName = d.isPlayerDisguise()
                ? Component.text(((PlayerDisguise) d).getName())
                : Component.translatable(d.getType().getEntityType().translationKey());

        //更新技能Flag
        this.abilityFlag = 0;

        var disgType = d.getType().getEntityType();

        if (EntityTypeUtils.canBreatheUnderWater(disgType))
            this.abilityFlag |= canBreatheUnderWater;

        if (EntityTypeUtils.hasFireResistance(disgType))
            this.abilityFlag |= hasFireResistance;

        if (EntityTypeUtils.takesDamageFromWater(disgType))
            this.abilityFlag |= takesDamageFromWater;

        if (EntityTypeUtils.burnsUnderSun(disgType))
            this.abilityFlag |= burnsUnderSun;

        if (EntityTypeUtils.alwaysNightVision(disgType))
            this.abilityFlag |= alwaysNightVision;

        if (EntityTypeUtils.canFly(disgType))
            this.abilityFlag |= canFly;

        this.hasSkill = switch (disgType)
                {
                    case ENDERMAN, ENDER_DRAGON, GHAST, BLAZE, WITHER, PLAYER,
                            ARMOR_STAND, CREEPER, SHULKER, ELDER_GUARDIAN, DOLPHIN -> true;
                    default -> false;
                };

        //更新冷却
        defaultCooldown = switch (disgType)
        {
            case ELDER_GUARDIAN -> 1200;
            case ENDER_DRAGON -> 100;
            case ENDERMAN, GHAST -> 40;
            case BLAZE, WITHER -> 10;
            case SHULKER, DOLPHIN -> 80;
            default -> 20;
        };

        abilityCooldown = 40;

        //重置盔甲存储
        defaultArmors = emptyArmorStack;
        handItems = emptyHandItems;

        //更新盔甲存储
        //只对克隆的伪装生效
        if ((disgType.equals(EntityType.PLAYER) || disgType.equals(EntityType.ARMOR_STAND)) && isClone)
        {
            var watcher = disguise.getWatcher();
            defaultArmors = watcher.getArmor();

            var itemInMainhand = watcher.getItemInMainHand();
            var itemInOffhand = watcher.getItemInOffHand();
            handItems = new ItemStack[]
            {
                    itemInMainhand == null ? air : itemInMainhand,
                    itemInOffhand == null ? air : itemInOffhand
            };

            //开启默认装备显示或者更新显示
            if (!showDefaultItems) toggleDefaultArmors();
            else updateEquipment();
        }
    }

    /**
     * 盔甲存储（适用于盔甲架和玩家伪装）
     */
    @Nullable
    private ItemStack[] defaultArmors;

    @Nullable
    private ItemStack[] handItems;

    private static final ItemStack air = new ItemStack(Material.AIR);

    //用null会显示玩家自己的装备
    private final ItemStack[] emptyArmorStack = new ItemStack[]{ null, null, null, null };

    private final ItemStack[] emptyHandItems = new ItemStack[]{ null, null };

    private boolean showDefaultItems = true;

    /**
     * 切换默认装备是否可见
     * @return 是否成功？
     */
    public boolean toggleDefaultArmors()
    {
        if (defaultArmors == null || handItems == null) throw new RuntimeException("盔甲显示对此伪装不可用");

        //如果伪装没有任何默认装备，返回false
        if (Arrays.equals(defaultArmors, emptyArmorStack)
            && Arrays.equals(handItems, emptyHandItems)) return false;

        showDefaultItems = !showDefaultItems;
        updateEquipment();

        return showDefaultItems;
    }

    /**
     * 更新伪装的盔甲显示
     */
    private void updateEquipment()
    {
        var watcher = disguise.getWatcher();

        watcher.setArmor(showDefaultItems ? defaultArmors : emptyArmorStack);
        watcher.setItemInMainHand(showDefaultItems ? handItems[0] : emptyHandItems[0]);
        watcher.setItemInOffHand(showDefaultItems ? handItems[1] : emptyHandItems[1]);
    }

    //region 被动技能
    public static final short canBreatheUnderWater = 1 << 0;
    public static final short hasFireResistance = 1 << 1;
    public static final short canFly = 1 << 2;
    public static final short burnsUnderSun = 1 << 3;
    public static final short takesDamageFromWater = 1 << 4;
    public static final short alwaysNightVision = 1 << 5;

    /**
     * 检查某个被动能力是否设置
     * @param value Flag
     * @return 是否设置
     */
    public boolean isAbilityFlagSet(short value)
    {
        return (abilityFlag & value) == value;
    }
    //endregion abilityFlag
}
