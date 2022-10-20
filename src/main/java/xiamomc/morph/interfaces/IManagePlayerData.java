package xiamomc.morph.interfaces;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.misc.DisguiseInfo;
import xiamomc.morph.storage.playerdata.PlayerMorphConfiguration;

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
     * @param rawString 原始ID
     * @return 伪装信息
     */
    public DisguiseInfo getDisguiseInfo(String rawString, boolean isPlayer);

    /**
     * 获取某一玩家所有可用的伪装
     * @param player 目标玩家
     * @return 目标玩家拥有的伪装
     */
    public ArrayList<DisguiseInfo> getAvaliableDisguisesFor(Player player);

    /**
     * 授予某一玩家某类实体的伪装
     * @param player 要授予的玩家
     * @param type 要添加给player的实体
     * @return 添加是否成功（此玩家先前是否已经拥有目标伪装）
     */
    public boolean grantMorphToPlayer(Player player, EntityType type);

    /**
     * 授予某一玩家其他玩家的伪装
     * @param sourcePlayer 要添加的玩家
     * @param targtPlayerName 要添加给sourcePlayer的玩家
     * @return 添加是否成功（此玩家先前是否已经拥有目标伪装）
     */
    public boolean grantPlayerMorphToPlayer(Player sourcePlayer, String targtPlayerName);

    /**
     * 授予某一玩家自定义伪装
     * @param player 要授予的玩家
     * @param disguiseName 自定义伪装的名称
     * @return 添加是否成功（伪装是否可用或玩家是否已经拥有目标伪装）
     */
    public boolean grantCustomMorphToPlayer(Player player, String disguiseName);

    public boolean revokeMorphFromPlayer(Player player, EntityType entityType);

    public boolean revokePlayerMorphFromPlayer(Player player, String playerName);
    public boolean revokeCustomMorphFromPlayer(Player player, String disguiseName);

    /**
     * 获取玩家的伪装配置
     * @param player 目标玩家
     * @return 伪装信息
     */
    public PlayerMorphConfiguration getPlayerConfiguration(Player player);

    public boolean reloadConfiguration();

    public boolean saveConfiguration();
}
