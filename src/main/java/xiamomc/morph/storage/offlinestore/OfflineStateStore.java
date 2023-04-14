package xiamomc.morph.storage.offlinestore;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.interfaces.IManageOfflineStates;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.storage.MorphJsonBasedStorage;

import java.util.List;
import java.util.UUID;

public class OfflineStateStore extends MorphJsonBasedStorage<OfflineStateContainer> implements IManageOfflineStates
{
    @Override
    protected @NotNull String getFileName()
    {
        return "offline_store.json";
    }

    @Override
    protected @NotNull OfflineStateContainer createDefault()
    {
        return new OfflineStateContainer();
    }

    @Override
    protected @NotNull String getDisplayName()
    {
        return "离线存储";
    }

    /**
     * 将一个玩家的DisguiseState推到存储里
     * @param state DisguiseState
     */
    public void pushDisguiseState(DisguiseState state)
    {
        if (storingObject.disguiseStates.stream().anyMatch(s -> s.playerUUID == state.getPlayerUniqueID()))
        {
            logger.warn("将放弃存储中已有的" + state.getPlayerUniqueID() + "条目...");
            storingObject.disguiseStates.removeIf(s -> s.playerUUID == state.getPlayerUniqueID());
        }

        storingObject.disguiseStates.add(state.toOfflineState());
    }

    /**
     * 获取所有可用的离线伪装存储
     * @return 存储列表
     */
    public List<OfflineDisguiseState> getAvaliableDisguiseStates()
    {
        return storingObject.disguiseStates;
    }

    /**
     * 从存储里取出离线State并从池里移除此State
     * @param uuid 玩家UUID
     * @return 离线State
     */
    @Nullable
    public OfflineDisguiseState popDisguiseState(UUID uuid)
    {
        var state = storingObject.disguiseStates.stream()
                .filter(s -> s.playerUUID.equals(uuid)).findFirst().orElse(null);

        if (state != null)
        {
            storingObject.disguiseStates.remove(state);

            if (state.disguiseData != null)
            {
                try
                {
                    //todo: 是否要保留离线伪装?
                    //state.disguise = DisguiseParser.parseDisguise(state.disguiseData);
                }
                catch (Throwable e)
                {
                    logger.warn("无法从数据创建伪装：" + e.getMessage());
                    e.printStackTrace();
                }
            }

            saveConfiguration();

            return state;
        }

        return null;
    }
}
