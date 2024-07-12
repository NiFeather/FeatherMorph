package xiamomc.morph;

import io.papermc.paper.util.TickThread;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.abilities.AbilityManager;
import xiamomc.morph.backends.DisguiseBackend;
import xiamomc.morph.backends.DisguiseWrapper;
import xiamomc.morph.backends.WrapperAttribute;
import xiamomc.morph.backends.fallback.NilBackend;
import xiamomc.morph.backends.server.ServerBackend;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.events.api.gameplay.*;
import xiamomc.morph.events.api.lifecycle.ManagerFinishedInitializeEvent;
import xiamomc.morph.interfaces.IManagePlayerData;
import xiamomc.morph.messages.CommandStrings;
import xiamomc.morph.messages.HintStrings;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.MorphStrings;
import xiamomc.morph.messages.vanilla.VanillaMessageStore;
import xiamomc.morph.misc.*;
import xiamomc.morph.misc.permissions.CommonPermissions;
import xiamomc.morph.network.commands.S2C.clientrender.S2CRenderMapAddCommand;
import xiamomc.morph.network.commands.S2C.clientrender.S2CRenderMapSyncCommand;
import xiamomc.morph.network.commands.S2C.map.S2CMapCommand;
import xiamomc.morph.network.commands.S2C.map.S2CMapRemoveCommand;
import xiamomc.morph.network.commands.S2C.map.S2CPartialMapCommand;
import xiamomc.morph.network.commands.S2C.set.*;
import xiamomc.morph.network.multiInstance.MultiInstanceService;
import xiamomc.morph.network.multiInstance.protocol.Operation;
import xiamomc.morph.network.server.MorphClientHandler;
import xiamomc.morph.providers.DisguiseProvider;
import xiamomc.morph.providers.FallbackProvider;
import xiamomc.morph.providers.PlayerDisguiseProvider;
import xiamomc.morph.providers.VanillaDisguiseProvider;
import xiamomc.morph.skills.MorphSkillHandler;
import xiamomc.morph.skills.SkillCooldownInfo;
import xiamomc.morph.skills.SkillType;
import xiamomc.morph.storage.offlinestore.OfflineDisguiseState;
import xiamomc.morph.storage.offlinestore.OfflineStateStore;
import xiamomc.morph.storage.playerdata.PlayerDataStore;
import xiamomc.morph.storage.playerdata.PlayerMeta;
import xiamomc.morph.utilities.DisguiseUtils;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Bindables.Bindable;
import xiamomc.pluginbase.Bindables.BindableList;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class MorphManager extends MorphPluginObject implements IManagePlayerData
{
    private final List<DisguiseState> disguiseStates = new ObjectArrayList<>();

    private final PlayerDataStore data = new PlayerDataStore();

    private final OfflineStateStore offlineStorage = new OfflineStateStore();

    @Resolved
    private MorphSkillHandler skillHandler;

    @Resolved
    private AbilityManager abilityHandler;

    @Resolved
    private MorphConfigManager config;

    @Resolved
    private NetworkingHelper networkingHelper;

    @Resolved
    private MultiInstanceService multiInstanceService;

    public static final DisguiseProvider fallbackProvider = new FallbackProvider();

    public static final String disguiseFallbackName = "@default";

    public static final String forcedDisguiseNoneId = "@none";

    //region Backends

    private final NilBackend nilBackend = new NilBackend();

    @NotNull
    private DisguiseBackend<?, ?> defaultBackend = nilBackend;

    @NotNull
    public DisguiseBackend<?, ?> getDefaultBackend()
    {
        return defaultBackend;
    }

    private final Map<String, DisguiseBackend<?, ?>> backends = new Object2ObjectArrayMap<>();

    public boolean registerBackend(DisguiseBackend<?, ?> backend)
    {
        var id = backend.getIdentifier();

        if (backends.containsKey(id))
            return false;

        backends.put(id, backend);

        return true;
    }

    @Nullable
    public DisguiseBackend<?, ?> getBackend(String id)
    {
        return backends.getOrDefault(id, null);
    }

    @Nullable
    public <I, W extends DisguiseWrapper<I>, T extends DisguiseBackend<I, W>> T getBackend(String id, Class<T> exceptedClass)
    {
        var backend = this.getBackend(id);

        if (exceptedClass.isInstance(backend))
            return (T) backend;

        return null;
    }

    /**
     * @return A list that contains all backends registered to this MorphManager instance
     */
    public Collection<DisguiseBackend<?, ?>> listManagedBackends()
    {
        return backends.values();
    }

    public boolean switchBackend(DisguiseBackend<?, ?> backend)
    {
        if (!backends.containsKey(backend.getIdentifier()))
        {
            logger.error("Trying to switch to a backend that is not registered");
            return false;
        }

        try
        {
            defaultBackend = backend;

            disguiseStates.forEach(state ->
            {
                state.getDisguiseWrapper().getBackend().unDisguise(state.getPlayer());

                //TODO: 更改默认后端时刷新伪装
                //var newWrapper = backend.cloneWrapperFrom(state.getDisguiseWrapper());
                //state.updateDisguise(
                //        state.getDisguiseIdentifier(), state.skillLookupIdentifier(),
                //        newWrapper, false, state.getDisguisedItems()
                //);

                // 等待1tick让客户端处理一些网络事务
                this.addSchedule(() ->
                {
                    if (!state.getDisguiseWrapper().disposed())
                        backend.disguise(state.getPlayer(), state.getDisguiseWrapper());
                });
            });
        }
        catch (Throwable t)
        {
            logger.error("Error occurred switching backend: " + t.getMessage());
            t.printStackTrace();

            return false;
        }

        return true;
    }

    private void tryBackends()
    {
        try
        {
            var serverBackend = new ServerBackend();

            registerBackend(serverBackend);
            switchBackend(serverBackend);
        }
        catch (NoClassDefFoundError e)
        {
            logger.error("Unable to initialize ServerBackend as our disguise backend, maybe ProtocolLib is not installed on the server.");
            logger.error("Using NilBackend, displaying disguises at the server side will not be supported this run.");
        }
        catch (Throwable t)
        {
            logger.error("Unable to initialize ServerBackend as our disguise backend: " + t.getMessage());
            logger.error("Using NilBackend, displaying disguises at the server side will not be supported this run.");
            logger.error("Please consider reporting this issue to our GitHub: https://github.com/XiaMoZhiShi/MorphPlugin/issues");

            t.printStackTrace();
        }
    }

    //endregion Backends

    private Material actionItem;
    public Material getActionItem()
    {
        return actionItem;
    }

    @Resolved
    private MorphClientHandler clientHandler;

    @Initializer
    private void load()
    {
        this.addSchedule(this::update);

        registerBackend(nilBackend);
        tryBackends();

        logger.info("Default backend: %s".formatted(defaultBackend));

        bannedDisguises = config.getBindableList(String.class, ConfigOption.BANNED_DISGUISES);
        config.bind(allowHeadMorph, ConfigOption.ALLOW_HEAD_MORPH);
        config.bind(allowAcquireMorph, ConfigOption.ALLOW_ACQUIRE_MORPHS);
        config.bind(useClientRenderer, ConfigOption.USE_CLIENT_RENDERER);

        registerProviders(ObjectList.of(
                new VanillaDisguiseProvider(),
                new PlayerDisguiseProvider(),
                //new ItemDisplayProvider(),
                //new LocalDisguiseProvider(),
                fallbackProvider
        ));

        var actionItemId = config.getBindable(String.class, ConfigOption.SKILL_ITEM);
        actionItemId.onValueChanged((o, n) ->
        {
            var item = Material.matchMaterial(n);
            var disabled = "disabled";

            if (item == null && !disabled.equals(n))
                logger.warn("Cannot find any item that matches \"" + n + "\" to set for the skill item, some related features may not work!");

            actionItem = item;
        }, true);

        Bukkit.getPluginManager().callEvent(new ManagerFinishedInitializeEvent(this));
    }

    private void update()
    {
        this.addSchedule(this::update);

        var states = this.getDisguiseStates();

        states.forEach(state ->
        {
            var p = state.tryGetPlayer();

            if (p == null) return;

            if (!TickThread.isTickThreadFor(NmsRecord.ofPlayer(p)))
                this.scheduleOn(p, () -> this.updateDisguiseSingle(state));
            else
                this.updateDisguiseSingle(state);
        });
    }

    private void updateDisguiseSingle(DisguiseState state)
    {
        var player = state.getPlayer();

        //logger.info("Run at " + plugin.getCurrentTick() + " -> " + i);

        //跳过离线玩家
        if (!player.isOnline() || state.disposed()) return;

        boolean stateSuccess = false;
        boolean providerSuccess = false;

        try
        {
            providerSuccess = state.getProvider().updateDisguise(player, state);
            stateSuccess = state.selfUpdate();
        }
        catch (Throwable t)
        {
            logger.error("Error occurred updating disguise! " + t.getMessage());
            t.printStackTrace();
        }

        if (!providerSuccess || !stateSuccess)
        {
            player.sendMessage(MessageUtils.prefixes(player, MorphStrings.errorWhileUpdatingDisguise()));

            unMorph(nilCommandSource, player, true, true);
        }
    }

    //region 玩家伪装相关

    /**
     * 使某个玩家执行伪装的主动技能
     * @param player 目标玩家
     */
    public void executeDisguiseSkill(Player player)
    {
        skillHandler.executeDisguiseSkill(player);
    }

    /**
     * 获取所有已伪装的玩家
     * @return 玩家列表
     * @apiNote 列表中的玩家可能已经离线
     */
    public List<DisguiseState> getDisguiseStates()
    {
        return new ObjectArrayList<>(disguiseStates);
    }

    private final Map<UUID, Long> uuidMoprhTimeMap = new ConcurrentHashMap<>();

    /**
     * 检查某个玩家是否可以伪装
     * @param player 玩家
     * @return 是否可以伪装
     */
    public boolean canMorph(Player player)
    {
        return this.canMorph(player.getUniqueId());
    }

    /**
     * 检查某个玩家是否可以伪装
     * @param uuid 玩家UUID
     * @return 是否可以伪装
     */
    public boolean canMorph(UUID uuid)
    {
        var val = uuidMoprhTimeMap.get(uuid);

        return val == null || plugin.getCurrentTick() - val >= 4;
    }

    /**
     * 更新某个玩家的上次伪装操作事件
     * @param player 要更新的玩家
     */
    public void updateLastPlayerMorphOperationTime(Player player)
    {
        uuidMoprhTimeMap.put(player.getUniqueId(), plugin.getCurrentTick());
    }

    private BindableList<String> bannedDisguises;

    /**
     * 内部轮子，检查某个伪装是否被禁用建议使用 {@link MorphManager#disguiseDisabled(String)}
     */
    @ApiStatus.Internal
    public BindableList<String> getBannedDisguises()
    {
        return bannedDisguises;
    }

    //region 伪装提供器

    private static final List<DisguiseProvider> providers = new ObjectArrayList<>();

    public static List<DisguiseProvider> getProviders()
    {
        return new ObjectArrayList<>(providers);
    }

    /**
     * 从ID获取DisguiseProvider
     * @param id 目标ID
     * @return 一个DisguiseProvider，若没找到或id是null则返回null
     */
    @NotNull
    public static DisguiseProvider getProvider(String id)
    {
        if (id == null) return null;

        id += ":";
        var splitedId = id.split(":", 2);

        return providers.stream().filter(p -> p.getNameSpace().equals(splitedId[0])).findFirst().orElse(fallbackProvider);
    }

    /**
     * 注册一个DisguiseProvider
     * @param provider 目标Provider
     * @return 操作是否成功
     */
    public boolean registerProvider(DisguiseProvider provider)
    {
        //logger.info("Registering disguise provider: " + provider.getNameSpace());

        if (provider.getNameSpace().contains(":"))
        {
            logger.error("Can't register disguise provider: Illegal character found in namespace: ':'");
            return false;
        }

        if (providers.stream().anyMatch(p -> p.getNameSpace().equals(provider.getNameSpace())))
        {
            logger.error("Can't register disguise provider: Another provider instance already registered as " + provider.getNameSpace() + " !");
            return false;
        }

        providers.add(provider);
        return true;
    }

    /**
     * 注册一批DisguiseProvider
     * @param providers Provider列表
     * @return 所有操作是否成功
     */
    public boolean registerProviders(List<DisguiseProvider> providers)
    {
        AtomicBoolean success = new AtomicBoolean(false);

        providers.forEach(p -> success.set(registerProvider(p) || success.get()));

        return success.get();
    }

    //endregion

    private final Bindable<Boolean> allowHeadMorph = new Bindable<>(true);

    private final Bindable<Boolean> allowAcquireMorph = new Bindable<>(true);
    private final Bindable<Boolean> useClientRenderer = new Bindable<>(false);

    private final Map<UUID, PlayerTextures> uuidPlayerTexturesMap = new ConcurrentHashMap<>();

    /**
     * 尝试调用快速伪装
     *
     * @param player 发起玩家
     * @return 操作是否成功
     */
    public boolean doQuickDisguise(Player player, boolean ignoreActionItem)
    {
        var actionItem = ignoreActionItem ? null : getActionItem();
        var state = this.getDisguiseStateFor(player);
        var mainHandItem = player.getEquipment().getItemInMainHand();
        var mainHandItemType = mainHandItem.getType();

        // 检查是否可以通过头颅伪装
        if (DisguiseUtils.validForHeadMorph(mainHandItemType))
        {
            if (!player.hasPermission(CommonPermissions.HEAD_MORPH))
            {
                player.sendMessage(MessageUtils.prefixes(player, CommandStrings.noPermissionMessage()));

                return true;
            }

            if (!allowHeadMorph.get())
            {
                player.sendMessage(MessageUtils.prefixes(player, MorphStrings.headDisguiseDisabledString()));

                return true;
            }

            if (!canMorph(player))
            {
                player.sendMessage(MessageUtils.prefixes(player, MorphStrings.disguiseCoolingDownString()));

                return true;
            }

            var targetEntity = player.getTargetEntity(5);

            switch (mainHandItemType)
            {
                case PIGLIN_HEAD -> morphOrUnMorph(player, EntityType.PIGLIN.getKey().asString(), targetEntity);
                case DRAGON_HEAD -> morphOrUnMorph(player, EntityType.ENDER_DRAGON.getKey().asString(), targetEntity);
                case ZOMBIE_HEAD -> morphOrUnMorph(player, EntityType.ZOMBIE.getKey().asString(), targetEntity);
                case SKELETON_SKULL -> morphOrUnMorph(player, EntityType.SKELETON.getKey().asString(), targetEntity);
                case WITHER_SKELETON_SKULL -> morphOrUnMorph(player, EntityType.WITHER_SKELETON.getKey().asString(), targetEntity);
                case PLAYER_HEAD ->
                {
                    var profile = ((SkullMeta) mainHandItem.getItemMeta()).getPlayerProfile();

                    //忽略没有profile的玩家伪装
                    if (profile == null)
                    {
                        player.sendMessage(MessageUtils.prefixes(player, MorphStrings.invalidSkinString()));
                        return true;
                    }

                    var name = profile.getName();
                    var profileTexture = profile.getTextures();
                    var playerUniqueId = player.getUniqueId();

                    //如果玩家有伪装，并且伪装的材质和Profile中的一样，那么取消伪装
                    if (state != null)
                    {
                        var disguise = state.getDisguiseWrapper();

                        if (disguise.isPlayerDisguise()
                                && disguise.getDisguiseName().equals(name)
                                && profileTexture.equals(uuidPlayerTexturesMap.get(playerUniqueId)))
                        {
                            unMorph(player);
                            return true;
                        }
                    }

                    //否则，更新或应用伪装
                    if (morph(player, player, DisguiseTypes.PLAYER.toId(profile.getName()), targetEntity))
                        uuidPlayerTexturesMap.put(playerUniqueId, profileTexture);
                }
            }

            updateLastPlayerMorphOperationTime(player);
        }
        else
        {
            if (actionItem != null && !mainHandItemType.equals(actionItem))
                return false;

            var targetedEntity = player.getTargetEntity(5);

            if (targetedEntity != null)
            {
                String targetKey;

                if (targetedEntity instanceof Player targetPlayer)
                {
                    var playerState = this.getDisguiseStateFor(targetPlayer);

                    //目标实体是玩家：玩家伪装ID > 玩家名
                    targetKey = playerState != null
                            ? playerState.getDisguiseIdentifier()
                            : DisguiseTypes.PLAYER.toId(targetPlayer.getName());
                }
                else
                {
                    //否则：伪装ID > 生物类型
                    var theirState = this.getDisguiseStateFor(targetedEntity);
                    targetKey = theirState != null
                            ? theirState.getDisguiseIdentifier()
                            : targetedEntity.getType().getKey().asString();
                }

                morph(player, player, targetKey, targetedEntity);

                return true;
            }
        }

        return false;
    }

    /**
     * 通过给定的key来决定要伪装还是取消伪装。
     * 如果伪装ID和给定的ID一致，则取消伪装，反之进行伪装。
     *
     * @param player 目标玩家
     * @param key 伪装ID
     * @param targetEntity 目标实体（如果有）
     */
    public void morphOrUnMorph(Player player, String key, @Nullable Entity targetEntity)
    {
        var state = this.getDisguiseStateFor(player);

        if (state != null && state.getDisguiseIdentifier().equals(key))
            unMorph(player);
        else
            morph(player, player, key, targetEntity);
    }

    /**
     * 伪装某一玩家
     *
     * @param source 伪装发起方
     * @param player 目标玩家
     * @param key 伪装ID
     * @param targetEntity 目标实体（如果有）
     * @return 操作是否成功
     */
    public boolean morph(CommandSender source, Player player,
                         String key, @Nullable Entity targetEntity)
    {
        var parameters = MorphParameters.create(player, key)
                .setSource(source)
                .setTargetedEntity(targetEntity);

        return this.doDisguise(parameters);
    }

    /**
     * 伪装某一玩家
     *
     * @param source 伪装发起方
     * @param player 目标玩家
     * @param key 伪装ID
     * @param targetEntity 目标实体（如果有）
     * @param forceExecute 是否强制执行
     * @return 操作是否成功
     */
    public boolean morph(CommandSender source, Player player,
                         String key, @Nullable Entity targetEntity,
                         boolean forceExecute)
    {
        var parameters = MorphParameters.create(player, key)
                .setForceExecute(forceExecute)
                .setSource(source)
                .setTargetedEntity(targetEntity);

        return this.doDisguise(parameters);
    }

    public boolean morph(MorphParameters parameters)
    {
        return doDisguise(parameters);
    }

    private boolean doDisguise(MorphParameters parameters)
    {
        try
        {
            var meta = preDisguise(parameters);
            if (meta == null)
                return false;

            var buildResult = buildDisguise(parameters, meta);
            if (!buildResult.success)
                return false;

            var playerMeta = getPlayerMeta(parameters.targetPlayer);
            this.postBuildDisguise(buildResult, parameters, playerMeta);
            this.afterDisguise(buildResult, parameters, playerMeta);

            return true;
        }
        catch (Throwable t)
        {
            logger.error("Unable to disguise player: " + t.getMessage());
            t.printStackTrace();

            var source = parameters.commandSource;
            source.sendMessage(MessageUtils.prefixes(source, MorphStrings.errorWhileDisguising()));

            unMorph(parameters.targetPlayer);

            return false;
        }
    }

    /**
     * 准备一些伪装需要的东西，包括权限检查
     * @param parameters MorphParameters
     * @return 一个DisguiseMeta，如果构建失败则返回Null
     */
    @Nullable
    private DisguiseMeta preDisguise(MorphParameters parameters)
    {
        // 确保source不为null
        var source = parameters.commandSource == null ? nilCommandSource : parameters.commandSource;
        var player = parameters.targetPlayer;
        var disguiseIdentifier = parameters.targetDisguiseIdentifier();

        // 检查玩家是否拥有此伪装的权限
        if (!parameters.bypassPermission)
        {
            // 1.玩家是否可以通过指令或客户端伪装
            // 2.玩家是否可以通过此ID伪装；若没有设置，则默认为允许
            var childNode = CommonPermissions.MORPH + ".as." + disguiseIdentifier.replace(":", ".");
            var hasPerm = player.hasPermission(CommonPermissions.MORPH)
                    && (!player.isPermissionSet(childNode) || player.hasPermission(childNode));

            if (!hasPerm)
            {
                source.sendMessage(MessageUtils.prefixes(source, CommandStrings.noPermissionMessage()));

                return null;
            }
        }

        // 如果ID不包含命名空间，则为其加上 "minecraft:" 前缀
        if (!disguiseIdentifier.contains(":"))
        {
            disguiseIdentifier = DisguiseTypes.VANILLA.toId(disguiseIdentifier);
            parameters.setDisguiseIdentifier(disguiseIdentifier);
        }

        // 检查是否禁用
        if (disguiseDisabled(disguiseIdentifier))
        {
            source.sendMessage(MessageUtils.prefixes(source, MorphStrings.disguiseBannedOrNotSupportedString()));
            return null;
        }

        // 调用早期事件
        var earlyEventPassed = new PlayerMorphEarlyEvent(player, null, disguiseIdentifier, parameters.forceExecute).callEvent();
        if (!parameters.forceExecute && !earlyEventPassed)
        {
            source.sendMessage(MessageUtils.prefixes(source, MorphStrings.operationCancelledString()));
            return null;
        }

        // 检查是否拥有此伪装
        DisguiseMeta info = null;

        if (!parameters.bypassAvailableCheck)
        {
            String finalKey = disguiseIdentifier;
            info = getAvaliableDisguisesFor(player).stream()
                    .filter(i -> i.getIdentifier().equals(finalKey)).findFirst().orElse(null);
        }
        else if (!disguiseIdentifier.equals("minecraft:player")) // 禁止不带参数的玩家伪装
        {
            info = new DisguiseMeta(disguiseIdentifier, DisguiseTypes.fromId(disguiseIdentifier));
        }

        if (info == null)
        {
            source.sendMessage(MessageUtils.prefixes(source, MorphStrings.morphNotOwnedString()));
            return null;
        }

        return info;
    }

    /**
     *
     * @param success 是否成功
     * @param state 生产出来的DisguiseState
     * @param provider 对应的DisguiseProvider
     * @param meta 对应的DisguiseMeta
     */
    private record DisguiseBuildResult(boolean success,
                                       DisguiseState state,
                                       DisguiseProvider provider,
                                       DisguiseMeta meta,
                                       @Nullable Entity targetedEntity)
    {
        public static final DisguiseBuildResult FAILED = new DisguiseBuildResult(false, null, null, null, null);

        public static DisguiseBuildResult of(DisguiseState state, DisguiseProvider provider, DisguiseMeta meta, @Nullable Entity targetedEntity)
        {
            Objects.requireNonNull(state, "Null state!");
            Objects.requireNonNull(provider, "Null provider!");
            Objects.requireNonNull(meta, "Null Meta!");

            return new DisguiseBuildResult(true, state, provider, meta, targetedEntity);
        }
    }

    /**
     * 构建一个最小的
     * @param parameters A {@link MorphParameters}
     * @param disguiseMeta A {@link DisguiseMeta}
     * @return {@link DisguiseBuildResult} ，如果不能进行下一步则返回null
     */
    @NotNull
    private DisguiseBuildResult buildDisguise(MorphParameters parameters, DisguiseMeta disguiseMeta)
    {
        // 确保source不为null
        var source = parameters.commandSource == null ? nilCommandSource : parameters.commandSource;
        var player = parameters.targetPlayer;
        var disguiseIdentifier = parameters.targetDisguiseIdentifier();
        var targetEntity = parameters.targetedEntity;

        DisguiseState outComingState = null;

        // 执行伪装操作
        try
        {
            // 查找provider
            var strippedKey = disguiseIdentifier.split(":", 2);

            var provider = getProvider(strippedKey[0]);

            if (provider == MorphManager.fallbackProvider) // 如果没找到provider
            {
                source.sendMessage(MessageUtils.prefixes(source, MorphStrings.disguiseBannedOrNotSupportedString()));
                logger.error("Unable to find any provider that matches the identifier '%s'".formatted(strippedKey[0]));
                return DisguiseBuildResult.FAILED;
            }
            else if (!provider.isValid(disguiseIdentifier)) // 如果provider不认识这个ID
            {
                source.sendMessage(MessageUtils.prefixes(source, MorphStrings.invalidIdentityString()));
                return DisguiseBuildResult.FAILED;
            }
            else
            {
                // 从Provider获取此伪装的Wrapper
                var result = provider.makeWrapper(player, disguiseMeta, targetEntity);

                // 如果provider未能构建DisguiseWrapper
                if (!result.success())
                {
                    if (!result.failSilent())
                    {
                        source.sendMessage(MessageUtils.prefixes(source, MorphStrings.errorWhileDisguising()));
                        logger.error("Unable to get disguise for player with provider {}", provider);
                    }

                    return DisguiseBuildResult.FAILED;
                }

                // 玩家是否已有活跃的DisguiseState?
                var currentState = getDisguiseStateFor(player);

                // 重置上个State的伪装
                if (currentState != null)
                {
                    var stateProvider = currentState.getProvider();

                    // 检查上个State的后端是否允许我们在不取消伪装的情况下重置State
                    if (stateProvider.allowSwitchingWithoutUndisguise(provider, disguiseMeta))
                        currentState.reset(false);
                    else
                        currentState.reset();

                    disguiseStates.remove(currentState);
                }

                // 获取此伪装将用来显示的目标装备
                EntityEquipment equipment = null;

                var theirState = getDisguiseStateFor(targetEntity);
                if (targetEntity != null && provider.canConstruct(getDisguiseMeta(disguiseIdentifier), targetEntity, theirState))
                {
                    if (theirState != null)
                    {
                        equipment = theirState.showingDisguisedItems()
                                ? theirState.getDisguisedItems()
                                : ((LivingEntity) targetEntity).getEquipment();
                    }
                    else
                    {
                        equipment = ((LivingEntity) targetEntity).getEquipment();
                    }
                }

                // 技能
                var rawIdentifierHasSkill = skillHandler.hasSkill(disguiseIdentifier) || skillHandler.hasSpeficSkill(disguiseIdentifier, SkillType.NONE);
                var targetSkillID = rawIdentifierHasSkill ? disguiseIdentifier : provider.getNameSpace() + ":" + MorphManager.disguiseFallbackName;

                var playerMorphConfig = getPlayerMeta(player);
                outComingState = new DisguiseState(player, disguiseIdentifier, targetSkillID,
                        result.wrapperInstance(), provider, equipment,
                        clientHandler.getPlayerOption(player, true), playerMorphConfig);
                this.disguiseStates.add(outComingState);

                // 向Wrapper写入伪装ID
                result.wrapperInstance().write(WrapperAttribute.disguiseIdentifier, disguiseIdentifier);

                // 在初始化服务端伪装状态后，交由后端来为玩家套上伪装
                var backendSuccess = result.wrapperInstance().getBackend().disguise(player, result.wrapperInstance());
                if (!backendSuccess)
                {
                    logger.warn("Backend '%s' failed to disguise the player...".formatted(result.wrapperInstance().getBackend().getIdentifier()));
                    source.sendMessage(MessageUtils.prefixes(source, MorphStrings.errorWhileDisguising()));
                    return DisguiseBuildResult.FAILED;
                }
            }

            return DisguiseBuildResult.of(outComingState, provider, disguiseMeta, targetEntity);
        }
        catch (IllegalArgumentException iae)
        {
            source.sendMessage(MessageUtils.prefixes(source, MorphStrings.parseErrorString()
                    .resolve("id", disguiseIdentifier)));

            logger.error("Unable to parse key " + disguiseIdentifier + ": " + iae.getMessage());
            iae.printStackTrace();

            unMorph(player);

            return DisguiseBuildResult.FAILED;
        }
        catch (Throwable t)
        {
            source.sendMessage(MessageUtils.prefixes(source, MorphStrings.errorWhileDisguising()));

            logger.error("Error while disguising: %s".formatted(t.getMessage()));
            t.printStackTrace();

            unMorph(player);
            return DisguiseBuildResult.FAILED;
        }
    }

    @Resolved
    private VanillaMessageStore vanillaMessageStore;

    private void postBuildDisguise(DisguiseBuildResult result,
                                   MorphParameters parameters,
                                   PlayerMeta playerOptions)
    {
        if (!result.success())
            throw new IllegalArgumentException("Passing a failed result to postDisguise() !");

        // 确保source不为null
        var player = parameters.targetPlayer;
        var disguiseIdentifier = parameters.targetDisguiseIdentifier();
        var targetEntity = parameters.targetedEntity;

        // 向客户端更新当前伪装ID
        // 因为下面postConstruct有初始化技能的操作，根据协议标准中current会重置客户端伪装状态的规定，因此在这里更新
        clientHandler.updateCurrentIdentifier(player, disguiseIdentifier);

        var provider = result.provider();
        var state = result.state();

        // 初始化nbt
        var wrapperCompound = provider.getInitialNbtCompound(state, targetEntity, false);

        if (wrapperCompound != null)
            state.getDisguiseWrapper().mergeCompound(wrapperCompound);

        // 如果此伪装可以同步给客户端，那么初始化客户端状态
        if (provider.validForClient(state))
        {
            clientHandler.sendCommand(player, new S2CSetSNbtCommand(state.getCulledNbtString()));

            clientHandler.sendCommand(player, new S2CSetSelfViewIdentifierCommand(provider.getSelfViewIdentifier(state)));
            provider.getInitialSyncCommands(state).forEach(s -> clientHandler.sendCommand(player, s));

            // 设置Profile
            if (state.haveProfile())
                clientHandler.sendCommand(player, new S2CSetProfileCommand(state.getProfileNbtString()));
        }

        // 向管理员发送map消息
        networkingHelper.sendCommandToRevealablePlayers(genPartialMapCommand(state));

        // CustomName
        if (targetEntity != null && targetEntity.customName() != null)
        {
            var name = targetEntity.customName();

            state.entityCustomName = name;
            state.setDisplayName(name);
        }
        else
        {
            var disguiseID = parameters.targetDisguiseIdentifier();
            var playerDisplay = provider.getDisplayName(disguiseID, MessageUtils.getLocale(player));
            var serverDisplay = provider.getDisplayName(disguiseID, config.get(String.class, ConfigOption.LANGUAGE_CODE));

            state.setPlayerDisplay(playerDisplay);
            state.setServerDisplay(serverDisplay);
        }

        // 调用Provider和Wrapper的postConstructDisguise()
        if (provider != fallbackProvider)
            provider.postConstructDisguise(state, targetEntity);
        else
            logger.warn("A disguise with id '%s' don't have a matching provider?".formatted(disguiseIdentifier));

        var wrapper = state.getDisguiseWrapper();

        wrapper.onPostConstructDisguise(state, targetEntity);

        // 如果玩家客户端不可用并且在骑乘实体，发送伪装在起身后可用的消息
        //if (player.getVehicle() != null && !clientHandler.clientConnected(player))
        //    player.sendMessage(MessageUtils.prefixes(player, MorphStrings.morphVisibleAfterStandup()));

        // 确保玩家可以根据设置看到自己的伪装
        state.setServerSideSelfVisible(playerOptions.showDisguiseToSelf && !this.clientViewAvailable(player));

        // 更新上次操作时间
        updateLastPlayerMorphOperationTime(player);

        SkillCooldownInfo cdInfo;

        //获取与技能对应的CDInfo
        cdInfo = skillHandler.getCooldownInfo(player.getUniqueId(), state.skillLookupIdentifier());
        state.setCooldownInfo(cdInfo);

        state.setSkillCooldown(Math.max(40, cdInfo.getCooldown()));
        cdInfo.setLastInvoke(plugin.getCurrentTick());

        // 切换CD
        skillHandler.switchCooldown(player.getUniqueId(), cdInfo);

        // 调用事件
        new PlayerMorphEvent(player, state).callEvent();
    }

    private void afterDisguise(DisguiseBuildResult result,
                               MorphParameters parameters,
                               PlayerMeta playerOptions)
    {
        // 确保source不为null
        var source = parameters.commandSource == null ? nilCommandSource : parameters.commandSource;
        var player = parameters.targetPlayer;
        var disguiseMeta = result.meta();

        // 消息源是否为玩家自己
        var isDirect = source == player;

        // 返回消息
        var playerLocale = MessageUtils.getLocale(source);

        var morphSuccessMessage = (isDirect ? MorphStrings.morphSuccessString() : CommandStrings.morphedSomeoneString())
                .withLocale(playerLocale)
                .resolve("who", player.getName())
                .resolve("what", disguiseMeta.asComponent(playerLocale));

        source.sendMessage(MessageUtils.prefixes(source, morphSuccessMessage));

        // 显示粒子
        double cX, cY, cZ;

        var wrapper = result.state.getDisguiseWrapper();
        var box = wrapper.getDimensions();
        cX = cZ = box.width();
        cY = box.height();

        spawnParticle(player, player.getLocation(), cX, cY, cZ);

        player.getWorld().playSound(
                player.getLocation(),
                Sound.UI_LOOM_TAKE_RESULT,
                SoundCategory.PLAYERS,
                1, 1
        );

        // 发送提示
        var isClientPlayer = clientHandler.clientConnected(player);
        if (isClientPlayer)
        {
            if (!playerOptions.shownClientSkillHint)
            {
                player.sendMessage(MessageUtils.prefixes(player, HintStrings.clientSkillString()));
                playerOptions.shownClientSkillHint = true;
            }
        }
        else
        {
            if (!playerOptions.shownServerSkillHint && actionItem != null)
            {
                var locale = MessageUtils.getLocale(player);
                var skillHintMessage = HintStrings.skillString()
                        .withLocale(locale)
                        .resolve("item", vanillaMessageStore.get(actionItem.translationKey(), "???", locale));

                player.sendMessage(MessageUtils.prefixes(player, skillHintMessage));
                playerOptions.shownServerSkillHint = true;
            }

            if (!playerOptions.shownClientSuggestionMessage)
            {
                player.sendMessage(MessageUtils.prefixes(player, HintStrings.clientSuggestionStringA()));
                player.sendMessage(MessageUtils.prefixes(player, HintStrings.clientSuggestionStringB()));

                playerOptions.shownClientSuggestionMessage = true;
            }

            if (clientHandler.clientInitialized(player) && !playerOptions.shownDisplayToSelfHint)
            {
                player.sendMessage(MessageUtils.prefixes(player, HintStrings.morphVisibleAfterCommandString()));
                playerOptions.shownDisplayToSelfHint = true;
            }
        }

    }

    //region Command generating

    /**
     * 生成用于橙字显示的map指令
     */
    public S2CMapCommand genMapCommand()
    {
        var map = new HashMap<Integer, String>();
        for (DisguiseState disguiseState : this.disguiseStates)
        {
            var player = disguiseState.getPlayer();
            map.put(player.getEntityId(), player.getName());
        }

        return new S2CMapCommand(map);
    }

    public S2CRenderMapSyncCommand genRenderSyncCommand()
    {
        var map = new HashMap<Integer, String>();
        for (DisguiseState disguiseState : this.disguiseStates)
        {
            var player = disguiseState.getPlayer();
            map.put(player.getEntityId(), disguiseState.getDisguiseIdentifier());
        }

        return S2CRenderMapSyncCommand.of(map);
    }

    /**
     * 生成用于橙字显示的部分map(mapp)指令
     * @param diff 用于生成的伪装状态
     */
    public S2CPartialMapCommand genPartialMapCommand(DisguiseState... diff)
    {
        return networkingHelper.genPartialMapCommand(diff);
    }

    public S2CRenderMapAddCommand genClientRenderAddCommand(DisguiseState diff)
    {
        return networkingHelper.genClientRenderAddCommand(diff);
    }

    //endregion Command generating

    /**
     * 检查某个伪装是否已被禁用
     * @param key 伪装ID
     * @return 此伪装是否已被禁用
     */
    public boolean disguiseDisabled(String key)
    {
        if (bannedDisguises.contains(key)) return true;

        var splitKey = key.split(":", 2);

        if (splitKey.length == 0) return false;

        return bannedDisguises.contains(splitKey[0] + ":" + disguiseFallbackName);
    }

    /**
     * 向客户端发送一组用于同步伪装状态的指令
     * @param state {@link DisguiseState}
     */
    public void refreshClientState(DisguiseState state)
    {
        var player = state.getPlayer();

        clientHandler.updateCurrentIdentifier(player, state.getDisguiseIdentifier());
        clientHandler.sendCommand(player, new S2CSetSNbtCommand(state.getCulledNbtString()));
        clientHandler.sendCommand(player, new S2CSetSelfViewIdentifierCommand(state.getProvider().getSelfViewIdentifier(state)));

        //刷新主动
        var skill = state.getSkill();
        state.setSkillCooldown(state.getSkillCooldown());
        skill.onClientinit(state);

        //刷新被动
        state.getAbilityUpdater().getRegisteredAbilities().forEach(a -> a.onClientInit(state));

        //和客户端同步数据
        state.getProvider().getInitialSyncCommands(state).forEach(c -> clientHandler.sendCommand(player, c));

        //Profile
        if (state.haveProfile())
            clientHandler.sendCommand(player, new S2CSetProfileCommand(state.getProfileNbtString()));

        clientHandler.sendCommand(player, new S2CSetDisplayingFakeEquipCommand(state.showingDisguisedItems()));
    }

    /**
     * 取消所有玩家的伪装
     */
    public void unMorphAll(boolean ignoreOffline)
    {
        var players = new ObjectArrayList<>(disguiseStates);
        players.forEach(i ->
        {
            if (ignoreOffline && !i.getPlayer().isOnline()) return;

            unMorph(i.getPlayer(), i.getPlayer(), true, true);
        });
    }

    /**
     * 取消某一玩家的伪装
     *
     * @param player 目标玩家
     */
    public void unMorph(Player player)
    {
        this.unMorph(player, player, false, false);
    }

    /**
     * 取消某一玩家的伪装
     *
     * @param player 目标玩家
     * @param bypassPermission 是否绕过权限检查（强制取消伪装）
     */
    public void unMorph(Player player, boolean bypassPermission)
    {
        this.unMorph(player, player, bypassPermission, false);
    }

    public static final NilCommandSource nilCommandSource = new NilCommandSource();

    @Resolved
    private RevealingHandler revealingHandler;

    /**
     * 取消某一玩家的伪装
     *
     * @param player 目标玩家
     * @param bypassPermission 是否绕过权限检查
     * @param source 消息要发送的目标来源
     * @param forceUnmorph 是否强制执行操作
     *
     * @apiNote 如果 forceUnmorph 不为 true，则此操作可以被其他来源取消
     */
    public void unMorph(@Nullable CommandSender source, Player player, boolean bypassPermission, boolean forceUnmorph)
    {
        // 确保source不为null
        source = source == null ? nilCommandSource : source;

        // 检查玩家是否可以通过指令或客户端取消伪装
        if (!bypassPermission && !player.hasPermission(CommonPermissions.UNMORPH))
        {
            source.sendMessage(MessageUtils.prefixes(player, CommandStrings.noPermissionMessage()));

            return;
        }

        // 获取当前伪装状态
        var state = disguiseStates.stream()
                .filter(i -> i.getPlayer().getUniqueId().equals(player.getUniqueId())).findFirst().orElse(null);

        // 如果当前没有状态，则不做任何事
        if (state == null)
            return;

        // 调用早期事件
        var earlyEventPassed = new PlayerUnMorphEarlyEvent(player, state, forceUnmorph).callEvent();
        if (!earlyEventPassed && !forceUnmorph)
        {
            source.sendMessage(MessageUtils.prefixes(source, MorphStrings.operationCancelledString()));

            return;
        }

        // 后端的取消操作在Provider里，因此调用Provider的unMorph()
        // state.getProvider().unMorph(player, state);

        // 重置此State
        state.reset();

        // 如果玩家在线，则生成粒子
        if (player.isConnected())
            spawnParticle(player, player.getLocation(), player.getWidth(), player.getHeight(), player.getWidth());

        // 从disguiseStates里移除此状态
        disguiseStates.remove(state);

        // 更新最后操作时间
        updateLastPlayerMorphOperationTime(player);

        // 移除CD
        skillHandler.switchCooldown(player.getUniqueId(), null);

        // 移除Bossbar
        state.setBossbar(null);

        // 从材质map中移除此玩家
        uuidPlayerTexturesMap.remove(player.getUniqueId());

        // 向客户端同步伪装属性
        clientHandler.updateCurrentIdentifier(player, null);
        clientHandler.sendCommand(player, new S2CSetSelfViewIdentifierCommand(null));

        var revLevel = revealingHandler.getRevealingState(player).getBaseValue();
        clientHandler.sendCommand(player, new S2CSetRevealingCommand(revLevel));

        //发送消息以及重置actionbar
        source.sendMessage(MessageUtils.prefixes(player, MorphStrings.unMorphSuccessString().withLocale(MessageUtils.getLocale(player))));
        player.sendActionBar(Component.empty());

        // 调用事件
        new PlayerUnMorphEvent(player).callEvent();

        // 向管理员发送map移除指令
        networkingHelper.sendCommandToRevealablePlayers(new S2CMapRemoveCommand(player.getEntityId()));

        state.dispose();
    }

    public void spawnParticle(Player player, Location location, double collX, double collY, double collZ)
    {
        if (player.getGameMode() == GameMode.SPECTATOR) return;

        location.setY(location.getY() + (collY / 2));

        //根据碰撞箱计算粒子数量缩放
        //缩放为碰撞箱体积的1/15，最小为1
        var particleScale = Math.max(1, (collX * collY * collZ) / 15);

        //显示粒子
        player.getWorld().spawnParticle(Particle.CLOUD, location, //类型和位置
                (int) (25 * particleScale), //数量
                collX * 0.6, collY / 4, collZ * 0.6, //分布空间
                particleScale >= 10 ? 0.2 : 0.05); //速度
    }

    /**
     * 客户端预览是否可用？
     *
     * @param player 目标玩家
     * @return 正在伪装时返回客户端预览是否可用并已启用，没在伪装时返回玩家的客户端设置
     */
    public boolean clientViewAvailable(Player player)
    {
        var state = this.getDisguiseStateFor(player);
        var playerOption = clientHandler.getPlayerOption(player, true);

        if (state == null)
            return playerOption.isClientSideSelfView();

        //logger.warn(player.getName() + " SV "
        //            + " Option? " + clientHandler.getPlayerOption(player).isClientSideSelfView()
        //            + " StateValid? " + state.getProvider().validForClient(state));

        return playerOption.isClientSideSelfView() && state.getProvider().validForClient(state);
    }

    public void setSelfDisguiseVisible(Player player, boolean val, boolean saveToConfig)
    {
        this.setSelfDisguiseVisible(player, val, saveToConfig, clientHandler.getPlayerOption(player, true).isClientSideSelfView(), false);
    }

    public void setSelfDisguiseVisible(Player player, boolean value, boolean saveToConfig, boolean dontSetServerSide, boolean noClientCommand)
    {
        var state = getDisguiseStateFor(player);
        var config = data.getPlayerMeta(player);

        if (state != null)
        {
            //如果客户端预览启用，则不要调整服务端预览
            if (!dontSetServerSide && !clientViewAvailable(player))
                state.setServerSideSelfVisible(value);
        }

        if (!noClientCommand)
            clientHandler.sendCommand(player, new S2CSetSelfViewingCommand(value));

        if (saveToConfig)
        {
            player.sendMessage(MessageUtils.prefixes(player, value
                    ? MorphStrings.selfVisibleOnString()
                    : MorphStrings.selfVisibleOffString()));

            config.showDisguiseToSelf = value;
        }
    }

    /**
     * 获取某一玩家的伪装状态
     *
     * @param player 目标玩家
     * @return 伪装状态，如果为null则表示玩家没有通过插件伪装
     */
    @Nullable
    public DisguiseState getDisguiseStateFor(@Nullable Player player)
    {
        if (player == null) return null;

        return this.disguiseStates.stream()
                .filter(i -> !i.disposed() && i.getPlayer().getUniqueId().equals(player.getUniqueId()))
                .findFirst().orElse(null);
    }

    @Nullable
    public DisguiseState getDisguiseStateFor(Entity entity)
    {
        if (!(entity instanceof Player player)) return null;

        return getDisguiseStateFor(player);
    }

    public void onPluginDisable()
    {
        getDisguiseStates().forEach(s ->
        {
            var player = s.getPlayer();

            player.sendMessage(MessageUtils.prefixes(player, MorphStrings.resetString()));
            if (!player.isOnline())
                offlineStorage.pushDisguiseState(s);
        });

        unMorphAll(false);
        saveConfiguration();

        offlineStorage.saveConfiguration();

        providers.clear();
    }

    public OfflineDisguiseState getOfflineState(Player player)
    {
        return offlineStorage.popDisguiseState(player.getUniqueId());
    }

    public List<OfflineDisguiseState> getAvaliableOfflineStates()
    {
        return offlineStorage.getAvaliableDisguiseStates();
    }

    public void disguiseFromState(DisguiseState state)
    {
        if (!disguiseStates.contains(state))
            disguiseStates.add(state);

        state.getDisguiseWrapper().getBackend().disguise(state.getPlayer(), state.getDisguiseWrapper());

        var meta = getDisguiseMeta(state.getDisguiseIdentifier());
        var result = DisguiseBuildResult.of(state, state.getProvider(), meta, null);
        this.postBuildDisguise(result, MorphParameters.create(state.getPlayer(), state.getDisguiseIdentifier()), getPlayerMeta(state.getPlayer()));
    }

    /**
     * 尝试从离线存储恢复伪装
     * @param player
     * @param offlineState
     * @return Disguise result
     */
    public OfflineDisguiseResult disguiseFromOfflineState(Player player, OfflineDisguiseState offlineState)
    {
        try
        {
            if (player.getUniqueId() == offlineState.playerUUID)
            {
                logger.error("OfflineState UUID mismatch: %s <-> %s".formatted(player.getUniqueId(), offlineState.playerUUID));
                return OfflineDisguiseResult.FAIL;
            }

            var key = offlineState.disguiseID;

            if (disguiseDisabled(key) || !getPlayerMeta(player).getUnlockedDisguiseIdentifiers().contains(key))
                return OfflineDisguiseResult.FAIL;

            if (DisguiseTypes.fromId(key) == DisguiseTypes.UNKNOWN) return OfflineDisguiseResult.FAIL;

            var provider = getProvider(DisguiseTypes.fromId(key).getNameSpace());

            var state = DisguiseStateGenerator.fromOfflineState(offlineState,
                    clientHandler.getPlayerOption(player, true), getPlayerMeta(player), skillHandler, provider.getPreferredBackend());

            if (state != null)
            {
                this.disguiseFromState(state);

                // 向管理员发送map消息
                networkingHelper.sendCommandToRevealablePlayers(genPartialMapCommand(state));

                new PlayerDisguisedFromOfflineStateEvent(player, state).callEvent();

                return OfflineDisguiseResult.SUCCESS;
            }

            //有限还原
            if (morph(player, player, key, null))
            {
                var newState = getDisguiseStateFor(player);

                if (newState != null)
                    new PlayerDisguisedFromOfflineStateEvent(player, newState).callEvent();

                return OfflineDisguiseResult.LIMITED;
            }
            else
            {
                return OfflineDisguiseResult.FAIL;
            }
        }
        catch (Throwable t)
        {
            logger.error("Unable to recover disguise from OfflineState: " + t.getMessage());
            t.printStackTrace();
        }

        return OfflineDisguiseResult.FAIL;
    }

    //endregion 玩家伪装相关

    //region Implementation of IManagePlayerData

    @Override
    @Nullable
    public DisguiseMeta getDisguiseMeta(String rawString)
    {
        return data.getDisguiseMeta(rawString);
    }

    @Override
    public ObjectArrayList<DisguiseMeta> getAvaliableDisguisesFor(Player player)
    {
        return data.getAvaliableDisguisesFor(player);
    }

    @Override
    public boolean grantMorphToPlayer(Player player, String disguiseIdentifier)
    {
        var success = data.grantMorphToPlayer(player, disguiseIdentifier);

        if (success)
        {
            clientHandler.sendDiff(List.of(disguiseIdentifier), null, player);
            multiInstanceService.notifyDisguiseMetaChange(player.getUniqueId(), Operation.ADD_IF_ABSENT, disguiseIdentifier);

            var config = data.getPlayerMeta(player);

            if (clientHandler.clientConnected(player))
            {
                if (!config.shownMorphClientHint)
                {
                    player.sendMessage(MessageUtils.prefixes(player, HintStrings.firstGrantClientHintString()));
                    config.shownMorphClientHint = true;
                }
            }
            else if (!config.shownMorphHint)
            {
                player.sendMessage(MessageUtils.prefixes(player, HintStrings.firstGrantHintString()));
                config.shownMorphHint = true;
            }
        }

        return success;
    }

    @Override
    public boolean revokeMorphFromPlayer(Player player, String disguiseIdentifier)
    {
        var success = data.revokeMorphFromPlayer(player, disguiseIdentifier);

        if (success)
        {
            clientHandler.sendDiff(null, List.of(disguiseIdentifier), player);
            multiInstanceService.notifyDisguiseMetaChange(player.getUniqueId(), Operation.REMOVE, disguiseIdentifier);
        }

        return success;
    }

    @Override
    public PlayerMeta getPlayerMeta(OfflinePlayer player)
    {
        return data.getPlayerMeta(player);
    }

    @Override
    public boolean reloadConfiguration()
    {
        //重载完数据后要发到离线存储的人
        var stateToOfflineStore = new ObjectArrayList<DisguiseState>();

        disguiseStates.forEach(s ->
        {
            if (!s.getPlayer().isOnline())
                stateToOfflineStore.add(s);
        });

        disguiseStates.removeAll(stateToOfflineStore);
        var stateToRecover = getDisguiseStates();

        unMorphAll(false);

        var success = data.reloadConfiguration() && offlineStorage.reloadConfiguration();

        stateToOfflineStore.forEach(offlineStorage::pushDisguiseState);

        //重载完成后恢复玩家伪装
        stateToRecover.forEach(s ->
        {
            var player = s.getPlayer();

            this.scheduleOn(player, () ->
            {
                var config = this.getPlayerMeta(player);
                if (!disguiseDisabled(s.getDisguiseIdentifier()) && config.getUnlockedDisguiseIdentifiers().contains(s.getDisguiseIdentifier()))
                {
                    var newState = s.createCopy(s.getPlayer());
                    s.dispose();

                    disguiseFromState(newState);
                    refreshClientState(newState);

                    player.sendMessage(MessageUtils.prefixes(player, MorphStrings.recoverString()));
                }
                else
                    unMorph(nilCommandSource, player, true, true);
            });
        });

        Bukkit.getOnlinePlayers().forEach(p -> clientHandler.refreshPlayerClientMorphs(this.getPlayerMeta(p).getUnlockedDisguiseIdentifiers(), p));

        return success;
    }

    @Override
    public boolean saveConfiguration()
    {
        return data.saveConfiguration() && offlineStorage.saveConfiguration();
    }
    //endregion Implementation of IManagePlayerData

    @ApiStatus.Internal
    public List<PlayerMeta> listAllPlayerMeta()
    {
        return data.getAll();
    }
}