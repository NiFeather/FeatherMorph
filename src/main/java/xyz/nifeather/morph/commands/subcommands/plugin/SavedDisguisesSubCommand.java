package xyz.nifeather.morph.commands.subcommands.plugin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Messages.FormattableMessage;
import xyz.nifeather.morph.api.FeatherMorphAPI;
import xyz.nifeather.morph.commands.brigadier.BrigadierCommand;
import xyz.nifeather.morph.commands.brigadier.arguments.RelaxedStringArgumentType;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.messages.strings.CommandStrings;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SavedDisguisesSubCommand extends BrigadierCommand
{
    @Override
    public @Nullable String getPermissionRequirement()
    {
        return CommonPermissions.ADMIN;
    }

    @Override
    public @NotNull String name()
    {
        return "saved_disguises";
    }

    @Override
    public FormattableMessage getHelpMessage()
    {
        return null;
    }

    @Override
    public void registerAsChild(ArgumentBuilder<CommandSourceStack, ?> parentBuilder)
    {
        parentBuilder.then(
                Commands.literal(name())
                        .requires(this::checkPermission)
                        .then(
                                Commands.literal("save")
                                        .then(
                                                Commands.argument("uuid", ArgumentTypes.uuid())
                                                        .executes(this::saveUUID)
                                        )
                                        .then(
                                                Commands.argument("entities", ArgumentTypes.entities())
                                                        .executes(this::saveEntities)
                                        )
                                        .then(
                                                Commands.argument("entity", ArgumentTypes.entity())
                                                        .then(
                                                                Commands.argument("save-name", RelaxedStringArgumentType.relaxed())
                                                                        .executes(this::saveEntityWithName)
                                                        )
                                        )
                        )
                        .then(
                                Commands.literal("disguise")
                                        .then(
                                                Commands.argument("saved-disguise-id", RelaxedStringArgumentType.relaxed())
                                                        .suggests(this::suggestSavedDisguise)
                                                        .executes(this::disguiseSelf)
                                                        .then(
                                                                Commands.argument("player", ArgumentTypes.players())
                                                                        .executes(this::disguiseOthers)
                                                        )
                                        )
                        )
        );
    }

    private CompletableFuture<Suggestions> suggestSavedDisguise(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder)
    {
        var morphManager = Objects.requireNonNull(FeatherMorphAPI.instance())
                .directAccess()
                .morphManager();

        var input = builder.getRemaining();

        return CompletableFuture.supplyAsync(() ->
        {
            morphManager.availableSavedDisguises().stream()
                    .filter(name -> name.startsWith(input))
                    .forEach(builder::suggest);

            return builder.build();
        });
    }

    private int disguiseOthers(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        var stateUUID = StringArgumentType.getString(context, "saved-disguise-id");
        var players = context.getArgument("player", PlayerSelectorArgumentResolver.class)
                .resolve(context.getSource());

        disguiseFromDisk(stateUUID, players);

        return 0;
    }

    private int disguiseSelf(CommandContext<CommandSourceStack> context)
    {
        if (!(context.getSource().getExecutor() instanceof Player player))
        {
            MessageUtils.send(context.getSource().getSender(), "Only player can use this command");
            return 0;
        }

        var stateUUID = StringArgumentType.getString(context, "saved-disguise-id");
        disguiseFromDisk(stateUUID, List.of(player));

        return 1;
    }

    private boolean disguiseFromDisk(String name, List<Player> targets)
    {
        var morphManager = Objects.requireNonNull(FeatherMorphAPI.instance())
                .directAccess()
                .morphManager();

        var state = morphManager.getSavedDisguise(name);
        if (state == null) return false;

        for (Player target : targets)
            morphManager.disguiseFromSavedDisguise(target, state);

        return true;
    }

    private int saveEntityWithName(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        var morphManager = Objects.requireNonNull(FeatherMorphAPI.instance())
                .directAccess()
                .morphManager();

        var entities = context.getArgument("entity", EntitySelectorArgumentResolver.class)
                .resolve(context.getSource());

        if (entities.isEmpty())
            return 0;

        var entity = entities.getFirst();

        var name = StringArgumentType.getString(context, "save-name");
        var state = morphManager.getDisguiseStateFor(entity);

        if (state != null && morphManager.savedDisguiseStore().save(state, name))
            MessageUtils.send(context.getSource().getSender(), CommandStrings.success());
        else
            MessageUtils.send(context.getSource().getSender(), CommandStrings.unknownError());

        return 0;
    }

    private int saveEntities(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        var morphManager = Objects.requireNonNull(FeatherMorphAPI.instance())
                .directAccess()
                .morphManager();

        int success = 0;
        int fail = 0;

        for (Entity entity : context.getArgument("entities", EntitySelectorArgumentResolver.class)
                .resolve(context.getSource()))
        {
            var state = morphManager.getDisguiseStateFor(entity);
            if (state == null)
            {
                fail++;
                continue;
            }

            if (morphManager.savedDisguiseStore().save(state))
                success++;
            else
                fail++;
        }

        MessageUtils.send(context.getSource().getSender(), "OK %s Success %s Failed".formatted(success, fail));

        return 0;
    }

    private int saveUUID(CommandContext<CommandSourceStack> context)
    {
        var uuid = context.getArgument("uuid", UUID.class);

        var success = Objects.requireNonNull(FeatherMorphAPI.instance())
                .directAccess()
                .morphManager()
                .saveDisguise(uuid);

        if (success)
            MessageUtils.send(context.getSource().getSender(), CommandStrings.success());
        else
            MessageUtils.send(context.getSource().getSender(), CommandStrings.unknownError());

        return 0;
    }
}
