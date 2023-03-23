package xiamomc.morph.backends.libsdisg;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.mojang.authlib.GameProfile;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.*;
import me.libraryaddict.disguise.disguisetypes.watchers.*;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.DisguiseValues;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.reflection.FakeBoundingBox;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.backends.DisguiseWrapper;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.utilities.DisguiseUtils;
import xiamomc.pluginbase.Utilities.ColorUtils;

public class LibsDisguiseWrapper extends DisguiseWrapper<Disguise>
{
    public LibsDisguiseWrapper(@NotNull Disguise instance)
    {
        super(instance);

        this.watcher = instance.getWatcher();
    }

    private final FlagWatcher watcher;

    @Override
    public EntityEquipment getDisplayingEquipments()
    {
        return watcher.getEquipment();
    }

    @Override
    public void setDisplayingEquipments(EntityEquipment newEquipment)
    {
        watcher.setArmor(newEquipment.getArmorContents());
        watcher.setItemInMainHand(newEquipment.getItemInMainHand());
        watcher.setItemInOffHand(newEquipment.getItemInOffHand());

        invalidateCompound();
    }

    @Override
    public void toggleServerSelfView(boolean enabled)
    {
        instance.setSelfDisguiseVisible(enabled);
    }

    @Override
    public EntityType getEntityType()
    {
        return instance.getType().getEntityType();
    }

    @Override
    public Disguise copyInstance()
    {
        return instance.clone();
    }

    @Override
    public DisguiseWrapper<Disguise> clone()
    {
        var newWrapper = new LibsDisguiseWrapper(instance.clone());
        newWrapper.compoundTag.merge(this.compoundTag);

        return newWrapper;
    }

    /**
     * 返回此伪装的名称
     *
     * @return 伪装名称
     */
    @Override
    public String getDisguiseName()
    {
        return instance instanceof PlayerDisguise playerDisguise ? playerDisguise.getName() : instance.getDisguiseName();
    }

    @Override
    public void setDisguiseName(String name)
    {
        if(instance instanceof PlayerDisguise playerDisguise)
            playerDisguise.setName(name);
        else
            instance.setDisguiseName(name);

        invalidateCompound();
    }

    @Override
    public BoundingBox getBoundingBox()
    {
        FakeBoundingBox box;
        var mobDisguise = (MobDisguise) instance;
        var values = DisguiseValues.getDisguiseValues(instance.getType());

        if (!mobDisguise.isAdult() && values.getBabyBox() != null)
            box = values.getBabyBox();
        else
            box = values.getAdultBox();

        return new BoundingBox(0, 0, 0, box.getX(), box.getY(), box.getZ());
    }

    @Override
    public void setGlowingColor(ChatColor glowingColor)
    {
        watcher.setGlowColor(glowingColor);
    }

    @Override
    public void setGlowing(boolean glowing)
    {
        watcher.setGlowing(glowing);
    }

    @Override
    public ChatColor getGlowingColor()
    {
        return watcher.getGlowColor();
    }

    @Override
    public void addCustomData(String key, Object data)
    {
        instance.addCustomData(key, data);
    }

    @Override
    public Object getCustomData(String key)
    {
        return instance.getCustomData(key);
    }

    @Override
    public GameProfile getSkin()
    {
        if (!(instance instanceof PlayerDisguise playerDisguise)) return null;

        return (GameProfile) playerDisguise.getWatcher().getSkin().getHandle();
    }

    @Override
    public void applySkin(GameProfile profile)
    {
        if (!(instance instanceof PlayerDisguise playerDisguise)) return;

        var wrappedProfile = WrappedGameProfile.fromHandle(profile);
        var LDprofile = ReflectionManager.getGameProfileWithThisSkin(wrappedProfile.getUUID(), wrappedProfile.getName(), wrappedProfile);

        //LD不支持直接用profile设置皮肤，只能先存到本地设置完再移除
        DisguiseAPI.addGameProfile(LDprofile.toString(), LDprofile);
        playerDisguise.setSkin(LDprofile);
        DisguiseUtilities.removeGameProfile(LDprofile.toString());

        invalidateCompound();
    }

