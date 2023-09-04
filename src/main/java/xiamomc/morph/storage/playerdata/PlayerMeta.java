package xiamomc.morph.storage.playerdata;

import com.google.gson.annotations.Expose;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.misc.DisguiseMeta;
import xiamomc.morph.utilities.DisguiseUtils;

import java.util.UUID;

public class PlayerMeta
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
    private ObjectArrayList<DisguiseMeta> unlockedDisguises = new ObjectArrayList<>();

    private boolean disguiseListLocked = false;

    public ObjectArrayList<DisguiseMeta> getUnlockedDisguises()
    {
        return disguiseListLocked
                ? new ObjectArrayList<>(unlockedDisguises)
                : unlockedDisguises;
    }

    public void setUnlockedDisguises(ObjectArrayList<DisguiseMeta> newList)
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

    public void addDisguise(DisguiseMeta info)
    {
        unlockedDisguiseIdentifiers.add(DisguiseUtils.asString(info));
        unlockedDisguises.add(info);
    }

    public void removeDisguise(DisguiseMeta info)
    {
        unlockedDisguiseIdentifiers.remove(DisguiseUtils.asString(info));
        unlockedDisguises.remove(info);
    }

    /**
     * 此玩家解锁的所有伪装（原始数据）
     */
    @Expose
    private ObjectArrayList<String> unlockedDisguiseIdentifiers = new ObjectArrayList<>();

    public ObjectArrayList<String> getUnlockedDisguiseIdentifiers()
    {
        return disguiseListLocked
                ? new ObjectArrayList<>(unlockedDisguiseIdentifiers)
                : unlockedDisguiseIdentifiers;
    }

    public void setUnlockedDisguiseIdentifiers(ObjectArrayList<String> newList)
    {
        if (disguiseListLocked) throw new IllegalStateException("伪装列表已被锁定，不能设置");

        unlockedDisguiseIdentifiers = newList;
    }

    /**
     * 伪装是否对自身可见？
     */
    @Expose
    public boolean showDisguiseToSelf = false;

    /**
     * 是否显示过一次自身可见提示？
     */
    @Expose
    public boolean shownDisplayToSelfHint = false;

    @Expose
    public boolean shownServerSkillHint;

    @Expose
    public boolean shownClientSkillHint;

    @Expose
    public boolean shownMorphHint;

    @Expose
    public boolean shownMorphClientHint;

    @Expose
    public boolean shownClientSuggestionMessage;
}
