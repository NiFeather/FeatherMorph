package xiamomc.morph.transforms.clock;

public class SystemClock implements IClock
{
    public final static SystemClock INSTANCE = new SystemClock();

    @Override
    public long getCurrentTimeMills()
    {
        return System.currentTimeMillis();
    }

    @Override
    public long getCurrentTimeTicks()
    {
        return System.currentTimeMillis() / 50;
    }
}
