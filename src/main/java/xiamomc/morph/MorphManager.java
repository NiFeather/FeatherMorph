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
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.interfaces.IManagePlayerData;
import xiamomc.morph.interfaces.IManageRequests;
import xiamomc.morph.misc.*;
import xiamomc.morph.storage.offlinestore.OfflineDisguiseState;
import xiamomc.morph.storage.offlinestore.OfflineStorageManager;
import xiamomc.morph.storage.playerdata.PlayerDataManager;
import xiamomc.morph.storage.playerdata.PlayerMorphConfiguration;
import xiamomc.pluginbase.Annotations.Initializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MorphManager extends MorphPluginObject implements IManagePlayerData, IManageRequests
{
    /**
     * 变成其他玩家的玩家
     * 因为插件限制，需要每tick更新下蹲和疾跑状态
     */
    private final List<DisguiseState> disguisedPlayers = new ArrayList<>();

    private final PlayerDataManager data = new PlayerDataManager();

    private final OfflineStorageManager offlineStorage = new OfflineStorageManager();

    @Initializer
    private void load()
    {
        this.addSchedule(c -> update());
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

        //更新请求
        var requests = new ArrayList<>(this.requests);
        for (var r : requests)
        {
            r.ticksRemain -= 1;
            if (r.ticksRemain <= 0) this.requests.remove(r);
        }

        this.addSchedule(c -> update());
    }

    private final PotionEffect waterBreathEffect = new PotionEffect(PotionEffectType.WATER_BREATHING, 20, 0);
    private final PotionEffect conduitEffect = new PotionEffect(PotionEffectType.CONDUIT_POWER, 20, 0);
    private final PotionEffect nightVisionEffect = new PotionEffect(PotionEffectType.NIGHT_VISION, 300, 0);
    private final PotionEffect fireResistance = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20, 0);

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
        player.sendActionBar(MessageUtils.prefixes(player, Component.translatable("正伪装为").append(state.getDisplayName())));

        //workaround: 复制实体伪装时会一并复制隐身标签
        //            会导致复制出来的伪装永久隐身
        watcher.setInvisible(player.isInvisible());

        //workaround: 复制出来的玩家伪装会忽略下蹲等状态
        if (disguise.isPlayerDisguise())
        {
            watcher.setSneaking(player.isSneaking() && !player.isFlying());
            watcher.setFlyingWithElytra(player.isGliding());
            watcher.setSprinting(player.isSprinting());
        }

        //tick伪装行为
        var flag = state.getFlag();

        if (state.isFlagSet(DisguiseState.canBreatheUnderWater) && player.isInWaterOrRainOrBubbleColumn())
        {
            player.addPotionEffect(conduitEffect);
            player.addPotionEffect(waterBreathEffect);
        }

        if (state.isFlagSet(DisguiseState.hasFireResistance))
        {
            player.addPotionEffect(fireResistance);
        }

        if (state.isFlagSet(DisguiseState.takesDamageFromWater) && player.isInWaterOrRainOrBubbleColumn())
        {
            player.damage(1);
        }

        if (state.isFlagSet(DisguiseState.burnsUnderSun)
                && player.getEquipment().getHelmet() == null
                && player.getWorld().isDayTime()
                && player.getWorld().isClearWeather()
                && player.getWorld().getEnvironment().equals(World.Environment.NORMAL)
                && player.getLocation().getBlock().getLightFromSky() == 15)
        {
            player.setFireTicks(200);
        }

        if (state.isFlagSet(DisguiseState.alwaysNightVision))
            player.addPotionEffect(nightVisionEffect);
    }

    public List<DisguiseState> getDisguisedPlayers()
    {
        return new ArrayList<>(disguisedPlayers);
    }

    //region 玩家伪装相关

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
        if (entityType == EntityType.PLAYER) return;

        constructedDisguise = new MobDisguise(DisguiseType.getType(entityType));

        postConstructDisguise(player, null, constructedDisguise);

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

        postConstructDisguise(sourcePlayer, entity, draftDisguise);
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

        if (!DisguiseAPI.isDisguised(targetEntity)) return;

        DisguiseAPI.disguiseEntity(sourcePlayer, DisguiseAPI.getDisguise(targetEntity));
        draftDisguise = DisguiseAPI.getDisguise(sourcePlayer);

        postConstructDisguise(sourcePlayer, targetEntity, draftDisguise);
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

        postConstructDisguise(sourcePlayer, null, disguise);

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

        player.sendMessage(MessageUtils.prefixes(player, Component.text("已取消伪装")));
        player.sendActionBar(Component.empty());

        //取消玩家飞行
        player.setAllowFlight(canFly(player, null));
        player.setFlySpeed(0.1f);

        spawnParticle(player, player.getLocation(), player.getWidth(), player.getHeight(), player.getWidth());

        disguisedPlayers.remove(targetInfoOptional.get());
    }

    private boolean canFly(Player player, @Nullable DisguiseState state)
    {
        var gamemode = player.getGameMode();
        var gamemodeAllowFlying = gamemode.equals(GameMode.CREATIVE) || gamemode.equals(GameMode.SPECTATOR);

        if (state == null) return gamemodeAllowFlying;
        else return gamemodeAllowFlying || state.isFlagSet(DisguiseState.canFly);
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

    /**
     * 构建好伪装之后要做的事
     *
     * @param sourcePlayer     伪装的玩家
     * @param targetEntity     伪装的目标实体
     * @param disguise         伪装
     */
    private void postConstructDisguise(Player sourcePlayer, @Nullable Entity targetEntity, Disguise disguise)
    {
        //设置自定义数据用来跟踪
        DisguiseUtils.addTrace(disguise);

        var watcher = disguise.getWatcher();

        //workaround: 伪装已死亡的LivingEntity
        if (targetEntity instanceof LivingEntity living && living.getHealth() <= 0)
            ((LivingWatcher) watcher).setHealth(1);

        //workaround: 玩家伪装副手问题
        //如果目标实体有伪装，则不要修改
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

        //禁用actionBar
        DisguiseAPI.setActionBarShown(sourcePlayer, false);

        //更新或者添加DisguiseState
        var state = getDisguiseStateFor(sourcePlayer);

        if (state == null)
        {
            state = new DisguiseState(sourcePlayer, disguise);

            disguisedPlayers.add(state);
        }
        else
            state.setDisguise(disguise);

        //如果伪装的时候坐着，显示提示
        if (sourcePlayer.getVehicle() != null)
            sourcePlayer.sendMessage(MessageUtils.prefixes(sourcePlayer, Component.text("您将在起身后看到自己的伪装")));

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
    }

    private void spawnParticle(Player player, Location location, double collX, double collY, double collZ)
    {
        location.setY(location.getY() + (collY / 2));

        //根据碰撞箱计算粒子数量缩放
        //缩放为碰撞箱体积的1/15，最小为1
        var particleScale = Math.max(1, (collX * collY * collZ) / 15);

        //显示粒子
        player.spawnParticle(Particle.EXPLOSION_NORMAL, location, //类型和位置
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

    public boolean disguiseFromOfflineState(Player player, OfflineDisguiseState state)
    {
        if (player.getUniqueId() == state.playerUUID)
        {
            Logger.error("玩家UUID与OfflineState的UUID不一致: " + player.getUniqueId() + " :: " + state.playerUUID);
            return false;
        }

        var key = state.disguiseID;

        var avaliableDisguises = getAvaliableDisguisesFor(player);

        //直接还原
        if (state.disguise != null)
        {
            var disguise = state.disguise;
            DisguiseAPI.disguiseEntity(player, disguise);

            postConstructDisguise(player, null, disguise);
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

    //region Implementation of IManageRequests

    private final List<RequestInfo> requests = new ArrayList<>();

    @Override
    public void createRequest(Player source, Player target)
    {
        if (requests.stream()
                .anyMatch(i -> i.sourcePlayer.getUniqueId() == source.getUniqueId()
                        && i.targetPlayer.getUniqueId() == target.getUniqueId()))
        {
            source.sendMessage(MessageUtils.prefixes(source, Component.text("你已经向" + target + "发送过一个请求了")));
            return;
        }

        var req = new RequestInfo();
        req.sourcePlayer = source;
        req.targetPlayer = target;
        req.ticksRemain = 1200;

        requests.add(req);

        var msg = Component.translatable("你收到了来自")
                .append(Component.text(source.getName()))
                .append(Component.translatable("的交换请求！"));

        target.sendMessage(MessageUtils.prefixes(target, msg));

        source.sendMessage(MessageUtils.prefixes(source, Component.translatable("请求已发送！对方将有1分钟的时间来接受")));
    }

    @Override
    public void acceptRequest(Player source, Player target)
    {
        var match = requests.stream()
                .filter(i -> i.sourcePlayer.getUniqueId().equals(target.getUniqueId())
                        && i.targetPlayer.getUniqueId().equals(source.getUniqueId())).findFirst();

        if (match.isEmpty())
        {
            source.sendMessage(MessageUtils.prefixes(source, Component.text("未找到目标请求，可能已经过期？")));
            return;
        }

        var req = match.get();
        req.ticksRemain = -1;

        data.grantPlayerMorphToPlayer(target, source.getName());
        data.grantPlayerMorphToPlayer(source, target.getName());

        target.sendMessage(MessageUtils.prefixes(target, Component.text("成功与" + source.getName() + "交换！")));
        source.sendMessage(MessageUtils.prefixes(source, Component.text("成功与" + target.getName() + "交换！")));
    }

    @Override
    public void denyRequest(Player source, Player target)
    {
        var match = requests.stream()
                .filter(i -> i.sourcePlayer.getUniqueId().equals(target.getUniqueId())
                        && i.targetPlayer.getUniqueId().equals(source.getUniqueId())).findFirst();

        if (match.isEmpty())
        {
            source.sendMessage(MessageUtils.prefixes(source, Component.text("未找到目标请求，可能已经过期？")));

            //"未找到目标请求，可能已经过期？"
            return;
        }

        var req = match.get();
        req.ticksRemain = -1;

        var msg = Component.text("请求已拒绝");

        target.sendMessage(MessageUtils.prefixes(target, Component.text("发往" + source.getName() + "的").append(msg)));
        source.sendMessage(MessageUtils.prefixes(source, Component.text("来自" + target.getName() + "的").append(msg)));
    }

    @Override
    public List<RequestInfo> getAvaliableRequestFor(Player player)
    {
        return requests.stream()
                .filter(t -> t.targetPlayer.getUniqueId().equals(player.getUniqueId()))
                .toList();
    }

    //endregion Implementation of IManageRequests

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
        var val = data.grantMorphToPlayer(player, type);

        if (val)
            sendMorphAcquiredNotification(player,
                    Component.text("✔ 已解锁")
                            .append(Component.translatable(type.translationKey()))
                            .append(Component.text("的伪装")).color(NamedTextColor.GREEN));

        return val;
    }

    @Override
    public boolean grantPlayerMorphToPlayer(Player sourcePlayer, String targetPlayerName)
    {
        var val = data.grantPlayerMorphToPlayer(sourcePlayer, targetPlayerName);

        if (val)
            sendMorphAcquiredNotification(sourcePlayer,
                    Component.text("✔ 已解锁" + targetPlayerName + "的伪装").color(NamedTextColor.GREEN));

        return val;
    }

    @Override
    public boolean revokeMorphFromPlayer(Player player, EntityType entityType)
    {
        var val = data.revokeMorphFromPlayer(player, entityType);

        if (val)
        {
            var state = getDisguiseStateFor(player);
            if (state != null && state.getDisguise().getType().getEntityType().equals(entityType))
                unMorph(player);

            sendMorphAcquiredNotification(player,
                    Component.text("❌ 已失去")
                            .append(Component.translatable(entityType.translationKey()))
                            .append(Component.text("的伪装")).color(NamedTextColor.RED));
        }

        return val;
    }

    @Override
    public boolean revokePlayerMorphFromPlayer(Player player, String playerName)
    {
        var val = data.revokePlayerMorphFromPlayer(player, playerName);

        if (val)
        {
            var state = getDisguiseStateFor(player);

            if (state != null
                    && state.getDisguise().isPlayerDisguise()
                    && ((PlayerDisguise)state.getDisguise()).getName().equals(playerName))
            {
                unMorph(player);
            }

            sendMorphAcquiredNotification(player,
                    Component.text("❌ 已失去" + playerName + "的伪装").color(NamedTextColor.RED));
        }

        return val;
    }

    private void sendMorphAcquiredNotification(Player player, Component text)
    {
        if (getDisguiseStateFor(player) == null)
            player.sendActionBar(text);
        else
            player.sendMessage(MessageUtils.prefixes(player, text));
    }

    @Override
    public PlayerMorphConfiguration getPlayerConfiguration(Player player)
    {
        return data.getPlayerConfiguration(player);
    }

    @Override
    public void reloadConfiguration()
    {
        unMorphAll(true);

        data.reloadConfiguration();
    }

    @Override
    public void saveConfiguration()
    {
        data.saveConfiguration();
    }
    //endregion Implementation of IManagePlayerData
}