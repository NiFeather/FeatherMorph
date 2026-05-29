package xyz.nifeather.morph.misc;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.RevealingHandler;
import xyz.nifeather.morph.abilities.AbilityUpdater;
import xyz.nifeather.morph.backends.DisguiseWrapper;
import xyz.nifeather.morph.messages.strings.CommandStrings;
import xyz.nifeather.morph.messages.strings.EmoteStrings;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.messages.strings.MorphStrings;
import xyz.nifeather.morph.misc.actions.ConsumerActions;
import xyz.nifeather.morph.misc.attributes.DisguiseAttributeHandler;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.BaseLivingEntityPropertyCollection;
import xyz.nifeather.morph.misc.gui.IconLookup;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;
import xyz.nifeather.morph.misc.waypoint.DisguiseWaypointTransmitter;
import xyz.nifeather.morph.misc.waypoint.DummyDisguiseWaypointTransmitter;
import xyz.nifeather.morph.misc.waypoint.IDisguiseWaypointTransmitter;
import xyz.nifeather.morph.network.PlayerOptions;
import xyz.nifeather.morph.network.commands.S2C.S2CPlayAnimationCommand;
import xyz.nifeather.morph.network.commands.S2C.set.S2CSetAnimationDisplayNameCommand;
import xyz.nifeather.morph.network.server.MorphClientHandler;
import xyz.nifeather.morph.network.server.frog.S2CEntityAnimateCommand;
import xyz.nifeather.morph.network.server.frog.S2CUpdateEntityAnimateMaskCommand;
import xyz.nifeather.morph.providers.animation.PlayableAction;
import xyz.nifeather.morph.providers.disguise.DisguiseProvider;
import xyz.nifeather.morph.skills.ISkill;
import xyz.nifeather.morph.skills.SkillManager;
import xyz.nifeather.morph.skills.SkillUpdater;
import xyz.nifeather.morph.skills.impl.NoneMorphSkill;
import xyz.nifeather.morph.storage.playerdata.PlayerMeta;
import xyz.nifeather.morph.storage.skill.ISkillAbilityOption;
import xyz.nifeather.morph.utilities.NbtUtils;
import xyz.nifeather.morph.utilities.PermissionUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class DisguiseState extends MorphPluginObject
{
    public DisguiseState(@NotNull Player player, @NotNull String identifier, @NotNull String skillIdentifier,
                         @NotNull DisguiseWrapper<?> wrapper, @NotNull DisguiseProvider provider,
                         @NotNull PlayerOptions<Player> playerOptions,
                         @NotNull PlayerMeta playerMeta, boolean transmitWaypoint)
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
        this.playerMeta = playerMeta;

        this.soundHandler = new SoundHandler(player);
        this.abilityUpdater = new AbilityUpdater(this);
        this.disguiseAttributes = new DisguiseAttributeHandler();

        this.disguiseWaypointTransmitter = transmitWaypoint
                ? new DisguiseWaypointTransmitter(this)
                : new DummyDisguiseWaypointTransmitter(player.getName());

        this.disguiseWrapper = wrapper;
        this.disguiseIdentifier = identifier;
        this.skillLookupIdentifier = skillIdentifier;

        disguiseType = DisguiseTypes.fromId(identifier);

        //设置声音
        this.soundHandler.refreshSounds(this, wrapper.getEntityType(), wrapper.isBaby());

        this.cachedPlayer = CacheWithDefault.of(player);

        disguiseAttributes.initializeFor(getEntityType());
        disguiseAttributes.hookOnAttributeChange(this::onDisguiseAttributeChange);

        actionHandler.setCooldown(10);
        actionHandler.onNewStage(anim ->
        {
            anim.onPlay().accept(this);

            var legacyName = anim.legacyName();
            if (legacyName != null)
                clientHandler.sendCommand(getPlayer(), new S2CPlayAnimationCommand(legacyName));
        });

        actionHandler.onStageFinish(stage -> stage.onFinish().accept(this));

        actionHandler.onNewAction(name ->
                clientHandler.sendCommand(getPlayer(), new S2CSetAnimationDisplayNameCommand(name)));

        disguisePropertyHandler().hookOnPropertyWrite(this::onPropertyWrite);
        disguisePropertyHandler().hookOnPropertyDiscard(this::onPropertyDiscard);

        disguisePropertyHandler().hookOnTemporaryPropertyWrite(this::onTempPropertyWrite);
        disguisePropertyHandler().hookOnTemporaryPropertyDiscard(this::onTemporaryDiscard);
    }

    private void onTemporaryDiscard(SingleProperty<Object> property)
    {
        var persistValue = disguisePropertyHandler().getOr(property, null);
        if (persistValue == null)
        {
            disguiseWrapper.discardProperty(property);
            return;
        }

        disguiseWrapper.writeProperty(property, persistValue);
    }

    private <V> void onTempPropertyWrite(SingleProperty<V> proper, V o)
    {
        this.onPropertyWrite(proper, o);
    }

    private void onPropertyDiscard(SingleProperty<Object> property)
    {
        disguiseWrapper.discardProperty(property);
    }

    private void onPropertyWrite(SingleProperty<?> singleProperty, Object o)
    {
        switch (singleProperty.id())
        {
            case PropertyNames.ENTITY_CUSTOM_NAME ->
            {
                var component = (Component) o;
                this.setCustomDisplayName(component);
                requestActionbarUpdate();
            }

            case PropertyNames.MANNEQUIN_SKIN, PropertyNames.PLAYER_SKIN ->
            {
                requestActionbarUpdate();
            }
        }

        disguiseWrapper.writeProperty((SingleProperty<Object>) singleProperty, o);
    }

    private void onDisguiseAttributeChange(NamespacedKey id, AttributeInstance attribute)
    {
        disguiseWrapper.onDisguiseAttributeChange(id, attribute);
    }

    @Resolved(shouldSolveImmediately = true)
    private SkillManager skillManager;

    @Resolved(shouldSolveImmediately = true)
    private MorphClientHandler clientHandler;

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

    private final PlayerMeta playerMeta;

    private final AnimationHandler actionHandler = new AnimationHandler();

    public void stopActions()
    {
        actionHandler.reset();
    }

    public void onPlayerQuit()
    {
        this.stopActions();

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
    public boolean tryScheduleAction(@NotNull String name, PlayableAction action)
    {
        if (!canScheduleSequence()) return false;
        this.scheduleAction(name, action);

        return true;
    }

    public void scheduleAction(String name, PlayableAction action)
    {
        this.scheduleAction(name, action, true);
    }

    private void scheduleAction(String name, PlayableAction action, boolean checkPermission)
    {
        var player = getPlayer();

        if (checkPermission
                && !PermissionUtils.hasPermission(player, CommonPermissions.animationPermissionOf(name, this.getDisguiseIdentifier()), true))
        {
            MessageUtils.send(player, CommandStrings.noPermissionMessage());
            return;
        }

        this.actionHandler.scheduleNext(name, action);

        var animationString = CommandStrings.goingToPlayAnimation()
                .resolve("what", EmoteStrings.get(name));

        MessageUtils.send(player, animationString);
    }

    public AnimationHandler getActionHandler()
    {
        return actionHandler;
    }

    /**
     * See {@link net.minecraft.network.protocol.game.ClientboundAnimatePacket}
     * @param animateName
     */
    @ApiStatus.Experimental
    public void playEntityAnimation(String animateName)
    {
        clientHandler.sendCommand(getPlayer(), new S2CEntityAnimateCommand(animateName));
        disguiseWrapper.playEntityAnimation(animateName);
    }

    /**
     * Set whether allow disguise to play several entity animates triggered by the player. <br>
     * Note that this doesn't affect {@link DisguiseState#playEntityAnimation(String)}
     */
    @ApiStatus.Experimental
    public void updateEntityAnimateMask(String animateName, boolean isAllowed)
    {
        clientHandler.sendCommand(getPlayer(), new S2CUpdateEntityAnimateMaskCommand(animateName, isAllowed));
        disguiseWrapper.updateEntityAnimateMask(animateName, isAllowed);
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
        return playerOptions.isClientSideSelfView() ? playerMeta.showDisguiseToSelf : serverSideSelfVisible;
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
    private final String disguiseIdentifier;

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

    private final DisguiseTypes disguiseType;

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
    private final DisguiseProvider provider;

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

    //region Disguise Attribute

    private final DisguiseAttributeHandler disguiseAttributes;
    public DisguiseAttributeHandler disguiseAttributeHandler()
    {
        return disguiseAttributes;
    }

    //endregion Disguise Attribute

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

    /**
     * See {@link SkillUpdater#executeSkillCheckPermission()}
     */
    public boolean executeSkillCheckPermission()
    {
        if (skillUpdater.executeSkillCheckPermission())
        {
            postExecuteSkill();
            return true;
        }

        return false;
    }

    /**
     * See {@link SkillUpdater#executeSkill()}
     */
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

    private final IDisguiseWaypointTransmitter disguiseWaypointTransmitter;

    public IDisguiseWaypointTransmitter waypointTransmitter()
    {
        return disguiseWaypointTransmitter;
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

    //region Updating

    private final CompletableFuture<DisguiseState> stateFuture = new CompletableFuture<>();

    // If the selfUpdate loop has scheduled, or began
    private volatile boolean selfUpdateBegan;

    /**
     * Get A {@link CompletableFuture} that binds to this state.<br>
     * Finishes when this state has been disposed.<br>
     * Fail with exception if an error occurred while updating this state
     * @apiNote If you wish to do something immediately on disposal, use {@link DisguiseState#onDispose(Consumer)}
     */
    public CompletableFuture<DisguiseState> getStateFuture()
    {
        // Return a new CompletableFuture, so that folks calling this method can do their own things.
        // Like MorphManager#applyDisguise
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

                this.selfUpdate();
            }
        }
        catch (Throwable t)
        {
            handleException(t);
        }
    }

    private volatile boolean exceptionOnce = false;

    public void handleException(Throwable t)
    {
        if (!exceptionOnce)
        {
            exceptionOnce = true;

            logger.error("Error occurred with disguise", t);
            stateFuture.completeExceptionally(t);
        }
        else
        {
            logger.warn("Error occurred with disguise (Has earlier exception, not triggering)", t);
        }
    }

    public void selfUpdate()
    {
        if (this.canPlayAmbient())
            this.getSoundHandler().update();

        this.actionHandler.update();
        this.disguiseWaypointTransmitter.tick();
        this.skillUpdater.update();
        this.disguiseWrapper.update();
        this.abilityUpdater.update();

        if (playerOptions.displayDisguiseOnHUD && plugin.getCurrentTick() % (this.haveSkill() ? 2 : 5) == 0)
            updateActionbarMessage();
    }

    @Resolved(shouldSolveImmediately = true)
    private RevealingHandler revealingHandler;

    @Nullable
    private CachedMessageStatus cachedMessageStatus;

    private volatile boolean requestedActionbarUpdate;

    /**
     * Request to update the actionbar message next time {@link DisguiseState#updateActionbarMessage()} is called.
     */
    public void requestActionbarUpdate()
    {
        requestedActionbarUpdate = true;
    }

    private void updateActionbarMessage()
    {
        var player = getPlayer();
        var locale = MessageUtils.getLocale(player);
        var haveSkill = this.haveSkill();

        boolean updateAnyway = requestedActionbarUpdate;
        requestedActionbarUpdate = false;

        // If this mismatches, we will know that we should refresh the message component
        short magicBit = 0;

        if (haveSkill)
            magicBit |= 1;

        if (skillInCooldown())
            magicBit |= 2;
        else
            magicBit |= 4;

        var revLevel = revealingHandler.getRevealingLevel(player);
        switch (revLevel)
        {
            case SAFE -> magicBit |= 8;
            case SUSPECT -> magicBit |= 16;
            case REVEALED -> magicBit |= 32;
        }

        magicBit |= (short) locale.hashCode();

        var msgConfig = this.cachedMessageStatus;
        if (msgConfig == null) msgConfig = CachedMessageStatus.DEFAULT;

        short stateBit = msgConfig.statusBit();

        if (stateBit != magicBit || updateAnyway)
        {
            //更新actionbar信息
            var msg = haveSkill
                    ? (!skillInCooldown()
                    ? MorphStrings.disguisingWithSkillAvaliableString()
                    : MorphStrings.disguisingWithSkillPreparingString())
                    : MorphStrings.disguisingAsString();

            var disguiseRevealed = revLevel == RevealingHandler.RevealingLevel.REVEALED || revLevel == RevealingHandler.RevealingLevel.SUSPECT;
            var display = disguiseRevealed
                    ? getPlayerDisplay().append((revLevel == RevealingHandler.RevealingLevel.REVEALED ? MorphStrings.revealed() : MorphStrings.partialRevealed()).createComponent(locale))
                    : getPlayerDisplay();

            msgConfig = new CachedMessageStatus(magicBit,
                    msg.resolve("what", display).resolve("icon", IconLookup.instance().lookupDisguiseIcon(this)).createComponent(locale));

            this.cachedMessageStatus = msgConfig;
        }

        player.sendActionBar(msgConfig.display());
    }

    //endregion Updating

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
        var properties = DisguiseProperties.INSTANCE.getCollectionOrThrow(BaseLivingEntityPropertyCollection.class);
        propertyHandler.set(properties.EQUIPMENT, equipment);
    }

    /**
     * 设置是否要显示伪装物品
     * @param value 值
     */
    public void setShowingDisguisedEquipment(boolean value)
    {
        var properties = DisguiseProperties.INSTANCE.getCollectionOrThrow(BaseLivingEntityPropertyCollection.class);
        propertyHandler.set(properties.DISPLAY_DISGUISE_EQUIPMENT, value);
    }

    /**
     * 获取此State的伪装物品
     * @return 此State的伪装物品
     */
    public DisguiseEquipment getDisguiseEquipment()
    {
        var properties = DisguiseProperties.INSTANCE.getCollectionOrThrow(BaseLivingEntityPropertyCollection.class);
        return propertyHandler.getOptional((SingleProperty<DisguiseEquipment>)properties.EQUIPMENT).orElseGet(DisguiseEquipment::empty);
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

    private final ConsumerActions<DisguiseState> onDisposeActions = new ConsumerActions<>();

    public void onDispose(Consumer<DisguiseState> consumer)
    {
        onDisposeActions.hook(consumer);
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

        stateFuture.complete(this);
        onDisposeActions.invoke(this);

        disposed.set(true);

        this.waypointTransmitter().dispose();
        this.disguiseWrapper.dispose();
        this.abilityUpdater.dispose();
        this.propertyHandler.dispose();

        this.provider.unMorph(getPlayer(), this);
        this.abilityUpdater.setAbilities(List.of());
        this.skillUpdater.submitCooldown(skillManager.cooldownManager());

        this.bindSkill(null, null);
    }
}
