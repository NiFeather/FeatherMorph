package xyz.nifeather.morph.misc.sentry;

import io.papermc.paper.ServerBuildInfo;
import io.sentry.Sentry;
import io.sentry.SentryEvent;
import io.sentry.SentryLevel;
import io.sentry.protocol.Message;
import xyz.nifeather.morph.FeatherMorphMain;

import java.util.Arrays;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class SentryLogger extends Handler
{
    private volatile boolean enabled;

    public boolean enabled()
    {
        return enabled;
    }

    public void enabled(boolean enabled)
    {
        if (enabled)
            FeatherMorphMain.getInstance().getSLF4JLogger().info("Enabled Sentry Logger!");
        else
            FeatherMorphMain.getInstance().getSLF4JLogger().info("Disabled Sentry Logger! Further errors will not be processed!");

        this.enabled = enabled;
    }

    public SentryLogger()
    {
    }

    public boolean init()
    {
        var logger = FeatherMorphMain.getInstance().getSLF4JLogger();

        try
        {
            Sentry.init(options ->
            {
                options.setDsn("https://df575acbacac5e3b7b42f1da22dad59b@o4509673738469376.ingest.us.sentry.io/4509673769598976");
                options.setShutdownTimeoutMillis(3000);
                options.setConnectionTimeoutMillis(3000);
                options.setEnableAutoSessionTracking(true);
                options.setRelease(FeatherMorphMain.getInstance().getPluginMeta().getVersion());
                options.setDebug(false);
            });

            logger.info("Done setting up sentry!");
            return true;
        }
        catch (Throwable t)
        {
            logger.error("Can't setup SentryLogger: " + t.getMessage());
            return false;
        }
    }

    @Override
    public void publish(LogRecord logRecord)
    {
        if (!enabled)
            return;

        if (logRecord.getLevel().intValue() < Level.WARNING.intValue())
            return;

        var throwable = logRecord.getThrown();
        if (throwable == null) return;

        if (!containsOurCodePath(throwable))
            return;

        var sentryEvent = new SentryEvent(throwable);
        var sentryMessage = new Message();

        sentryMessage.setMessage(throwable.getMessage());

        sentryEvent.setTag("server_version", ServerBuildInfo.buildInfo().minecraftVersionName());
        sentryEvent.setTag("server_brand", ServerBuildInfo.buildInfo().brandName());

        //sentryEvent.setTag("plugin_version", FeatherMorphMain.getInstance().getPluginMeta().getVersion());
        sentryEvent.setTag("os_name", System.getProperty("os.name"));

        sentryEvent.setMessage(sentryMessage);
        sentryEvent.setLevel(SentryLevel.ERROR);

        Sentry.captureEvent(sentryEvent);
    }

    private boolean containsOurCodePath(Throwable t)
    {
        // Maybe we want to check whether we really cause this exception (No other plugins' codepath is included)
        return Arrays.stream(t.getStackTrace())
                .anyMatch(element -> element.getClassName().startsWith("xyz.nifeather.morph"));
    }

    @Override
    public void flush() { }

    @Override
    public void close() throws SecurityException
    {
        Sentry.close();
    }
}
