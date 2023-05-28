package xiamomc.morph.providers;

import io.papermc.paper.util.CollisionUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.backends.DisguiseWrapper;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.MorphStrings;
import xiamomc.morph.messages.vanilla.VanillaMessageStore;
import xiamomc.morph.misc.DisguiseInfo;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.misc.DisguiseTypes;
import xiamomc.morph.misc.NmsRecord;
import xiamomc.morph.utilities.EntityTypeUtils;
import xiamomc.morph.utilities.NbtUtils;
import xiamomc.morph.utilities.ReflectionUtils;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Bindables.Bindable;

import java.util.List;
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
    public DisguiseResult makeWrapper(Player player, DisguiseInfo disguiseInfo, @Nullable Entity targetEntity)
    {
        var identifier = disguiseInfo.getIdentifier();

        DisguiseWrapper<?> constructedDisguise;
        var backend = getBackend();

        var entityType = EntityTypeUtils.fromString(identifier, true);

        if (entityType == null || entityType == EntityType.PLAYER || !entityType.isAlive())
        {
            logger.error("Illegal mob type: " + identifier + "(" + entityType + ")");
            return DisguiseResult.fail();
        }

        var copyResult = constructFromEntity(disguiseInfo, targetEntity);

        constructedDisguise = copyResult.success()
                ? copyResult.wrapperInstance() //copyResult.success() -> wrapperInstance() != null
                : backend.createInstance(entityType);

        // 检查是否有足够的空间
        if (modifyBoundingBoxes.get() && checkSpaceBoundingBox.get())
        {
            var loc = player.getLocation();
            var box = constructedDisguise.getBoundingBoxAt(loc.x(), loc.y(), loc.z());

            var hasCollision = CollisionUtil.getCollisionsForBlocksOrWorldBorder(NmsRecord.ofPlayer(player).level, null, box, null, false, false, true, true, null);
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

            return true;
        }
        else
            return false;
    }

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
                disguise.showArms(true);
        }

        var player = state.getPlayer();
        if (doHealthScale.get())
        {
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

        if (modifyBoundingBoxes.get())
            this.tryModifyPlayerDimensions(player, state.getDisguiseWrapper());
    }

    private void resetPlayerDimensions(Player player)
    {
        var nmsPlayer = NmsRecord.ofPlayer(player);

        var targetField = ReflectionUtils.getPlayerDimensionsField(nmsPlayer);

        if (targetField != null)
        {
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
    }

    private void tryModifyPlayerDimensions(Player player, DisguiseWrapper<?> wrapper)
    {
        var nmsPlayer = NmsRecord.ofPlayer(player);

        //Find dimensions
        var targetField = ReflectionUtils.getPlayerDimensionsField(nmsPlayer);

        if (targetField != null)
        {
            try
            {
                var box = wrapper.getBoundingBoxAt(nmsPlayer.getX(), nmsPlayer.getY(), nmsPlayer.getZ());
                var dimensions = wrapper.getDimensions();

                targetField.set(nmsPlayer, dimensions);

                nmsPlayer.setBoundingBox(box);

                var eyeHeightField = ReflectionUtils.getPlayerEyeHeightField(NmsRecord.ofPlayer(player));

                if (eyeHeightField != null)
                    eyeHeightField.set(nmsPlayer, dimensions.height * 0.85F);
            }
            catch (Throwable t)
            {
                logger.warn("Unable to modify player's bounding box: " + t.getMessage());
                t.printStackTrace();
            }
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
    public @Nullable CompoundTag getNbtCompound(DisguiseState state, Entity targetEntity)
    {
        var info = getMorphManager().getDisguiseInfo(state.getDisguiseIdentifier());

        var rawCompound = targetEntity != null && canConstruct(info, targetEntity, null)
                ? NbtUtils.getRawTagCompound(targetEntity)
                : NbtUtils.toCompoundTag(state.getCachedNbtString());

        if (rawCompound == null) rawCompound = new CompoundTag();

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
                logger.error("Unable to copy NBT Tag from disguise: " + t.getMessage());
                t.printStackTrace();
            }
        }

        if (state.getEntityType().equals(EntityType.ARMOR_STAND)
                && rawCompound.get("ShowArms") == null)
        {
            rawCompound.putBoolean("ShowArms", armorStandShowArms.get());
        }

        if (targetEntity == null || targetEntity.getType() != state.getEntityType())
            rawCompound.merge(state.getDisguiseWrapper().getCompound());

        return cullNBT(rawCompound);
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
                ? theirState.getDisguiseWrapper().getEntityType().equals(info.getEntityType())
                : targetEntity.getType().equals(info.getEntityType());
    }

    @Override
    protected boolean canCloneDisguise(DisguiseInfo info, Entity targetEntity,
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
