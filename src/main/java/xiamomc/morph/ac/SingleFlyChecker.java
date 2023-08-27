package xiamomc.morph.ac;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.abilities.impl.FlyAbility;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.misc.NmsRecord;
import xiamomc.morph.transforms.Transformer;
import xiamomc.morph.transforms.easings.Easing;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Bindables.Bindable;

import java.util.Map;

public class SingleFlyChecker extends MorphPluginObject implements IFlyChecker
{
    private final FlyAbility bindingAbility;

    public SingleFlyChecker(FlyAbility bindingAbility)
    {
        this.bindingAbility = bindingAbility;
    }

    @Initializer
    private void load(MorphConfigManager configManager)
    {
        configManager.bind(debugOutput, ConfigOption.DEBUG_OUTPUT);
    }

    private final Bindable<Boolean> debugOutput = new Bindable<>(false);

    @Resolved(shouldSolveImmediately = true)
    private MorphManager manager;

    private final Map<Player, FlyMeta> playerFlyMeta = new Object2ObjectArrayMap<>();

    @Override
    public void onEvent(PlayerMoveEvent e)
    {
        var player = e.getPlayer();

        var meta = playerFlyMeta.getOrDefault(player, null);

        // 提前设置和检查meta属性
        if (meta == null)
        {
            meta = new FlyMeta();
            playerFlyMeta.put(player, meta);
        }

        // region 计算最大位移
        var maxMovement = 0d;

        // Base multiplier
        var hasHorizonal = (e.getFrom().x() - e.getTo().x() != 0) || (e.getFrom().z() - e.getTo().z() != 0);
        var hasVertical = e.getFrom().y() - e.getTo().y() != 0;

        // 步进Clock
        meta.clock.step();

        if (!hasHorizonal && !hasVertical) return;

        // 获取配置的飞行速度
        var spd = bindingAbility.getTargetFlySpeed(manager.getDisguiseStateFor(player).getDisguiseIdentifier());

        // 设置meta中的移动属性
        if (hasHorizonal) maxMovement += 5.4444;
        if (hasVertical) maxMovement += hasHorizonal ? 1.1704 : 3.75;

        var playerSprinting = player.isSprinting();
        var usingRiptide = player.isRiptiding();

        meta.wasSprinting = meta.isSprinting;
        meta.isSprinting = playerSprinting;

        // 疾行倍率
        if (meta.isSprinting != meta.wasSprinting)
        {
            //logger.info("Do change transform");
            meta.flyMultTransform = Transformer.transform(meta.flyMult, playerSprinting ? 1d : 0d, (playerSprinting ? 15 : 25) * 50L, Easing.Plain, meta.clock);

            // 标记为已中断来避免Transformer重复更新同一帧
            meta.flyMultTransform.abort();
        }

        if (meta.flyMultTransform != null)
            Transformer.update(meta.flyMultTransform, true);

        // 激流倍率
        meta.wasRiptiding = meta.isRiptiding;
        meta.isRiptiding = usingRiptide;

        if (meta.wasRiptiding != meta.isRiptiding)
        {
            meta.riptideMultTransform = Transformer.transform(meta.riptideMult, usingRiptide ? 7.4d : 0d, (usingRiptide ? 0 : 55) * 50L, Easing.Plain, meta.clock);
            meta.riptideMultTransform.abort();
        }

        if (meta.riptideMultTransform != null)
            Transformer.update(meta.riptideMultTransform, true);

        // 用疾行或激流倍率倍增到最大允许的移动距离上
        maxMovement *= Math.max(1, 1 + Math.max(meta.flyMult.get(), meta.riptideMult.get()));

        // endregion 计算最大位移

        // 取得此玩家在1tick内最多可以飞行的距离
        // 计算方法为 【配置的飞行速度】x【此事件最大的移动距离】
        var threshold = spd * maxMovement;

        var distanceDelta = e.getFrom().distance(e.getTo());

        var diff = distanceDelta - threshold ; //Math.abs(travelled - spd * c);

        if (debugOutput.get())
        {
            var debugLine = "%s 本次 %.5f 阈值 %.5f 差异 %.5f 疾跑倍率 %s 激流倍率 %s 当前Tick %s 时钟Tick %s".formatted(player.getName(), distanceDelta, threshold, diff, meta.flyMult.get(), meta.riptideMult.get(), plugin.getCurrentTick(), meta.clock.getCurrentTimeTicks());

            if (diff > 0.01)
                logger.warn(debugLine);
            else
                logger.info(debugLine);
        }

        if (diff > 0.01)
        {
            if (meta.ignoreNext > 0)
            {
                if (debugOutput.get())
                    logger.info("忽略此移动，因为ignoreNext是" + meta.ignoreNext);

                meta.ignoreNext--;
                return;
            }

            bindingAbility.ignoreNextTeleport = true;
            var loc = meta.lastLegalPosition == null ? e.getFrom() : meta.lastLegalPosition;
            player.teleport(loc);
        }
    }

    @Override
    public void setLastLegalLocation(Player player, Location loc, boolean ignoreNextMovement)
    {
        var meta = playerFlyMeta.getOrDefault(player, new FlyMeta());
        meta.lastLegalPosition = loc;

        if (ignoreNextMovement)
            meta.ignoreNext += 5;
    }

    @Override
    public void dropMeta(Player player)
    {
        playerFlyMeta.remove(player);
    }
}
