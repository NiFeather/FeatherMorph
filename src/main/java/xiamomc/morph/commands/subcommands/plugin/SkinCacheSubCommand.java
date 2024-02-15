package xiamomc.morph.commands.subcommands.plugin;

import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.destroystokyo.paper.profile.PlayerProfile;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.minecraft.nbt.NbtUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.commands.subcommands.plugin.skincache.cmdTree.CommandBuilder;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.messages.CommandStrings;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.SkinCacheStrings;
import xiamomc.morph.misc.CapeURL;
import xiamomc.morph.misc.DisguiseTypes;
import xiamomc.morph.misc.MorphParameters;
import xiamomc.morph.misc.permissions.CommonPermissions;
import xiamomc.morph.misc.skins.PlayerSkinProvider;
import xiamomc.morph.misc.skins.SingleSkin;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Bindables.Bindable;
import xiamomc.pluginbase.Command.ISubCommand;
import xiamomc.pluginbase.Messages.FormattableMessage;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class SkinCacheSubCommand extends MorphPluginObject implements ISubCommand
{
    @Override
    public @NotNull String getCommandName()
    {
        return "skin_cache";
    }

    @Override
    public @Nullable String getPermissionRequirement()
    {
        return CommonPermissions.SKIN_CACHE;
    }

    @Override
    public FormattableMessage getHelpMessage()
    {
        return new FormattableMessage(plugin, "skin_cache");
    }

    private final PlayerSkinProvider skinProvider = PlayerSkinProvider.getInstance();

    private List<ISubCommand> genSubCmd()
    {
        return CommandBuilder.builder()
                .startNew()
                .name("list")
                //.permission("dddd")
                .onFilter(args -> List.of("all"))
                .executes((sender, args) ->
                {
                    var currentTime = System.currentTimeMillis();
                    var skins = skinProvider.getAllSkins();
                    var str = Component.empty();

                    sender.sendMessage(
                            MessageUtils.prefixes(sender, SkinCacheStrings.listHeader().resolve("count", skins.size() + ""))
                    );

                    var limit = args.isEmpty()
                            ? 20
                            : args.get(0).equalsIgnoreCase("all")
                                ? Integer.MAX_VALUE
                                : 20;

                    var current = 0;

                    var overallLine = SkinCacheStrings.skinInfoOverallLine();
                    var expiredString = SkinCacheStrings.skinExpired().toComponent(MessageUtils.getLocale(sender));

                    overallLine.resolve("x_more", Component.empty());

                    var it = skins.iterator();
                    while (it.hasNext())
                    {
                        current++;

                        var next = it.next();
                        str = str.append(Component.text(next.name));

                        if (currentTime > next.expiresAt)
                            str = str.append(expiredString);

                        if (it.hasNext() && !(current == limit))
                            str = str.append(Component.text(", "));

                        if (current == limit)
                        {
                            var remaining = skins.size() - current;
                            overallLine.resolve("x_more",
                                    SkinCacheStrings.andXMore()
                                            .resolve("count", remaining + "")
                                            .withLocale(MessageUtils.getLocale(sender)));

                            break;
                        }
                    }

                    overallLine.resolve("info_line", str);

                    sender.sendMessage(MessageUtils.prefixes(sender, overallLine));

                    return true;
                })

                .startNew()
                .name("drop")
                //.permission("dddd")
                .onFilter(args ->
                {
                    var stream = skinProvider.getAllSkins()
                            .stream()
                            .map(sk -> sk.name);

                    var filterName = args.isEmpty() ? "" : args.get(0);

                    var list = new ObjectArrayList<>(stream
                            .filter(name -> name.toLowerCase().contains(filterName))
                            .toList());

                    list.add("*");

                    return list;
                })
                .executes((sender, args) ->
                {
                    if (args.isEmpty())
                    {
                        sender.sendMessage(MessageUtils.prefixes(sender, CommandStrings.listNoEnoughArguments()));
                        return true;
                    }

                    var targetName = args.get(0);

                    if (targetName.equals("*"))
                    {
                        var skinCount = skinProvider.getAllSkins().size();
                        skinProvider.dropAll();

                        sender.sendMessage(MessageUtils.prefixes(sender, SkinCacheStrings.droppedAllSkins().resolve("count", skinCount + "")));
                    }
                    else
                    {
                        skinProvider.dropSkin(targetName);

                        sender.sendMessage(MessageUtils.prefixes(sender, SkinCacheStrings.droppedSkin().resolve("name", targetName)));
                    }

                    return true;
                })

                .startNew()
                .name("cache")
                //.permission("dddd")
                .executes((sender, args) ->
                {
                    if (args.isEmpty())
                    {
                        sender.sendMessage(MessageUtils.prefixes(sender, CommandStrings.listNoEnoughArguments()));
                        return true;
                    }

                    var targetName = args.get(0);

                    sender.sendMessage(MessageUtils.prefixes(sender, SkinCacheStrings.fetchingSkin().resolve("name", targetName)));

                    skinProvider.fetchSkin(targetName)
                            .thenAccept(optional ->
                            {
                                optional.ifPresentOrElse(profile -> sender.sendMessage(MessageUtils.prefixes(sender, SkinCacheStrings.fetchSkinSuccess().resolve("name", targetName))),
                                        () -> sender.sendMessage(MessageUtils.prefixes(sender, SkinCacheStrings.targetSkinNotFound())));
                            });

                    return true;
                })

                .startNew()
                .name("info")
                .onFilter(this::filterSkinName)
                .executes((sender, args) ->
                {
                    if (args.isEmpty())
                    {
                        sender.sendMessage(MessageUtils.prefixes(sender, CommandStrings.listNoEnoughArguments()));
                        return true;
                    }

                    var targetName = args.get(0);
                    var skinMatch = skinProvider.getCachedProfile(targetName);

                    if (skinMatch == null)
                    {
                        sender.sendMessage(MessageUtils.prefixes(sender, SkinCacheStrings.targetSkinNotFound()));
                        return true;
                    }

                    var texDesc = "<Nil>";
                    var capeDesc = "<Nil>";
                    var tex = skinMatch.getProperties().get("textures").stream().findFirst().orElse(null);

                    if (tex != null)
                    {
                        var playerProfile = CraftPlayerProfile.asBukkitCopy(skinMatch);

                        var skinURL = playerProfile.getTextures().getSkin();
                        if (skinURL != null)
                            texDesc = skinURL.toString();

                        var capeURL = playerProfile.getTextures().getCape();
                        if (capeURL != null)
                            capeDesc = capeURL.toString();
                    }

                    sender.sendMessage(MessageUtils.prefixes(sender, SkinCacheStrings.infoLine().resolve("name", skinMatch.getName())));

                    sender.sendMessage(
                            MessageUtils.prefixes(
                                    sender,
                                    SkinCacheStrings.infoSkinLine().resolve(
                                            "url",
                                            Component.text(texDesc)
                                                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, texDesc))
                                    )
                            )
                    );

                    sender.sendMessage(
                            MessageUtils.prefixes(
                                    sender,
                                    SkinCacheStrings.infoCapeLine().resolve(
                                            "cape",
                                            CapeURL.findMatching(capeDesc).withLocale(MessageUtils.getLocale(sender))
                                    )
                            )
                    );

                    if (debug.get())
                        sender.sendMessage("Cape " + capeDesc);

                    return true;
                })

                .startNew()
                .name("disguise")
                .onFilter(this::filterSkinName)
                .executes((sender, args) ->
                {
                    if (args.isEmpty())
                    {
                        sender.sendMessage(MessageUtils.prefixes(sender, CommandStrings.listNoEnoughArguments()));
                        return true;
                    }

                    if (!(sender instanceof Player player))
                    {
                        sender.sendMessage(
                                MessageUtils.prefixes(
                                        sender,
                                        CommandStrings.unknownOperation().resolve("operation", "disguise_from_skin_cache_in_console")
                                )
                        );

                        return true;
                    }

                    var targetName = args.get(0);
                    var skinMatch = skinProvider.getCachedProfile(targetName);

                    if (skinMatch == null)
                    {
                        sender.sendMessage(MessageUtils.prefixes(sender, SkinCacheStrings.targetSkinNotFound()));
                        return true;
                    }

                    morphManager.morph(
                            sender, player, DisguiseTypes.PLAYER.toId(skinMatch.getName()), null,
                            MorphParameters.create().setBypassAvailableCheck(true)
                    );

                    return true;
                })

                .buildAll();
    }

    private List<String> filterSkinName(List<String> args)
    {
        var targetName = args.isEmpty() ? "" : args.get(0);

        return skinProvider.getAllSkins()
                .stream()
                .map(ss -> ss.name)
                .filter(name -> name.toLowerCase().contains(targetName.toLowerCase()))
                .toList();
    }

    @Resolved(shouldSolveImmediately = true)
    private MorphManager morphManager;

    private final Bindable<Boolean> debug = new Bindable<>(false);

    @Initializer
    private void load(MorphConfigManager config)
    {
        config.bind(debug, ConfigOption.DEBUG_OUTPUT);
    }

    private List<ISubCommand> getSubCmd()
    {
        if (subCommands == null)
            subCommands = genSubCmd();

        return subCommands;
    }

    @Nullable
    private List<ISubCommand> subCommands;

    @Override
    public @Nullable List<ISubCommand> getSubCommands()
    {
        return getSubCmd();
    }
}
