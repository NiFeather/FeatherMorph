package xyz.nifeather.morph.commands.subcommands.plugin.applets;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Messages.FormattableMessage;
import xyz.nifeather.morph.api.FeatherMorphAPI;
import xyz.nifeather.morph.commands.brigadier.BrigadierCommand;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;
import xyz.nifeather.morph.storage.playerdata.PlayerDataStoreNew;
import xyz.nifeather.morph.storage.playerdata.PlayerMeta;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class RemoveDuplicateEntriesAppletCommand extends BrigadierCommand
{
    @Override
    public @Nullable String getPermissionRequirement()
    {
        return CommonPermissions.ADMIN;
    }

    @Override
    public void registerAsChild(ArgumentBuilder<CommandSourceStack, ?> parentBuilder)
    {
        parentBuilder.then(
                Commands.literal(name())
                        .executes(this::execute)
        );

        super.registerAsChild(parentBuilder);
    }

    private int execute(CommandContext<CommandSourceStack> context)
    {
        var sender = context.getSource().getSender();

        if (!Bukkit.getOnlinePlayers().isEmpty())
        {
            if (!(sender instanceof Player))
            {
                sender.sendMessage("This command can only be run when no players are online.");
                return 0;
            }

            sender.sendMessage("Please keep online while we're processing player data ;)");
            sender.sendMessage("And make sure nobody join or leave the server during the process");
        }

        sender.sendMessage("Loading player data...");

        var morphManager = FeatherMorphAPI.instance().directAccess().morphManager();
        var ds = morphManager.getDataStore();

        if (!(ds instanceof PlayerDataStoreNew dataStore))
        {
            sender.sendMessage("Storage not PDSN");
            return 0;
        }

        dataStore.loadAll();
        var data = new ObjectArrayList<>(dataStore.listAll());

        sender.sendMessage("Done, now processing with CompletableFuture...");

        var future = CompletableFuture.supplyAsync(() -> process(data, sender));

        future.thenAccept(finishedMetaList ->
        {
            sender.sendMessage("Now schedule save process...");
            addSchedule(() ->
            {
                save(finishedMetaList, sender);
                sender.sendMessage("All Done!");
            });
        });

        return 1;
    }

    private List<PlayerMeta> process(List<PlayerMeta> metaList, CommandSender sender)
    {
        for (PlayerMeta playerMeta : metaList)
        {
            if (!playerMeta.disguiseListLocked())
                logger.warn("Processing a not locked PlayerMeta! {}", playerMeta.uniqueId);

            var prevUnlocked = playerMeta.getUnlockedDisguises();
            int prevSize = prevUnlocked.size();

            Collection<DisguiseMeta> uniqueMetas = new LinkedHashSet<>(prevUnlocked);
            int uniqueSize = uniqueMetas.size();

            var sorted = uniqueMetas.stream().sorted(Comparator.comparing(DisguiseMeta::getIdentifier)).toList();
            prevUnlocked.forEach(playerMeta::removeDisguise);
            sorted.forEach(playerMeta::addDisguise);

            // For some reason, we can't use the method down below to filter the duplicates
            // var duplicates = prevUnlocked.clone();
            // duplicates.removeAll(uniqueMetas);
            // sender.sendMessage("%s duplicates".formatted(duplicates.size()));
            // duplicates.forEach(playerMeta::removeDisguise);

            if (prevSize != uniqueSize)
                sender.sendMessage("%s(%s): prev %s -> unique %s".formatted(playerMeta.playerName, playerMeta.uniqueId, prevUnlocked.size(), uniqueMetas.size()));
        }

        sender.sendMessage("Done processing all PlayerMeta!");

        return metaList;
    }

    private void save(List<PlayerMeta> processedList, CommandSender sender)
    {
        sender.sendMessage("Saving data...");

        var morphManager = FeatherMorphAPI.instance().directAccess().morphManager();
        var ds = morphManager.getDataStore();

        if (!(ds instanceof PlayerDataStoreNew dataStore))
        {
            sender.sendMessage("Storage not PDSN");
            return;
        }

        processedList.forEach(meta ->
        {
            logger.info("%s has %s entries".formatted(meta.playerName, meta.getUnlockedDisguises().size()));
            dataStore.save(meta);
        });
        dataStore.clearCache();

        sender.sendMessage("Done!");
    }

    @Override
    public @NotNull String name()
    {
        return "remove_duplicate_entries";
    }

    @Override
    public FormattableMessage getHelpMessage()
    {
        return null;
    }
}
