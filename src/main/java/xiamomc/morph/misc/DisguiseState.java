package xiamomc.morph.misc;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.abilities.AbilityFlag;
import xiamomc.morph.abilities.AbilityHandler;
import xiamomc.morph.skills.SkillCooldownInfo;
import xiamomc.morph.storage.offlinestore.OfflineDisguiseState;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.UUID;

public class DisguiseState extends MorphPluginObject
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
    private SkillCooldownInfo cooldownInfo;

    public int getSkillCooldown()
    {
        return cooldownInfo == null ? -1 : cooldownInfo.getCooldown();
    }

    public long getSkillLastInvoke()
    {
        return cooldownInfo == null ? Long.MIN_VALUE : cooldownInfo.getLastInvoke();
    }

    public void setSkillCooldown(int val)
    {
        if (haveCooldown())
            cooldownInfo.setCooldown(val);
    }

    public boolean haveCooldown()
    {
        return cooldownInfo != null;
    }

    public void setCooldownInfo(SkillCooldownInfo info)
    {
        this.cooldownInfo = info;
    }

    /**
     * 伪装被动技能Flag
     */
    private final EnumSet<AbilityFlag> abilityFlag = EnumSet.noneOf(AbilityFlag.class);
    public EnumSet<AbilityFlag> getAbilityFlags()
    {
        return abilityFlag;
    }

    /**
     * 要不要手动更新伪装Pose？
     */
    private boolean shouldHandlePose;
    public boolean shouldHandlePose()
    {
        return shouldHandlePose;
    }

    public void setDisguise(Disguise d, boolean shouldHandlePose)
    {
        setDisguise(d, shouldHandlePose, true);
    }

    public void setAbilities(@Nullable EnumSet<AbilityFlag> newAbilities)
    {
        abilityFlag.clear();

        if (newAbilities != null)
            abilityFlag.addAll(newAbilities);
    }

    @Resolved(shouldSolveImmediately = true)
    private AbilityHandler abilityHandler;

    /**
     * 更新伪装
     * @param d 目标伪装
     * @param shouldHandlePose 是否要处理玩家Pose（或：是否为克隆的伪装）
     * @param shouldRefreshDisguiseItems 要不要刷新伪装物品？
     */
    public void setDisguise(Disguise d, boolean shouldHandlePose, boolean shouldRefreshDisguiseItems)
    {
        if (!DisguiseUtils.isTracing(d))
            throw new RuntimeException("此Disguise不能由插件管理");

        if (disguise == d) return;

        disguise = d;
        this.shouldHandlePose = shouldHandlePose;

        displayName = d.isPlayerDisguise()
                ? Component.text(((PlayerDisguise) d).getName())
                : Component.translatable(d.getType().getEntityType().translationKey());

        //更新技能Flag
        var disgType = d.getType().getEntityType();
        setAbilities(abilityHandler.getFlagsFor(disgType));

        //设置初始CD
        //skillCooldown = 40;

        supportsDisguisedItems = disgType.equals(EntityType.PLAYER) || disgType.equals(EntityType.ARMOR_STAND);

        //重置伪装物品
        if (shouldRefreshDisguiseItems)
        {
            defaultArmors = emptyArmorStack;
            handItems = emptyHandItems;

            //更新伪装物品
            //只对克隆的伪装生效
            if (supportsDisguisedItems && shouldHandlePose)
            {
                var watcher = disguise.getWatcher();

                //设置默认盔甲
                var disguiseArmor = watcher.getArmor();
                defaultArmors = new ItemStack[]
                        {
                                itemOrAir(disguiseArmor[0]),
                                itemOrAir(disguiseArmor[1]),
                                itemOrAir(disguiseArmor[2]),
                                itemOrAir(disguiseArmor[3])
                        };

                //设置默认手持物
                handItems = new ItemStack[]
                        {
                                itemOrAir(watcher.getItemInMainHand()),
                                itemOrAir(watcher.getItemInOffHand())
                        };

                //全是空的，则禁用装备显示
                if (Arrays.stream(defaultArmors).allMatch(i -> i != null && i.getType().isAir())
                        && Arrays.stream(handItems).allMatch(i -> i != null && i.getType().isAir()))
                {
                    defaultArmors = emptyArmorStack;
                    handItems = emptyHandItems;
                }

                //开启默认装备显示或者更新显示
                if (!showDisguisedItems) toggleDisguisedItems();
                else updateEquipment();
            }
        }
    }

    private ItemStack itemOrAir(ItemStack item)
    {
        return item == null ? air : item;
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

    private boolean showDisguisedItems = false;

    private boolean supportsDisguisedItems = false;

    /**
     * 此阶段是否支持显示伪装物品
     * @return 是否支持
     */
    public boolean supportsShowingDefaultItems()
    {
        return supportsDisguisedItems;
    }

    /**
     * 此阶段是否正在显示伪装物品
     * @return 是否正在显示
     */
    public boolean showingDefaultItems()
    {
        return showDisguisedItems;
    }

    /**
     * 设置是否要显示伪装物品
     * @param value 值
     */
    public void setShowingDisguisedItems(boolean value)
    {
        if (!supportsDisguisedItems) throw new RuntimeException("伪装物品对此状态不可用");

        if (showDisguisedItems == value) return;

        //如果伪装没有任何默认装备，永远显示玩家自己的伪装
        if (value && Arrays.equals(defaultArmors, emptyArmorStack)
                  && Arrays.equals(handItems, emptyHandItems))
        {
            value = false;
        }

        var watcher = disguise.getWatcher();
        updateEquipment(watcher, value);
        showDisguisedItems = value;
    }

    /**
     * 切换伪装物品是否可见
     * @return 切换后的值
     */
    public boolean toggleDisguisedItems()
    {
        setShowingDisguisedItems(!showDisguisedItems);

        return showDisguisedItems;
    }

    /**
     * 更新伪装物品显示
     */
    private void updateEquipment()
    {
        var watcher = disguise.getWatcher();

        updateEquipment(watcher, showDisguisedItems);
    }

    /**
     * 更新伪装物品显示
     * @param watcher 伪装的Watcher
     * @param showDefaults 是否显示默认盔甲
     * @apiNote 此方法在将状态转换为离线存储的过程中才会直接调用，其他情况下请用不带参数的方法
     */
    private void updateEquipment(FlagWatcher watcher, boolean showDefaults)
    {
        watcher.setArmor(showDefaults ? defaultArmors : emptyArmorStack);
        watcher.setItemInMainHand(showDefaults ? handItems[0] : emptyHandItems[0]);
        watcher.setItemInOffHand(showDefaults ? handItems[1] : emptyHandItems[1]);
    }

    //region 被动技能

    /**
     * 检查某个被动能力是否设置
     * @param value Flag
     * @return 是否设置
     */
    public boolean isAbilityFlagSet(AbilityFlag value)
    {
        return abilityFlag.contains(value);
    }
    //endregion abilityFlag

    /**
     * 转换为离线存储格式
     * @return 此State的离线存储
     */
    public OfflineDisguiseState toOfflineState()
    {
        var offlineState = new OfflineDisguiseState();

        offlineState.playerUUID = this.playerUniqueID;
        offlineState.playerName = this.player.getName();

        offlineState.disguiseID = disguise.isPlayerDisguise()
                ? "player:" + ((PlayerDisguise) disguise).getName()
                : disguise.getType().getEntityType().getKey().asString();

        var newDisguise = disguise.clone();
        updateEquipment(newDisguise.getWatcher(), true);

        offlineState.disguiseData = DisguiseParser.parseToString(newDisguise);
        offlineState.shouldHandlePose = this.shouldHandlePose;
        offlineState.showingDisguisedItems = this.showDisguisedItems;

        return offlineState;
    }

    /**
     * 从离线存储转换为实例
     * @param offlineState 离线存储
     * @return DisguiseState的实例
     */
    public static DisguiseState fromOfflineState(OfflineDisguiseState offlineState)
    {
        var player = Bukkit.getPlayer(offlineState.playerUUID);

        if (player == null) throw new RuntimeException("未找到与" + offlineState.playerUUID + "对应的玩家");

        var state = new DisguiseState(player, offlineState.disguise, offlineState.shouldHandlePose);

        if (state.supportsDisguisedItems)
            state.setShowingDisguisedItems(offlineState.showingDisguisedItems);

        return state;
    }
}
