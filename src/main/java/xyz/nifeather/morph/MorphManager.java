package xyz.nifeather.morph;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Bindables.Bindable;
import xiamomc.pluginbase.Bindables.BindableList;
import xiamomc.pluginbase.Messages.FormattableMessage;
import xyz.nifeather.morph.api.events.gameplay.*;
import xyz.nifeather.morph.api.events.lifecycle.LateDisguiseBuildEvent;
import xyz.nifeather.morph.api.events.lifecycle.LateDisguisePropertiesSetupEvent;
import xyz.nifeather.morph.api.events.lifecycle.ManagerFinishedInitializeEvent;
import xyz.nifeather.morph.api.events.misc.DataStoreSwitchEvent;
import xyz.nifeather.morph.api.morphs.skills.SkillNames;
import xyz.nifeather.morph.backends.DisguiseBackend;
import xyz.nifeather.morph.backends.DisguiseWrapper;
import xyz.nifeather.morph.backends.WrapperProperties;
import xyz.nifeather.morph.backends.client.ModBackend;
import xyz.nifeather.morph.backends.server.ServerBackend;
import xyz.nifeather.morph.config.ConfigOptions;
import xyz.nifeather.morph.config.MorphConfigManager;
import xyz.nifeather.morph.interfaces.IManagePlayerData;
import xyz.nifeather.morph.messages.strings.CommandStrings;
import xyz.nifeather.morph.messages.strings.ExceptionStrings;
import xyz.nifeather.morph.messages.strings.HintStrings;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.messages.strings.MorphStrings;
import xyz.nifeather.morph.misc.*;
import xyz.nifeather.morph.misc.disguiseProperty.*;
import xyz.nifeather.morph.misc.disguiseProperty.values.OffTreeProperties;
import xyz.nifeather.morph.misc.disguiseProperty.values.PlayerPropertyCollection;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;
import xyz.nifeather.morph.network.Constants;
import xyz.nifeather.morph.network.commands.S2C.S2CUpdatePropertiesCommand;
import xyz.nifeather.morph.network.commands.S2C.admin.reveal.S2CRemoveAdminRevealCommand;
import xyz.nifeather.morph.network.commands.S2C.admin.reveal.S2CSyncAdminRevealCommand;
import xyz.nifeather.morph.network.commands.S2C.set.*;
import xyz.nifeather.morph.network.multiInstance.MultiInstanceService;
import xyz.nifeather.morph.network.multiInstance.protocol.Operation;
import xyz.nifeather.morph.network.server.MorphClientHandler;
import xyz.nifeather.morph.providers.disguise.DisguiseProvider;
import xyz.nifeather.morph.providers.disguise.FallbackDisguiseProvider;
import xyz.nifeather.morph.providers.disguise.PlayerDisguiseProvider;
import xyz.nifeather.morph.providers.disguise.VanillaDisguiseProvider;
import xyz.nifeather.morph.skills.SkillManager;
import xyz.nifeather.morph.storage.offlinestore.OfflineDisguise;
import xyz.nifeather.morph.storage.offlinestore.OfflineStateStore;
import xyz.nifeather.morph.storage.playerdata.PlayerDataStoreNew;
import xyz.nifeather.morph.storage.playerdata.PlayerMeta;
import xyz.nifeather.morph.utilities.DisguiseUtils;
import xyz.nifeather.morph.utilities.ExceptionUtils;
import xyz.nifeather.morph.utilities.NbtUtils;
import xyz.nifeather.morph.utilities.PermissionUtils;

import java.io.InvalidObjectException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

public class MorphManager extends MorphPluginObject implements IManagePlayerData
{
    private final Map<UUID, DisguiseState> activeDisguises = new ConcurrentHashMap<>();

    private final IManagePlayerData defaultData = new PlayerDataStoreNew();

    @NotNull
    private volatile IManagePlayerData data = new PlayerDataStoreNew();

    private final OfflineStateStore offlineStorage = new OfflineStateStore();

    public IManagePlayerData getDataStore()
    {
        return data;
    }

    public void setDataStore(@Nullable IManagePlayerData newDataStore)
    {
        this.data = newDataStore == null ? defaultData : newDataStore;
        logger.info("Updating Player Data Store to %s".formatted(newDataStore));

        reload();

        new DataStoreSwitchEvent(this, this.data).callEvent();
    }

    @Resolved
    private SkillManager skillManager;

    @Resolved
    private MorphConfigManager config;

    @Resolved
    private ModNetworkingHelper modNetworkingHelper;

    @Resolved
    private MultiInstanceService multiInstanceService;

    @Resolved
    private DisguiseProperties disguiseProperties;

    public static final DisguiseProvider fallbackProvider = new FallbackDisguiseProvider();

    public static final String disguiseFallbackName = "@default";

    public static final String forcedDisguiseNoneId = "@none";

    public MorphManager()
    {
    }

    //region Backends

    private final ModBackend modBackend = new ModBackend();

    @NotNull
    private DisguiseBackend<?, ?> defaultBackend = modBackend;

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

    /**
     * NOT FINISHED, DO NOT USE
     * <br>
     * AND MAY NEVER FINISH...
     */
    @ApiStatus.Internal
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

