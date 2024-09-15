package xiamomc.morph.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.messages.CommandStrings;
import xiamomc.morph.messages.EmoteStrings;
import xiamomc.morph.messages.HelpStrings;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.IPluginCommand;
import xiamomc.pluginbase.Messages.FormattableMessage;

import java.util.List;

public class AnimationCommand extends MorphPluginObject implements IPluginCommand
{
    @Override
    public String getCommandName()
    {
        return "play-action";
    }

    @Override
    public FormattableMessage getHelpMessage()
    {
        return HelpStrings.animationDescription();
    }

    @Resolved
    private MorphManager morphManager;

    @Override
    public List<String> onTabComplete(List<String> args, CommandSender source)
    {
        if (!(source instanceof Player player))
            return List.of("nil");

        var state = morphManager.getDisguiseStateFor(player);
        if (state == null) return List.of();

        if (args.size() >= 2) return List.of();

        var animations = state.getProvider()
                              .getAnimationProvider()
                              .getAnimationSetFor(state.getDisguiseIdentifier())
                              .getAvailableAnimationsForClient();

        var arg = args.get(0);
        return animations.stream().filter(id -> id.startsWith(arg)).toList();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (!(commandSender instanceof Player player))
            return true;

        var state = morphManager.getDisguiseStateFor(player);
        if (state == null)
        {
            player.sendMessage(MessageUtils.prefixes(player, CommandStrings.notDisguised()));
            return true;
        }

        if (args.length == 0)
        {
            player.sendMessage(MessageUtils.prefixes(player, CommandStrings.listNoEnoughArguments()));
            return true;
        }

        var animationId = args[0];

        var animationSet = state.getProvider()
                .getAnimationProvider()
                .getAnimationSetFor(state.getDisguiseIdentifier());

        var animations = animationSet.getAvailableAnimationsForClient();

        if (!animations.contains(animationId))
        {
            player.sendMessage(MessageUtils.prefixes(player, CommandStrings.noSuchAnimation()));
            return true;
        }

        var sequencePair = animationSet.sequenceOf(animationId);
        if (!state.tryScheduleSequence(animationId, sequencePair.left(), sequencePair.right()))
            player.sendMessage(MessageUtils.prefixes(player, EmoteStrings.notAvailable()));

        return false;
    }
}
