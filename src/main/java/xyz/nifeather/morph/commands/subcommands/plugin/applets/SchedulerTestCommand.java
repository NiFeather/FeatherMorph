package xyz.nifeather.morph.commands.subcommands.plugin.applets;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Messages.FormattableMessage;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.commands.brigadier.BrigadierCommand;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class SchedulerTestCommand extends BrigadierCommand
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
                        .then(
                                Commands.argument("count", IntegerArgumentType.integer(1))
                                        .executes(this::execute)
                        )
        );

        super.registerAsChild(parentBuilder);
    }

    private final AtomicInteger scheduleCount = new AtomicInteger(0);
    private final AtomicInteger taskId = new AtomicInteger(0);

    @Nullable
    private ExecutorService threadPoolExecutor;

    private int execute(CommandContext<CommandSourceStack> context)
    {
        var sender = context.getSource().getSender();

        int limit = IntegerArgumentType.getInteger(context, "count");

        logger.info("Executing scheduler leak test with limit: " + limit);

        if (threadPoolExecutor != null)
        {
            threadPoolExecutor.shutdownNow();
            threadPoolExecutor = null;
        }

        scheduleCount.set(0);

        threadPoolExecutor = Executors.newFixedThreadPool(20, new ThreadFactory() {
            private final AtomicInteger count = new AtomicInteger();

            public Thread newThread(Runnable run) {
                Thread ret = new Thread(run);
                ret.setName("Test Thread #" + this.count.getAndIncrement());
                ret.setUncaughtExceptionHandler((thread, throwable) -> logger.error("Uncaught exception in thread " + thread.getName(), throwable));
                return ret;
            }
        });

        AtomicInteger cc = new AtomicInteger();
        AtomicInteger server = new AtomicInteger();

        for (int i = 0; i < 20; i++)
        {
            CompletableFuture.runAsync(() ->
            {
                while (cc.get() < limit)
                {
                    cc.addAndGet(1);

                    plugin.schedule(() ->
                    {
                        int xxc = 0;

                        xxc++;

                        var list = new ObjectArrayList<Object>();
                        list.addAll(List.of(
                                1103,
                                "CCB",
                                994.14f
                        ));

                        var xx = server.addAndGet(1);

                        sender.sendActionBar(Component.text("Hello from Thread %s! complete schedule is %s"
                                .formatted(Thread.currentThread().getName(), xx)));
                    });

                    //sender.sendActionBar(Component.text("Hello from Thread %s! cc is %s"
                    //        .formatted(Thread.currentThread().getName(), cc.get())));
                }

                logger.info("%s Complete: %s".formatted(Thread.currentThread().getName(), cc.get()));
            }, threadPoolExecutor);
        }

        plugin.schedule(() ->
        {
            sender.sendMessage("Ran %s/%s tasks --> %s".formatted(cc.get(), limit, ((FeatherMorphMain)plugin).execSchedules ));
        }, 20);

        return 1;
    }

    @Override
    public @NotNull String name()
    {
        return "scheduler_leak_test";
    }

    @Override
    public FormattableMessage getHelpMessage()
    {
        return null;
    }
}
