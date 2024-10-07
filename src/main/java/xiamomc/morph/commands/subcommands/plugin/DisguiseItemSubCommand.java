package xiamomc.morph.commands.subcommands.plugin;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.messages.CommandStrings;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.misc.NmsRecord;
import xiamomc.morph.utilities.ItemUtils;
import xiamomc.pluginbase.Command.ISubCommand;
import xiamomc.pluginbase.Messages.FormattableMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class DisguiseItemSubCommand extends MorphPluginObject implements ISubCommand
{
    public DisguiseItemSubCommand()
    {
        CompletableFuture.supplyAsync(() ->
        {
            Map<String, Material> items = new Object2ObjectOpenHashMap<>();

            for (Material value : Material.values())
            {
                if (!value.isItem()) continue;

                items.put(value.getKey().asString(), value);
            }

            return items;
        }).thenAccept(this.availableItems::putAll);
    }

    private final Map<String, Material> availableItems = new Object2ObjectOpenHashMap<>();

    @Override
    public @NotNull String getCommandName()
    {
        return "disguise_item";
    }

    @Override
    public FormattableMessage getHelpMessage()
    {
        return new FormattableMessage(plugin, "disguise item");
    }

    private final List<String> emptyList = List.of();

    @Override
    public @Nullable List<String> onTabComplete(List<String> args, CommandSender source)
    {
        if (args.size() > 1) return emptyList;

        return availableItems.keySet().stream().filter(s -> s.contains(args.get(0))).toList();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args)
    {
        if (args.length < 1)
        {
            sender.sendMessage(MessageUtils.prefixes(sender, CommandStrings.listNoEnoughArguments()));
            return true;
        }

        if (!(sender instanceof Player player))
        {
            sender.sendMessage(MessageUtils.prefixes(sender, CommandStrings.noPermissionMessage()));
            return true;
        }

        var itemId = args[0];
        var material = availableItems.getOrDefault(itemId, null);

        if (material == null)
        {
            sender.sendMessage(MessageUtils.prefixes(sender, CommandStrings.illegalArgumentString().resolve("detail", "'%s' -> null".formatted(itemId))));
            return true;
        }

        var item = ItemUtils.buildSkillItemFrom(ItemStack.of(material));
        player.getInventory().addItem(item);

        sender.sendMessage(MessageUtils.prefixes(sender, CommandStrings.grantItemSuccess()));

        return true;
    }
}
