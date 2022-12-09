package xiamomc.morph.misc;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.EnumHand;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R2.block.CraftBlock;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.BeaconInventory;
import org.bukkit.inventory.EquipmentSlot;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Bindables.Bindable;

import java.util.Map;
import java.util.function.Predicate;

public class PlayerOperationSimulator extends MorphPluginObject
{
    private final Map<Player, BlockDestroyHandler> playerHandlerMap = new Object2ObjectOpenHashMap<>();

    @Initializer
    private void load(MorphConfigManager config)
    {
        this.addSchedule(c -> update(), 1);

        config.bind(destroyTimeout, ConfigOption.REVERSE_DESTROY_TIMEOUT);
    }

    private final Bindable<Integer> destroyTimeout = new Bindable<>(40);

    private void update()
    {
        var list = new Object2ObjectOpenHashMap<>(playerHandlerMap);

        var currentTick = plugin.getCurrentTick();

        list.forEach((p, i) ->
        {
            if (!p.isOnline())
            {
                playerHandlerMap.remove(p);
                return;
            }

            //移除不需要的Info
            if (currentTick - i.getLastUpdate() >= destroyTimeout.get())
            {
                i.setProgress(-1, currentTick);

                playerHandlerMap.remove(p);
            }
        });

        this.addSchedule(c -> update(), 5);
    }

    private final float blockReachDistance = 4.5f;
    private final float entityReachDistance = 3f;

    /**
     * 模拟玩家左键
     *
     * @param player 目标玩家
     * @param targetHand 目标手（主手/副手）
     * @return 操作是否成功
     */
    public boolean simulateLeftClick(Player player, EquipmentSlot targetHand)
    {
        if (targetHand != EquipmentSlot.HAND && targetHand != EquipmentSlot.OFF_HAND)
            throw new IllegalArgumentException("The given slot is neither MainHand or OffHand");

        //logger.warn("模拟左键！");
        //Thread.dumpStack();

        if (targetHand == EquipmentSlot.OFF_HAND)
        {
            //副手模拟未实现
            return false;
        }

        //获取正在看的方块或实体
        var eyeLoc = player.getEyeLocation();
        var traceResult = player.getWorld().rayTrace(eyeLoc, eyeLoc.getDirection(), blockReachDistance,
                FluidCollisionMode.NEVER, false, 0d, Predicate.not(Predicate.isEqual(player)));

        var targetBlock = traceResult == null ? null : traceResult.getHitBlock();
        var targetEntity = traceResult == null ? null : traceResult.getHitEntity();

        //获取方块破坏信息
        var destroyHandler = playerHandlerMap.getOrDefault(player, null);

        if ((targetBlock == null || targetEntity != null) && destroyHandler != null)
            destroyHandler.changeBlock(null);

        if (targetEntity != null)
        {
            if (player.getLocation().distance(targetEntity.getLocation()) > entityReachDistance)
                return true;

            player.attack(targetEntity);

            return true;
        }

        if (targetBlock == null)
            return true;

        //初始化destoryInfo
        if (destroyHandler == null)
        {
            destroyHandler = new BlockDestroyHandler(targetBlock, 0, player);
            playerHandlerMap.put(player, destroyHandler);
        }

        //变更方块
        if (!targetBlock.equals(destroyHandler.getBlock()))
            destroyHandler.changeBlock(targetBlock);

        //NMS
        var nmsWorld = destroyHandler.getNmsWorld();
        var nmsBlock = destroyHandler.getNmsBlock();
        var nmsPlayer = ((CraftPlayer) player).getHandle();

        assert nmsBlock != null;
        assert nmsWorld != null;

        //AbstractBlockState#onBlockBreakStart(World world, BlockPos pos, PlayerEntity player)
        nmsBlock.a(nmsWorld, ((CraftBlock) targetBlock).getPosition(), nmsPlayer);

        //AbstractBlockState#calcBlockBreakingDelta(PlayerEntity player, BlockView world, BlockPos pos)
        var delta = nmsBlock.a(nmsPlayer, nmsWorld, ((CraftBlock) targetBlock).getPosition());

        //设置破坏进度
        var val = destroyHandler.getProgress();
        val += delta;

        destroyHandler.setProgress(val, plugin.getCurrentTick());

        return true;
    }

