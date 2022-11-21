package xiamomc.morph;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.DisguiseValues;
import me.libraryaddict.disguise.utilities.reflection.FakeBoundingBox;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.abilities.AbilityHandler;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.events.PlayerMorphEvent;
import xiamomc.morph.events.PlayerUnMorphEvent;
import xiamomc.morph.interfaces.IManagePlayerData;
import xiamomc.morph.messages.CommandStrings;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.MorphStrings;
import xiamomc.morph.misc.*;
import xiamomc.morph.misc.permissions.CommonPermissions;
import xiamomc.morph.network.MorphClientHandler;
import xiamomc.morph.providers.DisguiseProvider;
import xiamomc.morph.providers.LocalDisguiseProvider;
import xiamomc.morph.providers.PlayerDisguiseProvider;
import xiamomc.morph.providers.VanillaDisguiseProvider;
import xiamomc.morph.skills.MorphSkillHandler;
import xiamomc.morph.skills.SkillCooldownInfo;
import xiamomc.morph.skills.SkillType;
import xiamomc.morph.storage.offlinestore.OfflineDisguiseState;
import xiamomc.morph.storage.offlinestore.OfflineStorageManager;
import xiamomc.morph.storage.playerdata.PlayerDataStore;
import xiamomc.morph.storage.playerdata.PlayerMorphConfiguration;
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
    /**
     * 已伪装的玩家
     */
    private final List<DisguiseState> disguisedPlayers = new ObjectArrayList<>();

    private final PlayerDataStore data = new PlayerDataStore();

    private final OfflineStorageManager offlineStorage = new OfflineStorageManager();

    @Resolved
    private MorphSkillHandler skillHandler;

    @Resolved
    private AbilityHandler abilityHandler;

    @Resolved
    private MorphConfigManager config;

    @Initializer
    private void load()
    {
        this.addSchedule(c -> update());

        bannedDisguises = config.getBindableList(String.class, ConfigOption.BANNED_DISGUISES);
        config.bind(allowHeadMorph, ConfigOption.ALLOW_HEAD_MORPH);

        registerProviders(ObjectList.of(
                new VanillaDisguiseProvider(),
                new PlayerDisguiseProvider(),
                new LocalDisguiseProvider()
        ));
    }

    private void update()
    {
        var states = this.getDisguisedPlayers();

        states.forEach(i ->
        {
            var p = i.getPlayer();

            //跳过离线玩家
            if (!p.isOnline()) return;

            var disg = DisguiseAPI.getDisguise(p);
            var disgInState = i.getDisguise();

            //检查State中的伪装和实际的是否一致
            if (!disgInState.equals(disg))
            {
                if (DisguiseUtils.isTracing(disgInState))
                {
                    logger.warn(p.getName() + "在State中的伪装拥有Tracing标签，但却和DisguiseAPI中获得的不一样");
                    logger.warn("API: " + disg + " :: State: " + disgInState);

                    p.sendMessage(MessageUtils.prefixes(p, MorphStrings.errorWhileDisguising()));
                    unMorph(p, true);
                }
                else
                {
                    logger.warn("removing: " + p + " :: " + i.getDisguise() + " <-> " + disg);
                    unMorph(p, true);
                    DisguiseAPI.disguiseEntity(p, disg);
                    disguisedPlayers.remove(i);
                }

                return;
            }

            abilityHandler.handle(p, i);

            var provider = i.getProvider();

            if (provider != null)
            {
                if (!provider.updateDisguise(p, i))
                {
                    p.sendMessage(MessageUtils.prefixes(p, MorphStrings.errorWhileUpdatingDisguise()));

                    unMorph(p, true);
                }
            }
        });

        this.addSchedule(c -> update());
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
    public List<DisguiseState> getDisguisedPlayers()
    {
        return new ObjectArrayList<>(disguisedPlayers);
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
    @Nullable
    public static DisguiseProvider getProvider(String id)
    {
        if (id == null) return null;

        id += ":";
        var splitedId = id.split(":", 2);

        return providers.stream().filter(p -> p.getIdentifier().equals(splitedId[0])).findFirst().orElse(null);
    }

    /**
     * 注册一个DisguiseProvider
     * @param provider 目标Provider
     * @return 操作是否成功
     */
    public boolean registerProvider(DisguiseProvider provider)
    {
        logger.info("注册伪装提供器：" + provider.getIdentifier());

        if (providers.stream().anyMatch(p -> p.getIdentifier().equals(provider.getIdentifier())))
        {
            logger.error("已经注册过一个ID为" + provider.getIdentifier() + "的Provider了");
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

    public boolean doQuickDisguise(Player player)
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
                        var disguise = state.getDisguise();

                        if (disguise instanceof PlayerDisguise playerDisguise
                                && playerDisguise.getName().equals(name)
                                && profileTexture.equals(uuidPlayerTexturesMap.get(playerUniqueId)))
                        {
                            unMorph(player);
                            return true;
                        }
                    }

                    //否则，更新或应用伪装
                    if (morph(player, DisguiseTypes.PLAYER.toId(profile.getName()), targetEntity))
                    {
                        //成功伪装后设置皮肤为头颅的皮肤
                        var disguise = (PlayerDisguise) DisguiseAPI.getDisguise(player);
                        var wrappedProfile = WrappedGameProfile.fromHandle(new MorphGameProfile(profile));

                        var LDprofile = ReflectionManager.getGameProfileWithThisSkin(wrappedProfile.getUUID(), wrappedProfile.getName(), wrappedProfile);

                        //LD不支持直接用profile设置皮肤，只能先存到本地设置完再移除
                        DisguiseAPI.addGameProfile(LDprofile.toString(), LDprofile);
                        disguise.setSkin(LDprofile);
                        DisguiseUtilities.removeGameProfile(LDprofile.toString());

                        uuidPlayerTexturesMap.put(playerUniqueId, profileTexture);
                        return true;
                    }
                }
            }

            updateLastPlayerMorphOperationTime(player);
        }
        else
        {
            var targetedEntity = player.getTargetEntity(5);

            if (targetedEntity != null)
            {
                var disg = DisguiseAPI.getDisguise(targetedEntity);

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
                            ? (disg instanceof PlayerDisguise pd)
                            ? DisguiseTypes.PLAYER.toId(pd.getName())
                            : disg.getType().getEntityType().getKey().asString()
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
        return this.morph(player, key, targetEntity, false);
    }

    /**
     * 伪装某一玩家
     *
     * @param player 要伪装的玩家
     * @param key 伪装ID
     * @param targetEntity 玩家正在看的实体
     * @param bypassPermission 是否绕过权限检查
     * @return 操作是否成功
     */
    public boolean morph(Player player, String key, @Nullable Entity targetEntity, boolean bypassPermission)
    {
        if (!bypassPermission && !player.hasPermission(CommonPermissions.MORPH))
        {
            player.sendMessage(MessageUtils.prefixes(player, CommandStrings.noPermissionMessage()));

            return false;
        }

        if (!key.contains(":")) key = DisguiseTypes.VANILLA.toId(key);

        String finalKey = key;
        var info = getAvaliableDisguisesFor(player).stream()
                .filter(i -> i.getIdentifier().equals(finalKey)).findFirst().orElse(null);

        if (bannedDisguises.contains(key))
        {
            player.sendMessage(MessageUtils.prefixes(player, MorphStrings.disguiseBannedOrNotSupportedString()));
            return false;
        }

        //提前取消所有被动
        var state = getDisguiseStateFor(player);

        if (state != null)
            state.getAbilities().forEach(a -> a.revokeFromPlayer(player, state));

        //检查有没有伪装
        if (info != null)
        {
            try
            {
                //查找provider
                var strippedKey = key.split(":", 2);

                var provider = getProvider(strippedKey[0]);

                if (provider == null)
                {
                    player.sendMessage(MessageUtils.prefixes(player, MorphStrings.disguiseBannedOrNotSupportedString()));
                    logger.error("未能找到和命名空间" + strippedKey[0] + "匹配的Provider");
                    return false;
                }
                else
                {
                    var result = provider.morph(player, info, targetEntity);

                    if (!result.success())
                    {
                        player.sendMessage(MessageUtils.prefixes(player, MorphStrings.errorWhileDisguising()));
                        logger.error(provider + "在执行伪装时出现问题");
                        return false;
                    }

                    postConstructDisguise(player, targetEntity,
                            info.getIdentifier(), result.disguise(), result.isCopy(), provider);
                }

                var msg = MorphStrings.morphSuccessString()
                        .resolve("what", info.asComponent());

                player.sendMessage(MessageUtils.prefixes(player, msg));

                clientHandler.updateCurrentIdentifier(player, key);

                return true;
            }
            catch (IllegalArgumentException iae)
            {
                player.sendMessage(MessageUtils.prefixes(player, MorphStrings.parseErrorString()
                        .resolve("id", key)));

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
     * 取消所有玩家的伪装
     */
    public void unMorphAll(boolean ignoreOffline)
    {
        var players = new ObjectArrayList<>(disguisedPlayers);
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

        var state = disguisedPlayers.stream()
                .filter(i -> i.getPlayerUniqueID().equals(player.getUniqueId())).findFirst().orElse(null);

        if (state == null)
            return;

        if (state.getProvider() != null)
            state.getProvider().unMorph(player, state);

        //移除所有被动
        state.getAbilities().forEach(a -> a.revokeFromPlayer(player, state));

        spawnParticle(player, player.getLocation(), player.getWidth(), player.getHeight(), player.getWidth());

        disguisedPlayers.remove(state);

        updateLastPlayerMorphOperationTime(player);

        //移除CD
        skillHandler.switchCooldown(player.getUniqueId(), null);

        //移除Bossbar
        state.setBossbar(null);

        player.sendMessage(MessageUtils.prefixes(player, MorphStrings.unMorphSuccessString()));
        player.sendActionBar(Component.empty());

        uuidPlayerTexturesMap.remove(player.getUniqueId());

        if (plugin.isEnabled())
            clientHandler.updateCurrentIdentifier(player, null);

        Bukkit.getPluginManager().callEvent(new PlayerUnMorphEvent(player));
    }

    private void postConstructDisguise(DisguiseState state)
    {
        postConstructDisguise(state.getPlayer(), null,
                state.getDisguiseIdentifier(), state.getDisguise(), state.shouldHandlePose(), state.getProvider());
    }

    /**
     * 构建好伪装之后要做的事
     *
     * @param sourcePlayer     伪装的玩家
     * @param targetEntity     伪装的目标实体
     * @param disguise         伪装
     * @param shouldHandlePose 要不要手动更新伪装Pose？（伪装是否为克隆）
     * @param provider {@link DisguiseProvider}
     */
    private void postConstructDisguise(Player sourcePlayer, @Nullable Entity targetEntity,
                                       String id, Disguise disguise, boolean shouldHandlePose,
                                       @Nullable DisguiseProvider provider)
    {
        //设置自定义数据用来跟踪
        DisguiseUtils.addTrace(disguise);

        var disguiseTypeLD = disguise.getType();
        var entityType = disguiseTypeLD.getEntityType();

        var config = getPlayerConfiguration(sourcePlayer);

        //禁用actionBar
        DisguiseAPI.setActionBarShown(sourcePlayer, false);

        //技能
        var rawIdentifierHasSkill = skillHandler.hasSkill(id) || skillHandler.hasSpeficSkill(id, SkillType.NONE);
        var targetSkillID = rawIdentifierHasSkill ? id : entityType.getKey().asString();

        //更新或者添加DisguiseState
        var state = getDisguiseStateFor(sourcePlayer);
        if (state == null)
        {
            state = new DisguiseState(sourcePlayer, id, targetSkillID, disguise, shouldHandlePose, provider);

            disguisedPlayers.add(state);
        }
        else
        {
            state.setDisguise(id, targetSkillID, disguise, shouldHandlePose);
        }

        if (provider != null)
            provider.postConstructDisguise(state, targetEntity);
        else
            logger.warn("id为" + id + "的伪装没有Provider?");

        //workaround: Disguise#getDisguiseName()不会正常返回实体的自定义名称
        if (targetEntity != null && targetEntity.customName() != null)
            state.setDisplayName(targetEntity.customName());

        //如果伪装的时候坐着，显示提示
        if (sourcePlayer.getVehicle() != null)
            sourcePlayer.sendMessage(MessageUtils.prefixes(sourcePlayer, MorphStrings.morphVisibleAfterStandup()));

        //显示粒子
        var cX = 0d;
        var cZ = 0d;
        var cY = 0d;

        //如果伪装成生物，则按照此生物的碰撞体积来
        if (disguise.isMobDisguise())
        {
            var mobDisguise = (MobDisguise) disguise;
            FakeBoundingBox box;

            var values = DisguiseValues.getDisguiseValues(disguiseTypeLD);

            if (!mobDisguise.isAdult() && values.getBabyBox() != null)
                box = values.getBabyBox();
            else
                box = values.getAdultBox();

            cX = box.getX();
            cY = box.getY();
            cZ = box.getZ();
        }
        else //否则，按玩家的碰撞体积算
        {
            cX = cZ = sourcePlayer.getWidth();
            cY = sourcePlayer.getHeight();
        }

        spawnParticle(sourcePlayer, sourcePlayer.getLocation(), cX, cY, cZ);

        //确保玩家可以根据设置看到自己的伪装
        setSelfDisguiseVisible(sourcePlayer, config.showDisguiseToSelf, false);

        if (!config.shownMorphAbilityHint && skillHandler.hasSkill(id))
        {
            sourcePlayer.sendMessage(MessageUtils.prefixes(sourcePlayer, MorphStrings.skillHintString()));
            config.shownMorphAbilityHint = true;
        }

        if (!config.shownDisplayToSelfHint)
        {
            sourcePlayer.sendMessage(MessageUtils.prefixes(sourcePlayer, MorphStrings.morphVisibleAfterCommandString()));
            config.shownDisplayToSelfHint = true;
        }

        //更新上次操作时间
        updateLastPlayerMorphOperationTime(sourcePlayer);

        SkillCooldownInfo cdInfo;

        //CD时间
        cdInfo = skillHandler.getCooldownInfo(sourcePlayer.getUniqueId(), targetSkillID);

        if (cdInfo != null)
        {
            state.setCooldownInfo(cdInfo);
            cdInfo.setCooldown(Math.max(40, state.getSkillCooldown()));
            cdInfo.setLastInvoke(plugin.getCurrentTick());
        }

        //切换CD
        skillHandler.switchCooldown(sourcePlayer.getUniqueId(), cdInfo);

        //调用事件
        Bukkit.getPluginManager().callEvent(new PlayerMorphEvent(sourcePlayer, state));
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

    public void setSelfDisguiseVisible(Player player, boolean value, boolean saveToConfig)
    {
        var state = getDisguiseStateFor(player);
        var config = data.getPlayerConfiguration(player);

        if (state != null)
            state.setSelfVisible(value);

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
        return this.disguisedPlayers.stream()
                .filter(i -> i.getPlayerUniqueID().equals(player.getUniqueId()))
                .findFirst().orElse(null);
    }

    public void onPluginDisable()
    {
        unMorphAll(true);
        saveConfiguration();

        getDisguisedPlayers().forEach(s ->
        {
            if (!s.getPlayer().isOnline())
                offlineStorage.pushDisguiseState(s);
        });

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

    public boolean disguiseFromOfflineState(Player player, OfflineDisguiseState offlineState)
    {
        if (player.getUniqueId() == offlineState.playerUUID)
        {
            logger.error("玩家UUID与OfflineState的UUID不一致: " + player.getUniqueId() + " :: " + offlineState.playerUUID);
            return false;
        }

        var key = offlineState.disguiseID;

        var avaliableDisguises = getAvaliableDisguisesFor(player);

        var disguiseType = DisguiseTypes.fromId(key);

        if (disguiseType == DisguiseTypes.UNKNOWN) return false;

        //直接还原非LD的伪装
        if (offlineState.disguise != null && disguiseType != DisguiseTypes.LD)
        {
            DisguiseUtils.addTrace(offlineState.disguise);

            var state = DisguiseState.fromOfflineState(offlineState, data.getPlayerConfiguration(player));

            disguisedPlayers.add(state);

            DisguiseAPI.disguiseEntity(player, state.getDisguise());
            postConstructDisguise(state);
            return true;
        }

        //有限还原
        if (avaliableDisguises.stream().anyMatch(i -> i.getKey().matches(key)))
        {
            morph(player, key, null);
            return true;
        }

        return false;
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
        clientHandler.sendDiff(List.of(disguiseIdentifier), null, player);
        return data.grantMorphToPlayer(player, disguiseIdentifier);
    }

    @Override
    public boolean revokeMorphFromPlayer(Player player, String disguiseIdentifier)
    {
        clientHandler.sendDiff(null, List.of(disguiseIdentifier), player);
        return data.revokeMorphFromPlayer(player, disguiseIdentifier);
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

        getDisguisedPlayers().forEach(s ->
        {
            if (!s.getPlayer().isOnline())
                stateToOfflineStore.add(s);
        });

        unMorphAll(false);

        var success = data.reloadConfiguration() && offlineStorage.reloadConfiguration();

        stateToOfflineStore.forEach(offlineStorage::pushDisguiseState);

        Bukkit.getOnlinePlayers().forEach(p -> clientHandler.refreshPlayerClientMorphs(this.getPlayerConfiguration(p).getUnlockedDisguiseIdentifiers(), p));

        return success;
    }

    @Override
    public boolean saveConfiguration()
    {
        return data.saveConfiguration() && offlineStorage.reloadConfiguration();
    }
    //endregion Implementation of IManagePlayerData
}