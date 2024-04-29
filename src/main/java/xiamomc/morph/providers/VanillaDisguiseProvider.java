package xiamomc.morph.providers;

import io.papermc.paper.util.CollisionUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.backends.DisguiseWrapper;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.MorphStrings;
import xiamomc.morph.messages.vanilla.VanillaMessageStore;
import xiamomc.morph.misc.DisguiseMeta;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.misc.DisguiseTypes;
import xiamomc.morph.misc.NmsRecord;
import xiamomc.morph.utilities.*;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Bindables.Bindable;
import xiamomc.pluginbase.Exceptions.NullDependencyException;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

public class VanillaDisguiseProvider extends DefaultDisguiseProvider
{
    @Override
    public @NotNull String getNameSpace()
    {
        return DisguiseTypes.VANILLA.getNameSpace();
    }

    @Override
    public boolean isValid(String rawIdentifier)
    {
        var idStripped = DisguiseTypes.VANILLA.toStrippedId(rawIdentifier);
        return getAllAvailableDisguises().contains(idStripped);
    }

    public VanillaDisguiseProvider()
    {
        var list = new ObjectArrayList<String>();

        for (var eT : EntityType.values())
        {
            if (eT == EntityType.UNKNOWN || !eT.isAlive()) continue;

            list.add(eT.getKey().getKey());
        }

        list.removeIf(s -> s.equals("player"));

        vanillaIdentifiers = list;
    }

    private final Bindable<Boolean> armorStandShowArms = new Bindable<>(false);
    private final Bindable<Boolean> doHealthScale = new Bindable<>(true);
    private final Bindable<Integer> healthCap = new Bindable<>(60);
    private final Bindable<Boolean> modifyBoundingBoxes = new Bindable<>(false);
    private final Bindable<Boolean> checkSpaceBoundingBox = new Bindable<>(true);

    @Initializer
    private void load(MorphConfigManager configManager)
    {
        configManager.bind(armorStandShowArms, ConfigOption.ARMORSTAND_SHOW_ARMS);
        configManager.bind(doHealthScale, ConfigOption.HEALTH_SCALE);
        configManager.bind(healthCap, ConfigOption.HEALTH_SCALE_MAX_HEALTH);
        configManager.bind(modifyBoundingBoxes, ConfigOption.MODIFY_BOUNDING_BOX);
        configManager.bind(checkSpaceBoundingBox, ConfigOption.CHECK_AVAILABLE_SPACE);

        modifyBoundingBoxes.onValueChanged((o, n) ->
        {
            if (o && !n)
                Bukkit.getOnlinePlayers().forEach(p -> NmsRecord.ofPlayer(p).refreshDimensions());
        });
    }

    private final List<String> vanillaIdentifiers;

    @Override
    public List<String> getAllAvailableDisguises()
    {
        return vanillaIdentifiers;
    }

    @Override
    @NotNull
    public DisguiseResult makeWrapper(Player player, DisguiseMeta disguiseMeta, @Nullable Entity targetEntity)
    {
        var identifier = disguiseMeta.getIdentifier();

        DisguiseWrapper<?> constructedDisguise;
        var backend = getBackend();

        var entityType = EntityTypeUtils.fromString(identifier, true);

        if (entityType == null || entityType == EntityType.PLAYER || !entityType.isAlive())
        {
            logger.error("Illegal mob type: " + identifier + "(" + entityType + ")");
            return DisguiseResult.fail();
        }

        var copyResult = constructFromEntity(disguiseMeta, targetEntity);

        constructedDisguise = copyResult.success()
                ? copyResult.wrapperInstance() //copyResult.success() -> wrapperInstance() != null
                : backend.createInstance(entityType);

        // Make IDE happy
        Objects.requireNonNull(constructedDisguise);

        // 检查是否有足够的空间
        if (modifyBoundingBoxes.get() && checkSpaceBoundingBox.get())
        {
            //手动指定史莱姆和岩浆怪的大小
            if (entityType == EntityType.SLIME || entityType == EntityType.MAGMA_CUBE)
            {
                var canCons = canConstruct(disguiseMeta, targetEntity, null);

                if (canCons)
                {
                    var size = targetEntity != null
                                    ? NbtUtils.getRawTagCompound(targetEntity).getInt("Size")
                                    : new Random().nextInt(1, 4);

                    var initialTag = new CompoundTag();

                    initialTag.putInt("Size", size);
                    constructedDisguise.mergeCompound(initialTag);
                }
            }

            var loc = player.getLocation();
            var box = constructedDisguise.getBoundingBoxAt(loc.x(), loc.y(), loc.z());

            var hasCollision = CollisionUtil.getCollisionsForBlocksOrWorldBorder(
                    NmsRecord.ofPlayer(player).level(), null, box,
                    new ObjectArrayList<>(), new ObjectArrayList<>(), CollisionUtil.COLLISION_FLAG_CHECK_BORDER, null);

            if (hasCollision)
            {
                player.sendMessage(MessageUtils.prefixes(player, MorphStrings.noEnoughSpaceString()));
                return DisguiseResult.FAILED_COLLISION;
            }
        }

        return DisguiseResult.success(constructedDisguise, copyResult.isCopy());
    }

