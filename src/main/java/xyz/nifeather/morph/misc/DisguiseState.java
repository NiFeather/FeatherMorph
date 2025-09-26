package xyz.nifeather.morph.misc;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.abilities.AbilityUpdater;
import xyz.nifeather.morph.api.morphs.skills.SkillNames;
import xyz.nifeather.morph.backends.DisguiseWrapper;
import xyz.nifeather.morph.messages.CommandStrings;
import xyz.nifeather.morph.messages.EmoteStrings;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.BaseLivingEntityProperties;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;
import xyz.nifeather.morph.misc.waypoint.DisguiseWaypointUpdater;
import xyz.nifeather.morph.network.PlayerOptions;
import xyz.nifeather.morph.network.commands.S2C.S2CPlayAnimationCommand;
import xyz.nifeather.morph.network.commands.S2C.set.S2CSetAnimationDisplayNameCommand;
import xyz.nifeather.morph.network.server.MorphClientHandler;
import xyz.nifeather.morph.providers.animation.SingleAnimation;
import xyz.nifeather.morph.providers.disguise.DisguiseProvider;
import xyz.nifeather.morph.skills.ISkill;
import xyz.nifeather.morph.skills.SkillManager;
import xyz.nifeather.morph.skills.SkillUpdater;
import xyz.nifeather.morph.skills.impl.NoneMorphSkill;
import xyz.nifeather.morph.storage.playerdata.PlayerMeta;
import xyz.nifeather.morph.storage.skill.ISkillAbilityOption;
import xyz.nifeather.morph.utilities.ItemUtils;
import xyz.nifeather.morph.utilities.NbtUtils;
import xyz.nifeather.morph.utilities.PermissionUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

import static xyz.nifeather.morph.utilities.DisguiseUtils.itemOrAir;

public class DisguiseState extends MorphPluginObject
{
    public DisguiseState(@NotNull Player player, @NotNull String identifier, @NotNull String skillIdentifier,
                         @NotNull DisguiseWrapper<?> wrapper, @NotNull DisguiseProvider provider,
                         @NotNull PlayerOptions<Player> playerOptions,
                         @NotNull PlayerMeta playerMeta)
    {
        Objects.requireNonNull(wrapper, "Wrapper cannot be null.");
        Objects.requireNonNull(identifier, "Disguise identifier cannot be null.");
        Objects.requireNonNull(skillIdentifier, "Skill identifier cannot be null.");
        Objects.requireNonNull(provider, "Disguise provider cannot be null");
        Objects.requireNonNull(playerOptions, "Player options cannot be null");
        Objects.requireNonNull(playerMeta, "Player metadata cannot be null");

        this.playerUUID = player.getUniqueId();
        this.provider = provider;
        this.playerOptions = playerOptions;
        this.morphConfiguration = playerMeta;

        this.soundHandler = new SoundHandler(player);
        this.abilityUpdater = new AbilityUpdater(this);
        this.disguiseWaypointUpdater = new DisguiseWaypointUpdater(this);

        this.disguiseWrapper = wrapper;
        this.disguiseIdentifier = identifier;
        skillLookupIdentifier(skillIdentifier);

        disguiseType = DisguiseTypes.fromId(identifier);
        this.provider = MorphManager.getProvider(identifier);

        //设置声音
        this.soundHandler.refreshSounds(this, wrapper.getEntityType(), wrapper.isBaby());

        this.cachedPlayer = CacheWithDefault.of(player);

        animationSequence.setCooldown(10);
        animationSequence.onNewAnimation(anim ->
        {
            var animSubId = anim.subId();

            if (anim.availableForClient())
                clientHandler.sendCommand(getPlayer(), new S2CPlayAnimationCommand(animSubId));

            this.getDisguiseWrapper().playAnimation(animSubId);

            if (animSubId.startsWith("exec_"))
                handleInternalExec(animSubId);
        });
        animationSequence.onNewAnimationSequence(newAnimSeqId ->
        {
            clientHandler.sendCommand(getPlayer(), new S2CSetAnimationDisplayNameCommand(newAnimSeqId));

            // Not done yet...
            /*
            if (newAnimSeqId.equals(AnimationNames.NONE))
            {
                var isPersistent = this.sequencePersistent.get();
                this.sequencePersistent.set(false);

                if (!isPersistent)
                    clientHandler.sendCommand(getPlayer(), new S2CSetAnimationDisplayNameCommand(newAnimSeqId));
            }
            else
            {
                clientHandler.sendCommand(getPlayer(), new S2CSetAnimationDisplayNameCommand(newAnimSeqId));
            }
            */
        });

        disguisePropertyHandler().hookOnPropertyWrite(this::onPropertyWrite);
    }

