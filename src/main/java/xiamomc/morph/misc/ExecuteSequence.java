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

public class ExecuteSequence extends MorphPluginObject
{
    private final List<SingleAnimation> animationState = Collections.synchronizedList(new ObjectArrayList<>());

    // StartTick <-> Animation record
    @Nullable
    private Pair<Long, SingleAnimation> currentAnimation;

    private int cooldown;

    public void setCooldown(int cd)
    {
        this.cooldown = cd;
    }

    public int getCooldown()
    {
        return cooldown;
    }

    public void addAction(SingleAnimation animation)
    {
        if (animationState.size() > 10)
            animationState.remove(0);

        animationState.add(animation);
    }

    public void setSequences(List<SingleAnimation> animations)
    {
        animationState.addAll(animations);
    }

    public void reset()
    {
        animationState.clear();
        currentAnimation = null;
    }

    private void findNextAnimation()
    {
        currentAnimation = null;
        if (animationState.isEmpty()) return;

        var current = animationState.remove(0);
        currentAnimation = new Pair<>(plugin.getCurrentTick(), current);

        //logger.info("Pick new animation! " + current.identifier());
        //current.action().run();

        if (hookOnNewAnimation != null)
            hookOnNewAnimation.accept(current);
    }

    public void update()
    {
        if (disposed.get()) return;

        if (currentAnimation == null && animationState.isEmpty()) return;

        if (currentAnimation == null)
        {
            do findNextAnimation();
            while (currentAnimation != null && currentAnimation.getB().duration() == 0);

            if (currentAnimation == null) return;
        }

        if (plugin.getCurrentTick() - currentAnimation.getA() >= currentAnimation.getB().duration() + cooldown)
        {
            //logger.info(currentAnimation.getB().identifier() + "Finished!");
            //currentAnimation.getB().onFinish().run();
            currentAnimation = null;
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
        animationState.clear();
    }
}
