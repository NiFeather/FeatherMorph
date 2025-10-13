package xyz.nifeather.morph.commands;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Messages.FormattableMessage;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.commands.brigadier.IConvertibleBrigadier;
import xyz.nifeather.morph.commands.brigadier.arguments.DisguiseIdentifierArgumentType;
import xyz.nifeather.morph.messages.strings.CommandStrings;
import xyz.nifeather.morph.messages.strings.CommonStrings;
import xyz.nifeather.morph.messages.strings.HelpStrings;
import xyz.nifeather.morph.messages.MessageUtils;

public class DiscardMorphCommand extends MorphPluginObject implements IConvertibleBrigadier
{
    @Override
    public @NotNull String name()
    {
        return "discard-morph";
    }

    @Override
    public boolean register(Commands dispatcher)
    {
        dispatcher.register(
                Commands.literal(name())
                        .requires(this::checkPermission)
                        .then(
                                Commands.argument("discard-target", DisguiseIdentifierArgumentType.FOR_PLAYER)
                                        .executes(this::execUserDiscard)
                        )
                        .build()
        );

        return IConvertibleBrigadier.super.register(dispatcher);
    }

    @Resolved
    private MorphManager morphManager;

    private int execUserDiscard(CommandContext<CommandSourceStack> context)
    {
        var sender = context.getSource().getSender();
        if (!(context.getSource().getExecutor() instanceof Player player))
        {
            MessageUtils.send(sender, CommonStrings.playerNotDefinedString());
            return 0;
        }

        String targetID = DisguiseIdentifierArgumentType.getArgument(context, "discard-target");
        var formattable = morphManager.revokeMorphFromPlayer(player, targetID)
                ? CommandStrings.revokeSuccessString()
                : CommandStrings.revokeFailString();

        MessageUtils.send(sender, formattable.resolve("what", targetID).resolve("who", player.getName()));

        return 1;
    }

    @Override
    public FormattableMessage getHelpMessage()
    {
        return HelpStrings.discardMorphCommandDescription();
    }
}