    @Override
    public boolean updateDisguise(Player player, DisguiseState state)
    {
        if (super.updateDisguise(player, state))
        {
            if (modifyBoundingBoxes.get())
                tryModifyPlayerDimensions(player, state.getDisguiseWrapper());

            if (plugin.getCurrentTick() % 20 == 0)
                ReflectionUtils.cleanCaches();

            if (plugin.getCurrentTick() % 5 == 0)
            {
                var nearByEntities = player.getNearbyEntities(12, 12, 12);
                targetingHelper.entity(nearByEntities);
            }

            return true;
        }
        else
            return false;
    }

    private final EntityTargetingHelper targetingHelper = new EntityTargetingHelper();


    @Override
    public void postConstructDisguise(DisguiseState state, @Nullable Entity targetEntity)
    {
        super.postConstructDisguise(state, targetEntity);

        var disguise = state.getDisguiseWrapper();
        var backend = getMorphManager().getCurrentBackend();

        if (!backend.isDisguised(targetEntity))
        {
            //盔甲架加上手臂
            if (disguise.getEntityType().equals(EntityType.ARMOR_STAND) && armorStandShowArms.get())
                disguise.setShowArms(true);
        }

        var player = state.getPlayer();
        if (doHealthScale.get())
        {
            removeAllHealthModifiers(player);

            var entityClazz = state.getEntityType().getEntityClass();
            if (entityClazz != null)
                tryAddModifier(state);
        }

        if (modifyBoundingBoxes.get())
            this.tryModifyPlayerDimensions(player, state.getDisguiseWrapper());
    }

