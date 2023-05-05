package xiamomc.morph;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
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
import xiamomc.morph.abilities.AbilityHandler;
import xiamomc.morph.events.api.lifecycle.ManagerFinishedInitializeEvent;
import xiamomc.morph.backends.DisguiseBackend;
import xiamomc.morph.backends.DisguiseWrapper;
import xiamomc.morph.backends.fallback.NilBackend;
import xiamomc.morph.backends.libsdisg.LibsBackend;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.events.api.gameplay.PlayerMorphEvent;
import xiamomc.morph.events.api.gameplay.PlayerUnMorphEvent;
import xiamomc.morph.interfaces.IManagePlayerData;
import xiamomc.morph.messages.CommandStrings;
import xiamomc.morph.messages.HintStrings;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.MorphStrings;
import xiamomc.morph.messages.vanilla.VanillaMessageStore;
import xiamomc.morph.misc.DisguiseInfo;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.misc.DisguiseStateGenerator;
import xiamomc.morph.misc.DisguiseTypes;
import xiamomc.morph.misc.permissions.CommonPermissions;
import xiamomc.morph.network.commands.S2C.set.*;
import xiamomc.morph.network.server.MorphClientHandler;
import xiamomc.morph.providers.*;
import xiamomc.morph.skills.MorphSkillHandler;
import xiamomc.morph.skills.SkillCooldownInfo;
import xiamomc.morph.skills.SkillType;
import xiamomc.morph.storage.offlinestore.OfflineDisguiseState;
import xiamomc.morph.storage.offlinestore.OfflineStateStore;
import xiamomc.morph.storage.playerdata.PlayerDataStore;
import xiamomc.morph.storage.playerdata.PlayerMorphConfiguration;
import xiamomc.morph.utilities.DisguiseUtils;
import xiamomc.morph.utilities.NbtUtils;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Bindables.Bindable;
import xiamomc.pluginbase.Bindables.BindableList;