    private void onPropertyWrite(SingleProperty<?> singleProperty, Object o)
    {
        switch (singleProperty.id())
        {
            case PropertyNames.ENTITY_CUSTOM_NAME ->
            {
                var component = (Component) o;
                this.setCustomDisplayName(component);
            }

            case PropertyNames.ENTITY_DISPLAY_DISGUISE_EQUIPMENT ->
            {
                var value = (Boolean) o;
                disguiseWrapper.setDisplayingFakeEquipments(value);
            }

            case PropertyNames.ENTITY_EQUIPMENT ->
            {
                var equipment = (EntityEquipment) o;
                disguiseWrapper.setFakeEquipments(equipment);
            }
        }
    }

    private final AtomicBoolean sequencePersistent = new AtomicBoolean(false);

    @Resolved(shouldSolveImmediately = true)
    private SkillManager skillManager;

    @Resolved(shouldSolveImmediately = true)
    private MorphClientHandler clientHandler;

    private void handleInternalExec(String animationSubId)
    {
        switch (animationSubId)
        {
            case AnimationNames.INTERNAL_DISABLE_AMBIENT -> this.requestAmbientState(this, true);
            case AnimationNames.INTERNAL_ENABLE_AMBIENT -> this.requestAmbientState(this, false);
            case AnimationNames.INTERNAL_DISABLE_SKILL -> this.requestSkillState(this, true);
            case AnimationNames.INTERNAL_ENABLE_SKILL -> this.requestSkillState(this, false);
            case AnimationNames.INTERNAL_DISABLE_BOSSBAR -> this.requestBossbarState(this, true);
            case AnimationNames.INTERNAL_ENABLE_BOSSBAR -> this.requestBossbarState(this, false);
        }
    }

    private final List<Object> disableSkillRequests = Collections.synchronizedList(new ObjectArrayList<>());
    private final List<Object> disableAmbientRequests = Collections.synchronizedList(new ObjectArrayList<>());
    private final List<Object> disableBossbarRequests = Collections.synchronizedList(new ObjectArrayList<>());

    public void requestSkillState(Object source, boolean shouldDisable)
    {
        if (shouldDisable)
        {
            if (!disableSkillRequests.contains(source))
                disableSkillRequests.add(source);
        }
        else
        {
            disableSkillRequests.remove(source);
        }
    }

    public boolean canActivateSkill()
    {
        return disableSkillRequests.isEmpty();
    }

    public void requestAmbientState(Object source, boolean shouldDisable)
    {
        if (shouldDisable)
        {
            if (!disableAmbientRequests.contains(source))
                disableAmbientRequests.add(source);
        }
        else
        {
            disableAmbientRequests.remove(source);
        }
    }

    public boolean canPlayAmbient()
    {
        return disableSkillRequests.isEmpty();
    }

    public void requestBossbarState(Object source, boolean shouldDisable)
    {
        if (shouldDisable)
        {
            if (!disableBossbarRequests.contains(source))
                disableBossbarRequests.add(source);
        }
        else
        {
            disableBossbarRequests.remove(source);
        }
    }

    public boolean canDisplayBossbar()
    {
        return disableBossbarRequests.isEmpty();
    }

    private final PlayerOptions<Player> playerOptions;

    private final PlayerMeta morphConfiguration;

    private final AnimationSequence animationSequence = new AnimationSequence();

    public void stopAnimations()
    {
        animationSequence.reset();
    }

    public void onPlayerQuit()
    {
        this.stopAnimations();

        this.abilityUpdater.onPlayerOffline();
        this.getDisguiseWrapper().onPlayerOffline();
        this.getProvider().onPlayerQuitWithDisguise(this);
    }

    @ApiStatus.Internal
    public void onPlayerJoin()
    {
        this.abilityUpdater.reApplyAbility();
        this.skillUpdater.onPlayerJoin();

        this.getProvider().onPlayerJoinWithDisguise(this);
        this.getDisguiseWrapper().onPlayerJoin(this.getPlayer());

        var currentLoopTask = this.loopTask;
        if (currentLoopTask == null || currentLoopTask.isCancelled())
            this.scheduleSelfUpdate();
    }

