package xiamomc.morph.providers;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.abilities.AbilityHandler;
import xiamomc.morph.backends.DisguiseBackend;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.MorphStrings;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.misc.DisguiseTypes;
import xiamomc.morph.network.commands.S2C.AbstractS2CCommand;
import xiamomc.morph.network.server.MorphClientHandler;
import xiamomc.morph.network.server.ServerSetEquipCommand;
import xiamomc.morph.skills.MorphSkillHandler;
import xiamomc.morph.skills.SkillType;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Messages.MessageStore;
import xiamomc.pluginbase.Utilities.ColorUtils;

import java.util.List;

/**
 * 提供一个默认的DisguiseProvider
 * 包括自动设置Bossbar、飞行技能和应用针对LibsDisguises的一些workaround
 */
public abstract class DefaultDisguiseProvider extends DisguiseProvider
{
    @Override
    public boolean unMorph(Player player, DisguiseState state)
    {
        super.unMorph(player, state);

        state.getAbilities().forEach(a -> a.revokeFromPlayer(player, state));
        state.setAbilities(List.of());

        return true;
    }

    @Resolved
    private MorphSkillHandler skillHandler;

    @Resolved
    private AbilityHandler abilityHandler;

    @Resolved
    private Scoreboard scoreboard;

    @Resolved
    private MorphClientHandler clientHandler;

    @Resolved
    private MessageStore<?> messageStore;

    protected DisguiseBackend<?, ?> getBackend()
    {
        return getMorphManager().getCurrentBackend();
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

            player.sendActionBar(msg.resolve("what", state.getPlayerDisplay()).toComponent(locale, messageStore));
        }

        try
        {
            disguise.update(state.getDisguiseType() != DisguiseTypes.LD, state, player);
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
        var disguise = state.getDisguiseWrapper();
        var backend = getMorphManager().getCurrentBackend();

        //被动技能
        var abilities = abilityHandler.getAbilitiesFor(state.getSkillLookupIdentifier());
        state.setAbilities(abilities);
        state.setSkill(skillHandler.getSkill(state.getSkillLookupIdentifier()));

        //发光颜色
        ChatColor glowColor = null;
        Entity teamTargetEntity = targetEntity;
        var disguiseID = state.getDisguiseIdentifier();
        var morphDisguiseType = DisguiseTypes.fromId(disguiseID);

        switch (morphDisguiseType)
        {
            //LD伪装为玩家时会自动复制他们当前的队伍到名字里
            //为了确保一致，除非targetEntity不是null，不然尝试将teamTargetEntity设置为目标玩家
            case PLAYER ->
            {
                if (teamTargetEntity == null)
                    teamTargetEntity = Bukkit.getPlayerExact(DisguiseTypes.PLAYER.toStrippedId(disguiseID));
            }

            //LD的伪装直接从伪装flag里获取发光颜色
            //只要和LD直接打交道事情就变得玄学了起来..
            case LD ->
            {
                glowColor = disguise.getGlowingColor();
                teamTargetEntity = null;
            }
        }

        //从teamTargetEntity获取发光颜色
        if (teamTargetEntity != null)
        {
            var team = scoreboard.getEntityTeam(teamTargetEntity);

            //如果伪装是复制来的，并且目标实体有伪装，则将颜色设置为他们伪装的发光颜色
            if (state.shouldHandlePose() && backend.isDisguised(teamTargetEntity))
            {
                //backend.isDisguised -> backend.getWrapper != null
                glowColor = backend.getWrapper(teamTargetEntity).getGlowingColor();
            }
            else
            {
                var color = team == null ? null : (team.hasColor() ? team.color() : null);

                glowColor = ColorUtils.toChatColor(color); //否则，尝试设置成我们自己的
            }
        }

        //设置发光颜色
        state.setCustomGlowColor(ColorUtils.fromChatColor(glowColor));
        disguise.setGlowingColor(glowColor);
    }
}
