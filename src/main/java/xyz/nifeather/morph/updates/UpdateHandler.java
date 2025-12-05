package xyz.nifeather.morph.updates;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.command.ServerCommandSender;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Bindables.Bindable;
import xiamomc.pluginbase.Messages.FormattableMessage;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.config.ConfigOptions;
import xyz.nifeather.morph.config.MorphConfigManager;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.messages.strings.UpdateStrings;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class UpdateHandler extends MorphPluginObject
{
    @Resolved(shouldSolveImmediately = true)
    private FeatherMorphMain plugin;

    private final AtomicInteger requestId = new AtomicInteger(0);

    private final Bindable<Boolean> checkUpdate = new Bindable<>(true);

    @Initializer
    private void load(MorphConfigManager config)
    {
        config.bind(checkUpdate, ConfigOptions.CHECK_UPDATE);

        this.update();
    }

    private void update()
    {
        // 每三小时检查一次更新
        this.addSchedule(this::update, 3 * 60 * 60 * 20);

        if (checkUpdate.get())
            this.checkUpdate(true, null);
    }

    private volatile CompletableFuture<CheckResult> runningFuture;

    private final Object lock = new Object();

    public CompletableFuture<CheckResult> checkUpdate(boolean sendMessages,
                            @Nullable CommandSender forwardTarget)
    {
        CompletableFuture<CheckResult> newFuture;
        synchronized (lock)
        {
            if (this.runningFuture != null && runningFuture.state() == Future.State.RUNNING)
                return runningFuture;
            else
                this.runningFuture = null;

            //Run async
            newFuture = doCheckUpdateAsync(sendMessages, forwardTarget);
            this.runningFuture = newFuture;
        }

        newFuture.thenRun(() ->
        {
            synchronized (lock)
            {
                this.runningFuture = null;
            }
        });

        return newFuture;
    }

    private CompletableFuture<CheckResult> doCheckUpdateAsync(boolean sendMessages, @Nullable CommandSender forwardTarget)
    {
        return CompletableFuture.supplyAsync(() -> doCheckUpdate(sendMessages, forwardTarget));
    }

    private CheckResult doCheckUpdate(boolean sendMessages, @Nullable CommandSender forwardTarget)
    {
        logger.info("Checking updates...");
        updateAvailable = false;

        var reqId = requestId.addAndGet(1);

        HttpClient httpClient = null;

        try
        {
            var urlString = "https://api.modrinth.com"
                    + "/v2/project/feathermorph/version"
                    + "?"
                    + "game_versions=[\"%s\"]";

            urlString = urlString.formatted(Bukkit.getMinecraftVersion())
                    .replace("[", "%5B") // Make URI happy
                    .replace("]", "%5D")
                    .replace("\"", "%22")
                    .replace(" ", "%20");

            var uri = new URI(urlString);

            var request = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .timeout(Duration.ofSeconds(10))
                    .header("User-Agent", "feathermorph")
                    .build();

            httpClient = HttpClient.newBuilder()
                            .followRedirects(HttpClient.Redirect.ALWAYS)
                            .build();

            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200)
            {
                logger.error("Failed to check update: Server returned HTTP code {}", response.statusCode());
                logger.error("Server response: {}", response.body());

                return CheckResult.FAIL;
            }

            return this.onUpdateReqFinish(response.body(), reqId, sendMessages, forwardTarget);
        }
        catch (Throwable t)
        {
            this.onUpdateReqFail(t, reqId);

            return CheckResult.FAIL;
        }
        finally
        {
            if (httpClient != null)
                httpClient.close();
        }
    }

    private void onUpdateReqFail(Throwable e, int reqId)
    {
        if (this.requestId.get() != reqId)
            return;

        logger.error("Failed checking update", e);
    }

    private CheckResult onUpdateReqFinish(String responseStr, int reqId,
                                                       boolean sendMessages,
                                                       @Nullable CommandSender forwardTarget)
    {
        if (this.requestId.get() != reqId)
            return CheckResult.FAIL;

        try
        {
            // 反序列化为Map
            // 之后看情况再考虑要不要反序列化成一个类
            var gson = new GsonBuilder().create();
            var versionList = gson.fromJson(responseStr, new TypeToken<ArrayList<Map<?, ?>>>(){});
            var metaList = new ObjectArrayList<SingleUpdateInfoMeta>();
            for (var map : versionList)
                metaList.add(SingleUpdateInfoMeta.fromMap(map));

            var matchMeta = metaList.stream()
                    .filter(m ->
                    {
                        var supportedLoaders = m.supportedLoaders;
                        if (supportedLoaders == null) return false;

                        var isRelease = "Release".equalsIgnoreCase(m.versionType);
                        var loaderMatch = supportedLoaders.stream().anyMatch(s -> s.equalsIgnoreCase(Platforms.fromName(Bukkit.getName()).getImplName()));

                        return isRelease && loaderMatch;
                    }).findFirst().orElse(null);

            if (matchMeta == null)
            {
                logger.error("Unable to check update: This version of Minecraft is not listed yet, or your server '%s' is not supported"
                        .formatted(Bukkit.getName()));

                return CheckResult.NOT_LISTED_OR_UNSUPPORTED;
            }

            var currentVersion = VersionHandling.toVersionInfo(plugin.getPluginMeta().getVersion());
            var latestVersion = VersionHandling.toVersionInfo(matchMeta.versionNumber);

            if (latestVersion.isInvalid())
            {
                logger.error("Null version number from response: " + gson.toJson(matchMeta));
                return CheckResult.FAIL;
            }

            var compare = currentVersion.compare(latestVersion);

            if (compare == VersionHandling.CompareResult.EQUAL)
            {
                logger.info("Already on the latest version for " + Bukkit.getMinecraftVersion());

                return CheckResult.ALREADY_LATEST;
            }

            if (compare == VersionHandling.CompareResult.INPUT_OLDER)
            {
                logger.info("Your version is newer than released for %s!".formatted(Bukkit.getMinecraftVersion()));

                return CheckResult.CURRENT_IS_NEWER;
            }

            if (compare == VersionHandling.CompareResult.NOT_ON_SAME_CHANNEL)
                logger.info("We are not on the same channel with the latest release, assuming there is a new update!");

            // 提醒服务器关于更新的消息
            var serverOps = Bukkit.getOperators();
            var sendTargets = new ObjectArrayList<CommandSender>();

            if (forwardTarget == null)
            {
                serverOps.forEach(offlinePlayer ->
                {
                    var onlinePlayer = offlinePlayer.getPlayer();
                    if (onlinePlayer != null && onlinePlayer.hasPermission(CommonPermissions.CHECK_UPDATE))
                        sendTargets.add(onlinePlayer);
                });
            }
            else
            {
                if (!(forwardTarget instanceof ServerCommandSender || forwardTarget instanceof ConsoleCommandSender))
                    sendTargets.add(forwardTarget);
            }

            sendTargets.add(Bukkit.getConsoleSender());

            this.msgPrimary = UpdateStrings.newVersionAvailable()
                    .resolve("current", currentVersion.toString())
                    .resolve("origin", latestVersion.toString());

            this.msgSecondary = UpdateStrings.update_here()
                    .resolve("url", "https://modrinth.com/plugin/feathermorph");

            this.updateAvailable = true;

            if (sendMessages)
            {
                for (var sendTarget : sendTargets)
                    sendUpdateNotifyTo(sendTarget);
            }

            return CheckResult.HAS_UPDATE;
        }
        catch (Throwable t)
        {
            logger.error("Error occurred while processing response", t);

            return CheckResult.FAIL;
        }
    }

    private final FormattableMessage messageHeaderFooter = UpdateStrings.messageHeaderFooter();

    private final FormattableMessage noNewVersionAvailable = UpdateStrings.noNewVersionAvailable();

    @Nullable
    private FormattableMessage msgPrimary;

    @Nullable
    private FormattableMessage msgSecondary;

    private boolean updateAvailable = false;

    public boolean updateAvailable()
    {
        return updateAvailable;
    }

    public void sendUpdateNotifyTo(CommandSender sendTarget)
    {
        if (!updateAvailable)
        {
            MessageUtils.send(sendTarget, noNewVersionAvailable);
            return;
        }

        assert msgPrimary != null;
        assert msgSecondary != null;

        MessageUtils.send(sendTarget, messageHeaderFooter);
        MessageUtils.send(sendTarget, msgPrimary);
        MessageUtils.send(sendTarget, msgSecondary);
        MessageUtils.send(sendTarget, messageHeaderFooter);
    }

    private static class InvalidOperationException extends RuntimeException
    {
        public InvalidOperationException() {
        }

        public InvalidOperationException(String message) {
            super(message);
        }

        public InvalidOperationException(String message, Throwable cause) {
            super(message, cause);
        }

        public InvalidOperationException(Throwable cause) {
            super(cause);
        }
    }

    public enum CheckResult
    {
        HAS_UPDATE,
        ALREADY_LATEST,
        NOT_LISTED_OR_UNSUPPORTED,
        CURRENT_IS_NEWER,
        FAIL
    }
}
