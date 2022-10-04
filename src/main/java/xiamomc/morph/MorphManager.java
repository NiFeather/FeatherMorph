package xiamomc.morph;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.*;
import me.libraryaddict.disguise.disguisetypes.watchers.ArmorStandWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.utilities.DisguiseValues;
import me.libraryaddict.disguise.utilities.reflection.FakeBoundingBox;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.misc.*;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MorphManager extends MorphPluginObject
{
    /**
     * 变成其他玩家的玩家
     * 因为插件限制，需要每tick更新下蹲和疾跑状态
     */
    private final List<DisguiseState> disguisedPlayers = new ArrayList<>();

    private final List<DisguiseInfo> cachedInfos = new ArrayList<>();

    private MorphConfiguration morphConfiguration;

    private final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    @Resolved
    private MorphPlugin plugin;

    @Initializer
    private void load()
    {
        this.addSchedule(c -> update());

        reloadConfiguration();
    }

    public void reloadConfiguration()
    {
        unMorphAll();

        //加载JSON配置
        MorphConfiguration targetConfiguration = null;
        var success = false;

        try ( var jsonStream = new InputStreamReader(new FileInputStream(configurationFile)) )
        {
            targetConfiguration = gson.fromJson(jsonStream, MorphConfiguration.class);
            success = true;
        }
        catch (IOException e)
        {
            Logger.warn("无法加载JSON配置：" + e.getMessage());
            e.printStackTrace();
        }

        if (targetConfiguration == null) targetConfiguration = new MorphConfiguration();

        morphConfiguration = targetConfiguration;
        if (success) saveConfiguration();
    }

    public List<DisguiseState> getDisguisedPlayers()
    {
        return new ArrayList<>(disguisedPlayers);
    }

    //region 配置

    private final File configurationFile = new File("/dev/shm/test");

    private void saveConfiguration()
    {
        try
        {
            var jsonString = gson.toJson(morphConfiguration);

            if (configurationFile.exists()) configurationFile.delete();

            configurationFile.createNewFile();

            try ( var stream = new FileOutputStream(configurationFile) )
            {
                stream.write(jsonString.getBytes(StandardCharsets.UTF_8));
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private PlayerMorphConfiguration getPlayerConfiguration(Player player)
    {
        var valueOptional = morphConfiguration.playerMorphConfigurations
                .stream().filter(c -> c.uniqueId.equals(player.getUniqueId())).findFirst();

        if (valueOptional.isPresent()) return valueOptional.get();
        else
        {
            var newInstance = new PlayerMorphConfiguration();
            newInstance.uniqueId = player.getUniqueId();
            newInstance.shownTutorialOnce = false;
            newInstance.unlockedDisguises = new ArrayList<>();

            var msg = Component.text("不知道如何使用伪装? 发送 /mmorph help 即可查看！");
            player.sendMessage(MessageUtils.prefixes(player, msg));

            morphConfiguration.playerMorphConfigurations.add(newInstance);
            return newInstance;
        }
    }

    public void addNewMorphToPlayer(Player player, Entity entity)
    {
        var playerConfiguration = getPlayerConfiguration(player);
        var info = this.getDisguiseInfo(entity.getType());

        if (playerConfiguration.unlockedDisguises.stream().noneMatch(c -> c.equals(info)))
        {
            playerConfiguration.unlockedDisguises.add(info);
            saveConfiguration();
        }
        else return;

        sendMorphAcquiredNotification(player,
                Component.text("✔ 已解锁")
                        .append(Component.translatable(entity.getType().translationKey()))
                        .append(Component.text("的伪装")).color(NamedTextColor.GREEN));
    }

    public void addNewPlayerMorphToPlayer(Player sourcePlayer, Player targtPlayer)
    {
        var playerConfiguration = getPlayerConfiguration(sourcePlayer);

        if (playerConfiguration.unlockedDisguises.stream().noneMatch(c -> c.equals(targtPlayer.getName())))
            playerConfiguration.unlockedDisguises.add(this.getDisguiseInfo(targtPlayer));
        else
            return;

        sendMorphAcquiredNotification(sourcePlayer,
                Component.text("✔ 已解锁" + targtPlayer.getName() + "的伪装").color(NamedTextColor.GREEN));

        saveConfiguration();
    }

    public ArrayList<DisguiseInfo> getAvaliableDisguisesFor(Player player)
    {
        return getPlayerConfiguration(player).unlockedDisguises;
    }

    //endregion 配置

    private void update()
    {
        var infos = new ArrayList<>(disguisedPlayers);
        for (var i : infos)
        {
            var p = i.getPlayer();

            if (!p.isOnline()) continue;

            var disg = DisguiseAPI.getDisguise(p);
            if (!i.getDisguise().equals(disg))
            {
                Logger.warn("removing: " + p + " :: " + i.getDisguise() + " <-> " + disg);
                disguisedPlayers.remove(i);
                continue;
            }

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

    /**
     * 更新伪装状态
     * @param player 目标玩家
     * @param info 伪装信息
     */
    private void updateDisguise(@NotNull Player player, @NotNull DisguiseState info)
    {
        var disguise = info.getDisguise();
        var watcher = disguise.getWatcher();

        //更新actionbar信息
        player.sendActionBar(MessageUtils.prefixes(player, Component.translatable("正伪装为").append(info.getDisplayName())));

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
    }

    /**
     * 获取包含某一EntityType的伪装信息
     * @param type 目标实体类型
     * @return 伪装信息
     */
    public DisguiseInfo getDisguiseInfo(EntityType type)
    {
        if (this.cachedInfos.stream().noneMatch(o -> o.equals(type)))
            cachedInfos.add(new DisguiseInfo(type));

        return cachedInfos.stream().filter(o -> o.equals(type)).findFirst().get();
    }

    /**
     * 获取包含某一玩家的伪装信息
     * @param player 玩家名
     * @return 伪装信息
     * @apiNote 要获取玩家正在伪装的状态，请使用getDisguiseStateFor(player)
     */
    public DisguiseInfo getDisguiseInfo(Player player)
    {
        if (this.cachedInfos.stream().noneMatch(o -> o.equals(player.getName())))
            cachedInfos.add(new DisguiseInfo(player.getName()));

        return cachedInfos.stream().filter(o -> o.equals(player.getName())).findFirst().get();
    }

    private void sendMorphAcquiredNotification(Player player, Component text)
    {
        if (disguisedPlayers.stream().noneMatch(i -> i.getPlayerUniqueID().equals(player.getUniqueId())))
            player.sendActionBar(text);
        else
            player.sendMessage(MessageUtils.prefixes(player, text));
    }

    //region 玩家伪装相关

    /**
     * 将玩家伪装成指定的实体类型
     * @param player 目标玩家
     * @param entityType 目标实体类型
     */
    public void morphEntityType(Player player, EntityType entityType)
    {
        var targetedEntity = player.getTargetEntity(5);
        Disguise constructedDisguise = null;

        //不要构建玩家类型的伪装
        if (entityType == EntityType.PLAYER) return;

        constructedDisguise = new MobDisguise(DisguiseType.getType(entityType));

        postConstructDisguise(player, targetedEntity, constructedDisguise, entityType, null);

        DisguiseAPI.disguiseEntity(player, constructedDisguise);
    }

    /**
     * 将玩家伪装成指定的实体
     * @param sourcePlayer 目标玩家
     * @param entity 目标实体
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
     * @param sourcePlayer 发起玩家
     * @param targetPlayerName 目标玩家的玩家名
     */
    public void morphPlayer(Player sourcePlayer, String targetPlayerName)
    {
        var disguise = new PlayerDisguise(targetPlayerName);

        postConstructDisguise(sourcePlayer, null, disguise, null, targetPlayerName);

        DisguiseAPI.disguiseEntity(sourcePlayer, disguise);
    }

    /**
     * 取消所有玩家的伪装
     */
    public void unMorphAll()
    {
        disguisedPlayers.forEach(i -> unMorph(i.getPlayer()));
    }

    /**
     * 取消某一玩家的伪装
     * @param player 目标玩家
     */
    public void unMorph(Player player)
    {
        if (!DisguiseAPI.isDisguised(player)) return;

        var targetInfoOptional = disguisedPlayers.stream().filter(i -> i.getPlayerUniqueID().equals(player.getUniqueId())).findFirst();
        if (targetInfoOptional.isEmpty())
            return;
;
        targetInfoOptional.get().getDisguise().removeDisguise(player);

        player.sendMessage(MessageUtils.prefixes(player, Component.text("已取消伪装")));
        player.sendActionBar(Component.empty());

        spawnParticle(player, player.getLocation(), player.getWidth(), player.getHeight(), player.getWidth());
    }

    private void postConstructDisguise(Player sourcePlayer, Entity targetEntity, Disguise disguise)
    {
        this.postConstructDisguise(sourcePlayer, targetEntity, disguise, null, "");
    }

    /**
     * 构建好伪装之后要做的事
     * @param sourcePlayer 伪装的玩家
     * @param targetEntity 伪装的目标实体
     * @param disguise 伪装
     * @param type 实体类型
     * @param targetPlayerName 伪装的目标玩家名（仅在目标实体为玩家时可用）
     */
    private void postConstructDisguise(Player sourcePlayer, Entity targetEntity, Disguise disguise, EntityType type, String targetPlayerName)
    {
        //设置自定义数据用来跟踪
        disguise.addCustomData("XIAMO_MORPH", true);

        var watcher = disguise.getWatcher();

        var targetType = type == null ? disguise.getType().getEntityType() : type;

        //workaround: 伪装已死亡的LivingEntity
        if (targetEntity instanceof LivingEntity living && living.getHealth() <= 0)
            ((LivingWatcher)watcher).setHealth(1);

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

        //添加到disguisedPlayers
        var disguiseDisplayName = disguise.isPlayerDisguise()
                ? Component.text((targetEntity == null ? targetPlayerName : ((PlayerDisguise)disguise).getName()))
                : Component.translatable(targetType.translationKey());

        var info = new DisguiseState(sourcePlayer, disguiseDisplayName, disguise);

        if (sourcePlayer.getVehicle() != null)
            sourcePlayer.sendMessage(MessageUtils.prefixes(sourcePlayer, Component.text("您将在起身后看到自己的伪装")));

        disguisedPlayers.add(info);

        //显示粒子
        var cX = 0d;
        var cZ = 0d;
        var cY = 0d;

        //如果伪装成生物，则按照此生物的碰撞体积来
        if (disguise.isMobDisguise())
        {
            var mobDisguise = (MobDisguise)disguise;
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
        var xOffset = collX / 2;
        var zOffset = collZ / 2;

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

    //endregion 玩家伪装相关

    //region 玩家请求

    private final List<RequestInfo> requests = new ArrayList<>();

    /**
     * 发起请求
     * @param source 请求发起方
     * @param target 请求接受方
     */
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

    /**
     * 接受请求
     * @param source 请求接受方
     * @param target 请求发起方
     */
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

        addNewPlayerMorphToPlayer(target, source);
        addNewPlayerMorphToPlayer(source, target);

        target.sendMessage(MessageUtils.prefixes(target, Component.text("成功与" + source.getName() + "交换！")));
        source.sendMessage(MessageUtils.prefixes(source, Component.text("成功与" + target.getName() + "交换！")));
    }

    /**
     * 拒绝请求
     * @param source 请求接受方
     * @param target 请求发起方
     */
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

    /**
     * 获取目标为player的所有请求
     * @param player 目标玩家
     * @return 请求列表
     */
    public List<RequestInfo> getAvaliableRequestFor(Player player)
    {
        return requests.stream()
                .filter(t -> t.targetPlayer.getUniqueId().equals(player.getUniqueId()))
                .toList();
    }

    //endregion 玩家请求
}