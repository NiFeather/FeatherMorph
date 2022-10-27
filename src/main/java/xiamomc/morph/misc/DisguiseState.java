package xiamomc.morph.misc;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.abilities.IMorphAbility;
import xiamomc.morph.providers.DisguiseProvider;
import xiamomc.morph.skills.MorphSkillHandler;
import xiamomc.morph.abilities.AbilityHandler;
import xiamomc.morph.skills.SkillCooldownInfo;
import xiamomc.morph.skills.SkillType;
import xiamomc.morph.storage.offlinestore.OfflineDisguiseState;
import xiamomc.morph.storage.playerdata.PlayerMorphConfiguration;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.*;

public class DisguiseState extends MorphPluginObject
{
    public DisguiseState(Player player, @NotNull String id, @NotNull String skillId,
                         Disguise disguiseInstance, boolean isClone, @Nullable DisguiseProvider provider)
    {
        this.player = player;
        this.playerUniqueID = player.getUniqueId();
        this.provider = provider;

        this.setDisguise(id, skillId, disguiseInstance, isClone);
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
     * 自身可见
     */
    private boolean selfVisible;

    public boolean getSelfVisible()
    {
        return selfVisible;
    }

    public void setSelfVisible(boolean val)
    {
        DisguiseAPI.setViewDisguiseToggled(player, val);

        selfVisible = val;
    }

    /**
     * 伪装的显示名称
     */
    private Component displayName;

    public Component getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(Component newName)
    {
        displayName = newName;
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
     * 伪装的ID
     */
    private String disguiseIdentifier = SkillType.UNKNOWN.asString();

    public String getDisguiseIdentifier()
    {
        return disguiseIdentifier;
    }

    public EntityType getEntityType()
    {
        return disguise.getType().getEntityType();
    }

    private DisguiseTypes disguiseType;

    public DisguiseTypes getDisguiseType()
    {
        return disguiseType;
    }

    /**
     * 伪装的Provider
     */
    private DisguiseProvider provider;

    private boolean noProvider;

    @Nullable
    public DisguiseProvider getProvider()
    {
        if (provider == null && !noProvider)
        {
            var val = MorphManager.getProvider(this.getDisguiseIdentifier());

            if (val == null) noProvider = true;
            else provider = val;
        }

        return provider;
    }

    /**
     * 伪装的Bossbar
     */
    @Nullable
    private BossBar bossbar;

    @Nullable
    public BossBar getBossbar()
    {
        return bossbar;
    }

    public void setBossbar(BossBar bossbar)
    {
        if (this.bossbar != null)
            Bukkit.getOnlinePlayers().forEach(p -> p.hideBossBar(this.bossbar));

        this.bossbar = bossbar;
    }

    @Nullable
    private TextColor customGlowColor;

    @Nullable
    public TextColor getCustomGlowColor()
    {
        return customGlowColor;
    }

    public boolean haveCustomGlowColor()
    {
        return customGlowColor != null;
    }

    public void setCustomGlowColor(@Nullable TextColor color)
    {
        this.customGlowColor = color;
    }

    /**
     * 伪装的技能
     */
    private String skillIdentifier;

    /**
     * 获取用来执行的技能的ID
     *
     * @return 技能ID
     * @apiNote 这个ID可能是技能ID(morph:none)或伪装ID(minecraft:allay、player:Notch、ld:SavedDisguise)中的任何一种
     */
    @Nullable
    public String getSkillIdentifier()
    {
        return skillIdentifier;
    }

    /**
     * 设置技能ID
     *
     * @param skillID 技能ID
     */
    public void setSkillIdentifier(@Nullable String skillID)
    {
        this.skillIdentifier = skillID;
    }

    /**
     * 伪装技能冷却
     */
    private SkillCooldownInfo cooldownInfo;

    public long getSkillCooldown()
    {
        return cooldownInfo == null ? -1 : cooldownInfo.getCooldown();
    }

    public long getSkillLastInvoke()
    {
        return cooldownInfo == null ? Long.MIN_VALUE : cooldownInfo.getLastInvoke();
    }

    public void setSkillCooldown(long val)
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

    //region 被动技能

    /**
     * 伪装被动技能Flag
     */
    private final List<IMorphAbility> abilities = new ArrayList<>();

    public List<IMorphAbility> getAbilities()
    {
        return abilities;
    }

    public void setAbilities(@Nullable List<IMorphAbility> newAbilities)
    {
        abilities.clear();

        if (newAbilities != null)
            abilities.addAll(newAbilities);
    }

    /**
     * 检查某个被动能力是否设置
     * @param key {@link NamespacedKey}
     * @return 是否设置
     */
    public boolean containsAbility(NamespacedKey key)
    {
        return abilities.stream().anyMatch(a -> a.getIdentifier().equals(key));
    }

    //endregion abilityFlag

    /**
     * 要不要手动更新伪装Pose？
     */
    private boolean shouldHandlePose;
    public boolean shouldHandlePose()
    {
        return shouldHandlePose;
    }

    @Resolved(shouldSolveImmediately = true)
    private AbilityHandler abilityHandler;

    @Resolved(shouldSolveImmediately = true)
    private MorphSkillHandler skillHandler;

    public void setDisguise(@NotNull String identifier, @NotNull String skillIdentifier, Disguise d, boolean shouldHandlePose)
    {
        setDisguise(identifier, skillIdentifier, d, shouldHandlePose, true);
    }

    /**
     * 更新伪装
     * @param d 目标伪装
     * @param shouldHandlePose 是否要处理玩家Pose（或：是否为克隆的伪装）
     * @param shouldRefreshDisguiseItems 要不要刷新伪装物品？
     */
    public void setDisguise(@NotNull String identifier, @NotNull String skillIdentifier,
                            Disguise d, boolean shouldHandlePose, boolean shouldRefreshDisguiseItems)
    {
        if (!DisguiseUtils.isTracing(d))
            throw new RuntimeException("此Disguise不能由插件管理");

        if (disguise == d) return;

        this.disguise = d;
        this.disguiseIdentifier = identifier;
        this.shouldHandlePose = shouldHandlePose;
        setSkillIdentifier(skillIdentifier);

        disguiseType = DisguiseTypes.fromId(identifier);

        var provider = MorphManager.getProvider(identifier);

        displayName = provider == null
                ? Component.text(identifier)
                : provider.getDisplayName(identifier);

        //更新技能Flag
        var disgType = d.getType().getEntityType();
        setAbilities(abilityHandler.getAbilitiesFor(disgType));

        //伪装类型是否支持设置伪装物品
        supportsDisguisedItems = skillHandler.hasSpeficSkill(skillIdentifier, SkillType.INVENTORY);

        //重置伪装物品
        if (shouldRefreshDisguiseItems)
        {
            defaultArmors = emptyArmorStack;
            handItems = emptyHandItems;

            //更新伪装物品
            if (supportsDisguisedItems)
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

                //workaround: 部分伪装复制装备时两个手会拥有一样的物品（虽然不是一个实例）
                if (handItems[0].isSimilar(handItems[1]))
                    handItems[1] = itemOrAir(null);

                //全是空的，则默认显示自身装备
                var emptyEquipment = Arrays.stream(defaultArmors).allMatch(i -> i != null && i.getType().isAir())
                        && Arrays.stream(handItems).allMatch(i -> i != null && i.getType().isAir());

                //开启默认装备显示或者更新显示
                setShowingDisguisedItems(showDisguisedItems || !emptyEquipment);
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

    /**
     * 转换为离线存储格式
     * @return 此State的离线存储
     */
    public OfflineDisguiseState toOfflineState()
    {
        var offlineState = new OfflineDisguiseState();

        offlineState.playerUUID = this.playerUniqueID;
        offlineState.playerName = this.player.getName();

        offlineState.disguiseID = this.getDisguiseIdentifier();
        offlineState.skillID = this.getSkillIdentifier();

        var newDisguise = disguise.clone();

        if (supportsDisguisedItems)
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
    public static DisguiseState fromOfflineState(OfflineDisguiseState offlineState, PlayerMorphConfiguration configuration)
    {
        if (!offlineState.isValid())
            throw new RuntimeException("离线存储损坏");

        var player = Bukkit.getPlayer(offlineState.playerUUID);

        if (player == null) throw new RuntimeException("未找到与" + offlineState.playerUUID + "对应的玩家");

        var state = new DisguiseState(player,
                offlineState.disguiseID, offlineState.skillID == null ? offlineState.disguiseID : offlineState.skillID,
                offlineState.disguise, offlineState.shouldHandlePose, null);

        if (state.supportsDisguisedItems)
            state.setShowingDisguisedItems(offlineState.showingDisguisedItems);

        return state;
    }
}
