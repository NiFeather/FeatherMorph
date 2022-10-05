package xiamomc.morph.interfaces;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.misc.DisguiseInfo;
import xiamomc.morph.misc.PlayerMorphConfiguration;

import java.util.ArrayList;

public interface IManagePlayerData
{
    /**
     * 获取包含某一EntityType的伪装信息
     *
     * @param type 目标实体类型
     * @return 伪装信息
     */
    public DisguiseInfo getDisguiseInfo(EntityType type);

    /**
     * 获取包含某一玩家的玩家名的伪装信息
     *
     * @param playerName 目标玩家名
     * @return 伪装信息
     */
    public DisguiseInfo getDisguiseInfo(String playerName);

    /**
     * 获取某一玩家所有可用的伪装
     * @param player 目标玩家
     * @return 目标玩家拥有的伪装
     */
    public ArrayList<DisguiseInfo> getAvaliableDisguisesFor(Player player);

    /**
     * 添加新的实体伪装到某一玩家
     * @param player 要添加的玩家
     * @param entity 要添加给player的实体
     */
    public void addNewMorphToPlayer(Player player, Entity entity);

    /**
     * 添加新的玩家伪装到某一玩家
     * @param sourcePlayer 要添加的玩家
     * @param targtPlayer 要添加给sourcePlayer的玩家
     */
    public void addNewPlayerMorphToPlayer(Player sourcePlayer, Player targtPlayer);

    /**
     * 获取玩家的伪装配置
     * @param player 目标玩家
     * @return 伪装信息
     */
    public PlayerMorphConfiguration getPlayerConfiguration(Player player);

    public void reloadConfiguration();
}
