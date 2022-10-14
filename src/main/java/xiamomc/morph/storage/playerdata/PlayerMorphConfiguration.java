package xiamomc.morph.storage.playerdata;

import com.google.gson.annotations.Expose;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.misc.DisguiseInfo;
import xiamomc.morph.misc.DisguiseUtils;

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
    public String playerName;

    /**
     * 此玩家解锁的所有伪装
     *
     * @apiNote 移除或添加伪装请使用addDisguise和removeDisguise
     */
    @Expose(serialize = false)
    public ArrayList<DisguiseInfo> unlockedDisguises = new ArrayList<>();

    public void addDisguise(DisguiseInfo info)
    {
        unlockedDisguiseIdentifiers.add(DisguiseUtils.asString(info));
        unlockedDisguises.add(info);
    }

    public void removeDisguise(DisguiseInfo info)
    {
        unlockedDisguiseIdentifiers.remove(DisguiseUtils.asString(info));
        unlockedDisguises.remove(info);
    }

    /**
     * 此玩家解锁的所有伪装（原始数据）
     */
    @Expose
    public ArrayList<String> unlockedDisguiseIdentifiers;

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