import java.util.List;
import java.util.Map;
import java.util.UUID;
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
    private AbilityHandler abilityHandler;

    @Resolved
    private MorphConfigManager config;

    public static final DisguiseProvider fallbackProvider = new FallbackProvider();

    public static final String disguiseFallbackName = "@default";

    private DisguiseBackend<?, ?> currentBackend = new NilBackend();

    public DisguiseBackend<?, ?> getCurrentBackend()
    {
        return currentBackend;
    }

    private Material actionItem;
    public Material getActionItem()
    {
        return actionItem;
    }

    @Initializer
    private void load()
    {
        this.addSchedule(this::update);

        try
        {
            this.currentBackend = new LibsBackend();
        }
        catch (NoClassDefFoundError e)
        {
            logger.error("Unable to initialize LibsDisguises as our disguise backend because it's not installed on the server.");
            logger.error("Initializing NilBackend, displaying disguises at the server side will not be supported this run.");
        }
        catch (Throwable t)
        {
            logger.error("Unable to initialize LibsDisguise as our disguise backend, please check your server setup!");
            logger.error("Initializing NilBackend, displaying disguises at the server side will not be supported this run.");
            logger.error("If you believe this is an error, please consider reporting this issue to our GitHub: https://github.com/XiaMoZhiShi/MorphPlugin/issues");

            t.printStackTrace();
        }

        logger.info("Using backend: %s".formatted(currentBackend));

        bannedDisguises = config.getBindableList(ConfigOption.BANNED_DISGUISES);
        config.bind(allowHeadMorph, ConfigOption.ALLOW_HEAD_MORPH);

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

        states.forEach(i ->
        {
            var p = i.getPlayer();

            //跳过离线玩家
            if (!p.isOnline()) return;

            if (!abilityHandler.handle(p, i))
            {
                p.sendMessage(MessageUtils.prefixes(p, MorphStrings.errorWhileUpdatingDisguise()));

                unMorph(p, true);
            }

            if (!i.getProvider().updateDisguise(p, i))
            {
                p.sendMessage(MessageUtils.prefixes(p, MorphStrings.errorWhileUpdatingDisguise()));

                unMorph(p, true);
            }
        });
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
        logger.info("Registering disguise provider: " + provider.getNameSpace());

        if (provider.getNameSpace().contains(":"))
        {
            logger.error("Illegal character found in namespace: ':'");
            return false;
        }

        if (providers.stream().anyMatch(p -> p.getNameSpace().equals(provider.getNameSpace())))
        {
            logger.error("Another provider instance already registered as " + provider.getNameSpace() + " !");
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

    private final Bindable<Boolean> allowHeadMorph = new Bindable<>();

    private final Map<UUID, PlayerTextures> uuidPlayerTexturesMap = new ConcurrentHashMap<>();

    public boolean doQuickDisguise(Player player, @Nullable Material actionItem)
    {
        var state = this.getDisguiseStateFor(player);
        var mainHandItem = player.getEquipment().getItemInMainHand();
        var mainHandItemType = mainHandItem.getType();

        //右键玩家头颅：快速伪装
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
                case DRAGON_HEAD ->
                {
                    morphOrUnMorph(player, EntityType.ENDER_DRAGON.getKey().asString(), targetEntity);
                }
                case ZOMBIE_HEAD ->
                {
                    morphOrUnMorph(player, EntityType.ZOMBIE.getKey().asString(), targetEntity);
                }
                case SKELETON_SKULL ->
                {
                    morphOrUnMorph(player, EntityType.SKELETON.getKey().asString(), targetEntity);
                }
                case WITHER_SKELETON_SKULL ->
                {
                    morphOrUnMorph(player, EntityType.WITHER_SKELETON.getKey().asString(), targetEntity);
                }
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
                    morph(player, DisguiseTypes.PLAYER.toId(profile.getName()), targetEntity);

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
                var disg = currentBackend.getWrapper(targetedEntity);

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
                    //否则：伪装ID > 伪装类型 > 生物类型
                    targetKey = disg != null
                            ? disg.isPlayerDisguise()
                                ? DisguiseTypes.PLAYER.toId(disg.getDisguiseName())
                                : disg.getEntityType().getKey().asString()
                            : targetedEntity.getType().getKey().asString();
                }

                morph(player, targetKey, targetedEntity);

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
            morph(player, key, targetEntity);
    }

    /**
     * 伪装某一玩家
     *
     * @param player 目标玩家
     * @param key 伪装ID
     * @param targetEntity 目标实体（如果有）
     * @return 操作是否成功
     */
    public boolean morph(Player player, String key, @Nullable Entity targetEntity)
    {
        return this.morph(player, key, targetEntity, false, false);
    }

    /**
     * 伪装某一玩家
     *
     * @param player 要伪装的玩家
     * @param key 伪装ID
     * @param targetEntity 玩家正在看的实体
     * @param bypassPermission 是否绕过权限检查
     * @param bypassAvailableCheck 是否绕过持有检查
     * @return 操作是否成功
     */
    public boolean morph(Player player, String key, @Nullable Entity targetEntity,
                         boolean bypassPermission, boolean bypassAvailableCheck)
    {
        if (!bypassPermission)
        {
            var childNode = CommonPermissions.MORPH + ".as." + key.replace(":", ".");
            var hasPerm = player.hasPermission(CommonPermissions.MORPH)
                    && (!player.isPermissionSet(childNode) || player.hasPermission(childNode));

            if (!hasPerm)
            {
                player.sendMessage(MessageUtils.prefixes(player, CommandStrings.noPermissionMessage()));

                return false;
            }
        }

        if (!key.contains(":")) key = DisguiseTypes.VANILLA.toId(key);

        String finalKey = key;
        DisguiseInfo info = null;

        if (!bypassAvailableCheck)
        {
            info = getAvaliableDisguisesFor(player).stream()
                    .filter(i -> i.getIdentifier().equals(finalKey)).findFirst().orElse(null);
        }
        else if (!key.equals("minecraft:player"))
        {
            info = new DisguiseInfo(key, DisguiseTypes.fromId(key));
        }

        if (disguiseDisabled(key))
        {
            player.sendMessage(MessageUtils.prefixes(player, MorphStrings.disguiseBannedOrNotSupportedString()));
            return false;
        }

        var state = getDisguiseStateFor(player);

        //检查有没有伪装
        if (info != null)
        {
            try
            {
                //查找provider
                var strippedKey = key.split(":", 2);

                var provider = getProvider(strippedKey[0]);

                DisguiseState outComingState = null;

                if (provider == MorphManager.fallbackProvider)
                {
                    player.sendMessage(MessageUtils.prefixes(player, MorphStrings.disguiseBannedOrNotSupportedString()));
                    logger.error("Unable to find any provider that matches the identifier '%s'".formatted(strippedKey[0]));
                    return false;
                }
                else if (!provider.isValid(finalKey))
                {
                    player.sendMessage(MessageUtils.prefixes(player, MorphStrings.invalidIdentityString()));
                    return false;
                }
                else
                {
                    //重置上个State的伪装
                    if (state != null)
                    {
                        state.getProvider().unMorph(player, state);
                        state.setAbilities(List.of());

                        state.setSkill(null);
                    }

                    var result = provider.makeWrapper(player, info, targetEntity);

                    if (!result.success())
                    {
                        player.sendMessage(MessageUtils.prefixes(player, MorphStrings.errorWhileDisguising()));
                        logger.error("Unable to apply disguise for player with provider " + provider);
                        return false;
                    }

                    var backendSuccess = currentBackend.disguise(player, result.wrapperInstance());
                    if (!backendSuccess)
                    {
                        player.sendMessage(MessageUtils.prefixes(player, MorphStrings.errorWhileDisguising()));
                        return false;
                    }

                    clientHandler.updateCurrentIdentifier(player, key);

                    outComingState = postConstructDisguise(player, targetEntity,
                            info.getIdentifier(), result.wrapperInstance(), result.isCopy(), provider);
                }

                var playerLocale = MessageUtils.getLocale(player);

                var msg = MorphStrings.morphSuccessString()
                        .withLocale(playerLocale)
                        .resolve("what", info.asComponent(playerLocale));

                player.sendMessage(MessageUtils.prefixes(player, msg));

                //如果此伪装可以同步给客户端，那么初始化客户端状态
                if (provider.validForClient(state))
                {
                    clientHandler.sendCommand(player, new S2CSetSelfViewIdentifierCommand(provider.getSelfViewIdentifier(outComingState)));

                    provider.getInitialSyncCommands(outComingState).forEach(s -> clientHandler.sendCommand(player, s));

                    //初始化nbt
                    var compound = provider.getNbtCompound(outComingState, targetEntity);

                    if (compound != null)
                    {
                        outComingState.setCachedNbtString(NbtUtils.getCompoundString(compound));
                        clientHandler.sendCommand(player, new S2CSetSNbtCommand(outComingState.getCachedNbtString()));

                        outComingState.getDisguiseWrapper().mergeCompound(compound);
                    }

                    //设置Profile
                    if (outComingState.haveProfile())
                        clientHandler.sendCommand(player, new S2CSetProfileCommand(outComingState.getProfileNbtString()));
                }

                return true;
            }
            catch (IllegalArgumentException iae)
            {
                player.sendMessage(MessageUtils.prefixes(player, MorphStrings.parseErrorString()
                        .resolve("id", key)));

                logger.error("Unable to parse key " + key + ": " + iae.getMessage());
                iae.printStackTrace();

                return false;
            }
        }
        else
        {
            player.sendMessage(MessageUtils.prefixes(player, MorphStrings.morphNotOwnedString()));
        }

        return false;
    }

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

        return bannedDisguises.contains(splitKey[0] + ":any");
    }

    /**
     * 向客户端发送一组用于同步伪装状态的指令
     * @param state {@link DisguiseState}
     */
    public void refreshClientState(DisguiseState state)
    {
        var player = state.getPlayer();

        clientHandler.updateCurrentIdentifier(player, state.getDisguiseIdentifier());
        clientHandler.sendCommand(player, new S2CSetSNbtCommand(state.getCachedNbtString()));
        clientHandler.sendCommand(player, new S2CSetSelfViewIdentifierCommand(state.getProvider().getSelfViewIdentifier(state)));

        //刷新主动
        var skill = state.getSkill();
        state.setSkillCooldown(state.getSkillCooldown());
        skill.onClientinit(state);

        //刷新被动
        var abilities = state.getAbilities();

        if (abilities != null)
            abilities.forEach(a -> a.onClientInit(state));

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

            unMorph(i.getPlayer(), true);
        });
    }

    public void unMorph(Player player)
    {
        this.unMorph(player, false);
    }

    /**
     * 取消某一玩家的伪装
     *
     * @param player 目标玩家
     */
    public void unMorph(Player player, boolean bypassPermission)
    {
        if (!bypassPermission && !player.hasPermission(CommonPermissions.UNMORPH))
        {
            player.sendMessage(MessageUtils.prefixes(player, CommandStrings.noPermissionMessage()));

            return;
        }

        var state = disguiseStates.stream()
                .filter(i -> i.getPlayerUniqueID().equals(player.getUniqueId())).findFirst().orElse(null);

        if (state == null)
            return;

        state.getProvider().unMorph(player, state);

        //移除所有技能
        state.setAbilities(List.of());
        state.setSkill(null);

        if (player.isOnline())
            spawnParticle(player, player.getLocation(), player.getWidth(), player.getHeight(), player.getWidth());

        disguiseStates.remove(state);

        updateLastPlayerMorphOperationTime(player);

        //移除CD
        skillHandler.switchCooldown(player.getUniqueId(), null);

        //移除Bossbar
        state.setBossbar(null);

        player.sendMessage(MessageUtils.prefixes(player, MorphStrings.unMorphSuccessString().withLocale(MessageUtils.getLocale(player))));
        player.sendActionBar(Component.empty());

        uuidPlayerTexturesMap.remove(player.getUniqueId());

        clientHandler.updateCurrentIdentifier(player, null);
        clientHandler.sendCommand(player, new S2CSetSelfViewIdentifierCommand(null));

        Bukkit.getPluginManager().callEvent(new PlayerUnMorphEvent(player));
    }

    private void postConstructDisguise(DisguiseState state)
    {
        postConstructDisguise(state.getPlayer(), null,
                state.getDisguiseIdentifier(), state.getDisguiseWrapper(), state.shouldHandlePose(), state.getProvider());
    }

    @Resolved
    private VanillaMessageStore messageStore;

    /**
     * 构建好伪装之后要做的事
     *
     * @param player     伪装的玩家
     * @param targetedEntity     伪装的目标实体
     * @param disguiseWrapper         伪装
     * @param shouldHandlePose 要不要手动更新伪装Pose？（伪装是否为克隆）
     * @param provider {@link DisguiseProvider}
     */
    private DisguiseState postConstructDisguise(Player player, @Nullable Entity targetedEntity,
                                                String disguiseIdentifier, DisguiseWrapper<?> disguiseWrapper, boolean shouldHandlePose,
                                                @NotNull DisguiseProvider provider)
    {
        var playerMorphConfig = getPlayerConfiguration(player);

        //更新或者添加DisguiseState
        var state = getDisguiseStateFor(player);

        //获取此伪装将用来显示的目标装备
        EntityEquipment equipment = null;

        var theirState = getDisguiseStateFor(targetedEntity);
        if (targetedEntity != null && provider.canConstruct(getDisguiseInfo(disguiseIdentifier), targetedEntity, theirState))
        {
            if (theirState != null)
            {
                equipment = theirState.showingDisguisedItems()
                        ? theirState.getDisguisedItems()
                        : ((LivingEntity) targetedEntity).getEquipment();
            }
            else
            {
                equipment = ((LivingEntity) targetedEntity).getEquipment();
            }
        }

        //技能
        var rawIdentifierHasSkill = skillHandler.hasSkill(disguiseIdentifier) || skillHandler.hasSpeficSkill(disguiseIdentifier, SkillType.NONE);
        var targetSkillID = rawIdentifierHasSkill ? disguiseIdentifier : provider.getNameSpace() + ":" + MorphManager.disguiseFallbackName;

        if (state == null)
        {
            state = new DisguiseState(player, disguiseIdentifier, targetSkillID,
                    disguiseWrapper, shouldHandlePose, provider, equipment,
                    clientHandler.getPlayerOption(player, true), playerMorphConfig);

            disguiseStates.add(state);
        }
        else
        {
            state.setDisguise(disguiseIdentifier, targetSkillID, disguiseWrapper, shouldHandlePose, equipment);
        }

        //workaround: Disguise#getDisguiseName()不会正常返回实体的自定义名称
        if (targetedEntity != null && targetedEntity.customName() != null)
        {
            var name = targetedEntity.customName();

            state.entityCustomName = name;
            state.setDisplayName(name);
        }

        if (provider != fallbackProvider)
            provider.postConstructDisguise(state, targetedEntity);
        else
            logger.warn("A disguise with id '%s' don't have a matching provider?".formatted(disguiseIdentifier));

        disguiseWrapper.onPostConstructDisguise(state, targetedEntity);

        //如果伪装的时候坐着，显示提示
        if (player.getVehicle() != null && !clientHandler.clientConnected(player))
            player.sendMessage(MessageUtils.prefixes(player, MorphStrings.morphVisibleAfterStandup()));

        //显示粒子
        double cX, cY, cZ;

        var box = disguiseWrapper.getDimensions();
        cX = cZ = box.width;
        cY = box.height;

        spawnParticle(player, player.getLocation(), cX, cY, cZ);

        //确保玩家可以根据设置看到自己的伪装
        state.setServerSideSelfVisible(playerMorphConfig.showDisguiseToSelf && !this.clientViewAvailable(player));

        var isClientPlayer = clientHandler.clientConnected(player);

        if (isClientPlayer)
        {
            if (!playerMorphConfig.shownClientSkillHint)
            {
                player.sendMessage(MessageUtils.prefixes(player, HintStrings.clientSkillString()));
                playerMorphConfig.shownClientSkillHint = true;
            }
        }
        else
        {
            if (!playerMorphConfig.shownServerSkillHint && actionItem != null)
            {
                var locale = MessageUtils.getLocale(player);
                var msg = HintStrings.skillString()
                        .withLocale(locale)
                        .resolve("item", messageStore.get(actionItem.translationKey(), "???", locale));

                player.sendMessage(MessageUtils.prefixes(player, msg));
                playerMorphConfig.shownServerSkillHint = true;
            }

            if (!playerMorphConfig.shownClientSuggestionMessage)
            {
                player.sendMessage(MessageUtils.prefixes(player, HintStrings.clientSuggestionStringA()));
                player.sendMessage(MessageUtils.prefixes(player, HintStrings.clientSuggestionStringB()));

                playerMorphConfig.shownClientSuggestionMessage = true;
            }

            if (!playerMorphConfig.shownDisplayToSelfHint)
            {
                player.sendMessage(MessageUtils.prefixes(player, HintStrings.morphVisibleAfterCommandString()));
                playerMorphConfig.shownDisplayToSelfHint = true;
            }
        }

        //更新上次操作时间
        updateLastPlayerMorphOperationTime(player);

        SkillCooldownInfo cdInfo;

        //获取与技能对应的CDInfo
        cdInfo = skillHandler.getCooldownInfo(player.getUniqueId(), disguiseIdentifier);
        state.setCooldownInfo(cdInfo);

        if (cdInfo != null)
        {
            cdInfo.setCooldown(Math.max(40, cdInfo.getCooldown()));
            cdInfo.setLastInvoke(plugin.getCurrentTick());
        }

        //切换CD
        skillHandler.switchCooldown(player.getUniqueId(), cdInfo);

        //调用事件
        Bukkit.getPluginManager().callEvent(new PlayerMorphEvent(player, state));

        return state;
    }

    public void spawnParticle(Player player, Location location, double collX, double collY, double collZ)
    {
        if (player.getGameMode() == GameMode.SPECTATOR) return;

        location.setY(location.getY() + (collY / 2));

        //根据碰撞箱计算粒子数量缩放
        //缩放为碰撞箱体积的1/15，最小为1
        var particleScale = Math.max(1, (collX * collY * collZ) / 15);

        //显示粒子
        player.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, location, //类型和位置
                (int) (25 * particleScale), //数量
                collX * 0.6, collY / 4, collZ * 0.6, //分布空间
                particleScale >= 10 ? 0.2 : 0.05); //速度
    }

    /**
     * 客户端预览是否可用？
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
        var config = data.getPlayerConfiguration(player);

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
    public DisguiseState getDisguiseStateFor(Player player)
    {
        return this.disguiseStates.stream()
                .filter(i -> i.getPlayerUniqueID().equals(player.getUniqueId()))
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

        currentBackend.disguise(state.getPlayer(), state.getDisguiseWrapper());
        postConstructDisguise(state);
    }

    /**
     *
     * @param player
     * @param offlineState
     * @return -1: Fail<br/>
     *         0: Success<br/>
     *         1: Success(Limited copy)<br/>
     */
    public int disguiseFromOfflineState(Player player, OfflineDisguiseState offlineState)
    {
        try
        {
            if (player.getUniqueId() == offlineState.playerUUID)
            {
                logger.error("OfflineState UUID mismatch: %s <-> %s".formatted(player.getUniqueId(), offlineState.playerUUID));
                return -1;
            }

            var key = offlineState.disguiseID;

            if (disguiseDisabled(key) || !getPlayerConfiguration(player).getUnlockedDisguiseIdentifiers().contains(key))
                return -1;

            var disguiseType = DisguiseTypes.fromId(key);

            if (disguiseType == DisguiseTypes.UNKNOWN) return -1;

            //直接还原非LD的伪装
            if (disguiseType != DisguiseTypes.LD)
            {
                var state = DisguiseStateGenerator.fromOfflineState(offlineState,
                        clientHandler.getPlayerOption(player, true), getPlayerConfiguration(player), skillHandler, currentBackend);

                if (state != null)
                {
                    this.disguiseFromState(state);

                    return 0;
                }
            }

            //有限还原
            morph(player, key, null);
            return 1;
        }
        catch (Throwable t)
        {
            logger.error("Unable to recover disguise from OfflineState: " + t.getMessage());
            t.printStackTrace();
        }

        return -1;
    }

    //endregion 玩家伪装相关

    @Resolved
    private MorphClientHandler clientHandler;

    //region Implementation of IManagePlayerData

    @Override
    @Nullable
    public DisguiseInfo getDisguiseInfo(String rawString)
    {
        return data.getDisguiseInfo(rawString);
    }

    @Override
    public ObjectArrayList<DisguiseInfo> getAvaliableDisguisesFor(Player player)
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

            var config = data.getPlayerConfiguration(player);

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
            clientHandler.sendDiff(null, List.of(disguiseIdentifier), player);

        return success;
    }

    @Override
    public PlayerMorphConfiguration getPlayerConfiguration(Player player)
    {
        return data.getPlayerConfiguration(player);
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
            var config = this.getPlayerConfiguration(player);

            if (!disguiseDisabled(s.getDisguiseIdentifier()) && config.getUnlockedDisguiseIdentifiers().contains(s.getDisguiseIdentifier()))
            {
                var newState = s.createCopy();

                disguiseFromState(newState);
                refreshClientState(newState);

                player.sendMessage(MessageUtils.prefixes(player, MorphStrings.recoverString()));
            }
            else
                unMorph(player, true);
        });

        Bukkit.getOnlinePlayers().forEach(p -> clientHandler.refreshPlayerClientMorphs(this.getPlayerConfiguration(p).getUnlockedDisguiseIdentifiers(), p));

        return success;
    }

    @Override
    public boolean saveConfiguration()
    {
        return data.saveConfiguration() && offlineStorage.saveConfiguration();
    }
    //endregion Implementation of IManagePlayerData
}