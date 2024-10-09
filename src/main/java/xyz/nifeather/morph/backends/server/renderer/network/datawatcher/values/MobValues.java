package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes.LivingEntityValues;

public class MobValues extends LivingEntityValues
{
    public final SingleValue<Byte> MOB_FLAGS = createSingle("mob_flags", (byte)0);

    public MobValues()
    {
        super();

        this.registerSingle(MOB_FLAGS);
    }
}