    @Override
    public void onPostConstructDisguise(DisguiseState state, @Nullable Entity targetEntity)
    {
        invalidateCompound();

        //workaround: 伪装已死亡的LivingEntity
        if (watcher instanceof LivingWatcher livingWatcher && livingWatcher.getHealth() <= 0)
            livingWatcher.setHealth(1);

        instance.setTallDisguisesVisible(true);

        //禁用actionBar
        DisguiseAPI.setActionBarShown(state.getPlayer(), false);

        if (targetEntity != null)
        {
            switch (targetEntity.getType())
            {
                case CAT ->
                {
                    if (instance.getType() == DisguiseType.CAT)
                    {
                        var watcher = (CatWatcher) instance.getWatcher();
                        var cat = (Cat) targetEntity;

                        watcher.setType(cat.getCatType());
                    }
                }

                case VILLAGER ->
                {
                    if (instance.getType() == DisguiseType.VILLAGER)
                    {
                        var watcher = (VillagerWatcher) instance.getWatcher();
                        var villager = (Villager) targetEntity;

                        watcher.setVillagerData(new VillagerData(villager.getVillagerType(),
                                villager.getProfession(), villager.getVillagerLevel()));
                    }
                }
            }
        }

        instance.setKeepDisguiseOnPlayerDeath(true);
    }

    @Override
    public String serializeDisguiseData()
    {
        return DisguiseParser.parseToString(instance);
    }
    private final Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

    @Override
    public void updateDisplay(boolean isClone, DisguiseState state, Player player)
    {
        if (!this.instance.equals(DisguiseAPI.getDisguise(player)))
        {
            this.instance = instance.clone();
            DisguiseAPI.disguiseEntity(player, this.instance);
        }

        //对克隆的伪装手动更新一些属性
        if (!isClone) return;

        var team = scoreboard.getPlayerTeam(player);
        var playerColor = (team == null || !team.hasColor()) ? NamedTextColor.WHITE : team.color();

        //workaround: 复制实体伪装时会一并复制隐身标签
        //            会导致复制出来的伪装永久隐身
        if (watcher.isInvisible() != player.isInvisible())
            watcher.setInvisible(player.isInvisible());

        //workaround: 伪装不会主动检测玩家有没有发光
        if (watcher.isGlowing() != player.isGlowing())
            watcher.setGlowing(player.isGlowing());

        //设置发光颜色
        if (!state.haveCustomGlowColor())
            watcher.setGlowColor(ColorUtils.toChatColor(playerColor));

        //设置滑翔状态
        if (watcher.isFlyingWithElytra() != player.isGliding())
            watcher.setFlyingWithElytra(player.isGliding());

        //workaround: 复制出来的伪装会忽略玩家Pose
        if (state.shouldHandlePose())
        {
            var pose = DisguiseUtils.toLibsEntityPose(player.getPose());

            if (watcher.getEntityPose() != pose)
                watcher.setEntityPose(pose);
        }
    }

    private final CompoundTag compoundTag = new CompoundTag();
    private CompoundTag mixedTag = new CompoundTag();
    private boolean tagValid;

    private void invalidateCompound()
    {
        this.tagValid = false;
    }

    @Override
    public void mergeCompound(CompoundTag compoundTag)
    {
        this.compoundTag.merge(compoundTag);
        invalidateCompound();
    }

