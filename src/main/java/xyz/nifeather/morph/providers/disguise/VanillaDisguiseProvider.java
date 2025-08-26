package xyz.nifeather.morph.providers.disguise;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Bindables.Bindable;
import xyz.nifeather.morph.backends.DisguiseWrapper;
import xyz.nifeather.morph.config.ConfigOption;
import xyz.nifeather.morph.config.MorphConfigManager;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.messages.MorphStrings;
import xyz.nifeather.morph.messages.vanilla.VanillaMessageStore;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.DisguiseTypes;
import xyz.nifeather.morph.misc.NmsRecord;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.values.ArmorStandProperties;
import xyz.nifeather.morph.providers.animation.AnimationProvider;
import xyz.nifeather.morph.providers.animation.provider.VanillaAnimationProvider;
import xyz.nifeather.morph.utilities.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

public class VanillaDisguiseProvider extends DefaultDisguiseProvider
{
    @Override
    public @NotNull String getNameSpace()
    {
        return DisguiseTypes.VANILLA.getNameSpace();
    }

    private final AnimationProvider animationProvider = new VanillaAnimationProvider();

    /**
     * 获取此DisguiseProvider的动画提供器
     */
    @Override
    public AnimationProvider getAnimationProvider()
    {
        return animationProvider;
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
    private final Bindable<Boolean> doHealthScale = new Bindable<>(false);
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
                featherMorph().getPlatform().onlinePlayersNative().forEach(p -> NmsRecord.ofPlayer(p).refreshDimensions());
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

        var backend = getPreferredBackend();

        var entityType = EntityTypeUtils.fromString(identifier, true);

        if (entityType == null || entityType == EntityType.PLAYER || !entityType.isAlive())
        {
            logger.error("Illegal mob type: " + identifier + "(" + entityType + ")");
            return DisguiseResult.fail();
        }

        var newDisguise = backend.createInstance(entityType);

        // Make IDE happy
        Objects.requireNonNull(newDisguise);

        // 检查是否有足够的空间
        if (modifyBoundingBoxes.get() && checkSpaceBoundingBox.get())
        {
            var loc = player.getLocation();
            var box = newDisguise.getBoundingBoxAt(loc.x(), loc.y(), loc.z());

            var hasCollision = CollisionUtils.hasCollisionWithBlockOrBorder(player, box);
            if (hasCollision)
            {
                player.sendMessage(MessageUtils.prefixes(player, MorphStrings.noEnoughSpaceString()));
                return DisguiseResult.FAIL_SILENT;
            }
        }

        return DisguiseResult.success(newDisguise);
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
    public void postBuildDisguise(DisguiseState state, @Nullable Entity targetEntity)
    {
        super.postBuildDisguise(state, targetEntity);

        var wrapper = state.getDisguiseWrapper();
        var theirDisguise = getMorphManager().getDisguiseStateFor(targetEntity);

        if (wrapper.getEntityType() != EntityType.ARMOR_STAND)
            return;

        var properties = DisguiseProperties.INSTANCE.getOrThrow(ArmorStandProperties.class);
        var wrapperShowArms = wrapper.readPropertyOr(properties.SHOW_ARMS, null);

        //盔甲架加上手臂
        if (wrapperShowArms != null)
            return;

        var showArm = theirDisguise != null
                ? theirDisguise.disguisePropertyHandler().getOr(properties.SHOW_ARMS, false)
                : targetEntity instanceof ArmorStand armorStand
                    ? armorStand.hasArms()
                    : this.armorStandShowArms.get();

        wrapper.writeProperty(properties.SHOW_ARMS, showArm);
    }

    private void tryAddModifier(DisguiseState state)
    {
        try
        {
            var player = state.getPlayer();
            var loc = player.getLocation();
            loc.setY(-8192);

            var entity = NmsUtils.spawnEntity(state.getEntityType(), state.getPlayer().getWorld(), loc);

            if (!(entity instanceof LivingEntity living))
            {
                entity.remove();
                return;
            }

            var craftLiving = (net.minecraft.world.entity.LivingEntity) ((CraftLivingEntity)living).getHandleRaw();
            var mobMaxHealth = craftLiving.craftAttributes.getAttribute(Attribute.MAX_HEALTH).getBaseValue();

            // patch: CREAKING only have half heart, and we don't want that.
            if (state.getEntityType() == EntityType.CREAKING)
                mobMaxHealth = 20;

            var playerAttribute = player.getAttribute(Attribute.MAX_HEALTH);

            if (mobMaxHealth <= 0d)
            {
                logger.warn("Entity has a max health that's lower than 0? Not applying...");
                return;
            }

            assert playerAttribute != null;

            // 获取生物最大生命和玩家最大生命的差异
            var diff = mobMaxHealth - playerAttribute.getBaseValue();

            // 如果玩家的基值加上差异大于限制
            // 确保血量不会超过上限
            if (playerAttribute.getBaseValue() + diff > healthCap.get())
                diff = healthCap.get() - playerAttribute.getBaseValue();

            //region Scale Health
            double diffFinal = diff;

            playerAttribute.removeModifier(healthModifierKey);

            // Also handle legacy keys
            playerAttribute.removeModifier(healthModifierKeyLegacy);

            var modifier = new AttributeModifier(healthModifierKey, diffFinal, AttributeModifier.Operation.ADD_NUMBER);

            runThenScaleHealth(player, playerAttribute, () -> playerAttribute.addTransientModifier(modifier));

            //endregion Scale Health

            entity.remove();
        }
        catch (Throwable t)
        {
            logger.error("Error occurred trying to modify player's health attribute", t);
        }
    }

    @NotNull
    public static final NamespacedKey healthModifierKeyLegacy = Objects.requireNonNull(NamespacedKey.fromString("feathermorph:health_modifier"), "How?!");

    @NotNull
    public static final NamespacedKey healthModifierKey = Objects.requireNonNull(NamespacedKey.fromString("feathermorph:fm_health_modifier"), "How?!");

    private void resetPlayerDimensions(Player player)
    {
        var nmsPlayer = NmsRecord.ofPlayer(player);

        //Find dimensions
        Field targetField = null;

        try
        {
            targetField = ReflectionUtils.getPlayerDimensionsField(nmsPlayer);
        }
        catch (Throwable t)
        {
            logger.error("Can't read player dimension.", t);
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
            logger.error("Unable to reset player's bounding box", t);
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
        catch (Throwable t)
        {
            logger.error("Can't read player dimension.", t);
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
            logger.warn("Unable to modify player's bounding box", t);
        }
    }

    private void runThenScaleHealth(Player player, AttributeInstance attributeInstance, Runnable runnable)
    {
        var currentPercent = player.getHealth() / attributeInstance.getValue();

        try
        {
            runnable.run();
        }
        catch (Throwable t)
        {
            logger.warn("Failed to execute Runnable in VanillaDisguiseProvider#runThenScaleHealth", t);
        }

        if (player.getHealth() > 0) //       v 偷懒
            player.setHealth(Math.min(player.getMaxHealth(), attributeInstance.getValue() * currentPercent));
    }

    @Override
    public void onPlayerJoinWithDisguise(DisguiseState state)
    {
        onDisguiseApply(state);
        mutePlayerWaypoint(state.getPlayer());

        super.onPlayerJoinWithDisguise(state);
    }

    private void removeAllHealthModifiers(Player player)
    {
        var attribute = player.getAttribute(Attribute.MAX_HEALTH);
        assert attribute != null;

        runThenScaleHealth(player, attribute, () -> attribute.removeModifier(healthModifierKey));
    }

    @Override
    public boolean unMorph(Player player, DisguiseState state)
    {
        if (super.unMorph(player, state))
        {
            removeAllHealthModifiers(player);
            resetPlayerDimensions(player);
            recoverPlayerWaypoint(player);

            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public void onDisguiseApply(DisguiseState state)
    {
        super.onDisguiseApply(state);

        var player = state.getPlayer();

        if (doHealthScale.get())
            tryAddModifier(state);

        if (modifyBoundingBoxes.get())
            tryModifyPlayerDimensions(player, state.getDisguiseWrapper());

        mutePlayerWaypoint(player);
    }

    @Override
    public boolean validForClient(DisguiseState state)
    {
        return true;
    }

    /**
     * 我们是否可以克隆目标实体/玩家的伪装？
     *
     * @param info         {@link DisguiseMeta}
     * @param targetEntity 目标实体
     * @param theirState   他们的{@link DisguiseState}，如果有
     * @return 是否允许克隆他们的装备进行显示
     */
    @Override
    public boolean canCloneEquipment(DisguiseMeta info, Entity targetEntity, DisguiseState theirState)
    {
        return theirState != null
                ? theirState.getDisguiseWrapper().getEntityType().equals(info.getEntityType())
                : targetEntity == null || targetEntity.getType().equals(info.getEntityType());
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
