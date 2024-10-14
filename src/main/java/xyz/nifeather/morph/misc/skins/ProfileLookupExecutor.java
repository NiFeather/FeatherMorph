package xyz.nifeather.morph.misc.skins;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.MorphPlugin;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ProfileLookupExecutor
{
    @NotNull
    public static ExecutorService executor()
    {
        var executor = EXECUTOR;
        if (executor == null || executor.isShutdown())
        {
            EXECUTOR = executor = createExecutor();

            MorphPlugin.getInstance()
                    .getSLF4JLogger()
                    .info("Creating new executor with maximum " + getMaximumThreadCount() + " thread(s) for profile lookup.");
        }

        return executor;
    }

    private static int getMaximumThreadCount()
    {
        return Math.min(16, Math.max(4, Runtime.getRuntime().availableProcessors() / 2));
    }

    private static ExecutorService createExecutor()
    {
        return new ThreadPoolExecutor(1, getMaximumThreadCount(),
                10, TimeUnit.MINUTES,
                new SynchronousQueue<>(),
                new ThreadFactory()
                {
                    private final AtomicInteger threadCount = new AtomicInteger(0);

                    @Override
                    public Thread newThread(@NotNull Runnable runnable)
                    {
                        Thread thread = new Thread(runnable);
                        var threadId = this.threadCount.getAndIncrement();
                        thread.setName("FeatherMorph Profile Executor #" + threadId);

                        thread.setUncaughtExceptionHandler((Thread t, Throwable error) ->
                        {
                            MorphPlugin.getInstance()
                                    .getSLF4JLogger()
                                    .error("Error occurred in thread " + t.getName(), error);
                        });

                        return thread;
                    }
                });
    }

    @Nullable
    private static volatile ExecutorService EXECUTOR;
}
