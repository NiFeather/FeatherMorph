package xyz.nifeather.morph.commands;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;
import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Messages.FormattableMessage;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.commands.brigadier.BrigadierCommand;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.messages.strings.CommandStrings;
import xyz.nifeather.morph.messages.strings.ExceptionStrings;
import xyz.nifeather.morph.messages.strings.MorphStrings;
import xyz.nifeather.morph.misc.ExecutionErrorException;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.OffTreeProperties;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;

import java.util.UUID;

public class CloneMorphCommand extends BrigadierCommand
{
    @Override
    public boolean register(Commands dispatcher)
    {
        dispatcher.register(
                Commands.literal(name())
                        .requires(this::checkPermission)
                        .then(
                                Commands.literal("apply")
                                        .then(
                                                Commands.argument("entity", ArgumentTypes.entities())
                                                        .executes(this::executeApply)
                                        )
                        )
                        .then(
                                Commands.literal("clear")
                                        .then(
                                                Commands.argument("entity", ArgumentTypes.entities())
                                                        .executes(this::executeClear)
                                        )
                        ).build()
        );

        return true;
    }

    private int executeClear(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        var entities = context.getArgument("entity", EntitySelectorArgumentResolver.class)
                .resolve(context.getSource());

        var backend = morphManager.getDefaultBackend();

        for (Entity entity : entities)
        {
            if (!(entity instanceof LivingEntity living)) continue;
            backend.unDisguise(living);
        }

        return 0;
    }

    @Resolved
    private MorphManager morphManager;

    private int executeApply(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        var entities = context.getArgument("entity", EntitySelectorArgumentResolver.class)
                .resolve(context.getSource());

        var backend = morphManager.getDefaultBackend();
        var executorSession = morphManager.getDisguiseStateFor(context.getSource().getExecutor());

        if (executorSession == null)
        {
            MessageUtils.send(context.getSource().getSender(), CommandStrings.notDisguised());
            return 0;
        }

        var properties = executorSession.disguisePropertyHandler().getAll();

        int successCount = 0;
        for (Entity entity : entities)
        {
            if (!(entity instanceof LivingEntity living))
                continue;

            if (backend.isDisguised(living))
                backend.unDisguise(living);

            var instance = backend.createInstance(executorSession.getEntityType());

            var str = "%s:%s".formatted(entity.getUniqueId(), RandomStringUtils.secure().next(12));
            instance.writeProperty(OffTreeProperties.VIRTUAL_ENTITY_UUID, UUID.nameUUIDFromBytes(str.getBytes()));

            properties.forEach((p, v) -> instance.writeProperty((SingleProperty<Object>)p, v));

            try
            {
                backend.disguise(living, instance);
                successCount++;
            }
            catch (ExecutionErrorException e)
            {
                MessageUtils.send(context.getSource().getSender(),
                        MorphStrings.errorWhileDisguisingWithError()
                                .resolve("error", e.getMessage())
                );
            }
        }

        MessageUtils.send(context.getSource().getSender(), "Ok 为 %s 个实体应用了变形".formatted(successCount));

        return 0;
    }

    @Override
    public @Nullable String getPermissionRequirement()
    {
        return CommonPermissions.ADMIN;
    }

    @Override
    public @NotNull String name()
    {
        return "clone-morph";
    }

    @Override
    public FormattableMessage getHelpMessage()
    {
        return null;
    }
}
