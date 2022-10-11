package xiamomc.morph;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.ArmorStandWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.utilities.DisguiseValues;
import me.libraryaddict.disguise.utilities.reflection.FakeBoundingBox;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.abilities.AbilityFlag;
import xiamomc.morph.abilities.AbilityHandler;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.events.PlayerMorphEvent;
import xiamomc.morph.events.PlayerUnMorphEvent;
import xiamomc.morph.interfaces.IManagePlayerData;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.MorphStrings;
import xiamomc.morph.misc.*;
import xiamomc.morph.skills.*;
import xiamomc.morph.storage.offlinestore.OfflineDisguiseState;
import xiamomc.morph.storage.offlinestore.OfflineStorageManager;
import xiamomc.morph.storage.playerdata.PlayerDataManager;
import xiamomc.morph.storage.playerdata.PlayerMorphConfiguration;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MorphManager extends MorphPluginObject implements IManagePlayerData
{
    /**
     * 变成其他玩家的玩家
     * 因为插件限制，需要每tick更新下蹲和疾跑状态
     */
    private final List<DisguiseState> disguisedPlayers = new ArrayList<>();

    private final PlayerDataManager data = new PlayerDataManager();

    private final OfflineStorageManager offlineStorage = new OfflineStorageManager();

    private final MorphSkillHandler skillHandler = new MorphSkillHandler();

    private final AbilityHandler abilityHandler = new AbilityHandler();

    //region 聊天覆盖

    private boolean allowChatOverride = false;

    public boolean allowChatOverride()
    {
        return allowChatOverride;
    }

    public void setChatOverride(boolean val)
    {
        allowChatOverride = val;

        config.set(ConfigOption.ALLOW_CHAT_OVERRIDE, val);
    }

    //endregion 聊天覆盖

    @Resolved
    private MorphConfigManager config;

    @Initializer
    private void load()
    {
        this.addSchedule(c -> update());

        config.onConfigRefresh(c -> this.onConfigRefresh(), true);

        Dependencies.Cache(abilityHandler);

        skillHandler.registerSkills(List.of(
                new ArmorStandMorphSkill(),
                new BlazeMorphSkill(),
                new CreeperMorphSkill(),
                new DolphinMorphSkill(),
                new ElderGuardianMorphSkill(),
                new EnderDragonMorphSkill(),
                new EndermanMorphSkill(),
                new GhastMorphSkill(),
                new PlayerMorphSkill(),
                new ShulkerMorphSkill(),
                new WitherMorphSkill()
        ));

        abilityHandler.registerAbility(EntityTypeUtils.canFly(), AbilityFlag.CAN_FLY);
        abilityHandler.registerAbility(EntityTypeUtils.hasFireResistance(), AbilityFlag.HAS_FIRE_RESISTANCE);
        abilityHandler.registerAbility(EntityTypeUtils.takesDamageFromWater(), AbilityFlag.TAKES_DAMAGE_FROM_WATER);
        abilityHandler.registerAbility(EntityTypeUtils.canBreatheUnderWater(), AbilityFlag.CAN_BREATHE_UNDER_WATER);
        abilityHandler.registerAbility(EntityTypeUtils.burnsUnderSun(), AbilityFlag.BURNS_UNDER_SUN);
        abilityHandler.registerAbility(EntityTypeUtils.alwaysNightVision(), AbilityFlag.ALWAYS_NIGHT_VISION);
        abilityHandler.registerAbility(EntityTypeUtils.hasJumpBoost(), AbilityFlag.HAS_JUMP_BOOST);
        abilityHandler.registerAbility(EntityTypeUtils.hasSmallJumpBoost(), AbilityFlag.HAS_SMALL_JUMP_BOOST);
        abilityHandler.registerAbility(EntityTypeUtils.hasSpeedBoost(), AbilityFlag.HAS_SPEED_BOOST);
        abilityHandler.registerAbility(EntityTypeUtils.noFallDamage(), AbilityFlag.NO_FALL_DAMAGE);
        abilityHandler.registerAbility(EntityTypeUtils.hasFeatherFalling(), AbilityFlag.HAS_FEATHER_FALLING);
    }

    private void onConfigRefresh()
    {
        setChatOverride(config.getOrDefault(Boolean.class, ConfigOption.ALLOW_CHAT_OVERRIDE, false));
    }

    private void update()
    {
        var infos = new ArrayList<>(disguisedPlayers);
        for (var i : infos)
        {
            var p = i.getPlayer();

            //跳过离线玩家
            if (!p.isOnline()) continue;

            //检查State中的伪装和实际的是否一致
            var disg = DisguiseAPI.getDisguise(p);
            var disgInState = i.getDisguise();
            if (!disgInState.equals(disg))
            {
                if (DisguiseUtils.isTracing(disgInState))
                {
                    Logger.warn(p.getName() + "在State中的伪装拥有Tracing标签，但却和DisguiseAPI中获得的不一样");
                    Logger.warn("API: " + disg + " :: State: " + disgInState);

                    p.sendMessage(MessageUtils.prefixes(p, Component.translatable("更新伪装时遇到了意外，正在取消伪装")));
                    unMorph(p);
                }
                else
                {
                    Logger.warn("removing: " + p + " :: " + i.getDisguise() + " <-> " + disg);
                    unMorph(p);
                    DisguiseAPI.disguiseEntity(p, disg);
                    disguisedPlayers.remove(i);
                }

                continue;
            }

            //更新伪装状态
            updateDisguise(p, i);
        }

        this.addSchedule(c -> update());
    }

    //region 玩家伪装相关

    /**
     * 更新伪装状态
     *
     * @param player 目标玩家
     * @param state   伪装信息
     */
    private void updateDisguise(@NotNull Player player, @NotNull DisguiseState state)
    {
        var disguise = state.getDisguise();
        var watcher = disguise.getWatcher();

        //更新actionbar信息
        var msg = skillHandler.hasSkill(disguise.getType().getEntityType())
                ? (state.getAbilityCooldown() <= 0
                    ? MorphStrings.disguisingWithSkillAvaliableString()
                    : MorphStrings.disguisingWithSkillPreparingString())
                : MorphStrings.disguisingAsString();

        player.sendActionBar(msg.resolve("what", state.getDisplayName()).toComponent());

        //workaround: 复制实体伪装时会一并复制隐身标签
        //            会导致复制出来的伪装永久隐身
        watcher.setInvisible(player.isInvisible());

        //workaround: 伪装不会主动检测玩家有没有发光
        watcher.setGlowing(player.isGlowing());

        watcher.setFlyingWithElytra(player.isGliding());

        //workaround: 复制出来的伪装会忽略玩家Pose
        if (state.shouldHandlePose())
            watcher.setEntityPose(DisguiseUtils.toEntityPose(player.getPose()));

        //tick伪装行为
        abilityHandler.handle(player, state);

        state.setAbilityCooldown(state.getAbilityCooldown() - 1);
    }

    /**
     * 使某个玩家执行伪装的主动技能
     * @param player 目标玩家
     */
    public void executeDisguiseAbility(Player player)
    {
        skillHandler.executeDisguiseAbility(player);
    }

    /**
     * 获取所有已伪装的玩家
     * @return 玩家列表
     * @apiNote 列表中的玩家可能已经离线
     */
    public List<DisguiseState> getDisguisedPlayers()
    {
        return new ArrayList<>(disguisedPlayers);
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

        return val == null || Plugin.getCurrentTick() - val >= 20;
    }

    /**
     * 更新某个玩家的上次伪装操作事件
     * @param player 要更新的玩家
     */
    public void updateLastPlayerMorphOperationTime(Player player)
    {
        uuidMoprhTimeMap.put(player.getUniqueId(), Plugin.getCurrentTick());
    }

    /**
     * 根据传入的key自动伪装
     * @param player 要伪装的玩家
     * @param key key
     * @param targetEntity 玩家正在看的实体
     * @return 操作是否成功
     */
    public boolean morphEntityTypeAuto(Player player, String key, @Nullable Entity targetEntity)
    {
        var infoOptional = getAvaliableDisguisesFor(player).stream()
                .filter(i -> i.getKey().equals(key)).findFirst();

        //检查有没有伪装
        if (infoOptional.isPresent())
        {
            try
            {
                //获取到的伪装
                var info = infoOptional.get();

                //目标类型
                var type = info.type;

                if (!type.isAlive())
                {
                    player.sendMessage(MessageUtils.prefixes(player, MorphStrings.invalidIdentityString()));

                    return false;
                }

                //是否应该复制伪装
                var shouldCopy = false;

                //如果实体有伪装，则检查实体的伪装类型
                if (DisguiseAPI.isDisguised(targetEntity))
                {
                    var disg = DisguiseAPI.getDisguise(targetEntity);
                    assert disg != null;

                    shouldCopy = info.isPlayerDisguise()
                            ? disg.isPlayerDisguise() && ((PlayerDisguise) disg).getName().equals(info.playerDisguiseTargetName)
                            : disg.getType().getEntityType().equals(type);
                }

                if (shouldCopy)
                {
                    morphCopy(player, targetEntity); //如果应该复制伪装，则复制给玩家
                }
                else if (info.isPlayerDisguise()
                            ? (targetEntity instanceof Player targetPlayer && targetPlayer.getName().equals(info.playerDisguiseTargetName))
                            : (targetEntity != null && targetEntity.getType().equals(type))
                        && !DisguiseAPI.isDisguised(targetEntity))
                {
                    morphEntity(player, targetEntity); //否则，如果目标实体是我们想要的实体，则伪装成目标实体
                }
                else
                {
                    if (info.isPlayerDisguise())
                        morphPlayer(player, info.playerDisguiseTargetName);
                    else
                        morphEntityType(player, type); //否则，只简单地创建实体伪装
                }

                var msg = MorphStrings.morphSuccessString()
                        .resolve("what", type == EntityType.PLAYER
                                ? Component.text(key.replace("player:", ""))
                                : Component.translatable(type.translationKey()));

                player.sendMessage(MessageUtils.prefixes(player, msg));

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
     * 将玩家伪装成指定的实体类型
     *
     * @param player     目标玩家
     * @param entityType 目标实体类型
     */
    public void morphEntityType(Player player, EntityType entityType)
    {
        Disguise constructedDisguise = null;

        //不要构建玩家类型的伪装
        if (entityType == EntityType.PLAYER) throw new IllegalArgumentException("玩家不能作为EntityType传入");

        constructedDisguise = new MobDisguise(DisguiseType.getType(entityType));

        postConstructDisguise(player, null, constructedDisguise, false);

        DisguiseAPI.disguiseEntity(player, constructedDisguise);
    }

    /**
     * 将玩家伪装成指定的实体
     *
     * @param sourcePlayer 目标玩家
     * @param entity       目标实体
     */
    public void morphEntity(Player sourcePlayer, Entity entity)
    {
        Disguise draftDisguise = null;

        draftDisguise = DisguiseAPI.constructDisguise(entity);

        postConstructDisguise(sourcePlayer, entity, draftDisguise, true);
        DisguiseAPI.disguiseEntity(sourcePlayer, draftDisguise);
    }

    /**
     * 复制目标实体的伪装给玩家
     *
     * @param sourcePlayer 要应用的玩家
     * @param targetEntity 目标实体
     */
    public void morphCopy(Player sourcePlayer, Entity targetEntity)
    {
        Disguise draftDisguise = null;

        if (!DisguiseAPI.isDisguised(targetEntity)) throw new IllegalArgumentException("目标实体没有伪装");

        DisguiseAPI.disguiseEntity(sourcePlayer, DisguiseAPI.getDisguise(targetEntity));
        draftDisguise = DisguiseAPI.getDisguise(sourcePlayer);

        postConstructDisguise(sourcePlayer, targetEntity, draftDisguise, true);
    }

    /**
     * 将玩家伪装成指定的玩家
     *
     * @param sourcePlayer     发起玩家
     * @param targetPlayerName 目标玩家的玩家名
     */
    public void morphPlayer(Player sourcePlayer, String targetPlayerName)
    {
        targetPlayerName = targetPlayerName.replace("player:", "");

        var disguise = new PlayerDisguise(targetPlayerName);

        postConstructDisguise(sourcePlayer, null, disguise, false);

        DisguiseAPI.disguiseEntity(sourcePlayer, disguise);
    }

    /**
     * 取消所有玩家的伪装
     */
    public void unMorphAll(boolean ignoreOffline)
    {
        var players = new ArrayList<>(disguisedPlayers);
        players.forEach(i ->
        {
            if (ignoreOffline && !i.getPlayer().isOnline()) return;

            unMorph(i.getPlayer());
        });
    }

    /**
     * 取消某一玩家的伪装
     *
     * @param player 目标玩家
     */
    public void unMorph(Player player)
    {
        var targetInfoOptional = disguisedPlayers.stream().filter(i -> i.getPlayerUniqueID().equals(player.getUniqueId())).findFirst();
        if (targetInfoOptional.isEmpty())
            return;

        var disguise = targetInfoOptional.get().getDisguise();
        disguise.removeDisguise(player);

        player.sendMessage(MessageUtils.prefixes(player, MorphStrings.unMorphSuccessString()));
        player.sendActionBar(Component.empty());

        //取消玩家飞行
        player.setAllowFlight(canFly(player, null));
        player.setFlySpeed(0.1f);

        spawnParticle(player, player.getLocation(), player.getWidth(), player.getHeight(), player.getWidth());

        disguisedPlayers.remove(targetInfoOptional.get());

        updateLastPlayerMorphOperationTime(player);

        Bukkit.getPluginManager().callEvent(new PlayerUnMorphEvent(player));
    }

    private boolean canFly(Player player, @Nullable DisguiseState state)
    {
        var gamemode = player.getGameMode();
        var gamemodeAllowFlying = gamemode.equals(GameMode.CREATIVE) || gamemode.equals(GameMode.SPECTATOR);

        if (state == null) return gamemodeAllowFlying;
        else return gamemodeAllowFlying || state.isAbilityFlagSet(AbilityFlag.CAN_FLY);
    }

    private void setPlayerFlySpeed(Player player, EntityType type)
    {
        var gameMode = player.getGameMode();
        if (type == null || gameMode.equals(GameMode.CREATIVE) || gameMode.equals(GameMode.SPECTATOR)) return;

        switch (type)
        {
            case ALLAY, BEE, BLAZE, VEX, BAT -> player.setFlySpeed(0.05f);
            case GHAST, PHANTOM -> player.setFlySpeed(0.06f);
            case ENDER_DRAGON -> player.setFlySpeed(0.15f);
            default -> player.setFlySpeed(0.1f);
        }
    }

    public boolean updateFlyingAbility(Player player)
    {
        var state = getDisguiseStateFor(player);
        if (state == null) return false;

        var canFly = canFly(player, state);
        player.setAllowFlight(canFly);
        setPlayerFlySpeed(player, canFly ? state.getDisguise().getType().getEntityType() : null);

        return canFly;
    }

    private void postConstructDisguise(DisguiseState state)
    {
        postConstructDisguise(state.getPlayer(), null, state.getDisguise(), state.shouldHandlePose());
    }

    /**
     * 构建好伪装之后要做的事
     *
     * @param sourcePlayer     伪装的玩家
     * @param targetEntity     伪装的目标实体
     * @param disguise         伪装
     * @param shouldHandlePose 要不要手动更新伪装Pose？（伪装是否为克隆）
     */
    private void postConstructDisguise(Player sourcePlayer, @Nullable Entity targetEntity, Disguise disguise, boolean shouldHandlePose)
    {
        //设置自定义数据用来跟踪
        DisguiseUtils.addTrace(disguise);

        var watcher = disguise.getWatcher();
        var disguiseType = disguise.getType();

        //workaround: 伪装已死亡的LivingEntity
        if (targetEntity instanceof LivingEntity living && living.getHealth() <= 0)
            ((LivingWatcher) watcher).setHealth(1);

        //目标实体没有伪装时要做的操作
        //workaround: 玩家伪装副手问题
        if (!DisguiseAPI.isDisguised(targetEntity))
        {
            ItemStack offhandItemStack = null;

            if (targetEntity instanceof Player targetPlayer)
                offhandItemStack = targetPlayer.getInventory().getItemInOffHand();

            if (targetEntity instanceof ArmorStand armorStand)
                offhandItemStack = armorStand.getItem(EquipmentSlot.OFF_HAND);

            if (offhandItemStack != null) watcher.setItemInOffHand(offhandItemStack);

            //盔甲架加上手臂
            if (disguise.getType().equals(DisguiseType.ARMOR_STAND))
                ((ArmorStandWatcher) watcher).setShowArms(true);
        }
        else if (shouldHandlePose && targetEntity instanceof Player targetPlayer)
        {
            //如果目标实体是玩家，并且此玩家伪装是复制的、目标玩家的伪装和我们的一样，那么复制他们的装备
            var theirDisguise = DisguiseAPI.getDisguise(targetPlayer);

            //如果是同类伪装，则复制盔甲
            if (disguiseType.equals(theirDisguise.getType()))
            {
                if (!(disguise instanceof PlayerDisguise ourPlayerDisguise) || !(theirDisguise instanceof PlayerDisguise theirPlayerDisguise)
                        || Objects.equals(ourPlayerDisguise.getName(), theirPlayerDisguise.getName()))
                {
                    DisguiseUtils.tryCopyArmorStack(targetPlayer, disguise.getWatcher(), theirDisguise.getWatcher());
                }
            }
        }

        //禁用actionBar
        DisguiseAPI.setActionBarShown(sourcePlayer, false);

        //更新或者添加DisguiseState
        var state = getDisguiseStateFor(sourcePlayer);
        if (state == null)
        {
            state = new DisguiseState(sourcePlayer, disguise, shouldHandlePose);

            disguisedPlayers.add(state);
        }
        else
            state.setDisguise(disguise, shouldHandlePose);

        //如果伪装的时候坐着，显示提示
        if (sourcePlayer.getVehicle() != null)
            sourcePlayer.sendMessage(MessageUtils.prefixes(sourcePlayer, MorphStrings.morphVisibleAfterStandup()));

        //如果实体能飞，那么也允许玩家飞行
        updateFlyingAbility(sourcePlayer);

        //显示粒子
        var cX = 0d;
        var cZ = 0d;
        var cY = 0d;

        //如果伪装成生物，则按照此生物的碰撞体积来
        if (disguise.isMobDisguise())
        {
            var mobDisguise = (MobDisguise) disguise;
            FakeBoundingBox box;

            var values = DisguiseValues.getDisguiseValues(disguise.getType());

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
        disguise.setSelfDisguiseVisible(DisguiseAPI.isViewSelfToggled(sourcePlayer));

        var config = getPlayerConfiguration(sourcePlayer);
        if (!config.shownMorphAbilityHint && skillHandler.hasSkill(disguiseType.getEntityType()))
        {
            sourcePlayer.sendMessage(MessageUtils.prefixes(sourcePlayer, MorphStrings.skillHintString()));
            config.shownMorphAbilityHint = true;
        }

        updateLastPlayerMorphOperationTime(sourcePlayer);

        Bukkit.getPluginManager().callEvent(new PlayerMorphEvent(sourcePlayer, state));
    }

    public void spawnParticle(Player player, Location location, double collX, double collY, double collZ)
    {
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
            Logger.error("玩家UUID与OfflineState的UUID不一致: " + player.getUniqueId() + " :: " + offlineState.playerUUID);
            return false;
        }

        var key = offlineState.disguiseID;

        var avaliableDisguises = getAvaliableDisguisesFor(player);

        //直接还原
        if (offlineState.disguise != null)
        {
            DisguiseUtils.addTrace(offlineState.disguise);

            var state = DisguiseState.fromOfflineState(offlineState);

            disguisedPlayers.add(state);

            DisguiseAPI.disguiseEntity(player, state.getDisguise());
            postConstructDisguise(state);
            return true;
        }

        //有限还原
        if (key.startsWith("player:"))
        {
            //检查玩家是否还拥有目标玩家的伪装
            if (avaliableDisguises.stream().anyMatch(i -> i.getKey().matches(key)))
            {
                morphPlayer(player, key);
                return true;
            }
        }
        else
        {
            //检查玩家是否还拥有目标类型的伪装
            if (avaliableDisguises.stream().anyMatch(i -> i.getKey().equals(key)))
            {
                var types = EntityType.values();
                EntityType targetType = null;

                //寻找type
                var aa = Arrays.stream(types)
                        .filter(t -> !t.equals(EntityType.UNKNOWN) && t.getKey().asString().equals(key)).findFirst();

                if (aa.isPresent()) targetType = aa.get();
                else return false;

                morphEntityType(player, targetType);

                return true;
            }
        }

        return false;
    }

    //endregion 玩家伪装相关

    //region Implementation of IManagePlayerData

    @Override
    public DisguiseInfo getDisguiseInfo(EntityType type)
    {
        return data.getDisguiseInfo(type);
    }

    @Override
    public DisguiseInfo getDisguiseInfo(String playerName)
    {
        return data.getDisguiseInfo(playerName);
    }

    @Override
    public ArrayList<DisguiseInfo> getAvaliableDisguisesFor(Player player)
    {
        return data.getAvaliableDisguisesFor(player);
    }

    @Override
    public boolean grantMorphToPlayer(Player player, EntityType type)
    {
        return data.grantMorphToPlayer(player, type);
    }

    @Override
    public boolean grantPlayerMorphToPlayer(Player sourcePlayer, String targtPlayerName)
    {
        return data.grantPlayerMorphToPlayer(sourcePlayer, targtPlayerName);
    }

    @Override
    public boolean revokeMorphFromPlayer(Player player, EntityType entityType)
    {
        return data.revokeMorphFromPlayer(player, entityType);
    }

    @Override
    public boolean revokePlayerMorphFromPlayer(Player player, String playerName)
    {
        return data.revokePlayerMorphFromPlayer(player, playerName);
    }

    @Override
    public PlayerMorphConfiguration getPlayerConfiguration(Player player)
    {
        return data.getPlayerConfiguration(player);
    }

    @Override
    public boolean reloadConfiguration()
    {
        unMorphAll(true);

        return data.reloadConfiguration() && offlineStorage.reloadConfiguration();
    }

    @Override
    public boolean saveConfiguration()
    {
        return data.saveConfiguration() && offlineStorage.reloadConfiguration();
    }
    //endregion Implementation of IManagePlayerData
}