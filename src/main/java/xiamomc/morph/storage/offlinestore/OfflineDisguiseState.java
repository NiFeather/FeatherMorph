package xiamomc.morph.storage.offlinestore;

import com.google.gson.annotations.Expose;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class OfflineDisguiseState implements IOfflineState
{
    /**
     * 玩家的UUID
     */
    @Expose
    public UUID playerUUID;

    /**
     * 玩家名（用于显示）
     */
    @Expose
    public String playerName;

    /**
     * 伪装ID(minecraft:pig, player:Icalingua 这种)<br>
     * 只会在伪装数据不可用或者构建失败时使用
     */
    @Expose
    public String disguiseID;

    /**
     * 伪装数据，如果存在则优先使用这里的数据
     */
    @Expose
    public String disguiseData;

    /**
     * 要不要手动更新Pose
     */
    @Expose
    public boolean shouldHandlePose;

    /**
     * 是否在显示伪装物品
     */
    @Expose
    public boolean showingDisguisedItems;

    @Nullable
    @Expose(deserialize = false, serialize = false)
    public Disguise disguise;
}
