package xiamomc.morph.providers;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.RevealingHandler;
import xiamomc.morph.abilities.AbilityManager;
import xiamomc.morph.backends.DisguiseBackend;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.MorphStrings;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.network.commands.S2C.AbstractS2CCommand;
import xiamomc.morph.network.server.MorphClientHandler;
import xiamomc.morph.network.server.ServerSetEquipCommand;
import xiamomc.morph.skills.MorphSkillHandler;
import xiamomc.morph.skills.SkillType;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Messages.MessageStore;

import java.util.List;

/**
 * 提供一个默认的DisguiseProvider
 * 包括自动设置Bossbar、技能等
 */
public abstract class DefaultDisguiseProvider extends DisguiseProvider
{
    @Resolved
    private MorphSkillHandler skillHandler;

    @Resolved
    private AbilityManager abilityHandler;

    @Resolved
    private MorphClientHandler clientHandler;

    @Resolved
    private MessageStore<?> messageStore;

    @Resolved
    private RevealingHandler revealingHandler;

    @Override
    public @NotNull DisguiseBackend<?, ?> getPreferredBackend()
    {
        return getMorphManager().getDefaultBackend();
    }

    @Override
    public boolean updateDisguise(Player player, DisguiseState state)
    {
        var disguise = state.getDisguiseWrapper();
        var option = clientHandler.getPlayerOption(player, true);

        var haveSkill = state.haveSkill();

        if (option.displayDisguiseOnHUD && plugin.getCurrentTick() % (haveSkill ? 2 : 5) == 0)
        {
            var locale = MessageUtils.getLocale(player);

            //更新actionbar信息
            var msg = haveSkill
                    ? (state.getSkillCooldown() <= 0
                        ? MorphStrings.disguisingWithSkillAvaliableString()
                        : MorphStrings.disguisingWithSkillPreparingString())
                    : MorphStrings.disguisingAsString();

            var revLevel = revealingHandler.getRevealingLevel(player);
            var disguiseRevealed = revLevel == RevealingHandler.RevealingLevel.REVEALED || revLevel == RevealingHandler.RevealingLevel.SUSPECT;
            var display = disguiseRevealed
                    ? Component.empty()
                        .append(state.getPlayerDisplay())
                        .append((revLevel == RevealingHandler.RevealingLevel.REVEALED ? MorphStrings.revealed() : MorphStrings.partialRevealed()).toComponent(locale))
                    : state.getPlayerDisplay();

            player.sendActionBar(msg.resolve("what", display).toComponent(locale, messageStore));
        }

        try
        {
            disguise.update(state, player);
        }
        catch (Throwable t)
        {
            logger.error("Error occurred while updating disguise!");
            t.printStackTrace();

            return false;
        }

        return true;
    }

    @Override
    @NotNull
    public List<AbstractS2CCommand<?>> getInitialSyncCommands(DisguiseState state)
    {
        //logger.info("SID: " + state.getSkillLookupIdentifier() + " :: DID: " + state.getDisguiseIdentifier());
        if (skillHandler.hasSpeficSkill(state.getSkillLookupIdentifier(), SkillType.INVENTORY))
        {
            var eqiupment = state.getDisguisedItems();

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

    private void addIfPresents(EntityEquipment equipment, ObjectArrayList<AbstractS2CCommand<?>> list, EquipmentSlot slot)
    {
        var item = equipment.getItem(slot);

        if (item.getType() != Material.AIR)
            list.add(new ServerSetEquipCommand(item, slot));
    }

    @Override
    public void postConstructDisguise(DisguiseState state, @Nullable Entity targetEntity)
    {
        //被动技能
        var abilities = abilityHandler.getAbilitiesFor(state.getSkillLookupIdentifier());
        state.getAbilityUpdater().setAbilities(abilities);
        state.setSkill(skillHandler.getSkill(state.getSkillLookupIdentifier()));
    }
}