    public boolean canScheduleSequence()
    {
        return skillUpdater.calculateRemainingCooldown() <= 0;
    }

    /**
     * @return Whether success.
     */
    public boolean tryScheduleSequence(@NotNull String sequenceIdentifier,
                                       List<SingleAnimation> sequence,
                                       boolean persistent)
    {
        if (!canScheduleSequence()) return false;
        this.scheduleSequence(sequenceIdentifier, sequence, persistent);

        return true;
    }

    public void scheduleSequence(String sequenceIdentifier,
                                 List<SingleAnimation> sequence,
                                 boolean persistent)
    {
        this.scheduleSequence(sequenceIdentifier, sequence, true, persistent);
    }

    private void scheduleSequence(String sequenceIdentifier,
                                  List<SingleAnimation> sequence,
                                  boolean checkPermission,
                                  boolean persistent)
    {
        if (checkPermission && sequenceIdentifier.equals(AnimationNames.RESET)
                || !PermissionUtils.hasPermission(
                        getPlayer(),
                        CommonPermissions.animationPermissionOf(sequenceIdentifier, this.getDisguiseIdentifier()),
                        true))
        {
            var player = getPlayer();
            player.sendMessage(MessageUtils.prefixes(player, CommandStrings.noPermissionMessage()));
            return;
        }

        this.animationSequence.scheduleNext(sequenceIdentifier, sequence);
        this.sequencePersistent.set(persistent);

        var player = getPlayer();
        var animationString = CommandStrings.goingToPlayAnimation().resolve("what", EmoteStrings.get(sequenceIdentifier).withLocale(MessageUtils.getLocale(player)));
        player.sendMessage(MessageUtils.prefixes(player, animationString));
    }

    public AnimationSequence getAnimationSequence()
    {
        return animationSequence;
    }

    /**
     * 谁在伪装
     */
    private final UUID playerUUID;

    public UUID getPlayerUUID()
    {
        return playerUUID;
    }

    @NotNull
    private final CacheWithDefault<Player> cachedPlayer;

