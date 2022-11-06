package xiamomc.morph.misc;

import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.block.state.IBlockData;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockDestroyHandler
{
    public BlockDestroyHandler(@Nullable Block block, float progress, Player player)
    {
        this.progress = progress;

        setPlayer(player);
        changeBlock(block);
    }

    private Player player;
    private EntityPlayer nmsPlayer;

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
     * @param newProgress 新进度
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
            //ServerWorld#setBlockBreakingInfo(int entityId, BlockPos pos, int progress)
            //nmsPlayer.aa() -> Entity#getId()
            //
            //进度大于1时设置破坏进度为-1避免客户端显示问题
            nmsWorld.a(nmsPlayer.aa(),
                    ((CraftBlock) block).getPosition(),
                    newProgress > 1f ? -1 : (int)(newProgress * 10F) - 1);
        }

        //进度大于1，视为破坏方块
        if (progress > 1)
        {
            lastDestroy = currentTick;
            player.breakBlock(block);

            changeBlock(null);
        }

        return true;
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

    private WorldServer nmsWorld;

    /**
     * 获取NMS世界
     *
     * @return 如果方块为null，则返回null，否则返回{@link WorldServer}
     * @apiNote <a href="https://maven.quiltmc.org/repository/release/org/quiltmc/quilt-mappings/1.19+build.1/quilt-mappings-1.19+build.1-javadoc.jar/net/minecraft/server/world/ServerWorld.html">
     *     WorldServer在Quilt mappings中的样子</a>，或许能帮助你查找想要的方法。
     */
    @Nullable
    public WorldServer getNmsWorld()
    {
        return nmsWorld;
    }

    private IBlockData nmsBlock;

    /**
     * 获取NMS方块
     *
     * @return 如果方块为null，则返回null，否则返回{@link IBlockData}
     * @apiNote <a href="https://maven.quiltmc.org/repository/release/org/quiltmc/quilt-mappings/1.19+build.1/quilt-mappings-1.19+build.1-javadoc.jar/net/minecraft/block/AbstractBlock.AbstractBlockState.html">
     *     IBlockData在Quilt mappings中的样子</a>，或许能帮助你查找想要的方法。
     */
    @Nullable
    public IBlockData getNmsBlock()
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