            unMorphAll(false);
        }
        catch (Throwable t)
        {
            logger.error("Error occurred switching backend", t);

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
            logger.error("Unable to initialize ServerBackend as our disguise backend, maybe PacketEvents is not installed on the server.");
            logger.error("Using NilBackend, displaying disguises at the server side will not be supported this run.");
        }
        catch (Throwable t)
        {
            logger.error("Unable to initialize ServerBackend as our disguise backend", t);
            logger.error("Using NilBackend, displaying disguises at the server side will not be supported this run.");
            logger.error("Please consider reporting this issue to our GitHub: https://github.com/MATRIX-feather/FeatherMorph/issues");
        }
    }

    //endregion Backends

    @Deprecated(forRemoval = true)
    public Material getActionItem()
    {
        return Material.AIR;
    }

    @Resolved
    private MorphClientHandler clientHandler;

    @Initializer
    private void load()
    {
        registerBackend(modBackend);
        tryBackends();

        logger.info("Default backend: %s".formatted(defaultBackend));

        bannedDisguises = config.getBindableList(String.class, ConfigOptions.BANNED_DISGUISES);
        disabledWorlds = config.getBindableList(String.class, ConfigOptions.DISGUISE_DISABLED_WORLDS);

        config.bind(allowHeadMorph, ConfigOptions.ALLOW_HEAD_MORPH);
        config.bind(allowAcquireMorph, ConfigOptions.ALLOW_ACQUIRE_MORPHS);
        config.bind(useClientRenderer, ConfigOptions.USE_CLIENT_RENDERER);
        config.bind(uuidRandomBaseString, ConfigOptions.UUID_RANDOM_BASE);

        registerProviders(ObjectList.of(
                new VanillaDisguiseProvider(),
                new PlayerDisguiseProvider(),
                fallbackProvider
        ));

        Bukkit.getPluginManager().callEvent(new ManagerFinishedInitializeEvent(this));
    }

    public boolean disguiseDisabledInWorld(World world)
    {
        return disguiseDisabledInWorld(world.getName());
    }

    public boolean disguiseDisabledInWorld(String worldName)
    {
        return disabledWorlds.contains(worldName);
    }

    //region 玩家伪装相关

    /**
     * 使某个玩家执行伪装的主动技能
     * @param player 目标玩家
     */
    public void executeDisguiseSkill(Player player)
    {
        var state = getDisguiseStateFor(player);
        if (state == null) return;

        state.executeSkillCheckPermission();
    }

    /**
     * 获取所有已伪装的玩家
     * @return 玩家列表
     * @apiNote 列表中的玩家可能已经离线
     */
    public List<DisguiseState> getActiveDisguises()
    {
        return ImmutableList.copyOf(activeDisguises.values());
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
    private BindableList<String> disabledWorlds;

    /**
     * To check whether a disguise is not available for players: {@link MorphManager#disguiseDisabled(String)}
     */
    @ApiStatus.Internal
    public BindableList<String> getBannedDisguises()
    {
        return bannedDisguises;
    }

    //region 伪装提供器

    private static final List<DisguiseProvider> providers = new CopyOnWriteArrayList<>();

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
        if (id == null)
            return fallbackProvider;

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

    private final Bindable<String> uuidRandomBaseString = new Bindable<>("???");

    /**
     * 尝试调用快速伪装
     *
     * @param player 发起玩家
     * @return 操作是否成功
     */
    public boolean tryQuickDisguise(Player player)
    {
        var mainHandItem = player.getEquipment().getItemInMainHand();
        var mainHandItemType = mainHandItem.getType();

        // 检查是否可以通过头颅伪装
        if (DisguiseUtils.validForHeadMorph(mainHandItemType))
        {
            if (!allowHeadMorph.get())
            {
                //player.sendMessage(MessageUtils.send(player, MorphStrings.headDisguiseDisabledString()));

                return true;
            }

            if (!player.hasPermission(CommonPermissions.HEAD_MORPH))
            {
                MessageUtils.send(player, CommandStrings.noPermissionMessage());
                return true;
            }

            if (!canMorph(player))
            {
                MessageUtils.send(player, MorphStrings.disguiseCoolingDownString());
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
                        MessageUtils.send(player, MorphStrings.invalidSkinString());
                        return true;
                    }

                    morph(player, player, DisguiseTypes.PLAYER.toId(profile.getName()), targetEntity);
                }
            }

            updateLastPlayerMorphOperationTime(player);
        }
        else
        {
            var targetedEntity = player.getTargetEntity(5);

            if (targetedEntity instanceof LivingEntity)
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
        var source = parameters.commandSource == null ? parameters.targetPlayer : parameters.commandSource;

        try
        {
            var meta = prepareDisguiseMeta(parameters);
            if (meta == null)
                return false;

            // 更新上次操作时间
            updateLastPlayerMorphOperationTime(parameters.targetPlayer);

            var validateResult = validateDisguise(meta);
            switch (validateResult)
            {
                case VALIDATE_NO_ISSUE -> {}

                case VALIDATE_NO_PROVIDER ->
                {
                    logger.error("Unable to find any provider that matches the identifier '%s'".formatted(parameters.targetDisguiseIdentifier()));
                    MessageUtils.send(source, MorphStrings.disguiseBannedOrNotSupportedString());
                    return false;
                }

                case VALIDATE_PROVIDER_FAIL ->
                {
                    MessageUtils.send(source, MorphStrings.invalidIdentityString());
                    return false;
                }

                // In case I forgot something...
                default -> throw new InvalidObjectException("Invalid validate result: " + validateResult);
            }

            var buildResult = prepareDisguiseState(parameters, meta);
            if (!buildResult.success())
                return false;

            var playerMeta = getPlayerMeta(parameters.targetPlayer);
            this.buildDisguise(buildResult, parameters);

            if (!applyDisguise(parameters, buildResult.state(), playerMeta))
                return false;

            this.afterDisguise(buildResult.state(), parameters, playerMeta);

            return true;
        }
        catch (Exception e)
        {
            if (FeatherMorphMain.getInstance().debugOutputEnabled())
                logger.warn("Unable to disguise player because a(n) %s has occurred".formatted(e.getClass().getSimpleName()), e);

            if (!(e instanceof IUserFault))
                logger.error("Unable to disguise player", e);

            FormattableMessage message = switch (e)
            {
                case PropertyValidationException propertyValidationException ->
                    MorphStrings.errorValidatingProperty().resolve("what", propertyValidationException.propertyName);

                case ParseErrorException parseErrorException ->
                    MorphStrings.errorParsingProperty().resolve("what", parseErrorException.propertyName);

                default -> MorphStrings.errorWhileDisguisingWithError();
            };

            message.resolve("error", ExceptionUtils.getExceptionMessageShort(e));

            MessageUtils.send(source, message, c -> c.hoverEvent(HoverEvent.showText(ExceptionUtils.getExceptionDetail(e))));

            return false;
        }
    }

    /**
     * 准备一些伪装需要的东西，包括权限检查
     * @param parameters MorphParameters
     * @return 一个DisguiseMeta，如果构建失败则返回Null
     */
    @Nullable
    private DisguiseMeta prepareDisguiseMeta(MorphParameters parameters)
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
                    && PermissionUtils.hasPermission(player, childNode, true);

            if (!hasPerm)
            {
                MessageUtils.send(source, CommandStrings.noPermissionMessage());

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
            MessageUtils.send(source, MorphStrings.disguiseBannedOrNotSupportedString());
            return null;
        }

        if (disguiseDisabledInWorld(player.getWorld()))
        {
            MessageUtils.send(source, MorphStrings.disguiseDisabledInWorldString());
            return null;
        }

        // 调用早期事件
        var earlyEventPassed = new PlayerMorphEarlyEvent(player,
                disguiseIdentifier,
                parameters.forceExecute, parameters.propertiesInput).callEvent();

        if (!parameters.forceExecute && !earlyEventPassed)
        {
            MessageUtils.send(source, MorphStrings.operationCancelledString());
            return null;
        }

        // 检查是否拥有此伪装
        DisguiseMeta info = null;

        if (!parameters.bypassAvailableCheck)
        {
            String finalKey = disguiseIdentifier;
            info = getAvailableDisguisesFor(player).stream()
                    .filter(i -> i.getIdentifier().equals(finalKey)).findFirst().orElse(null);
        }
        else if (!disguiseIdentifier.equals("minecraft:player")) // 禁止不带参数的玩家伪装
        {
            info = new DisguiseMeta(disguiseIdentifier, DisguiseTypes.fromId(disguiseIdentifier));
        }

        if (info == null)
        {
            MessageUtils.send(source, MorphStrings.morphNotOwnedString());
            return null;
        }

        return info;
    }

    public static final int VALIDATE_NO_ISSUE = 0;
    public static final int VALIDATE_NO_PROVIDER = 1;
    public static final int VALIDATE_PROVIDER_FAIL = 2;

    public int validateDisguise(DisguiseMeta meta)
    {
        var disguiseIdentifier = meta.getIdentifier();

        // 查找provider
        var strippedKey = disguiseIdentifier.split(":", 2);

        var provider = getProvider(strippedKey[0]);

        if (provider.equals(MorphManager.fallbackProvider)) // 如果没找到provider
            return VALIDATE_NO_PROVIDER;
        else if (!provider.isValid(disguiseIdentifier)) // 如果provider不认识这个ID
            return VALIDATE_PROVIDER_FAIL;

        return VALIDATE_NO_ISSUE;
    }

    //region Build and apply disguise

    /**
     * 构建一个最小的
     * @param parameters A {@link MorphParameters}
     * @param disguiseMeta A {@link DisguiseMeta}
     * @return {@link DisguiseBuildResult} ，如果不能进行下一步则返回null
     */
    @NotNull
    private DisguiseBuildResult prepareDisguiseState(MorphParameters parameters, DisguiseMeta disguiseMeta)
        throws ParseErrorException
    {
        // 确保source不为null
        var source = parameters.commandSource == null ? nilCommandSource : parameters.commandSource;
        var player = parameters.targetPlayer;
        var disguiseIdentifier = parameters.targetDisguiseIdentifier();
        var targetEntity = parameters.targetedEntity;

        DisguiseState outComingState = null;

        try
        {
            var provider = getProvider(disguiseIdentifier);

            if (!provider.validateDisguise(player, disguiseMeta, targetEntity))
                return DisguiseBuildResult.FAILED;

            // 从Provider获取此伪装的Wrapper
            var wrapper = provider.makeWrapper(player, disguiseMeta, targetEntity).orElse(null);

            // 如果provider未能构建DisguiseWrapper
            if (wrapper == null)
            {
                logger.error("Unable to create disguise wrapper for player with provider {}", provider);
                return DisguiseBuildResult.FAILED;
            }

            // 向Wrapper写入伪装ID
            wrapper.writeProperty(WrapperProperties.DISGUISE_ID, disguiseIdentifier);

            // 技能
            var rawIdentifierHasSkill = skillManager.hasSkill(disguiseIdentifier) || skillManager.hasSpeficSkill(disguiseIdentifier, SkillNames.NONE);
            var targetSkillID = rawIdentifierHasSkill ? disguiseIdentifier : provider.getNameSpace() + ":" + MorphManager.disguiseFallbackName;

            var playerMorphConfig = getPlayerMeta(player);
            outComingState = new DisguiseState(player, disguiseIdentifier, targetSkillID,
                    wrapper, provider,
                    clientHandler.getPlayerOption(player, true), playerMorphConfig);

            return DisguiseBuildResult.of(outComingState, disguiseMeta);
        }
        catch (IllegalArgumentException iae)
        {
            MessageUtils.send(source, MorphStrings.parseErrorString().resolve("id", disguiseIdentifier));

            logger.error("Unable to parse key " + disguiseIdentifier, iae);
            return DisguiseBuildResult.FAILED;
        }
        catch (Exception e)
        {
            MessageUtils.send(source, MorphStrings.errorWhileDisguisingWithError().resolve("error", e.getMessage()));
            logger.error("Error while disguising", e);

            return DisguiseBuildResult.FAILED;
        }
    }

    public static final String SESSIONKEY_TARGET_ENTITY = "MORPHMANAGER_TARGET_ENTITY";

    private void buildDisguise(DisguiseBuildResult result,
                               MorphParameters parameters) throws ParseErrorException, PropertyValidationException, ExecutionErrorException, NullPointerException
    {
        if (!result.success())
            throw new IllegalArgumentException("Passing a failed result to postDisguise() !");

        // 确保source不为null
        var player = parameters.targetPlayer;
        var targetEntity = parameters.targetedEntity;
        var state = result.state();
        var provider = state.getProvider();
        var wrapper = state.getDisguiseWrapper();

        // 设定形态属性

        // 设定初始UUID
        var str = uuidRandomBaseString.get()
                + parameters.targetDisguiseIdentifier()
                + player.getName();

        UUID virtualEntityUUID = UUID.nameUUIDFromBytes(str.getBytes());

        wrapper.writeProperty(OffTreeProperties.VIRTUAL_ENTITY_UUID, virtualEntityUUID);

        //Push DataKeys

        state.setSessionData(SESSIONKEY_TARGET_ENTITY, targetEntity);

        // Properties
        var propertyHandler = state.disguisePropertyHandler();
        var propertyCollection = disguiseProperties.getCollection(state.getEntityType());

        propertyHandler.reset();
        propertyHandler.registerFromPropertyCollection(propertyCollection); // Make sure that disguise properties are always available for further disguise construct
        disguiseProperties.lookupRange(parameters.propertiesInput.keySet())
                .forEach(propertyHandler::addProperty);

        provider.buildDisguise(state, targetEntity);
        provider.setupProperties(state, targetEntity);

        // Check property permission in prepare, before we execute anything
        if (!parameters.bypassPermission && !parameters.propertiesInput.isEmpty() && !player.hasPermission(CommonPermissions.USE_DISGUISE_PROPERTY))
        {
            throw ParseErrorException.forProperty("any")
                    .withLocalizableMessage(CommandStrings.noPermissionMessage())
                    .withMessage("Player don't have permission for disguise property inputs")
                    .create();
        }

        // Then apply properties
        propertyHandler.updateFromPropertiesInput(parameters.propertiesInput, player, parameters.bypassPermission ? EnumSet.of(ValidationFlag.SKIP_PERMISSIONS) : EnumSet.noneOf(ValidationFlag.class));

        propertyHandler.getAll().forEach((property, value) ->
                wrapper.writeProperty((SingleProperty<Object>) property, value));

        // Call the event so that others can manipulate the disguise properties
        var lateSetupEvent = new LateDisguisePropertiesSetupEvent(player, state);
        if (!lateSetupEvent.callEvent())
        {
            var failingException = lateSetupEvent.exception();
            switch (failingException)
            {
                case null -> throw ExecutionErrorException.forMethod("MorphManager#buildDisguise")
                        .withMessage("LateDisguisePropertiesSetupEvent cancelled by a plugin, but we didn't got any exceptions!")
                        .withLocalizableMessage(ExceptionStrings.unknownError())
                        .create();

                case ParseErrorException pee -> throw pee;

                case PropertyValidationException pve -> throw pve;

                default -> throw ExecutionErrorException.forMethod("MorphManager#buildDisguise")
                        .withMessage("We have exception reported by other plugins!")
                        .causedBy(failingException)
                        .create();
            }

        }

        provider.finalizeProperties(state);

        Component customName = propertyHandler.getOr(PropertyNames.ENTITY_CUSTOM_NAME, null);

        // 设定显示名称
        // CustomName property is now handled in DisguiseState
        // See DisguiseState#onPropertyWrite
        // ... Should we move default name setup into DisguiseState either?
        if (customName == null)
        {
            var disguiseID = parameters.targetDisguiseIdentifier();
            var playerDisplay = provider.getDisplayName(disguiseID, MessageUtils.getLocale(player));
            var serverDisplay = provider.getDisplayName(disguiseID, config.get(ConfigOptions.LANGUAGE_CODE));

            state.setPlayerDisplay(playerDisplay);
            state.setServerDisplay(serverDisplay);
        }

        wrapper.postBuildDisguise(state, targetEntity);

        long availableAfter = skillManager.getAvailableAfter(player.getUniqueId(), state.getDisguiseIdentifier());
        state.setAvailableAfter(Math.max(plugin.getCurrentTick() + 40, availableAfter), true);

        // Finally, we call the late disguise build event
        new LateDisguiseBuildEvent(player, state).callEvent();

        state.removeSessionData(SESSIONKEY_TARGET_ENTITY);
    }

    @SuppressWarnings("removal")
    private boolean applyDisguise(MorphParameters parameters,
                                  DisguiseState newState,
                                  PlayerMeta playerOptions) throws ExecutionErrorException
    {
        var player = parameters.targetPlayer;
        var uuid = player.getUniqueId();
        var provider = getProvider(parameters.targetDisguiseIdentifier());
        var wrapper = newState.getDisguiseWrapper();

        // 玩家是否已有活跃的DisguiseState?
        var previousState = getDisguiseStateFor(player);

        // 重置上个State的伪装
        if (previousState != null)
        {
            new PlayerSwitchMorphEvent(player, previousState, newState).callEvent();
            previousState.dispose();
            activeDisguises.remove(uuid, previousState);
        }

        wrapper.getBackend().disguise(player, wrapper);

        provider.onDisguiseApply(newState);

        newState.getStateFuture()
                .exceptionally(t ->
                {
                    scheduleOn(player, () ->
                    {
                        MessageUtils.send(player, MorphStrings.errorWhileUpdatingDisguise());
                        unMorph(nilCommandSource, player, true, true);
                    });

                    return null;
                }).thenAccept(s -> activeDisguises.remove(uuid, s));

        newState.scheduleSelfUpdate();

        this.activeDisguises.put(player.getUniqueId(), newState);

        // 确保玩家可以根据设置看到自己的伪装
        newState.setServerSideSelfVisible(playerOptions.showDisguiseToSelf && !this.clientViewAvailable(player));

        // Network below!

        // 向管理员发送map消息
        modNetworkingHelper.sendCommandToRevealablePlayers(modNetworkingHelper.genPartialMapCommand(newState));

        // 向客户端更新当前伪装ID
        // 因为下面postConstruct有初始化技能的操作，根据协议标准中current会重置客户端伪装状态的规定，因此在这里更新
        clientHandler.updateCurrentIdentifier(player, newState.getDisguiseIdentifier());

        // Skill
        newState.getSkill().applyToClient(newState);

        // Cooldown
        newState.applyCooldownToClient();

        // 如果此伪装可以同步给客户端，那么初始化客户端状态
        if (provider.validForClient(newState))
        {
            var clientApiVersion = clientHandler.getPlayerVersion(player);

            // For legacy client compat
            //todo: Remove at 2026, or 1.22 comes out
            if (clientApiVersion < Constants.ApiLevel.NETWORK_DISGUISE_PROPERTIES.protocolVersion)
            {
                clientHandler.sendCommand(player, new S2CSetSNbtCommand(newState.getCulledNbtString()));
            }
            else
            {
                try
                {
                    clientHandler.sendCommand(player, new S2CUpdatePropertiesCommand(newState.disguisePropertyHandler().toNetworkProperties()));
                }
                catch (ParseErrorException | ExecutionErrorException e)
                {
                    newState.handleException(e);
                    return false;
                }
            }

            provider.getInitialSyncCommands(newState).forEach(s -> clientHandler.sendCommand(player, s));

            if (clientApiVersion < Constants.ApiLevel.EQUIPMENT_AND_SKIN_ARE_NOW_PROPERTY.protocolVersion)
            {
                var properties = DisguiseProperties.INSTANCE.getCollectionOrThrow(PlayerPropertyCollection.class);
                newState.disguisePropertyHandler().getOptional(properties.SKIN)
                        .ifPresent(profile ->
                        {
                            // 设置Profile
                            var skinTag = NbtUtils.toCompoundTag(profile);

                            clientHandler.sendCommand(player, new S2CSetProfileCommand(NbtUtils.getCompoundString(skinTag)));
                        });
            }
        }

        // 设置可用动作
        var availableAnimations = provider.getAnimationProvider()
                .getAnimationSetFor(newState.getDisguiseIdentifier())
                .getAvailableAnimationsForClient();

        clientHandler.sendCommand(player, new S2CSetAvailableAnimationsCommand(availableAnimations));

        // 调用事件
        new PlayerMorphEvent(player, newState).callEvent();

        return true;
    }

    private void afterDisguise(DisguiseState state,
                               MorphParameters parameters,
                               PlayerMeta playerOptions)
    {
        // 确保source不为null
        var source = parameters.commandSource == null ? nilCommandSource : parameters.commandSource;
        var player = parameters.targetPlayer;

        // 消息源是否为玩家自己
        var isDirect = source.equals(player);

        var morphSuccessMessage = (isDirect ? MorphStrings.morphSuccessString() : CommandStrings.morphedSomeoneString())
                .resolve("who", player.getName())
                .resolve("what", state.getPlayerDisplay());

        MessageUtils.send(source, morphSuccessMessage);

        // 显示粒子
        double cX, cY, cZ;

        var box = BoundingBoxLookup.instance().getBoundboxOptional(state.getEntityType(), player.getLocation())
                .orElse(BoundingBox.of(player.getLocation().getBlock()));

        cX = cZ = box.getWidthX();
        cY = box.getHeight();

        spawnCloudParticle(player, player.getLocation(), cX, cY, cZ);

        player.getWorld().playSound(
                player.getLocation(),
                Sound.UI_LOOM_TAKE_RESULT,
                SoundCategory.PLAYERS,
                1, 1
        );

        var propertyHandler = state.disguisePropertyHandler();

        propertyHandler.hookOnPropertyWrite((property, value) ->
        {
            if (state.disposed()) return;

            // fix command not sending when player rejoins
            Player pl = player.isConnected() ? player : Bukkit.getPlayer(player.getUniqueId());
            Map<String ,String> diffMap = new ConcurrentHashMap<>();
            try
            {
                diffMap.put(property.id(), property.forValue(value));
            }
            catch (ParseErrorException e)
            {
                logger.error("Can't generate output from value", e);
                return;
            }

            clientHandler.sendCommand(pl, new S2CUpdatePropertiesCommand(diffMap));
        });

        // 发送提示
        var isClientPlayer = clientHandler.clientConnected(player);
        if (isClientPlayer)
        {
            if (!playerOptions.shownClientSkillHint)
            {
                MessageUtils.send(player, HintStrings.clientSkillString());
                playerOptions.shownClientSkillHint = true;
            }
        }
        else
        {
            if (clientHandler.clientInitialized(player) && !playerOptions.shownDisplayToSelfHint)
            {
                MessageUtils.send(player, HintStrings.morphVisibleAfterCommandString());
                playerOptions.shownDisplayToSelfHint = true;
            }
        }
    }

    //endregion Build and apply disguise

    //region Command generating

    /**
     * 生成用于橙字显示的map指令
     */
    public S2CSyncAdminRevealCommand genMapCommand()
    {
        var map = new HashMap<Integer, String>();
        for (DisguiseState disguiseState : activeDisguises.values())
        {
            var player = disguiseState.getPlayer();
            map.put(player.getEntityId(), player.getName());
        }

        return new S2CSyncAdminRevealCommand(map);
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
    @SuppressWarnings("removal")
    public void refreshClientState(DisguiseState state)
    {
        var player = state.getPlayer();

        clientHandler.updateCurrentIdentifier(player, state.getDisguiseIdentifier());

        int playerApiVersion = clientHandler.getPlayerVersion(player);

        // For legacy client compat
        //todo: Remove at 2026, or 1.22 comes out
        if (playerApiVersion < Constants.ApiLevel.NETWORK_DISGUISE_PROPERTIES.protocolVersion)
        {
            clientHandler.sendCommand(player, new S2CSetSNbtCommand(state.getCulledNbtString()));
        }
        else
        {
            try
            {
                clientHandler.sendCommand(player, new S2CUpdatePropertiesCommand(state.disguisePropertyHandler().toNetworkProperties()));
            }
            catch (ParseErrorException | ExecutionErrorException e)
            {
                state.handleException(e);
                return;
            }
        }

        //刷新主动
        state.applyCooldownToClient();
        state.getSkill().applyToClient(state);

        //刷新被动
        state.getAbilityUpdater().getRegisteredAbilities().forEach(a -> a.onClientInit(state));

        var provider = state.getProvider();

        //和客户端同步数据
        provider.getInitialSyncCommands(state).forEach(c -> clientHandler.sendCommand(player, c));

        // 设置可用动作
        var availableAnimations = provider.getAnimationProvider().getAnimationSetFor(state.getDisguiseIdentifier()).getAvailableAnimationsForClient();
        clientHandler.sendCommand(player, new S2CSetAvailableAnimationsCommand(availableAnimations));

        if (playerApiVersion < Constants.ApiLevel.EQUIPMENT_AND_SKIN_ARE_NOW_PROPERTY.protocolVersion)
        {
            var properties = DisguiseProperties.INSTANCE.getCollectionOrThrow(PlayerPropertyCollection.class);
            state.disguisePropertyHandler().getOptional(properties.SKIN)
                    .ifPresent(profile ->
                    {
                        // 设置Profile
                        var skinTag = NbtUtils.toCompoundTag(profile);

                        clientHandler.sendCommand(player, new S2CSetProfileCommand(NbtUtils.getCompoundString(skinTag)));
                    });

            clientHandler.sendCommand(player, new S2CSetDisplayingFakeEquipCommand(state.showingDisguisedItems()));
        }
    }

    /**
     * 取消所有玩家的伪装
     */
    public void unMorphAll(boolean ignoreOffline)
    {
        Map.copyOf(activeDisguises).forEach((uuid, state) ->
        {
            if (ignoreOffline && !state.getPlayer().isOnline()) return;

            unMorph(state.getPlayer(), state.getPlayer(), true, true);
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
        var uuid = player.getUniqueId();

        // 检查玩家是否可以通过指令或客户端取消伪装
        if (!bypassPermission && !player.hasPermission(CommonPermissions.UNMORPH))
        {
            MessageUtils.send(player, CommandStrings.noPermissionMessage());
            return;
        }

        // 获取当前伪装状态
        var state = activeDisguises.getOrDefault(uuid, null);

        // 如果当前没有状态，则不做任何事
        if (state == null)
            return;

        // 调用早期事件
        var earlyEventPassed = new PlayerUnMorphEarlyEvent(player, state, forceUnmorph).callEvent();
        if (!earlyEventPassed && !forceUnmorph)
        {
            MessageUtils.send(source, MorphStrings.operationCancelledString());
            return;
        }

        // 后端的取消操作在Provider里，因此调用Provider的unMorph()
        // state.getProvider().unMorph(player, state);

        // 如果玩家在线，则生成粒子
        if (player.isConnected())
        {
            spawnCloudParticle(player, player.getLocation(), player.getWidth(), player.getHeight(), player.getWidth());

            player.getWorld().playSound(
                    player.getLocation(),
                    Sound.UI_LOOM_TAKE_RESULT,
                    SoundCategory.PLAYERS,
                    1, 1
            );
        }

        // 从disguiseStates里移除此状态
        activeDisguises.remove(uuid, state);

        // 更新最后操作时间
        updateLastPlayerMorphOperationTime(player);

        // 移除Bossbar
        state.setBossbar(null);

        // 向客户端同步伪装属性
        clientHandler.updateCurrentIdentifier(player, null);

        var revLevel = revealingHandler.getRevealingState(player).getBaseValue();
        clientHandler.sendCommand(player, new S2CSetMobRevealCommand(revLevel));

        //发送消息以及重置actionbar
        MessageUtils.send(player, MorphStrings.unMorphSuccessString());
        player.sendActionBar(Component.empty());

        // 设置可用动作
        clientHandler.sendCommand(player, new S2CSetAvailableAnimationsCommand(List.of()));

        // 调用事件
        new PlayerUnMorphEvent(player, state).callEvent();

        // 向管理员发送map移除指令
        modNetworkingHelper.sendCommandToRevealablePlayers(new S2CRemoveAdminRevealCommand(player.getEntityId()));

        state.dispose();
    }

    public void spawnCloudParticle(Player player, Location location, double collX, double collY, double collZ)
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
            clientHandler.sendCommand(player, new S2CSetSelfViewingStatusCommand(value));

        if (saveToConfig)
        {
            MessageUtils.send(player, value
                    ? MorphStrings.selfVisibleOnString()
                    : MorphStrings.selfVisibleOffString());

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

        return this.activeDisguises.getOrDefault(player.getUniqueId(), null);
    }

    @Nullable
    public DisguiseState getDisguiseStateFor(@Nullable Entity entity)
    {
        if (!(entity instanceof Player player)) return null;

        return getDisguiseStateFor(player);
    }

    public void onPluginDisable()
    {
        getActiveDisguises().forEach(s ->
        {
            var player = s.getPlayer();

            MessageUtils.send(player, MorphStrings.resetString());

            if (!player.isOnline())
                offlineStorage.save(s);
        });

        unMorphAll(false);
        save();

        providers.clear();
    }

    public OfflineDisguise getOfflineState(Player player)
    {
        return offlineStorage.read(player.getUniqueId());
    }

    public List<String> availableOfflineDisguises()
    {
        return offlineStorage.listNames();
    }

    public boolean disguiseFromState(DisguiseState state)
    {
        var meta = getDisguiseMeta(state.getDisguiseIdentifier());
        var result = DisguiseBuildResult.of(state, meta);
        var playerMeta = getPlayerMeta(state.getPlayer());
        var parameters = MorphParameters.create(state.getPlayer(), state.getDisguiseIdentifier());

        if (this.prepareDisguiseMeta(parameters) == null)
            return false;

        try
        {
            this.buildDisguise(result, parameters);
            this.applyDisguise(parameters, state, playerMeta);
        }
        catch (Exception e) //todo: 或许之后能调整一下，让 ParseErrorException, ExecutionErrorException 单独提示
        {
            logger.error("Failed calling disguiseFromState", e);
            return false;
        }

        this.afterDisguise(state, parameters, playerMeta);

        return true;
    }

    /**
     * 尝试从离线存储恢复伪装
     * @param player
     * @param offlineState
     * @return Disguise result
     */
    public OfflineDisguiseResult disguiseFromOfflineState(Player player, OfflineDisguise offlineState)
    {
        return morph(
                MorphParameters.create(player, offlineState.disguiseIdentifier)
                        .withProperties(offlineState.properties)
            ) ? OfflineDisguiseResult.SUCCESS : OfflineDisguiseResult.FAIL;
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
    public List<DisguiseMeta> getAvailableDisguisesFor(Player player)
    {
        var avail = data.getAvailableDisguisesFor(player);
        return avail == null ? new ObjectArrayList<>() : avail;
    }

    @Override
    public boolean grantMorphToPlayer(Player player, String disguiseIdentifier)
    {
        return grantMorphToPlayer(player, disguiseIdentifier, false);
    }

    public boolean grantMorphToPlayer(Player player, String disguiseIdentifier, boolean bypassPermission)
    {
        if (!bypassPermission && !player.hasPermission(CommonPermissions.ACQUIRE_MORPH))
            return false;

        var success = data.grantMorphToPlayer(player, disguiseIdentifier);

        if (!success)
            return false;

        clientHandler.sendDiff(List.of(disguiseIdentifier), null, player);
        multiInstanceService.notifyDisguiseMetaChange(player.getUniqueId(), Operation.ADD_IF_ABSENT, disguiseIdentifier);

        var config = data.getPlayerMeta(player);
        var locale = MessageUtils.getLocale(player);

        var meta = data.getDisguiseMeta(disguiseIdentifier);
        if (meta == null)
            return false;

        MessageUtils.send(player, MorphStrings.morphUnlockedString()
                .resolve("what", meta.asComponent(locale)));

        //显示粒子
        player.getWorld().spawnParticle(Particle.TRIAL_SPAWNER_DETECTION_OMINOUS, player.getLocation(), //类型和位置
                100, //数量
                0.8, 0.8, 0.8, //分布空间
                0.05); //速度

        if (clientHandler.clientConnected(player))
        {
            if (!config.shownMorphClientHint)
            {
                MessageUtils.send(player, HintStrings.firstGrantClientHintString());
                config.shownMorphClientHint = true;
            }
        }
        else if (!config.shownMorphHint)
        {
            MessageUtils.send(player, HintStrings.firstGrantHintString());
            config.shownMorphHint = true;
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

            var locale = MessageUtils.getLocale(player);
            var meta = data.getDisguiseMeta(disguiseIdentifier);
            assert meta != null; // 和上面一样

            var message = MorphStrings.morphLockedString()
                    .resolve("what", meta.asComponent(locale))
                    .createComponent(locale);
            player.sendMessage(message);

            var disguiseState = this.getDisguiseStateFor(player);
            if (disguiseState != null && disguiseState.getDisguiseIdentifier().equalsIgnoreCase(disguiseIdentifier))
                this.unMorph(player, true);
        }

        return success;
    }

    @Override
    public @NotNull PlayerMeta getPlayerMeta(OfflinePlayer player)
    {
        return data.getPlayerMeta(player);
    }

    private volatile int reloadToken = 0;

    @Override
    public boolean reload()
    {
        //重载完数据后要发到离线存储的人
        var stateToOfflineStore = new ObjectArrayList<DisguiseState>();

        Map.copyOf(activeDisguises).forEach((uuid, state) ->
        {
            if (!state.getPlayer().isOnline())
            {
                stateToOfflineStore.add(state);
                activeDisguises.remove(uuid, state);
            }
        });

        var stateToRecover = getActiveDisguises();
        stateToRecover = stateToRecover.stream()
                .map(oldState -> oldState.createCopy(oldState.getPlayer()))
                .toList();

        unMorphAll(false);

        var success = data.reload();

        stateToOfflineStore.forEach(offlineStorage::save);

        //重载完成后恢复玩家伪装
        stateToRecover.forEach(s ->
        {
            var player = s.getPlayer();

            this.scheduleOn(player, () ->
            {
                var parameter = MorphParameters.create(player, s.getDisguiseIdentifier());
                if (this.prepareDisguiseMeta(parameter) == null)
                    return;

                if (disguiseFromState(s))
                {
                    refreshClientState(s);
                    MessageUtils.send(player, MorphStrings.recoverString());
                }
                else
                {
                    unMorph(nilCommandSource, player, true, true);
                }
            });
        });

        var currentToken = ThreadLocalRandom.current().nextInt();
        this.reloadToken = currentToken;

        featherMorph().getPlatform().onlinePlayersNative().forEach(p ->
        {
            if (this.reloadToken != currentToken) return;

            this.loadPlayerDataAsync(p.getUniqueId()).thenAccept(meta ->
            {
                clientHandler.refreshPlayerClientMorphs(meta.getUnlockedDisguiseIdentifiers(), p);
            });
        });

        return success;
    }

    @Override
    public boolean save()
    {
        return data.save();
    }

    @Override
    public List<PlayerMeta> getRange(List<UUID> list)
    {
        return data.getRange(list);
    }

    @Override
    public CompletableFuture<PlayerMeta> loadPlayerDataAsync(UUID uuid)
    {
        return data.loadPlayerDataAsync(uuid);
    }

    //endregion Implementation of IManagePlayerData
}