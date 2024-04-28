package xiamomc.morph.misc;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class BlockDestroyHandler
{
    public BlockDestroyHandler(@Nullable Block block, float progress, Player player)
    {
        this.progress = progress;

        setPlayer(player);
        changeBlock(block);
    }

    private Player player;
    private ServerPlayer nmsPlayer;

    private void setPlayer(Player player)
    {
        this.player = player;
        this.nmsPlayer = ((CraftPlayer) player).getHandle();
    }

    private float progress = -1;

    private long lastUpdate;

    //非秒破会有5tick的破坏延迟
    private long lastDestroy;

    public long getLastUpdate()
    {
        return lastUpdate;
    }

    /**
     * 设置当前方块的破坏进度
     *
     * @param newProgress 新进度 (0 ~ 1)
     * @param currentTick 当前Tick
     * @return 操作是否成功
     */
    public boolean setProgress(float newProgress, long currentTick)
    {
        var delta = newProgress - progress;
        var instant = delta > 1;

        //对于非秒破进度，每Tick限制只能设置一次
        if (!instant && currentTick - lastUpdate < 1)
            return false;

        //设置上次更新
        lastUpdate = currentTick;

        //如果进度一样，或者还在方块破坏CD
        //对于破坏CD，请见 https://minecraft.fandom.com/wiki/Breaking#Instant_breaking
        if (progress == newProgress || (currentTick - lastDestroy < 6 && delta > 0 && !instant))
            return false;

        progress = newProgress;

        if (block != null)
        {
            newProgress = newProgress > 1f ? -1 : newProgress;

            //进度大于1时设置破坏进度为-1避免客户端显示问题
            nmsWorld.destroyBlockProgress(nmsPlayer.getId(),
                    ((CraftBlock) block).getPosition(),
                    clamp(-1, 100, (int)(newProgress * 10F) - 1));
        }

        //进度大于1，视为破坏方块
        if (progress > 1)
        {
            lastDestroy = currentTick;

            if (block != null)
                player.breakBlock(block);

            changeBlock(null);
        }

        return true;
    }

    private int clamp(int min, int max, int val)
    {
        return val < min
                ? min
                : Math.min(val, max);
    }

    /**
     * 获取破坏进度
     *
     * @return 破坏进度，若返回 -1 则表示不可用（超出一段时间）
     */
    public float getProgress()
    {
        return progress;
    }

    @Nullable
    private Block block;

    /**
     * 获取目标方块
     *
     * @return 目标方块
     */
    @Nullable
    public Block getBlock()
    {
        return block;
    }

    private ServerLevel nmsWorld;

    /**
     * 获取NMS世界
     *
     * @return 如果方块为null，则返回null，否则返回{@link ServerLevel}
     */
    @Nullable
    public ServerLevel getNmsWorld()
    {
        return nmsWorld;
    }

    private BlockState nmsBlock;

    /**
     * 获取NMS方块
     *
     * @return 如果方块为null，则返回null，否则返回{@link BlockState}
     */
    @Nullable
    public BlockState getNmsBlock()
    {
        return nmsBlock;
    }

    /**
     * 更改当前目标方块
     *
     * @param block 新的目标方块
     */
    public void changeBlock(@Nullable Block block)
    {
        this.nmsBlock = null;
        this.nmsWorld = null;

        this.block = block;

        if (block != null)
        {
            this.nmsBlock = ((CraftBlock) block).getNMS();
            this.nmsWorld = ((CraftWorld) block.getWorld()).getHandle();
        }

        setProgress(0, lastUpdate + 2);
    }
}
