package xiamomc.morph.transforms.clock;

import java.util.concurrent.atomic.AtomicLong;

public class DecoupledTickClock implements IClock
{
    private final AtomicLong tick = new AtomicLong();

    public void setTick(long newTick)
    {
        this.tick.set(newTick);
    }

    public long getTick()
    {
        return tick.get();
    }

    public void step()
    {
        tick.incrementAndGet();
    }

    @Override
    public long getCurrentTimeMills()
    {
        return tick.get() * 50;
    }

    @Override
    public long getCurrentTimeTicks()
    {
        return tick.get();
    }
}
