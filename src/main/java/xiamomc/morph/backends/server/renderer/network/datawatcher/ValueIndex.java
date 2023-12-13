package xiamomc.morph.backends.server.renderer.network.datawatcher;

import xiamomc.morph.backends.server.renderer.network.datawatcher.values.AbstractValues;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.EntityValues;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.LivingEntityValues;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.PlayerValues;

public class ValueIndex
{
    public static final PlayerValues PLAYER = new PlayerValues();
    public static final LivingEntityValues BASE_LIVING = new LivingEntityValues();
    public static final EntityValues BASE_ENTITY = new EntityValues();
}
