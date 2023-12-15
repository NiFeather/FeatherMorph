package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.LivingEntityValues;

public class MobValues extends LivingEntityValues
{
    public final SingleValue MOB_FLAGS = getSingle((byte)0);

    public MobValues()
    {
        super();

        this.registerSingle(MOB_FLAGS);
    }
}
