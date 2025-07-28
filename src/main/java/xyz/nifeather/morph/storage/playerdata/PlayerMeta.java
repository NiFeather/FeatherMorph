package xyz.nifeather.morph.storage.playerdata;

import com.google.common.collect.ImmutableList;
import com.google.gson.annotations.Expose;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.utilities.DisguiseUtils;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PlayerMeta
{
    /**
     * 玩家的UUID
     */
    @Expose(serialize = false)
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
    private List<DisguiseMeta> unlockedDisguises = Collections.synchronizedList(new ObjectArrayList<>());

    @Unmodifiable
    public List<DisguiseMeta> getUnlockedDisguises()
    {
        return ImmutableList.copyOf(unlockedDisguises);
    }

    public void setUnlockedDisguises(ObjectArrayList<DisguiseMeta> newList)
    {
        unlockedDisguises = newList;
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
    private List<String> unlockedDisguiseIdentifiers = Collections.synchronizedList(new ObjectArrayList<>());

    /**
     * @apiNote Only use
     */
    @ApiStatus.Internal
    public void addUnlockedDisguiseIdentifier(List<String> list)
    {
        unlockedDisguiseIdentifiers.addAll(list);
    }

    @Unmodifiable
    public List<String> getUnlockedDisguiseIdentifiers()
    {
        return ImmutableList.copyOf(unlockedDisguiseIdentifiers);
    }

    public void setUnlockedDisguiseIdentifiers(List<String> newList)
    {
        unlockedDisguiseIdentifiers = newList;
    }

    @Override
    public String toString()
    {
        return "PlayerMeta{ UUID=%s, Name=%s }".formatted(this.uniqueId, this.playerName);
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
    @Deprecated(forRemoval = true)
    public boolean shownServerSkillHint;

    @Expose
    public boolean shownClientSkillHint;

    @Expose
    public boolean shownMorphHint;

    @Expose
    public boolean shownMorphClientHint;
}