    @Override
    public CompoundTag getCompound()
    {
        var compoundTag = this.compoundTag.copy();

        //todo: 将这些东西移动回VanillaDisguiseProvider，找个合适的方法来从外部获取伪装的NBT数据
        var watcher = instance.getWatcher();
        switch (getEntityType())
        {
            case SLIME, MAGMA_CUBE ->
            {
                var size = ((SlimeWatcher) watcher).getSize() - 1;
                compoundTag.putInt("Size", size);
            }

            case HORSE ->
            {
                var color = ((HorseWatcher) watcher).getColor().ordinal();
                var style = ((HorseWatcher) watcher).getStyle().ordinal();
                compoundTag.putInt("Variant", color | style << 8);
            }

            case PARROT ->
            {
                var variant = ((ParrotWatcher) watcher).getVariant().ordinal();
                compoundTag.putInt("Variant", variant);
            }

            case CAT ->
            {
                var variant = ((CatWatcher) watcher).getType().getKey().asString();
                compoundTag.putString("variant", variant);
            }

            case TROPICAL_FISH ->
            {
                var variant = ((TropicalFishWatcher) watcher).getVariant();

                compoundTag.putInt("Variant", variant);
            }

            case RABBIT ->
            {
                var type = ((RabbitWatcher) watcher).getType().getTypeId();
                compoundTag.putInt("RabbitType", type);
            }

            case FOX ->
            {
                var foxType = ((FoxWatcher) watcher).getType().name().toLowerCase();
                compoundTag.putString("Type", foxType);
            }

            case FROG ->
            {
                var variant = ((FrogWatcher) watcher).getVariant().getKey().asString();
                compoundTag.putString("variant", variant);
            }

            case GOAT ->
            {
                var goatWatcher = ((GoatWatcher) watcher);

                var hasLeftHorn = goatWatcher.hasLeftHorn();
                var hasRightHorn = goatWatcher.hasRightHorn();
                var isScreaming = goatWatcher.isScreaming();

                compoundTag.putBoolean("HasLeftHorn", hasLeftHorn);
                compoundTag.putBoolean("HasRightHorn", hasRightHorn);
                compoundTag.putBoolean("IsScreamingGoat", isScreaming);
            }

            case PANDA ->
            {
                var pandaWatcher = ((PandaWatcher) watcher);
                var mainGene = pandaWatcher.getMainGene();
                var hiddenGene = pandaWatcher.getHiddenGene();

                compoundTag.putString("MainGene", mainGene.toString().toLowerCase());
                compoundTag.putString("HiddenGene", hiddenGene.toString().toLowerCase());
            }
        }

        this.tagValid = true;
        this.mixedTag = compoundTag;

        return compoundTag.copy();
    }

    @Nullable
    @Override
    public <R extends Tag> R getTag(String path, TagType<R> type)
    {
        try
        {
            var obj = (tagValid ? this.mixedTag : getCompound()).get(path);

            if (obj != null && obj.getType() == type)
                return (R) obj;

            return null;
        }
        catch (Throwable t)
        {
            logger.error("Unable to read NBT '%s' from instance:".formatted(path));
            t.printStackTrace();

            return null;
        }
    }

    private static final Logger logger = MorphPlugin.getInstance(MorphPlugin.getMorphNameSpace()).getSLF4JLogger();

    @Override
    public void showArms(boolean showarms)
    {
        if (instance.getWatcher() instanceof ArmorStandWatcher armorStandWatcher)
            armorStandWatcher.setShowArms(showarms);

        invalidateCompound();
    }

    @Override
    public void setSaddled(boolean saddled)
    {
        if (instance.getWatcher() instanceof AbstractHorseWatcher horseWatcher)
            horseWatcher.setSaddled(saddled);

        invalidateCompound();
    }

    @Override
    public boolean isSaddled()
    {
        if (instance.getWatcher() instanceof AbstractHorseWatcher horseWatcher)
            return horseWatcher.isSaddled();

        invalidateCompound();

        return false;
    }

    @Override
    public void setAggresive(boolean aggresive)
    {
        if (watcher instanceof CreeperWatcher creeperWatcher)
        {
            creeperWatcher.setIgnited(aggresive);
        }
        else if (watcher instanceof GhastWatcher ghastWatcher)
        {
            ghastWatcher.setAggressive(aggresive);
        }

        invalidateCompound();
    }
}
