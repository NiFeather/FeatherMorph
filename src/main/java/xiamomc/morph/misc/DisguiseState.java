package xiamomc.morph.misc;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
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
import xiamomc.morph.abilities.IMorphAbility;
import xiamomc.morph.backends.DisguiseWrapper;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.network.PlayerOptions;
import xiamomc.morph.network.commands.S2C.set.S2CSetSkillCooldownCommand;
import xiamomc.morph.network.server.MorphClientHandler;
import xiamomc.morph.providers.DisguiseProvider;
import xiamomc.morph.skills.IMorphSkill;
import xiamomc.morph.skills.MorphSkillHandler;
import xiamomc.morph.skills.SkillCooldownInfo;
import xiamomc.morph.skills.SkillType;
import xiamomc.morph.skills.impl.NoneMorphSkill;
import xiamomc.morph.storage.playerdata.PlayerMeta;
import xiamomc.morph.utilities.EntityTypeUtils;
import xiamomc.morph.utilities.ItemUtils;
import xiamomc.morph.utilities.MathUtils;
import xiamomc.morph.utilities.SoundUtils;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static xiamomc.morph.utilities.DisguiseUtils.itemOrAir;

public class DisguiseState extends MorphPluginObject
{
    public DisguiseState(Player player, @NotNull String id, @NotNull String skillId,
                         @NotNull DisguiseWrapper<?> disguiseInstance, boolean isClone, @NotNull DisguiseProvider provider,
                         @Nullable EntityEquipment targetEquipment, @NotNull PlayerOptions<Player> playerOptions,
                         @NotNull PlayerMeta playerMeta)
    {
        this.player = player;
        this.playerUniqueID = player.getUniqueId();
        this.provider = provider;
        this.playerOptions = playerOptions;
        this.morphConfiguration = playerMeta;

        this.soundHandler = new SoundHandler(player);

        this.setDisguise(id, skillId, disguiseInstance, isClone, targetEquipment);
    }

    private final PlayerOptions<Player> playerOptions;

    private final PlayerMeta morphConfiguration;

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

    public boolean isClientSideSelfViewing()
    {
        return playerOptions.isClientSideSelfView();
    }

    public boolean isSelfViewing()
    {
        return playerOptions.isClientSideSelfView() ? morphConfiguration.showDisguiseToSelf : serverSideSelfVisible;
    }

    public void setServerSideSelfVisible(boolean val)
    {
        disguiseWrapper.setServerSelfView(val);
        serverSideSelfVisible = val;
    }

    /**
     * 此伪装面向玩家自己的显示名称。
     */
    private Component playerDisplay;

    /**
     * 获取此伪装面向玩家自己的显示名称
     * @return {@link DisguiseState#playerDisplay}
     * @apiNote 对于要显示到服务器公屏上的内容，请使用 {@link DisguiseState#getServerDisplay()}
     */
    public Component getPlayerDisplay()
    {
        return playerDisplay;
    }

    public void setPlayerDisplay(Component newName)
    {
        playerDisplay = newName == null ? Component.empty() : newName;
    }

    /**
     * 此伪装面向服务器其他人的显示名称
     */
    private Component serverDisplay;

    /**
     * 获取此伪装面向服务器其他人的显示名称
     * @return {@link DisguiseState#serverDisplay}
     * @apiNote 对于要显示给玩家自己的内容，请使用 {@link DisguiseState#getPlayerDisplay()}
     */
    public Component getServerDisplay()
    {
        return serverDisplay;
    }

    public void setServerDisplay(Component newName)
    {
        serverDisplay = newName == null ? Component.empty() : newName;
    }

    public void setDisplayName(Component newName)
    {
        setPlayerDisplay(newName);
        setServerDisplay(newName);
    }

    public Component entityCustomName;

    /**
     * 伪装的{@link DisguiseWrapper}实例
     */
    private DisguiseWrapper<?> disguiseWrapper;