    /**
     * 模拟玩家右键
     *
     * @param player 目标玩家
     * @param targetHand 目标手（主手/副手）
     * @return 操作是否成功
     */
    public boolean simulateRightClick(Player player, EquipmentSlot targetHand)
    {
        if (targetHand != EquipmentSlot.HAND && targetHand != EquipmentSlot.OFF_HAND)
            throw new IllegalArgumentException("The given slot is neither MainHand or OffHand");

        //logger.warn("正在模拟右键！");

        //获取正在看的方块或实体
        var eyeLoc = player.getEyeLocation();
        var traceResult = player.getWorld().rayTrace(eyeLoc, eyeLoc.getDirection(), blockReachDistance,
                FluidCollisionMode.NEVER, false, 0d, Predicate.not(Predicate.isEqual(player)));

        var targetBlock = traceResult == null ? null : traceResult.getHitBlock();
        var targetEntity = traceResult == null ? null : traceResult.getHitEntity();

        if (targetEntity != null && targetEntity.getLocation().distance(player.getLocation()) > entityReachDistance)
            targetEntity = null;

        //Player in fabric mojang mappings
        var playerHumanHandle = ((CraftHumanEntity) player).getHandle();

        //ServerPlayer in mojang mappings
        var playerHandle = ((CraftPlayer) player).getHandle();

        //ServerLevel in mojang mappings
        var worldHandle = ((CraftWorld)player.getWorld()).getHandle();

        //GameMode in fabric mojang mappings
        var mgr = playerHandle.d;

        var hand = targetHand == EquipmentSlot.HAND ? EnumHand.a : EnumHand.b;
        var item = player.getEquipment().getItem(targetHand);

        var pluginManager = Bukkit.getPluginManager();

        //如果目标实体不是null，则和实体互动
        if (targetEntity != null)
        {
            var entityHandle = ((CraftEntity)targetEntity).getHandle();

            //获取目标位置
            var hitPos = traceResult.getHitPosition();

            //获取互动位置
            hitPos.subtract(targetEntity.getLocation().toVector());

            var vec = new Vec3D(hitPos.getX(), hitPos.getY(), hitPos.getZ());

            //先试试InteractAt
            //如果不成功，调用Interact
            //最后试试useItem
            var interactEntityEvent = new PlayerInteractEntityEvent(player, targetEntity);
            var interactAtEvent = new PlayerInteractAtEntityEvent(player, targetEntity, hitPos);
            pluginManager.callEvent(interactEntityEvent);
            pluginManager.callEvent(interactAtEvent);

            if (!interactAtEvent.isCancelled() && !interactEntityEvent.isCancelled())
            {
                return entityHandle.a(playerHandle, vec, EnumHand.a).a()
                        || playerHumanHandle.a(entityHandle, EnumHand.a).a()
                        || mgr.a(playerHandle, worldHandle, CraftItemStack.asNMSCopy(item), hand).a();
            }
            else
                return false;
        }

        boolean success = false;

        if (targetBlock != null)
        {
            var loc = ((CraftBlock) targetBlock).getPosition();

            EnumDirection targetBlockDirection = null;
            var bukkitFace = player.getTargetBlockFace(5);
            bukkitFace = bukkitFace == null ? BlockFace.SELF : bukkitFace;

            //BlockFace(Bukkit)转BlockFace(Minecraft)
            switch (bukkitFace)
            {
                case DOWN -> targetBlockDirection = EnumDirection.a;
                case UP -> targetBlockDirection = EnumDirection.b;
                case NORTH -> targetBlockDirection = EnumDirection.c;
                case SOUTH -> targetBlockDirection = EnumDirection.d;
                case WEST -> targetBlockDirection = EnumDirection.e;
                case EAST -> targetBlockDirection = EnumDirection.f;
                default -> logger.error("Unknown BlockFace: " + bukkitFace + ", will not attempt 'useItemOn'!");
            }

            if (targetBlockDirection != null)
            {
                var moving = new MovingObjectPositionBlock(
                        new Vec3D(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ()),
                        targetBlockDirection, loc, false);

                var event = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, item, targetBlock, bukkitFace);
                pluginManager.callEvent(event);

                //ServerPlayerGameMode.useItemOn()
                if (event.useInteractedBlock() != Event.Result.DENY)
                    success = mgr.a(playerHandle, worldHandle, CraftItemStack.asNMSCopy(item), hand, moving).a();
            }
        }

        //ServerPlayerGameMode.useItem()
        if (!success)
        {
            var event = new PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, item, null, BlockFace.SELF);
            pluginManager.callEvent(event);

            if (event.useItemInHand() != Event.Result.DENY)
                return mgr.a(playerHandle, worldHandle, CraftItemStack.asNMSCopy(item), hand).a();
            else
                return false;
        }
        else
            return true;
    }

}
