package xiamomc.morph.interfaces;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.misc.DisguiseInfo;
import xiamomc.morph.storage.playerdata.PlayerMorphConfiguration;

public interface IManagePlayerData
{
    /**
     * 获取包含某一玩家的玩家名的伪装信息
     *
     * @param rawString 原始ID
     * @return 伪装信息
     * @apiNote 如果原始ID不是有效ID，则会返回null
     */
    @Nullable
    public DisguiseInfo getDisguiseInfo(String rawString);

    /**
     * 获取某一玩家所有可用的伪装
     * @param player 目标玩家
     * @return 目标玩家拥有的伪装
     */
    public ObjectArrayList<DisguiseInfo> getAvaliableDisguisesFor(Player player);

    /**
     * 将伪装授予某一玩家
     * @param player 要授予的玩家
     * @param disguiseIdentifier 伪装ID
     * @return 添加是否成功（伪装是否可用或玩家是否已经拥有目标伪装）
     */
    public boolean grantMorphToPlayer(Player player, String disguiseIdentifier);

    /**
     * 从某一玩家剥离伪装
     * @param player 要授予的玩家
     * @param disguiseIdentifier 伪装ID
     * @return 添加是否成功（伪装是否可用或玩家是否已经拥有目标伪装）
     */
    public boolean revokeMorphFromPlayer(Player player, String disguiseIdentifier);

    /**
     * 获取玩家的伪装配置
     * @param player 目标玩家
     * @return 伪装信息
     */
    public PlayerMorphConfiguration getPlayerConfiguration(OfflinePlayer player);

    public boolean reloadConfiguration();

    public boolean saveConfiguration();
}
