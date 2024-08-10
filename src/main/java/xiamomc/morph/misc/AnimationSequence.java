package xiamomc.morph.misc;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.misc.animation.SingleAnimation;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class AnimationSequence extends MorphPluginObject
{
    private final List<SingleAnimation> currentQueue = Collections.synchronizedList(new ObjectArrayList<>());

    @Nullable
    private List<SingleAnimation> nextQueue;

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

    public void scheduleNext(List<SingleAnimation> animations)
    {
        synchronized (this)
        {
            nextQueue = new ObjectArrayList<>(animations);
        }
    }

    public void reset()
    {
        currentQueue.clear();
        currentAnimation = null;
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
            if (nextQueue != null)
            {
                synchronized (this)
                {
                    // Find next queue
                    currentQueue.addAll(nextQueue);
                    nextQueue = null;
                }
            }
            else
            {
                return;
            }
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
                this.workingCooldown = cooldown;
        }
    }

    private Consumer<SingleAnimation> hookOnNewAnimation;

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
