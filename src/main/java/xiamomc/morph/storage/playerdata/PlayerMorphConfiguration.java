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
    private ArrayList<DisguiseInfo> unlockedDisguises = new ArrayList<>();

    private boolean disguiseListLocked = false;

    public ArrayList<DisguiseInfo> getUnlockedDisguises()
    {
        return disguiseListLocked
                ? new ArrayList<>(unlockedDisguises)
                : unlockedDisguises;
    }

    public void setUnlockedDisguises(ArrayList<DisguiseInfo> newList)
    {
        if (disguiseListLocked) throw new IllegalStateException("伪装列表已被锁定，不能设置");

        unlockedDisguises = newList;
    }

    /**
     * 锁定伪装列表使之后的获取操作只能获得其副本
     */
    public void lockDisguiseList()
    {
        disguiseListLocked = true;
    }

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
    private ArrayList<String> unlockedDisguiseIdentifiers = new ArrayList<>();

    public ArrayList<String> getUnlockedDisguiseIdentifiers()
    {
        return disguiseListLocked
                ? new ArrayList<>(unlockedDisguiseIdentifiers)
                : unlockedDisguiseIdentifiers;
    }

    public void setUnlockedDisguiseIdentifiers(ArrayList<String> newList)
    {
        if (disguiseListLocked) throw new IllegalStateException("伪装列表已被锁定，不能设置");

        unlockedDisguiseIdentifiers = newList;
    }

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
