package xyz.nifeather.morph.misc;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.providers.animation.ActionStage;
import xyz.nifeather.morph.providers.animation.AnimationSet;
import xyz.nifeather.morph.providers.animation.PlayableAction;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class AnimationHandler extends MorphPluginObject
{
    private final AtomicReference<String> currentActionNameReference = new AtomicReference<>(AnimationNames.NONE);

    // Time to finish <-> The ActionStage
    @Nullable
    private Pair<Long, ActionStage> currentStage;

    private final List<ActionStage> remainingActions = Collections.synchronizedList(new ObjectArrayList<>());

    @Nullable
    private Pair<String, PlayableAction> nextAction;

    // The cooldown
    private int cooldown;

    // Used to calculate whether we can run update
    private int workCooldown;

    public void setCooldown(int cd)
    {
        this.cooldown = cd;
    }

    public int getCooldown()
    {
        return cooldown;
    }

    @NotNull
    public String getCurrentActionName()
    {
        return currentActionNameReference.get();
    }

    public void scheduleNext(String actionName, PlayableAction action)
    {
        synchronized (this)
        {
            nextAction = Pair.of(actionName, action);
        }
    }

    public void reset()
    {
        synchronized (this)
        {
            nextAction = null;
        }

        currentStage = null;

        currentActionNameReference.set(AnimationNames.NONE);

        if (hookOnNewStage != null)
            hookOnNewStage.accept(AnimationSet.FINISH);
    }

    private void findNextStage()
    {
        if (remainingActions.isEmpty()) return;

        var next = remainingActions.removeFirst();
        currentStage = Pair.of(plugin.getCurrentTick() + next.duration(), next);
    }

    public void update()
    {
        if (disposed.get()) return;

        this.workCooldown--;

        if (workCooldown > 0)
            return;

        // 如果当前队列为空并且下一队列不为null，则切换到此队列
        if (remainingActions.isEmpty() && !tryNextQueue())
            return;

        if (currentStage == null)
        {
            findNextStage();

            // currentStage == null -> All action stages have done playing
            if (currentStage == null)
                return;

            if (hookOnNewStage != null)
                hookOnNewStage.accept(currentStage.right());
        }

        // If the stage has finished playing...
        if (plugin.getCurrentTick() > currentStage.left())
        {
            if (hookOnStageFinish != null)
                hookOnStageFinish.accept(currentStage.right());

            currentStage = null;

            // If we don't have any remaining, then notify others about actions has been finished.
            if (remainingActions.size() == 1)
            {
                if (hookOnNewAction != null)
                    hookOnNewAction.accept(AnimationNames.NONE);

                if (hookOnNewStage != null)
                    hookOnNewStage.accept(AnimationSet.FINISH);
            }
        }
    }

    /**
     * @return 是否切换了队列
     */
    private boolean tryNextQueue()
    {
        currentActionNameReference.set(AnimationNames.NONE);

        if (nextAction == null)
            return false;

        synchronized (this)
        {
            // Find next queue
            remainingActions.addAll(nextAction.right().animations());

            if (hookOnNewAction != null)
                hookOnNewAction.accept(nextAction.left());

            currentActionNameReference.set(nextAction.left());

            nextAction = null;
        }

        return true;
    }

    @ApiStatus.Internal
    @Nullable
    private Consumer<ActionStage> hookOnNewStage;

    @ApiStatus.Internal
    @Nullable
    private Consumer<ActionStage> hookOnStageFinish;

    @ApiStatus.Internal
    @Nullable
    private Consumer<String> hookOnNewAction;

    @ApiStatus.Internal
    public void onNewAction(Consumer<String> consumer)
    {
        this.hookOnNewAction = consumer;
    }

    @ApiStatus.Internal
    public void onNewStage(Consumer<ActionStage> consumer)
    {
        this.hookOnNewStage = consumer;
    }

    @ApiStatus.Internal
    public void onStageFinish(Consumer<ActionStage> consumer)
    {
        this.hookOnStageFinish = consumer;
    }

    private final AtomicBoolean disposed = new AtomicBoolean(false);

    @Override
    public void dispose()
    {
        super.dispose();

        disposed.set(true);
        remainingActions.clear();
    }
}
