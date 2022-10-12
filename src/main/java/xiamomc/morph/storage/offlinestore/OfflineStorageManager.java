package xiamomc.morph.storage.offlinestore;

import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.interfaces.IManageOfflineStates;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.storage.MorphJsonBasedStorage;

import java.util.List;
import java.util.UUID;

public class OfflineStorageManager extends MorphJsonBasedStorage<OfflineStates> implements IManageOfflineStates
{
    @Override
    protected @NotNull String getFileName()
    {
        return "offline_store.json";
    }

    @Override
    protected @NotNull OfflineStates createDefault()
    {
        return new OfflineStates();
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
        var targetState = storingObject.disguiseStates.stream().filter(s -> s.playerUUID.equals(uuid)).findFirst();

        if (targetState.isPresent())
        {
            var state = targetState.get();
            storingObject.disguiseStates.remove(state);

            if (state.disguiseData != null)
            {
                try
                {
                    state.disguise = DisguiseParser.parseDisguise(state.disguiseData);
                }
                catch (Throwable e)
                {
                    Logger.warn("无法从数据创建伪装：" + e.getMessage());
                    e.printStackTrace();
                }
            }

            saveConfiguration();

            return state;
        }

        return null;
    }
}