    /**
     *
     * @return The player that matches the UUID stored in this DisguiseState
     */
    @NotNull
    public Player getPlayer()
    {
        var cached = cachedPlayer.get();

        if (cached.isConnected())
            return cached;

        var newPlayer = Bukkit.getPlayer(playerUUID);
        if (newPlayer == null)
            return cached;

        return cachedPlayer.set(newPlayer);
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

    private static final Component fallbackDisplay = Component.text("~UNDEFINED~");

    /**
     * 此伪装面向玩家自己的显示名称。
     */
    @Nullable
    private Component playerDisplay;

    /**
     * 获取此伪装面向玩家自己的显示名称
     * @return {@link DisguiseState#playerDisplay}
     * @apiNote 对于要显示到服务器公屏上的内容，请使用 {@link DisguiseState#getServerDisplay()}
     */
    @NotNull
    public Component getPlayerDisplay()
    {
        return playerDisplay == null ? fallbackDisplay : playerDisplay;
    }

    public void setPlayerDisplay(@NotNull Component newName)
    {
        playerDisplay = newName;
    }

    /**
     * 此伪装面向服务器其他人的显示名称
     */
    @Nullable
    private Component serverDisplay;

    /**
     * 获取此伪装面向服务器其他人的显示名称
     * @return {@link DisguiseState#serverDisplay}
     * @apiNote 对于要显示给玩家自己的内容，请使用 {@link DisguiseState#getPlayerDisplay()}
     */
    @NotNull
    public Component getServerDisplay()
    {
        return serverDisplay == null ? fallbackDisplay : serverDisplay;
    }

    public void setServerDisplay(@NotNull Component newName)
    {
        serverDisplay = newName;
    }

    public void setCustomDisplayName(Component newName)
    {
        setPlayerDisplay(newName);
        setServerDisplay(newName);
    }

    public Component entityCustomName;

    /**
     * 伪装的{@link DisguiseWrapper}实例
     */
    @NotNull
    private final DisguiseWrapper<?> disguiseWrapper;

    /**
     * 获取此State的伪装Wrapper
     */
    @NotNull
    public DisguiseWrapper<?> getDisguiseWrapper()
    {
        return disguiseWrapper;
    }

    // 伪装ID
    private String disguiseIdentifier = SkillNames.UNKNOWN.asString();

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

    public void setBossbar(@Nullable BossBar bossbar)
    {
        if (this.bossbar != null)
            featherMorph().getPlatform().onlinePlayersNative().forEach(p -> p.hideBossBar(this.bossbar));

        this.bossbar = bossbar;
    }

    //region Disguise Property

    private final PropertyHandler propertyHandler = new PropertyHandler();

    public PropertyHandler disguisePropertyHandler()
    {
        return propertyHandler;
    }

    //endregion Disguise Property

    //region CustomProperty

    private final Map<String, Object> propertiesMap = new Object2ObjectArrayMap<>();

    /**
     * @apiNote 这不是伪装的属性，仅仅针对此DisguiseState会话！
     */
    public void setSessionData(String name, Object value)
    {
        if (value == null)
        {
            this.removeSessionData(name);
            return;
        }

        propertiesMap.put(name, value);
    }

    /**
     * @apiNote 这不是伪装的属性，仅仅针对此DisguiseState会话！
     */
    @Nullable
    public <T> T getSessionData(String name, Class<T> type)
    {
        var value = propertiesMap.getOrDefault(name, null);
        if (value == null) return null;

        if (type.isInstance(value)) return (T) value;

        return null;
    }

    @NotNull
    public <T> T getSessionDataOr(String name, Class<T> type, @NotNull T defaultVal)
    {
        var data = this.getSessionData(name, type);
        return data == null ? defaultVal : data;
    }

    /**
     * @apiNote 这不是伪装的属性，仅仅针对此DisguiseState会话！
     */
    public void removeSessionData(String name)
    {
        propertiesMap.remove(name);
    }

    //endregion CustomProperty

    /**
     * 技能查询ID
     */
    @Nullable
    private String skillLookupIdentifier = null;

    private static final String DEFAULT_SKILL_LOOKUP = NamespacedKey.MINECRAFT + ":" + MorphManager.disguiseFallbackName;

    /**
     * 获取用于查询技能的ID
     *
     * @return 技能ID
     */
    @NotNull
    public String skillLookupIdentifier()
    {
        return skillLookupIdentifier == null ? DEFAULT_SKILL_LOOKUP : skillLookupIdentifier;
    }

    /**
     * 设置技能查询ID
     *
     * @param newSkillID 技能ID
     */
    public void skillLookupIdentifier(@NotNull String newSkillID)
    {
        this.skillLookupIdentifier = newSkillID;
    }

    private final SkillUpdater skillUpdater = new SkillUpdater(this);

    private void postExecuteSkill()
    {
        soundHandler.resetSoundTime();
    }

    public void setDefaultSkillCooldown(int cd)
    {
        skillUpdater.defaultSkillCooldown = Math.max(0, cd);
    }

    public boolean skillInCooldown()
    {
        return plugin.getCurrentTick() < skillUpdater.getAvailableAfter();
    }

    public int getDefaultSkillCooldown()
    {
        return skillUpdater.defaultSkillCooldown;
    }

    public boolean executeSkillCheckPermission()
    {
        if (skillUpdater.executeSkillCheckPermission())
        {
            postExecuteSkill();
            return true;
        }

        return false;
    }

    public boolean executeSkillDirect()
    {
        if (skillUpdater.executeSkill())
        {
            postExecuteSkill();
            return true;
        }

        return false;
    }

    public <O extends ISkillAbilityOption> void bindSkill(@Nullable ISkill<O> newSkill, O option)
    {
        skillUpdater.bindSkill(newSkill, option);
    }

    /**
     * 获取此伪装的技能
     * @return {@link ISkill}
     */
    @NotNull
    public ISkill<?> getSkill()
    {
        return skillUpdater.getBindingSkill();
    }

    /**
     * 此伪装是否拥有技能
     * @return 伪装技能是否为 {@link NoneMorphSkill} 的实例
     */
    public boolean haveSkill()
    {
        return skillUpdater.getBindingSkill() != NoneMorphSkill.instance;
    }

    public long calculateRemainingCooldown()
    {
        return skillUpdater.calculateRemainingCooldown();
    }

    public void setSkillCooldown(long val, boolean notifyClient)
    {
        skillUpdater.setCooldown(val, notifyClient);
    }

    public void setAvailableAfter(long val, boolean notifyClient)
    {
        skillUpdater.setAvailableAfter(val, notifyClient);
    }

    public void applyCooldownToClient()
    {
        skillUpdater.applyCooldownToClient();
    }

    //region Waypoint

    private final DisguiseWaypointUpdater disguiseWaypointUpdater;

    public DisguiseWaypointUpdater waypointUpdater()
    {
        return disguiseWaypointUpdater;
    }

    //endregion Waypoint

    //region 被动技能

    @NotNull
    private final AbilityUpdater abilityUpdater;

    @NotNull
    public AbilityUpdater getAbilityUpdater()
    {
        return abilityUpdater;
    }

    /**
     * 检查某个被动能力是否设置
     * @param key {@link NamespacedKey}
     * @return 是否设置
     */
    public boolean containsAbility(NamespacedKey key)
    {
        return abilityUpdater.containsAbility(key);
    }

    //endregion abilityFlag

    @Deprecated
    public String getCulledNbtString()
    {
        return NbtUtils.getCompoundString(disguiseWrapper.getCompound());
    }

    //region ProfileNBT

    public String getProfileNbtString()
    {
        if (!haveProfile())
             return "{}";

        return NbtUtils.getCompoundString(NbtUtils.toCompoundTag(disguiseWrapper.getSkin()));
    }

    public boolean haveProfile()
    {
        return disguiseWrapper.getSkin() != null;
    }

    //endregion ProfileNBT

    //region Updating

    private final CompletableFuture<DisguiseState> stateFuture = new CompletableFuture<>();

    // If the selfUpdate loop has scheduled, or began
    private volatile boolean selfUpdateBegan;

    /**
     * Get A {@link CompletableFuture} that binds to this state.<br>
     * Finishes when this state has been disposed.<br>
     * Fail with exception if an error occurred while updating this state, or the state has been disposed without running {@link DisguiseState#doUpdate()} once
     */
    public CompletableFuture<DisguiseState> getStateFuture()
    {
        var instance = new CompletableFuture<DisguiseState>();
        stateFuture.thenAccept(instance::complete);
        stateFuture.exceptionally(t ->
        {
            instance.completeExceptionally(t);
            return null;
        });

        return instance;
    }

    /**
     * @throws UnsupportedOperationException If an existing update loop is running
     */
    public void scheduleSelfUpdate() throws UnsupportedOperationException
    {
        var currentLoopTask = this.loopTask;
        if (currentLoopTask != null && !currentLoopTask.isCancelled())
            throw new UnsupportedOperationException("Scheduling update loop while an existing loop is running");

        loopTask = getPlayer().getScheduler().runAtFixedRate(plugin, this::updateLoop, this::onEntityRetired, 1, 1);
    }

    @Nullable
    private volatile ScheduledTask loopTask;

    private void updateLoop(ScheduledTask task)
    {
        if (!task.isCancelled())
            doUpdate();
    }

    private void onEntityRetired()
    {
    }

    // Adding synchronized since we don't want someone to dispose when the DisguiseState is running self update
    private synchronized void doUpdate()
    {
        if (this.disposed()) return;

        if (!selfUpdateBegan)
            selfUpdateBegan = true;

        try
        {
            var player = getPlayer();

            if (player.isOnline())
            {
                if (!this.getProvider().updateDisguise(player, this))
                    throw new UpdateFailedException("Failed executing provider update");

                if (!this.selfUpdate())
                    throw new UpdateFailedException("Failed executing self update");
            }
        }
        catch (Exception e)
        {
            logger.warn("Error occurred while updating disguise", e);
            stateFuture.completeExceptionally(e);
        }
    }

    public boolean selfUpdate()
    {
        if (this.canPlayAmbient())
            this.getSoundHandler().update();

        this.animationSequence.update();
        disguiseWaypointUpdater.tick();
        skillUpdater.update();
        return this.abilityUpdater.update();
    }

    //endregion Updating

    public void refreshDisguiseItems(@Nullable EntityEquipment targetEquipment)
    {
        EntityEquipment equipment = targetEquipment != null ? targetEquipment : new DisguiseEquipment();

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

        var disguiseEquipments = new DisguiseEquipment();

        disguiseEquipments.allowNull = false;
        disguiseEquipments.setArmorContents(armors);
        disguiseEquipments.setHandItems(handItems);

        //开启默认装备显示或者更新显示
        setEquipment(disguiseEquipments);
        setShowingDisguisedEquipment(targetEquipment != null);
    }

    private <X> void consumeIfPropertiesSupported(Class<X> clazz, Consumer<X> consumer)
    {
        var bindingProperties = propertyHandler.bindingProperties();
        if (bindingProperties == null) return;
        if (!clazz.isInstance(bindingProperties)) return;

        consumer.accept((X) bindingProperties);
    }

    private <X, V> Optional<V> funcIfPropertiesSupported(Class<X> clazz, Function<X, Optional<V>> func)
    {
        var bindingProperties = propertyHandler.bindingProperties();
        if (bindingProperties == null) return Optional.empty();
        if (!clazz.isInstance(bindingProperties)) return Optional.empty();

        return func.apply((X) bindingProperties);
    }

    /**
     * 此阶段是否正在显示伪装物品
     * @return 是否正在显示
     */
    public boolean showingDisguisedItems()
    {
        return propertyHandler.getOr(PropertyNames.ENTITY_DISPLAY_DISGUISE_EQUIPMENT, false);
    }

    public void editEquipment(Consumer<DisguiseEquipment> consumer)
    {
        var equipment = getDisguiseEquipment();
        consumer.accept(equipment);

        setEquipment(equipment);
    }

    public void setEquipment(DisguiseEquipment equipment)
    {
        consumeIfPropertiesSupported(BaseLivingEntityProperties.class, p -> propertyHandler.set(p.EQUIPMENT, equipment));
        disguiseWrapper.setFakeEquipments(equipment);
    }

    /**
     * 设置是否要显示伪装物品
     * @param value 值
     */
    public void setShowingDisguisedEquipment(boolean value)
    {
        consumeIfPropertiesSupported(BaseLivingEntityProperties.class, p ->
                propertyHandler.set(p.DISPLAY_DISGUISE_EQUIPMENT, value));
    }

    /**
     * 获取此State的伪装物品
     * @return 此State的伪装物品
     */
    public DisguiseEquipment getDisguiseEquipment()
    {
        var eq = new DisguiseEquipment();

        var disguiseEquipments = funcIfPropertiesSupported(BaseLivingEntityProperties.class, properties ->
        {
            return propertyHandler.getOptional((SingleProperty<DisguiseEquipment>)properties.EQUIPMENT);
        }).orElse(null);

        if (disguiseEquipments != null)
        {
            eq.setArmorContents(ItemUtils.asCopy(disguiseEquipments.getArmorContents()));
            eq.setHandItems(ItemUtils.asCopy(disguiseEquipments.getHandItems()));
        }

        return eq;
    }

    @ApiStatus.Internal
    public void swapHands()
    {
        editEquipment(equipment ->
        {
            var handItems = equipment.getHandItems();

            if (handItems.length != 2) return;

            var mainHand = handItems[0];
            var offHand = handItems[1];

            equipment.setHandItems(offHand, mainHand);
        });
    }

    /**
     * 切换伪装物品是否可见
     * @return 切换后的值
     */
    public boolean toggleDisguisedItems()
    {
        var showDisguisedItems = showingDisguisedItems();

        setShowingDisguisedEquipment(!showDisguisedItems);

        return !showDisguisedItems;
    }

    //region Sound Handling

    private final SoundHandler soundHandler;

    public SoundHandler getSoundHandler()
    {
        return soundHandler;
    }

    //endregion Sound Handling

    public DisguiseState createCopy(Player player)
    {
        if (disposed())
            throw new RuntimeException("Can't create a copy of a disposed DisguiseState");

        var wrapper = this.disguiseWrapper.clone();

        var newInstance = new DisguiseState(player, this.disguiseIdentifier, this.skillLookupIdentifier(),
                wrapper, provider, this.playerOptions, morphConfiguration);

        newInstance.playerDisplay = this.playerDisplay;
        newInstance.serverDisplay = this.serverDisplay;

        return newInstance;
    }

    private final AtomicBoolean disposed = new AtomicBoolean(false);

    public boolean disposed()
    {
        return disposed.get();
    }

    // Adding synchronized since we don't want someone to dispose when the DisguiseState is running self update
    @Override
    public synchronized void dispose()
    {
        if (disposed())
            return;

        if (selfUpdateBegan)
            stateFuture.complete(this);
        else
            stateFuture.completeExceptionally(new EarlyDisposeException("The DisguiseState has been disposed before running once"));

        disposed.set(true);

        this.waypointUpdater().dispose();
        this.disguiseWrapper.dispose();
        this.abilityUpdater.dispose();
        this.propertyHandler.dispose();

        this.provider.unMorph(getPlayer(), this);
        this.abilityUpdater.setAbilities(List.of());
        this.skillUpdater.submitCooldown(skillManager.cooldownManager());

        this.bindSkill(null, null);
    }
}
