package xiamomc.morph.providers;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.*;
import net.minecraft.server.commands.data.CommandDataAccessorEntity;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.misc.DisguiseInfo;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.List;

public abstract class DisguiseProvider extends MorphPluginObject
{
    /**
     * 获取此DisguiseProvider的ID，此ID将被用于判断某个伪装是否属于此Provider
     *
     * @return ID
     */
    @NotNull
    public abstract String getIdentifier();

    /**
     * Gets all available disguise identifiers for this provider
     * @return A list containing available disguise identifiers for this provider
     */
    public abstract List<String> getAllAvailableDisguises();

    /**
     * 为某个玩家应用或更新伪装
     * @param player 目标玩家
     * @param disguiseInfo 伪装ID
     * @param targetEntity 玩家的目标实体(如果有), 可用来判断是否要复制伪装
     * @return 操作结果
     */
    @NotNull
    public abstract DisguiseResult morph(Player player, DisguiseInfo disguiseInfo, @Nullable Entity targetEntity);

    /**
     * 更新某个伪装的状态
     *
     * @param player 玩家
     * @param state 和玩家对应的{@link DisguiseState}
     * @return 操作是否成功
     */
    public abstract boolean updateDisguise(Player player, DisguiseState state);

    /**
     * 获取某个伪装的初始化指令
     * @param state 目标伪装
     * @return 要对客户端发送的指令列表
     */
    @NotNull
    public abstract List<String> getInitialSyncCommands(DisguiseState state);

    /**
     * 获取某个伪装的客户端预览ID
     * @param state 目标伪装
     * @return ID
     */
    public String getSelfViewIdentifier(DisguiseState state)
    {
        return state.getDisguiseIdentifier();
    }

    public boolean validForClient(DisguiseState state)
    {
        return false;
    }

    @Nullable
    public String getNbtCompound(DisguiseState state, Entity targetEntity)
    {
        if (targetEntity instanceof CraftLivingEntity craftEntity
            && canConstruct(getMorphManager().getDisguiseInfo(state.getDisguiseIdentifier()), targetEntity, null))
        {
            //nms
            var compund = new NBTTagCompound();

            var nmsEntity = craftEntity.getHandle();

            var entityDataObject = new CommandDataAccessorEntity(nmsEntity);

            //剔除不需要的nbt
            compund = cullNBT(entityDataObject.a());

            //StringNbtWriter
            var visitor = new StringTagVisitor();

            //StringNbtWriter#apply(NbtElement)
            return visitor.a((NBTBase) compund);
        }

        return null;
    }

    protected NBTTagCompound cullNBT(NBTTagCompound compound)
    {
        //compound.r() -> NBTCompound#remove()

        //common
        compound.r("UUID");
        compound.r("data");
        compound.r("Brain");
        compound.r("Motion");
        compound.r("palette");
        compound.r("Attributes");
        compound.r("Invulnerable");
        compound.r("DisabledSlots");

        //armor stand
        compound.r("ArmorItems");
        compound.r("HandItems");

        //player
        compound.r("Tags");
        compound.r("bukkit");
        compound.r("recipes");
        compound.r("Inventory");
        compound.r("abilities");
        compound.r("recipeBook");
        compound.r("EnderItems");
        compound.r("BukkitValues");
        compound.r("warden_spawn_tracker");
        compound.r("previousPlayerGameType");

        //paper, bukkit, spigot
        compound.r("Paper");
        compound.r("Paper.Origin");
        compound.r("Paper.OriginWorld");
        compound.r("Paper.SpawnReason");
        compound.r("Spigot.ticksLived");
        compound.r("Bukkit.updateLevel");
        compound.r("Bukkit.Aware");

        //villager
        compound.r("Offers");

        //misc
        compound.r("Pos");
        compound.r("WorldUUIDLeast");
        compound.r("WorldUUIDMost");
        compound.r("Rotation");

        return compound;
    }

    /**
     * 取消某个玩家的伪装
     * @param player 目标玩家
     * @return 操作是否成功
     */
    public boolean unMorph(Player player, DisguiseState state)
    {
        var disguise = state.getDisguise();

        return disguise.removeDisguise(player);
    }

    @Resolved
    private MorphManager morphs;

    protected MorphManager getMorphManager()
    {
        return morphs;
    }

    /**
     * 获取某一实体的伪装
     *
     * @param info 伪装信息
     * @param target 目标实体
     * @return 一个DisguiseResult，其isCopy永远为true。
     */
    @NotNull
    protected DisguiseResult getCopy(DisguiseInfo info, @Nullable Entity target)
    {
        if (target == null) return DisguiseResult.fail();

        boolean shouldClone = false;

        Disguise ourDisguise = null;
        Disguise theirDisguise = null;
        DisguiseState state = null;

        if (DisguiseAPI.isDisguised(target))
        {
            theirDisguise = DisguiseAPI.getDisguise(target);

            //如果玩家已伪装，则检查其目标伪装和我们想要的是否一致
            if (target instanceof Player targetPlayer)
            {
                state = morphs.getDisguiseStateFor(targetPlayer);

                if (state != null)
                {
                    var key = state.getDisguiseIdentifier();

                    //ID不一样则返回失败
                    if (!key.equals(info.getIdentifier())) return DisguiseResult.fail();
                }
            }

            shouldClone = canCopyDisguise(info, target, state, theirDisguise);
        }

        ourDisguise = shouldClone
                ? DisguiseAPI.getDisguise(target).clone()
                : canConstruct(info, target, state) ? DisguiseAPI.constructDisguise(target) : null;

        return ourDisguise == null
                ? DisguiseResult.fail()
                : DisguiseResult.success(ourDisguise, true);
    }

    /**
     * 如果不能复制，那么我们是否可以构建某个实体的伪装?
     *
     * @param info {@link DisguiseInfo}
     * @param targetEntity 目标实体
     * @param theirState 他们的{@link DisguiseState}，为null则代表他们不是玩家或没有通过MorphPlugin伪装
     * @return 是否允许此操作
     */
    public abstract boolean canConstruct(DisguiseInfo info, Entity targetEntity,
                                            @Nullable DisguiseState theirState);

    /**
     * 是否可以复制某个实体的伪装?
     *
     * @param info {@link DisguiseInfo}
     * @param targetEntity 目标实体
     * @param theirDisguise 他们目前应用的伪装
     * @param theirState 他们的{@link DisguiseState}，为null则代表他们不是玩家或没有通过MorphPlugin伪装
     * @return 是否允许此操作
     */
    protected abstract boolean canCopyDisguise(DisguiseInfo info, Entity targetEntity,
                                               @Nullable DisguiseState theirState, @NotNull Disguise theirDisguise);

    /**
     * 伪装后要做的事
     * @param state {@link DisguiseState}
     * @param targetEntity 目标实体
     */
    public void postConstructDisguise(DisguiseState state, @Nullable Entity targetEntity)
    {
    }

    /**
     * 获取某个伪装的显示名称
     *
     * @param disguiseIdentifier 伪装ID
     * @return 显示名称
     */
    public abstract Component getDisplayName(String disguiseIdentifier);
}
