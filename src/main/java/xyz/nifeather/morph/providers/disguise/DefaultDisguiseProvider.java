package xyz.nifeather.morph.providers.disguise;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.abilities.AbilityManager;
import xyz.nifeather.morph.api.morphs.skills.SkillNames;
import xyz.nifeather.morph.backends.DisguiseBackend;
import xyz.nifeather.morph.misc.DisguiseEquipment;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.disguiseProperty.ParseErrorException;
import xyz.nifeather.morph.network.Constants;
import xyz.nifeather.morph.network.commands.S2C.AbstractS2CCommand;
import xyz.nifeather.morph.network.server.MorphClientHandler;
import xyz.nifeather.morph.network.server.ServerSetEquipCommand;
import xyz.nifeather.morph.skills.ISkill;
import xyz.nifeather.morph.skills.SkillManager;
import xyz.nifeather.morph.storage.skill.ISkillAbilityOption;

import java.util.List;
import java.util.Objects;

/**
 * 提供一个默认的DisguiseProvider
 * 包括自动设置Bossbar、技能等
 */
public abstract class DefaultDisguiseProvider extends DisguiseProvider
{
    @Resolved
    private SkillManager skillHandler;

    @Resolved
    private AbilityManager abilityHandler;

    @Resolved
    private MorphClientHandler clientHandler;

    @Override
    public @NotNull DisguiseBackend<?, ?> getPreferredBackend()
    {
        return getMorphManager().getDefaultBackend();
    }

    @Override
    public boolean updateDisguise(Player player, DisguiseState state)
    {
        return true;
    }

    protected AttributeInstance acquireAttributeOrThrow(Player player, Attribute attribute)
    {
        return Objects.requireNonNull(player.getAttribute(attribute),
                "Player don't have a '%s' attribute, you might using a broken server implementation.".formatted(attribute.key().asString()));
    }

    @NotNull
    public static final NamespacedKey WAYPOINT_TRANSMIT_MODIFIER_KEY = Objects.requireNonNull(NamespacedKey.fromString("feathermorph:waypoint_transmit_modifier"));

    protected void mutePlayerWaypoint(Player player)
    {
        // I don't know if adding -1 with ADD_SCALAR is allowed
        // But to prevent the player from transmitting waypoint, this is the easiest way...?
        // And by doing this, we won't have to mess with the WaypointManager
        var attribute = this.acquireAttributeOrThrow(player, Attribute.WAYPOINT_TRANSMIT_RANGE);

        if (attribute.getModifier(WAYPOINT_TRANSMIT_MODIFIER_KEY) == null)
            attribute.addTransientModifier(new AttributeModifier(WAYPOINT_TRANSMIT_MODIFIER_KEY, -1, AttributeModifier.Operation.ADD_SCALAR));
    }

    protected void recoverPlayerWaypoint(Player player)
    {
        this.acquireAttributeOrThrow(player, Attribute.WAYPOINT_TRANSMIT_RANGE)
                .removeModifier(WAYPOINT_TRANSMIT_MODIFIER_KEY);
    }

    protected void enableDisguiseWaypoint(DisguiseState state)
    {
        var disguiseWaypoint = state.waypointUpdater();
        disguiseWaypoint.enabled(true);
    }

    public void disableDisguiseWaypoint(DisguiseState state)
    {
        var disguiseWaypoint = state.waypointUpdater();
        disguiseWaypoint.enabled(false);
    }

    @Override
    @NotNull
    public List<AbstractS2CCommand<?>> getInitialSyncCommands(DisguiseState state)
    {
        //logger.info("SID: " + state.getSkillLookupIdentifier() + " :: DID: " + state.getDisguiseIdentifier());
        if (skillHandler.hasSpeficSkill(state.skillLookupIdentifier(), SkillNames.FAKE_EQUIP)
                && clientHandler.getPlayerVersion(state.getPlayer()) < Constants.ApiLevel.EQUIPMENT_AND_SKIN_ARE_NOW_PROPERTY.protocolVersion)
        {
            var eqiupment = state.getDisguiseEquipment();

            var list = new ObjectArrayList<AbstractS2CCommand<?>>();

            this.addIfPresents(eqiupment, list, EquipmentSlot.HAND);
            this.addIfPresents(eqiupment, list, EquipmentSlot.OFF_HAND);

            this.addIfPresents(eqiupment, list, EquipmentSlot.HEAD);
            this.addIfPresents(eqiupment, list, EquipmentSlot.CHEST);
            this.addIfPresents(eqiupment, list, EquipmentSlot.LEGS);
            this.addIfPresents(eqiupment, list, EquipmentSlot.FEET);

            return list;
        }

        return List.of();
    }

    private void addIfPresents(DisguiseEquipment equipment,
                               ObjectArrayList<AbstractS2CCommand<?>> list,
                               EquipmentSlot slot)
    {
        var item = equipment.getItem(slot);

        if (item.getType() != Material.AIR)
            list.add(new ServerSetEquipCommand(item, slot));
    }

    @Override
    public void buildDisguise(DisguiseState state, @Nullable Entity targetEntity) throws ParseErrorException
    {
        //被动技能
        var abilities = abilityHandler.getAbilitiesFor(state.skillLookupIdentifier());
        state.getAbilityUpdater().setAbilities(abilities);

        var abilityOptions = abilityHandler.getOptionsFor(state.skillLookupIdentifier());
        abilityOptions.forEach((id, config) -> state.getAbilityUpdater().setAbilityConfig(id.asString(), config));

        var config = skillHandler.getConfiguration(state.skillLookupIdentifier());
        if (config == null) // Only setup if there's any skill config
        {
            if (FeatherMorphMain.getInstance().debugOutputEnabled())
                logger.warn("The skill lookup '{}' does not have a matching skill/ability configuration", state.skillLookupIdentifier());

            return;
        }

        var skill = skillHandler.getSkill(config.getSkillIdentifier().key().asString());
        ISkillAbilityOption option = skillHandler.lookupOptionFor(skill, state.skillLookupIdentifier());
        state.bindSkill((ISkill<? super ISkillAbilityOption>) skill, option);
        state.setDefaultSkillCooldown(config.getSkillCooldown());
    }
}