    private void tryAddModifier(DisguiseState state)
    {
        try
        {
            var player = state.getPlayer();
            var loc = player.getLocation();
            loc.setY(-8192);

            var entity = NmsUtils.spawnEntity(state.getEntityType(), state.getPlayer().getWorld(), loc);

            if (!(entity instanceof LivingEntity living)) return;

            var craftLiving = (net.minecraft.world.entity.LivingEntity) ((CraftLivingEntity)living).getHandleRaw();
            var mobMaxHealth = craftLiving.craftAttributes.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
            var playerAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);

            if (mobMaxHealth <= 0d)
            {
                logger.warn("Entity has a max health that's lower than 0? Not applying...");
                return;
            }

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

            entity.remove();
        }
        catch (Throwable t)
        {
            logger.error("Error occurred trying to modify player's health attribute: " + t.getMessage());
            t.printStackTrace();
        }
    }

    private void resetPlayerDimensions(Player player)
    {
        var nmsPlayer = NmsRecord.ofPlayer(player);

        //Find dimensions
        Field targetField = null;

        try
        {
            targetField = ReflectionUtils.getPlayerDimensionsField(nmsPlayer);
        }
        catch (NullDependencyException t)
        {
            logger.error("Can't read player dimension.");
        }
        catch (Throwable t)
        {
            logger.error("Can't read player dimension: " + t.getMessage());
            t.printStackTrace();
        }

        if (targetField == null)
            return;

        try
        {
            var dimension = net.minecraft.world.entity.player.Player.STANDING_DIMENSIONS;

            targetField.setAccessible(true);
            targetField.set(nmsPlayer, dimension);

            nmsPlayer.refreshDimensions();
        }
        catch (Throwable t)
        {
            logger.error("Unable to reset player's bounding box: " + t.getMessage());
            t.printStackTrace();
        }
    }

    private void tryModifyPlayerDimensions(Player player, DisguiseWrapper<?> wrapper)
    {
        var nmsPlayer = NmsRecord.ofPlayer(player);

        //Find dimensions
        Field targetField = null;

        try
        {
            targetField = ReflectionUtils.getPlayerDimensionsField(nmsPlayer);
        }
        catch (NullDependencyException t)
        {
            logger.error("Can't read player dimension.");
        }
        catch (Throwable t)
        {
            logger.error("Can't read player dimension: " + t.getMessage());
            t.printStackTrace();
        }

        if (targetField == null) return;

        try
        {
            var box = wrapper.getBoundingBoxAt(nmsPlayer.getX(), nmsPlayer.getY(), nmsPlayer.getZ());
            var dimensions = wrapper.getDimensions();

            // Update dimensions
            targetField.set(nmsPlayer, dimensions);

            nmsPlayer.setBoundingBox(box);

            // Update eye height
            var eyeHeightField = ReflectionUtils.getPlayerEyeHeightField(NmsRecord.ofPlayer(player));
            eyeHeightField.set(nmsPlayer, dimensions.height() * 0.85F);
        }
        catch (Throwable t)
        {
            logger.warn("Unable to modify player's bounding box: " + t.getMessage());
            t.printStackTrace();
        }
    }

    private final String modifierName = "FeatherMorphVDP_Modifier";

    private void executeThenScaleHealth(Player player, AttributeInstance attributeInstance, Runnable runnable)
    {
        var currentPercent = player.getHealth() / attributeInstance.getValue();

        runnable.run();

        if (player.getHealth() > 0)
            player.setHealth(Math.min(player.getMaxHealth(), attributeInstance.getValue() * currentPercent));
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
            resetPlayerDimensions(player);
            return true;
        }
        else
            return false;
    }

    @Override
    public @Nullable CompoundTag getNbtCompound(DisguiseState state, Entity targetEntity, boolean enableCulling)
    {
        var info = getMorphManager().getDisguiseMeta(state.getDisguiseIdentifier());

        var rawCompound = targetEntity != null && canConstruct(info, targetEntity, null)
                ? NbtUtils.getRawTagCompound(targetEntity)
                : NbtUtils.toCompoundTag(state.getFullNbtString());

        if (rawCompound == null) rawCompound = new CompoundTag();

        var theirDisguise = getMorphManager().getDisguiseStateFor(targetEntity);

        if (theirDisguise != null)
            rawCompound = theirDisguise.getDisguiseWrapper().getCompound();

        if (state.getEntityType().equals(EntityType.ARMOR_STAND)
                && rawCompound.get("ShowArms") == null)
        {
            rawCompound.putBoolean("ShowArms", armorStandShowArms.get());
        }

        if (targetEntity == null || targetEntity.getType() != state.getEntityType())
            rawCompound.merge(state.getDisguiseWrapper().getCompound());

        return enableCulling ? cullNBT(rawCompound) : rawCompound;
    }

    @Override
    public boolean validForClient(DisguiseState state)
    {
        return true;
    }

    @Override
    public boolean canConstruct(DisguiseMeta info, @Nullable Entity targetEntity, DisguiseState theirState)
    {
        return theirState != null
                ? theirState.getDisguiseWrapper().getEntityType().equals(info.getEntityType())
                : targetEntity == null || targetEntity.getType().equals(info.getEntityType());
    }

    @Override
    protected boolean canCloneDisguise(DisguiseMeta info, Entity targetEntity,
                                       @NotNull DisguiseState theirDisguiseState, @NotNull DisguiseWrapper<?> theirDisguise)
    {
        return theirDisguise.getEntityType().equals(info.getEntityType());
    }

    @Resolved
    private VanillaMessageStore vanillaMessageStore;

    @Override
    public Component getDisplayName(String disguiseIdentifier, String locale)
    {
        var type = EntityTypeUtils.fromString(disguiseIdentifier, true);

        if (type == null)
            return Component.text("???");
        else
            return vanillaMessageStore.getComponent(type.translationKey(), null, locale);
    }
}
