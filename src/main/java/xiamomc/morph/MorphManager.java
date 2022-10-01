package xiamomc.morph;

import com.google.gson.Gson;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.*;
import me.libraryaddict.disguise.disguisetypes.watchers.ArmorStandWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
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
    private final List<DisguisingInfo> disguisedPlayers = new ArrayList<>();

    private final List<DisguiseInfo> cachedInfos = new ArrayList<>();

    private MorphConfiguration morphConfiguration;

    private final Gson gson = new Gson();

    @Resolved
    private MorphPlugin plugin;

    @Initializer
    private void load()
    {
        this.addSchedule(c -> update());

        reloadConfiguration();
    }

    private void reloadConfiguration()
    {
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

            var msg = Component.text("不知道如何使用伪装? 发送 /morphhelp 即可查看！");
            player.sendMessage(MessageUtils.prefixes(msg));

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

        player.sendActionBar(Component.text("✔ 已解锁")
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

        sourcePlayer.sendActionBar(Component.text("✔ 已解锁" + targtPlayer.getName() + "的伪装").color(NamedTextColor.GREEN));

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
            var p = i.player;

            var disg = DisguiseAPI.getDisguise(p);
            if (!i.disguise.equals(disg))
            {
                disguisedPlayers.remove(i);
                continue;
            }

            var watcher = disg.getWatcher();
            if (disg.isPlayerDisguise())
            {
                watcher.setSneaking(p.isSneaking() && !p.isFlying());
                watcher.setFlyingWithElytra(p.isGliding());
                watcher.setSprinting(p.isSprinting());
            }

            var msg = Component.translatable("正伪装为").append(i.displayName);

            p.sendActionBar(MessageUtils.prefixes(msg));
        }

        var requests = new ArrayList<>(this.requests);
        for (var r : requests)
        {
            r.ticksRemain -= 1;
            if (r.ticksRemain <= 0) this.requests.remove(r);
        }

        this.addSchedule(c -> update());
    }

    //缓存伪装信息
    public DisguiseInfo getDisguiseInfo(EntityType type)
    {
        if (this.cachedInfos.stream().noneMatch(o -> o.equals(type)))
            cachedInfos.add(new DisguiseInfo(type));

        return cachedInfos.stream().filter(o -> o.equals(type)).findFirst().get();
    }

    public DisguiseInfo getDisguiseInfo(Player player)
    {
        if (this.cachedInfos.stream().noneMatch(o -> o.equals(player.getName())))
            cachedInfos.add(new DisguiseInfo(player.getName()));

        return cachedInfos.stream().filter(o -> o.equals(player.getName())).findFirst().get();
    }

    //region 玩家伪装相关

    /**
     * 将玩家伪装成指定的实体类型
     * @param player 目标玩家
     * @param entityType 目标实体类型
     */
    public void morph(Player player, EntityType entityType)
    {
        var targetedEntity = player.getTargetEntity(3);
        Disguise constructedDisguise = null;

        //如果正在看的实体和目标伪装类型一样，那么优先采用
        if (targetedEntity != null && targetedEntity.getType() == entityType && !targetedEntity.isDead())
        {
            constructedDisguise = DisguiseAPI.constructDisguise(targetedEntity, true, true);
        }
        else //如果没有，则正常构建
        {
            targetedEntity = null;

            //不要构建玩家类型的伪装
            if (entityType == EntityType.PLAYER) return;

            constructedDisguise = new MobDisguise(DisguiseType.getType(entityType));
        }

        postConstructDisguise(player, targetedEntity, constructedDisguise, entityType, null);

        DisguiseAPI.disguiseEntity(player, constructedDisguise);
    }

    /**
     * 将玩家伪装成指定的实体
     * @param sourcePlayer 目标玩家
     * @param entity 目标实体
     */
    public void morph(Player sourcePlayer, Entity entity)
    {
        Disguise constructedDisguise = null;

        //检查目标实体有没有伪装
        if (DisguiseAPI.isDisguised(entity))
            constructedDisguise = DisguiseAPI.getDisguise(entity);
        else
            constructedDisguise = DisguiseAPI.constructDisguise(entity);

        postConstructDisguise(sourcePlayer, entity, constructedDisguise);

        DisguiseAPI.disguiseEntity(sourcePlayer, constructedDisguise);
    }

    /**
     * 将玩家伪装成指定的玩家
     * @param sourcePlayer 发起玩家
     * @param targetPlayerName 目标玩家的玩家名
     */
    public void morph(Player sourcePlayer, String targetPlayerName)
    {
        var disguise = new PlayerDisguise(targetPlayerName);

        postConstructDisguise(sourcePlayer, null, disguise, null, targetPlayerName);

        DisguiseAPI.disguiseEntity(sourcePlayer, disguise);
    }

    public void unMorphAll()
    {
        disguisedPlayers.forEach(i -> unMorph(i.player));
    }

    public void unMorph(Player player)
    {
        if (!DisguiseAPI.isDisguised(player)) return;

        var targetInfoOptional = disguisedPlayers.stream().filter(i -> i.player.getUniqueId().equals(player.getUniqueId())).findFirst();
        if (targetInfoOptional.isEmpty())
            return;

        var info = targetInfoOptional.get();
        var disguise = info.disguise;
        disguise.removeDisguise(player);

        player.sendMessage(MessageUtils.prefixes(Component.text("已取消伪装")));
        player.sendActionBar(Component.empty());
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
        var watcher = disguise.getWatcher();

        var targetType = type == null ? disguise.getType().getEntityType() : type;

        watcher.setInvisible(false);

        //workaround: 伪装已死亡的LivingEntity
        if (targetEntity instanceof LivingEntity living && living.getHealth() <= 0)
            ((LivingWatcher)watcher).setHealth(1);

        //workaround: 玩家伪装副手问题
        ItemStack offhandItemStack = null;

        if (targetEntity instanceof Player targetPlayer)
            offhandItemStack = targetPlayer.getInventory().getItemInOffHand();

        if (targetEntity instanceof ArmorStand armorStand)
            offhandItemStack = armorStand.getItem(EquipmentSlot.OFF_HAND);

        if (offhandItemStack != null) watcher.setItemInOffHand(offhandItemStack);

        //盔甲架加上手臂
        if (disguise.getType().equals(DisguiseType.ARMOR_STAND))
            ((ArmorStandWatcher) watcher).setShowArms(true);

        //禁用actionBar
        DisguiseAPI.setActionBarShown(sourcePlayer, false);

        //添加到disguisedPlayers
        var info = new DisguisingInfo();
        info.player = sourcePlayer;
        info.displayName = disguise.isPlayerDisguise()
                ? Component.text((targetEntity == null ? targetPlayerName : targetEntity.getName()))
                : Component.translatable(targetType.translationKey());
        info.disguise = disguise;

        if (sourcePlayer.getVehicle() != null)
        {
            info.startSitting = true;
            sourcePlayer.sendMessage(MessageUtils.prefixes(Component.text("您将在起身后看到自己的伪装")));
        }

        disguisedPlayers.add(info);
    }

    @Nullable
    public DisguisingInfo getPlayerDisguisingInfo(Player player)
    {
        return this.disguisedPlayers.stream()
                .filter(i -> i.player.getUniqueId() == player.getUniqueId())
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
            source.sendMessage(MessageUtils.prefixes(Component.text("你已经向" + target + "发送过一个请求了")));
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

        target.sendMessage(MessageUtils.prefixes(msg));

        source.sendMessage(MessageUtils.prefixes(Component.translatable("请求已发送！对方将有1分钟的时间来接受")));
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
            source.sendMessage(MessageUtils.prefixes(Component.text("未找到目标请求，可能已经过期？")));
            return;
        }

        var req = match.get();
        req.ticksRemain = -1;

        addNewPlayerMorphToPlayer(target, source);
        addNewPlayerMorphToPlayer(source, target);

        target.sendMessage(MessageUtils.prefixes(Component.text("成功与" + source.getName() + "交换！")));
        source.sendMessage(MessageUtils.prefixes(Component.text("成功与" + target.getName() + "交换！")));
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
            source.sendMessage(MessageUtils.prefixes(Component.text("未找到目标请求，可能已经过期？")));

            //"未找到目标请求，可能已经过期？"
            return;
        }

        var req = match.get();
        req.ticksRemain = -1;

        var msg = Component.text("请求已拒绝");

        target.sendMessage(MessageUtils.prefixes(Component.text("发往" + source.getName() + "的").append(msg)));
        source.sendMessage(MessageUtils.prefixes(Component.text("来自" + target.getName() + "的").append(msg)));
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