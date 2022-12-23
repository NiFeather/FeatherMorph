package xiamomc.morph.providers;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.VillagerData;
import me.libraryaddict.disguise.disguisetypes.watchers.ArmorStandWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.CatWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.VillagerWatcher;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManagerAbstract;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.entity.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.misc.*;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Bindables.Bindable;

import java.util.List;

public class VanillaDisguiseProvider extends DefaultDisguiseProvider
{
    @Override
    public @NotNull String getNameSpace()
    {
        return DisguiseTypes.VANILLA.getNameSpace();
    }

    public VanillaDisguiseProvider()
    {
        var list = new ObjectArrayList<String>();

        for (var eT : EntityType.values())
        {
            if (eT == EntityType.UNKNOWN) continue;

            list.add(eT.getKey().asString());
        }

        list.removeIf(s -> s.equals("minecraft:player"));

        vanillaIdentifiers = list;
    }

    private final List<String> vanillaIdentifiers;

    @Override
    public List<String> getAllAvailableDisguises()
    {
        return vanillaIdentifiers;
    }

    @Override
    @NotNull
    public DisguiseResult morph(Player player, DisguiseInfo disguiseInfo, @Nullable Entity targetEntity)
    {
        var identifier = disguiseInfo.getIdentifier();

        Disguise constructedDisguise;

        var entityType = EntityTypeUtils.fromString(identifier, true);

        if (entityType == null || entityType == EntityType.PLAYER || !entityType.isAlive())
        {
            logger.error("无效的生物类型: " + identifier + "(" + entityType + ")");
            return DisguiseResult.fail();
        }

        var copyResult = getCopy(disguiseInfo, targetEntity);

        constructedDisguise = copyResult.success()
                ? copyResult.disguise()
                : new MobDisguise(DisguiseType.getType(entityType));

        DisguiseAPI.disguiseEntity(player, constructedDisguise);

        return DisguiseResult.success(constructedDisguise, copyResult.isCopy());
    }

    private final Bindable<Boolean> armorStandShowArms = new Bindable<>(false);

    @Initializer
    private void load(MorphConfigManager configManager)
    {
        configManager.bind(armorStandShowArms, ConfigOption.ARMORSTAND_SHOW_ARMS);
    }

    @Override
    public void postConstructDisguise(DisguiseState state, @Nullable Entity targetEntity)
    {
        super.postConstructDisguise(state, targetEntity);

        var disguise = state.getDisguise();

        if (!DisguiseAPI.isDisguised(targetEntity))
        {
            //盔甲架加上手臂
            if (disguise.getType().equals(DisguiseType.ARMOR_STAND) && armorStandShowArms.get())
                ((ArmorStandWatcher) disguise.getWatcher()).setShowArms(true);
        }

        if (targetEntity != null)
        {
            switch (targetEntity.getType())
            {
                case CAT ->
                {
                    var watcher = (CatWatcher) disguise.getWatcher();
                    var cat = (Cat) targetEntity;

                    watcher.setType(cat.getCatType());
                }

                case VILLAGER ->
                {
                    var watcher = (VillagerWatcher) disguise.getWatcher();
                    var villager = (Villager) targetEntity;

                    watcher.setVillagerData(new VillagerData(villager.getVillagerType(),
                            villager.getProfession(), villager.getVillagerLevel()));
                }
            }
        }
    }

    @Override
    public @Nullable String getNbtCompound(DisguiseState state, Entity targetEntity)
    {
        var info = getMorphManager().getDisguiseInfo(state.getDisguiseIdentifier());

        var rawCompound = targetEntity != null && canConstruct(info, targetEntity, null)
                ? NbtUtils.getRawTagCompound(targetEntity)
                : new CompoundTag();

        //NbtCompound#putBoolean()
        if (rawCompound != null
                && state.getEntityType().equals(EntityType.ARMOR_STAND)
                && rawCompound.get("ShowArms") == null)
        {
            rawCompound.putBoolean("ShowArms", armorStandShowArms.get());
        }

        return NbtUtils.getCompoundString(cullNBT(rawCompound));
    }

    @Override
    public boolean validForClient(DisguiseState state)
    {
        return true;
    }

    @Override
    public boolean canConstruct(DisguiseInfo info, Entity targetEntity, DisguiseState theirState)
    {
        return theirState != null
                ? theirState.getDisguise().getType().getEntityType().equals(info.getEntityType())
                : targetEntity.getType().equals(info.getEntityType());
    }

    @Override
    protected boolean canCopyDisguise(DisguiseInfo info, Entity targetEntity,
                                      @Nullable DisguiseState theirDisguiseState, @NotNull Disguise theirDisguise)
    {
        return theirDisguise.getType().getEntityType().equals(info.getEntityType());
    }

    @Override
    public Component getDisplayName(String disguiseIdentifier)
    {
        var type = EntityTypeUtils.fromString(disguiseIdentifier, true);

        if (type == null)
            return Component.text("???");
        else
            return MinecraftLanguageHelper.getComponent(type.translationKey());
    }
}