    /**
     * 获取此State的伪装Wrapper
     */
    public DisguiseWrapper<?> getDisguiseWrapper()
    {
        return disguiseWrapper;
    }

    // 伪装ID
    private String disguiseIdentifier = SkillType.UNKNOWN.asString();

    /**
     * 获取此伪装的ID
     */
    public String getDisguiseIdentifier()
    {
        return disguiseIdentifier;
    }

    public EntityType getEntityType()
    {
        return disguiseWrapper.getEntityType();
    }

    private DisguiseTypes disguiseType;

    /**
     * 获取此伪装的{@link DisguiseTypes}
     */
    public DisguiseTypes getDisguiseType()
    {
        return disguiseType;
    }

    /**
     * 伪装的构建器（提供器）
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

    public Entity beamTarget;

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
    private String skillLookupIdentifier = "minecraft:" + MorphManager.disguiseFallbackName;

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
    @Nullable
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

            if (clientHandler.isFutureClientProtocol(player, 3))
                clientHandler.sendCommand(player, new S2CSetSkillCooldownCommand(val));
        }
    }

    public boolean haveCooldown()
    {
        return cooldownInfo != null;
    }

    public void setCooldownInfo(@Nullable SkillCooldownInfo info)
    {
        this.cooldownInfo = info;

        if (info != null && clientHandler.isFutureClientProtocol(player, 3))
            clientHandler.sendCommand(player, new S2CSetSkillCooldownCommand(info.getCooldown()));
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
        {
            abilities.addAll(newAbilities);
            newAbilities.forEach(a -> a.applyToPlayer(player, this));
        }
    }

    @ApiStatus.Internal
    public void refreshSkillsAbilities()
    {
        this.abilities.forEach(a -> a.applyToPlayer(player, this));
        this.skill.onInitialEquip(this);
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
                            @NotNull DisguiseWrapper<?> d, boolean shouldHandlePose, @Nullable EntityEquipment equipment)
    {
        setDisguise(identifier, skillIdentifier, d, shouldHandlePose, true, equipment);
    }

    @Resolved(shouldSolveImmediately = true)
    private MorphConfigManager config;

    /**
     * 设置伪装
     * @param identifier 伪装ID
     * @param skillIdentifier 技能ID
     * @param wrapper 目标伪装
     * @param shouldHandlePose 是否要处理玩家Pose（或：是否为克隆的伪装）
     * @param shouldRefreshDisguiseItems 要不要刷新伪装物品？
     * @param targetEquipment 要使用的equipment，没有则从伪装获取
     */
    public void setDisguise(@NotNull String identifier, @NotNull String skillIdentifier,
                            @NotNull DisguiseWrapper<?> wrapper, boolean shouldHandlePose, boolean shouldRefreshDisguiseItems,
                            @Nullable EntityEquipment targetEquipment)
    {
        if (disguiseWrapper == wrapper) return;

        setCachedProfileNbtString(null);
        setCachedNbtString(null);

        this.entityCustomName = null;

        this.disguiseWrapper = wrapper;
        this.disguiseIdentifier = identifier;
        this.shouldHandlePose = shouldHandlePose;
        setSkillLookupIdentifier(skillIdentifier);

        disguiseType = DisguiseTypes.fromId(identifier);

        var provider = MorphManager.getProvider(identifier);

        this.provider = provider;
        playerDisplay = provider.getDisplayName(identifier, MessageUtils.getLocale(player));
        serverDisplay = provider.getDisplayName(identifier, config.get(String.class, ConfigOption.LANGUAGE_CODE));

        //伪装类型是否支持设置伪装物品
        supportsDisguisedItems = skillHandler.hasSpeficSkill(skillIdentifier, SkillType.INVENTORY);

        //设置声音
        this.soundHandler.refreshSounds(wrapper.getEntityType(), wrapper.isBaby());

        //重置伪装物品
        if (shouldRefreshDisguiseItems)
        {
            disguiseEquipments.clear();

            //更新伪装物品
            if (supportsDisguisedItems)
            {
                EntityEquipment equipment = targetEquipment != null ? targetEquipment : disguiseWrapper.getDisplayingEquipments();

                //设置默认盔甲
                var armors = new ItemStack[]
                        {
                                itemOrAir(equipment.getBoots()),
                                itemOrAir(equipment.getLeggings()),
                                itemOrAir(equipment.getChestplate()),
                                itemOrAir(equipment.getHelmet())
                        };

                //设置默认手持物
                var handItems = new ItemStack[]
                        {
                                itemOrAir(equipment.getItemInMainHand()),
                                itemOrAir(equipment.getItemInOffHand())
                        };

                armors = ItemUtils.asCopy(armors);
                handItems = ItemUtils.asCopy(handItems);

                //workaround: 部分伪装复制装备时两个手会拥有一样的物品（虽然不是一个实例）
                if (handItems[0].isSimilar(handItems[1]))
                    handItems[1] = itemOrAir(null);

                //全是空的，则默认显示自身装备
                var emptyEquipment = Arrays.stream(armors).allMatch(i -> i != null && i.getType().isAir())
                        && Arrays.stream(handItems).allMatch(i -> i != null && i.getType().isAir());

                disguiseEquipments.allowNull = true;
                disguiseEquipments.setArmorContents(armors);
                disguiseEquipments.setHandItems(handItems);

                //开启默认装备显示或者更新显示
                setShowingDisguisedItems(showDisguisedItems || !emptyEquipment);
            }
        }
    }

