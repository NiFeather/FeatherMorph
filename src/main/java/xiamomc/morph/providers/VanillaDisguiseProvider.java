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
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.checkerframework.checker.units.qual.A;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Attr;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.misc.*;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Bindables.Bindable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    private final Bindable<Boolean> doHealthScale = new Bindable<>(true);
    private final Bindable<Integer> healthCap = new Bindable<>(60);

    @Initializer
    private void load(MorphConfigManager configManager)
    {
        configManager.bind(armorStandShowArms, ConfigOption.ARMORSTAND_SHOW_ARMS);
        configManager.bind(doHealthScale, ConfigOption.HEALTH_SCALE);
        configManager.bind(healthCap, ConfigOption.HEALTH_SCALE_MAX_HEALTH);
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
                    if (disguise.getType() == DisguiseType.CAT)
                    {
                        var watcher = (CatWatcher) disguise.getWatcher();
                        var cat = (Cat) targetEntity;

                        watcher.setType(cat.getCatType());
                    }
                }

                case VILLAGER ->
                {
                    if (disguise.getType() == DisguiseType.VILLAGER)
                    {
                        var watcher = (VillagerWatcher) disguise.getWatcher();
                        var villager = (Villager) targetEntity;

                        watcher.setVillagerData(new VillagerData(villager.getVillagerType(),
                                villager.getProfession(), villager.getVillagerLevel()));
                    }
                }
            }
        }

        if (doHealthScale.get())
        {
            var player = state.getPlayer();
            var loc = player.getLocation();
            loc.setY(-8192);

            removeAllHealthModifiers(player);

            var entityClazz = state.getEntityType().getEntityClass();
            if (entityClazz != null)
            {
                var entity = state.getPlayer().getWorld().spawn(loc, entityClazz, CreatureSpawnEvent.SpawnReason.CUSTOM);

                if (entity instanceof LivingEntity living)
                {
                    var mobMaxHealth = living.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
                    var playerAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);

                    assert playerAttribute != null;
                    var diff = mobMaxHealth - playerAttribute.getBaseValue();

                    //确保血量不会超过上限
                    if (playerAttribute.getBaseValue() + diff > healthCap.get())
                        diff = healthCap.get() - playerAttribute.getBaseValue();

                    //缩放生命值
                    double finalDiff = diff;
                    this.executeThenScaleHealth(player, playerAttribute, () ->
                    {
                        var modifier = new AttributeModifier(modifierName, finalDiff, AttributeModifier.Operation.ADD_NUMBER);
                        playerAttribute.addModifier(modifier);
                    });
                }

                entity.remove();
            }
        }
    }

    private final String modifierName = "MorphModifier";

    private void executeThenScaleHealth(Player player, AttributeInstance attributeInstance, Runnable runnable)
    {
        var currentPercent = player.getHealth() / attributeInstance.getValue();

        runnable.run();

        player.setHealth(attributeInstance.getValue() * currentPercent);
    }

    private void removeAllHealthModifiers(Player player)
    {
        var attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        assert attribute != null;

        this.executeThenScaleHealth(player, attribute, () ->
        {
            attribute.getModifiers().stream()
                    .filter(m -> m.getName().equals(modifierName)).collect(Collectors.toSet())
                    .forEach(attribute::removeModifier);
        });
    }

    @Override
    public boolean unMorph(Player player, DisguiseState state)
    {
        if (super.unMorph(player, state))
        {
            removeAllHealthModifiers(player);
            return true;
        }
        else
            return false;
    }

    @Override
    public @Nullable String getNbtCompound(DisguiseState state, Entity targetEntity)
    {
        var info = getMorphManager().getDisguiseInfo(state.getDisguiseIdentifier());

        var rawCompound = targetEntity != null && canConstruct(info, targetEntity, null)
                ? NbtUtils.getRawTagCompound(targetEntity)
                : new CompoundTag();

        var theirDisguise = getMorphManager().getDisguiseStateFor(targetEntity);

        if (theirDisguise != null)
        {
            var theirNbtString = theirDisguise.getCachedNbtString();

            try
            {
                rawCompound = TagParser.parseTag(theirNbtString);
            }
            catch (Throwable t)
            {
                logger.error("无法复制目标伪装的NBT标签：" + t.getMessage());
                t.printStackTrace();
            }
        }

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
