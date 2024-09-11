package xiamomc.morph.misc;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.misc.animation.AnimationNames;
import xiamomc.morph.misc.animation.SingleAnimation;
import xiamomc.morph.misc.animation.animations.AnimationSet;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class AnimationSequence extends MorphPluginObject
{
    private final List<SingleAnimation> currentQueue = Collections.synchronizedList(new ObjectArrayList<>());
    private final AtomicReference<String> currentAnimationSequenceName = new AtomicReference<>(AnimationNames.NONE);

    // AnimationSequenceName <-> Animation Sequence
    @Nullable
    private Pair<String, List<SingleAnimation>> nextQueue;

    // StartTick <-> Animation record
    @Nullable
    private Pair<Long, SingleAnimation> currentAnimation;

    private int cooldown;
    private int workingCooldown;

    public void setCooldown(int cd)
    {
        this.cooldown = cd;
    }

    public int getCooldown()
    {
        return cooldown;
    }

    @NotNull
    public String getCurrentAnimationSequenceName()
    {
        return currentAnimationSequenceName.get();
    }

    public void scheduleNext(String sequenceIdentifier, List<SingleAnimation> animations)
    {
        synchronized (this)
        {
            nextQueue = new Pair<>(sequenceIdentifier, new ObjectArrayList<>(animations));
        }
    }

    public void reset()
    {
        currentQueue.clear();
        currentAnimation = null;
        currentAnimationSequenceName.set(AnimationNames.NONE);

        hookOnNewAnimation.accept(AnimationSet.RESET);
    }

    private void findNextAnimation()
    {
        currentAnimation = null;
        if (currentQueue.isEmpty()) return;

        var current = currentQueue.remove(0);
        currentAnimation = new Pair<>(plugin.getCurrentTick(), current);

        if (hookOnNewAnimation != null)
            hookOnNewAnimation.accept(current);
    }

    public void update()
    {
        if (disposed.get()) return;

        this.workingCooldown--;

        if (workingCooldown > 0)
            return;

        // 如果当前队列为空并且下一队列不为null，则切换到此队列
        if (currentQueue.isEmpty() && currentAnimation == null)
        {
            if (!tryNextQueue()) return;
        }

        // 如果当前动画为空，则寻找动画
        if (currentAnimation == null)
        {
            findNextAnimation();

            // currentAnimation == null -> 动画序列已空
            if (currentAnimation == null) return;
        }

        // 动画播放完毕
        if (plugin.getCurrentTick() - currentAnimation.getA() >= currentAnimation.getB().duration())
        {
            currentAnimation = null;

            if (currentQueue.isEmpty())
            {
                if (hookOnNewAnimationSequence != null)
                    hookOnNewAnimationSequence.accept(AnimationNames.NONE);

                this.workingCooldown = cooldown;
            }
        }
    }

    /**
     * @return 是否切换了队列
     */
    private boolean tryNextQueue()
    {
        currentAnimationSequenceName.set(AnimationNames.NONE);

        if (nextQueue == null)
            return false;

        synchronized (this)
        {
            // Find next queue
            currentQueue.addAll(nextQueue.getB());

            if (hookOnNewAnimationSequence != null)
                hookOnNewAnimationSequence.accept(nextQueue.getA());

            currentAnimationSequenceName.set(nextQueue.getA());

            nextQueue = null;
        }

        return true;
    }

    private Consumer<SingleAnimation> hookOnNewAnimation;
    private Consumer<String> hookOnNewAnimationSequence;

    public void onNewAnimationSequence(Consumer<String> consumer)
    {
        this.hookOnNewAnimationSequence = consumer;
    }

    public void onNewAnimation(Consumer<SingleAnimation> consumer)
    {
        this.hookOnNewAnimation = consumer;
    }

    private final AtomicBoolean disposed = new AtomicBoolean(false);

    @Override
    public void dispose()
    {
        super.dispose();

        disposed.set(true);
        currentQueue.clear();
    }
}
