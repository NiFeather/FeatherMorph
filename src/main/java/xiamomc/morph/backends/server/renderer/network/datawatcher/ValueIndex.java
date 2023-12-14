package xiamomc.morph.backends.server.renderer.network.datawatcher;

import org.checkerframework.checker.units.qual.A;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.*;

public class ValueIndex
{
    public static final EntityValues BASE_ENTITY = new EntityValues();
    public static final LivingEntityValues BASE_LIVING = new LivingEntityValues();
    public static final AgeableMobValues AGEABLE_MOB = new AgeableMobValues();

    public static final ArmorStandValues ARMOR_STAND = new ArmorStandValues();
    public static final AllayValues ALLAY = new AllayValues();
    public static final PlayerValues PLAYER = new PlayerValues();
    public static final SlimeValues SLIME_MAGMA = new SlimeValues();
    public static final GhastValues GHAST = new GhastValues();
    public static final AbstractHorseValues ABSTRACT_HORSE = new AbstractHorseValues();
    public static final HorseValues HORSE = new HorseValues();
    public static final ChestedHorseValues CHESTED_HORSE = new ChestedHorseValues();
    public static final LlamaValues LLAMA = new LlamaValues();
    public static final FoxValues FOX = new FoxValues();
}
