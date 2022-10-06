package xiamomc.morph.storage.playerdata;

import com.google.gson.annotations.Expose;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.misc.DisguiseInfo;

import java.util.ArrayList;
import java.util.UUID;

public class PlayerMorphConfiguration
{
    /**
     * 玩家的UUID
     */
    @Expose
    public UUID uniqueId;

    /**
     * 浏览JSON时参考用的数据
     */
    @Expose
    @Nullable
    public String playerName = "Unknown";

    /**
     * 此玩家解锁的所有伪装
     */
    @Expose
    public ArrayList<DisguiseInfo> unlockedDisguises;

    /**
     * 是否已经显示过一次morphplayer合并的消息？
     */
    @Expose
    public boolean shownMorphPlayerMessageOnce;

    /**
     * 是否已经显示过一次伪装技能提示？
     */
    @Expose
    public boolean shownMorphAbilityHint;
}
