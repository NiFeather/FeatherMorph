package xiamomc.morph.misc;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.abilities.AbilityHandler;
import xiamomc.morph.abilities.IMorphAbility;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.network.MorphClientHandler;
import xiamomc.morph.network.commands.S2C.S2CSetSkillCooldownCommand;
import xiamomc.morph.providers.DisguiseProvider;
import xiamomc.morph.skills.IMorphSkill;
import xiamomc.morph.skills.MorphSkillHandler;
import xiamomc.morph.skills.SkillCooldownInfo;
import xiamomc.morph.skills.SkillType;
import xiamomc.morph.skills.impl.NoneMorphSkill;
import xiamomc.morph.storage.offlinestore.OfflineDisguiseState;
import xiamomc.morph.storage.playerdata.PlayerMorphConfiguration;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static xiamomc.morph.misc.DisguiseUtils.itemOrAir;

public class DisguiseState extends MorphPluginObject
{
    public DisguiseState(Player player, @NotNull String id, @NotNull String skillId,
                         Disguise disguiseInstance, boolean isClone, @NotNull DisguiseProvider provider,
                         @Nullable EntityEquipment targetEquipment)
    {
        this.player = player;
        this.playerUniqueID = player.getUniqueId();
        this.provider = provider;

        this.setDisguise(id, skillId, disguiseInstance, isClone, targetEquipment);
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
    private boolean serverSideSelfVisible;

    public boolean getServerSideSelfVisible()
    {
        return serverSideSelfVisible;
    }

    public void setServerSideSelfVisible(boolean val)
    {
        DisguiseAPI.setViewDisguiseToggled(player, val);

        serverSideSelfVisible = val;
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

    @NotNull
    public DisguiseProvider getProvider()
    {
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
     * 技能查询ID
     */
    private String skillLookupIdentifier = "minecraft:@default";

    /**
     * 获取用于查询技能的ID
     *
     * @return 技能ID
     */
    @NotNull
    public String getSkillLookupIdentifier()
    {
        return skillLookupIdentifier;
    }

    /**
     * 设置技能查询ID
     *
     * @param skillID 技能ID
     */
    public void setSkillLookupIdentifier(@NotNull String skillID)
    {
        this.skillLookupIdentifier = skillID;
    }

    private IMorphSkill<?> skill = NoneMorphSkill.instance;

    /**
     * 设置此伪装的技能
     * @param s 目标技能
     * @apiNote 如果目标技能是null，则会fallback到 {@link NoneMorphSkill#instance}
     */
    public void setSkill(@Nullable IMorphSkill<?> s)
    {
        if (s == null) s = NoneMorphSkill.instance;

        if (this.skill != null)
            skill.onDeEquip(this);

        this.skill = s;
        s.onInitialEquip(this);
    }

    /**
     * 获取此伪装的技能
     * @return {@link IMorphSkill}
     */
    @NotNull
    public IMorphSkill<?> getSkill()
    {
        return skill;
    }

    /**
     * 此伪装是否拥有技能
     * @return 伪装技能是否为 {@link NoneMorphSkill} 的实例
     */
    public boolean haveSkill()
    {
        return skill != NoneMorphSkill.instance;
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
        {
            cooldownInfo.setCooldown(val);

            if (clientHandler.clientVersionCheck(player, 3))
                clientHandler.sendClientCommand(player, new S2CSetSkillCooldownCommand(val));
        }
    }

    public boolean haveCooldown()
    {
        return cooldownInfo != null;
    }

    public void setCooldownInfo(SkillCooldownInfo info)
    {
        this.cooldownInfo = info;

        if (clientHandler.clientVersionCheck(player, 3))
            clientHandler.sendClientCommand(player, new S2CSetSkillCooldownCommand(info.getCooldown()));
    }

    //region 被动技能

    /**
     * 伪装被动技能Flag
     */
    private final List<IMorphAbility<?>> abilities = new ObjectArrayList<>();

    public List<IMorphAbility<?>> getAbilities()
    {
        return abilities;
    }

    public void setAbilities(@Nullable List<IMorphAbility<?>> newAbilities)
    {
        abilities.forEach(a -> a.revokeFromPlayer(player, this));
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

    @Resolved(shouldSolveImmediately = true)
    private MorphClientHandler clientHandler;

    //region NBT

    private String cachedNbtString = "{}";

    public String getCachedNbtString()
    {
        return cachedNbtString;
    }

    public void setCachedNbtString(String newNbt)
    {
        if (newNbt == null || newNbt.isEmpty() || newNbt.isBlank()) newNbt = "{}";

        this.cachedNbtString = newNbt;
    }

    //endregion

    //region ProfileNBT

    private String cachedProfileNbtString = "{}";

    public String getProfileNbtString()
    {
        return cachedProfileNbtString;
    }

    public void setCachedProfileNbtString(String newNbt)
    {
        if (newNbt == null || newNbt.isEmpty() || newNbt.isBlank()) newNbt = "{}";

        this.cachedProfileNbtString = newNbt;
    }

    public boolean haveProfile()
    {
        return !cachedProfileNbtString.equals("{}");
    }

    //endregion ProfileNBT

    /**
     * 设置伪装
     * @param identifier 伪装ID
     * @param skillIdentifier 技能ID
     * @param d 目标伪装
     * @param shouldHandlePose 是否要处理玩家Pose（或：是否为克隆的伪装）
     * @param equipment 要使用的equipment，没有则从伪装获取
     */
    public void setDisguise(@NotNull String identifier, @NotNull String skillIdentifier,
                            Disguise d, boolean shouldHandlePose, @Nullable EntityEquipment equipment)
    {
        setDisguise(identifier, skillIdentifier, d, shouldHandlePose, true, equipment);
    }

    /**
     * 设置伪装
     * @param identifier 伪装ID
     * @param skillIdentifier 技能ID
     * @param d 目标伪装
     * @param shouldHandlePose 是否要处理玩家Pose（或：是否为克隆的伪装）
     * @param shouldRefreshDisguiseItems 要不要刷新伪装物品？
     * @param targetEquipment 要使用的equipment，没有则从伪装获取
     */
    public void setDisguise(@NotNull String identifier, @NotNull String skillIdentifier,
                            Disguise d, boolean shouldHandlePose, boolean shouldRefreshDisguiseItems,
                            @Nullable EntityEquipment targetEquipment)
    {
        if (!DisguiseUtils.isTracing(d))
            throw new RuntimeException("此Disguise不能由插件管理");

        if (disguise == d) return;

        setCachedProfileNbtString(null);
        setCachedNbtString(null);

        this.disguise = d;
        this.disguiseIdentifier = identifier;
        this.shouldHandlePose = shouldHandlePose;
        setSkillLookupIdentifier(skillIdentifier);

        disguiseType = DisguiseTypes.fromId(identifier);

        var provider = MorphManager.getProvider(identifier);

        this.provider = provider;
        displayName = provider.getDisplayName(identifier, MessageUtils.getLocale(player));

        //伪装类型是否支持设置伪装物品
        supportsDisguisedItems = skillHandler.hasSpeficSkill(skillIdentifier, SkillType.INVENTORY);

        //重置伪装物品
        if (shouldRefreshDisguiseItems)
        {
            disguiseArmors = emptyArmorStack;
            handItems = emptyHandItems;

            //更新伪装物品
            if (supportsDisguisedItems)
            {
                EntityEquipment equipment = targetEquipment != null ? targetEquipment : disguise.getWatcher().getEquipment();

                //设置默认盔甲
                disguiseArmors = new ItemStack[]
                        {
                                itemOrAir(equipment.getBoots()),
                                itemOrAir(equipment.getLeggings()),
                                itemOrAir(equipment.getChestplate()),
                                itemOrAir(equipment.getHelmet())
                        };

                //设置默认手持物
                handItems = new ItemStack[]
                        {
                                itemOrAir(equipment.getItemInMainHand()),
                                itemOrAir(equipment.getItemInOffHand())
                        };

                //workaround: 部分伪装复制装备时两个手会拥有一样的物品（虽然不是一个实例）
                if (handItems[0].isSimilar(handItems[1]))
                    handItems[1] = itemOrAir(null);

                //全是空的，则默认显示自身装备
                var emptyEquipment = Arrays.stream(disguiseArmors).allMatch(i -> i != null && i.getType().isAir())
                        && Arrays.stream(handItems).allMatch(i -> i != null && i.getType().isAir());

                //开启默认装备显示或者更新显示
                setShowingDisguisedItems(showDisguisedItems || !emptyEquipment);
            }
        }

        //更新技能Flag
        var abilities = abilityHandler.getAbilitiesFor(identifier);
        if (abilities != null)
        {
            setAbilities(abilities);
            abilities.forEach(a -> a.applyToPlayer(player, this));
        }

        setSkill(skillHandler.getSkill(this.getSkillLookupIdentifier()));
    }

    /**
     * 盔甲存储（适用于盔甲架和玩家伪装）
     */
    @Nullable
    private ItemStack[] disguiseArmors;

    @Nullable
    private ItemStack[] handItems;

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
    public boolean showingDisguisedItems()
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
     * 获取此State的伪装物品
     * @return 此State的伪装物品
     */
    public EntityEquipment getDisguisedItems()
    {
        var eq = new DisguiseEquipment();

        var targetStack = disguiseArmors == null
                    ? new ItemStack[]{itemOrAir(null), itemOrAir(null), itemOrAir(null), itemOrAir(null)}
                    : disguiseArmors;

        eq.setArmorContents(targetStack);
        eq.setItemInHand(handItems[0]);
        eq.setItemInOffHand(handItems[1]);

        return eq;
    }

    @ApiStatus.Internal
    public void swapHands()
    {
        if (handItems != null && handItems.length == 2)
        {
            var mainHand = handItems[0];
            var offHand = handItems[1];

            handItems[0] = offHand;
            handItems[1] = mainHand;
        }
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
        watcher.setArmor(showDefaults ? disguiseArmors : emptyArmorStack);
        watcher.setItemInMainHand(showDefaults ? handItems[0] : emptyHandItems[0]);
        watcher.setItemInOffHand(showDefaults ? handItems[1] : emptyHandItems[1]);
    }

    public DisguiseState createCopy()
    {
        var disguise = this.disguise.clone();
        DisguiseUtils.addTrace(disguise);

        var state = new DisguiseState(player, this.disguiseIdentifier, this.skillLookupIdentifier,
                disguise, shouldHandlePose, provider, getDisguisedItems());

        state.setCachedProfileNbtString(this.cachedProfileNbtString);
        state.setCachedNbtString(this.cachedNbtString);

        return state;
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
        offlineState.skillID = this.getSkillLookupIdentifier();

        var newDisguise = disguise.clone();

        if (supportsDisguisedItems)
            updateEquipment(newDisguise.getWatcher(), true);

        offlineState.disguiseData = DisguiseParser.parseToString(newDisguise);
        offlineState.shouldHandlePose = this.shouldHandlePose;
        offlineState.showingDisguisedItems = this.showDisguisedItems;
        offlineState.nbtString = this.cachedNbtString;
        offlineState.profileString = this.cachedProfileNbtString;

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

        //todo: 实现伪装装备保存和读取
        var state = new DisguiseState(player,
                offlineState.disguiseID, offlineState.skillID == null ? offlineState.disguiseID : offlineState.skillID,
                offlineState.disguise, offlineState.shouldHandlePose, MorphManager.getProvider(offlineState.disguiseID),
                null);

        state.setCachedProfileNbtString(offlineState.profileString);
        state.setCachedNbtString(offlineState.nbtString);

        if (state.supportsDisguisedItems)
            state.setShowingDisguisedItems(offlineState.showingDisguisedItems);

        return state;
    }
}