    private final DisguiseEquipment disguiseEquipments = new DisguiseEquipment();

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
        updateEquipment(value);
        showDisguisedItems = value;

        this.disguiseWrapper.setDisplayingFakeEquipments(value);
    }

    /**
     * 获取此State的伪装物品
     * @return 此State的伪装物品
     */
    public EntityEquipment getDisguisedItems()
    {
        var eq = new DisguiseEquipment();

        eq.setArmorContents(ItemUtils.asCopy(disguiseEquipments.getArmorContents()));
        eq.setHandItems(ItemUtils.asCopy(disguiseEquipments.getHandItems()));

        return eq;
    }

    @ApiStatus.Internal
    public void swapHands()
    {
        var handItems = disguiseEquipments.getHandItems();

        if (handItems.length == 2)
        {
            var mainHand = handItems[0];
            var offHand = handItems[1];

            disguiseEquipments.setHandItems(offHand, mainHand);
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

    private final ItemStack[] emptyArmorStack = new ItemStack[]{ null, null, null, null };

    //region Sound Handling

    private final SoundHandler soundHandler;

    public SoundHandler getSoundHandler()
    {
        return soundHandler;
    }

    public static class SoundHandler
    {
        public int ambientInterval = 0;
        public Sound ambientSoundPrimary;
        public Sound ambientSoundSecondary;
        private int soundTime;
        private double soundFrequency = 0D;

        public void resetSoundTime()
        {
            soundTime = 0;
        }

        private final Player bindingPlayer;

        @Nullable
        private EntityType entityType;

        @NotNull
        private EntityType getEntityType()
        {
            return entityType == null ? EntityType.PLAYER : entityType;
        }

        public SoundHandler(Player bindingPlayer)
        {
            this.bindingPlayer = bindingPlayer;
        }

        public void update()
        {
            soundTime++;

            // Java中浮点数除以0是正或负无穷
            // 因为soundFrequency永远大于等于0，而分子是1，因此frequencyScale的最大值是正无穷
            // 除非soundTime最后也加到了大于等于正无穷，否则不需要额外的判断，但这真的会发生吗（
            double frequencyScale = 1.0D / soundFrequency;

            //logger.info("Sound: %s <-- %s(%s) --> %s".formatted(soundTime, frequency, soundFrequency, ambientInterval * frequency));
            if (ambientInterval != 0 && soundTime >= ambientInterval * frequencyScale && !bindingPlayer.isSneaking())
            {
                var loc = bindingPlayer.getLocation();
                boolean playSecondary = false;

                if (getEntityType() == EntityType.ALLAY)
                {
                    var eq = bindingPlayer.getEquipment();
                    if (!eq.getItemInMainHand().getType().isAir()) playSecondary = true;
                }

                Sound sound = playSecondary ? ambientSoundSecondary : ambientSoundPrimary;

                var nmsPlayer = NmsRecord.ofPlayer(bindingPlayer);
                var isSpectator = nmsPlayer.isSpectator();

                // 和原版行为保持一致, 并且不要为旁观者播放音效:
                // net.minecraft.world.entity.Mob#baseTick()
                if (isSpectator)
                {
                    soundTime = -(int)(ambientInterval * frequencyScale);
                }
                else if (sound != null && random.nextInt((int)(1000 * frequencyScale)) < soundTime)
                {
                    soundTime = -(int)(ambientInterval * frequencyScale);
                    bindingPlayer.getWorld().playSound(sound, loc.getX(), loc.getY(), loc.getZ());
                }
            }
        }

        private final Random random = new Random();

        private final MorphConfigManager config = MorphConfigManager.getInstance();

        public void resetSound()
        {
            ambientSoundPrimary = null;
            ambientSoundSecondary = null;
            ambientInterval = 0;
            resetSoundTime();
        }

        public void refreshSounds(EntityType entityType, boolean isBaby)
        {
            resetSound();

            this.entityType = entityType;

            soundFrequency = MathUtils.clamp(0, 2, config.getBindable(Double.class, ConfigOption.AMBIENT_FREQUENCY).get());

            var soundEvent = EntityTypeUtils.getSoundEvent(entityType);

            var sound = soundEvent.sound();
            if (sound == null) return;

            this.ambientInterval = soundEvent.interval();
            var pitch = isBaby ? 1.5F : 1F;

            this.ambientSoundPrimary = SoundUtils.toBukkitSound(soundEvent, pitch);

            if (entityType == EntityType.ALLAY)
            {
                var allaySecondary = SoundEvents.ALLAY_AMBIENT_WITH_ITEM;
                var secSi = new EntityTypeUtils.SoundInfo(allaySecondary, SoundSource.NEUTRAL, ambientInterval, soundEvent.volume());
                this.ambientSoundSecondary = SoundUtils.toBukkitSound(secSi, pitch);
            }
        }

    }

    //endregion Sound Handling

    /**
     * 更新伪装物品显示
     * @param showDisguised 是否显示默认盔甲
     * @apiNote 此方法在将状态转换为离线存储的过程中才会直接调用，其他情况下请用不带参数的方法
     */
    private void updateEquipment(boolean showDisguised)
    {
        var handItems = disguiseEquipments.getHandItems();

        var eq = new DisguiseEquipment();
        eq.setArmorContents(showDisguised ? disguiseEquipments.getArmorContents() : emptyArmorStack);
        eq.setItemInMainHand(showDisguised ? handItems[0] : null);
        eq.setItemInOffHand(showDisguised ? handItems[1] : null);
        eq.allowNull = true;

        disguiseWrapper.setFakeEquipments(eq);
    }

    public DisguiseState createCopy()
    {
        var disguise = this.disguiseWrapper.clone();

        var state = new DisguiseState(player, this.disguiseIdentifier, this.skillLookupIdentifier,
                disguise, shouldHandlePose, provider, getDisguisedItems(), this.playerOptions, morphConfiguration);

        state.setCachedProfileNbtString(this.cachedProfileNbtString);
        state.setCachedNbtString(this.cachedNbtString);

        return state;
    }

    public void dispose()
    {
        this.disguiseWrapper.dispose();
    }

    public void reset()
    {
        this.provider.unMorph(player, this);

        this.setAbilities(List.of());
        this.setSkill(null);
    }
}
