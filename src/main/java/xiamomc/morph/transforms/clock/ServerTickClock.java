package xiamomc.morph.transforms.clock;

import xiamomc.morph.MorphPluginObject;

public class ServerTickClock extends MorphPluginObject implements IClock
{
    @Override
    public long getCurrentTimeMills()
    {
        return plugin.getCurrentTick() * 50;
    }

    @Override
    public long getCurrentTimeTicks()
    {
        return plugin.getCurrentTick();
    }
}
