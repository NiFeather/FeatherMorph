package xyz.nifeather.morph.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mannequin;
import org.jetbrains.annotations.NotNull;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Messages.FormattableMessage;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.api.FeatherMorphAPI;
import xyz.nifeather.morph.commands.brigadier.BrigadierCommand;
import xyz.nifeather.morph.storage.skill.SkillAbilityConfigContainer;
import xyz.nifeather.morph.storage.skill.SkillsConfigurationStoreNew;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DebugTestCommand extends BrigadierCommand
{
    @Override
    public String getPermissionRequirement()
    {
        return null;
    }

    @Resolved
    private MorphManager morphManager;

    @Override
    public boolean register(Commands dispatcher)
    {
        dispatcher.register(
                Commands.literal("listRaw")
                        .then(
                                Commands.argument("who", ArgumentTypes.player())
                                        .executes(ctx ->
                                        {
                                            var players = ctx.getArgument("who", PlayerSelectorArgumentResolver.class)
                                                    .resolve(ctx.getSource());

                                            if (players.isEmpty()) return 0;
                                            var player = players.getFirst();

                                            var source = ctx.getSource().getSender();
                                            for (var meta : morphManager.getPlayerMeta(player).getUnlockedDisguises())
                                            {
                                                source.sendMessage(meta.rawIdentifier);
                                            }


                                            return 1;
                                        })
                        ).build()
        );

        dispatcher.register(
                Commands.literal("testDataStore").executes(ctx ->
                {
                    try
                    {
                        var store = morphManager.getDataStore();
                        var sender = ctx.getSource().getSender();

                        Bukkit.getAsyncScheduler().runDelayed(plugin, task ->
                        {
                            sender.sendMessage("Test1");

                            var UUID = java.util.UUID.fromString("0-0-0-0-0");
                            store.getPlayerMeta(Bukkit.getOfflinePlayer(UUID));
                        }, 50, TimeUnit.MILLISECONDS);

                        sender.sendMessage("Test2");
                        var UUID = java.util.UUID.fromString("0-0-0-0-0");
                        store.getPlayerMeta(Bukkit.getOfflinePlayer(UUID));

                    }
                    catch (Throwable t)
                    {
                        logger.warn("Exception!", t);
                    }
                    return 1;
                })
                        .build()
        );

        dispatcher.register(
                Commands.literal("validate_skill_ability")
                        .executes(this::validateSkillAbilities)
                        .build()
        );

        dispatcher.register(
                Commands.literal("mannequinswing")
                        .then(
                                Commands.argument("entity", ArgumentTypes.entity())
                                        .executes(this::execMannequinSwing)
                        )
                        .build()
        );

        dispatcher.register(
                Commands.literal("listProperties")
                        .then(
                                Commands.argument("entity", ArgumentTypes.entity())
                                        .executes(this::listProperties)
                        ).build()
        );

        return true;
    }

    private int listProperties(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        var entity = context.getArgument("entity", EntitySelectorArgumentResolver.class)
                .resolve(context.getSource());

        var sender = context.getSource().getSender();

        if (entity.isEmpty())
        {
            sender.sendMessage("No entities!");
        }

        entity.forEach(e ->
        {
            sender.sendMessage("Listing properties for %s".formatted(e.getName()));
            var state = morphManager.getDisguiseStateFor(e);
            if (state == null)
            {
                sender.sendMessage("No disguise for %s".formatted(e.getName()));
                return;
            }

            state.disguisePropertyHandler().getAll().forEach((property, value) ->
            {
                sender.sendMessage(":: %s -> %s".formatted(property.identifier(), value));
            });
        });

        return 1;
    }

    private int execMannequinSwing(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        var entity = context.getArgument("entity", EntitySelectorArgumentResolver.class)
                .resolve(context.getSource());

        entity.forEach(e ->
        {
            if (!(e instanceof Mannequin mannequin)) return;

            mannequin.swingMainHand();
            context.getSource().getSender().sendMessage("Call swing!");
        });

        return 1;
    }

    private int validateSkillAbilities(CommandContext<CommandSourceStack> commandSourceStackCommandContext)
    {
        var api = FeatherMorphAPI.instance();
        assert api != null;

        var skillConfigs = api.directAccess().getGlobalDependency(SkillsConfigurationStoreNew.class);

        var abilityManager = api.directAccess().abilityManager();
        List<String> targets = Arrays.stream(EntityType.values())
                .filter(t -> !t.equals(EntityType.UNKNOWN))
                .map(t -> t.key().asString())
                .toList();

        var skillHandler = api.directAccess().skillHandler();

        Bukkit.broadcast(Component.text("Testing abilitymanager#getOptionsFor for any mc id"));

        try
        {
            for (String id : targets)
            {
                var xx = abilityManager.getOptionsFor(id);
                logger.info("%s no error: %s".formatted(id, xx));
            }
        }
        catch (Exception e)
        {
            Bukkit.broadcast(Component.text("Error! " + e.getMessage()));
            logger.error("Error!", e);
        }

        Bukkit.broadcast(Component.text("Now for skills..."));

        try
        {
            for (String id : targets)
            {
                var skill = skillHandler.lookupDisguiseSkill(id);

                if (!skillHandler.hasSkill(id))
                    continue;

                var option = skillHandler.lookupOptionFor(skill, id);
                logger.info("%s no error: %s".formatted(id, option));
            }
        }
        catch (Exception e)
        {
            Bukkit.broadcast(Component.text("Error! " + e.getMessage()));
            logger.error("Error!", e);
        }

        Bukkit.broadcast(Component.text("Test complete"));

        return 1;
    }

    @Override
    public @NotNull String name()
    {
        return "fmdebug";
    }

    @Override
    public FormattableMessage getHelpMessage()
    {
        return null;
    }
}
